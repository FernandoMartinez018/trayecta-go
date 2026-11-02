package co.trayectago.backend

import co.trayectago.backend.comun.ConfigBaseDatos
import co.trayectago.backend.comun.ConfigOidc
import co.trayectago.backend.comun.Configuracion
import co.trayectago.backend.comun.crearDataSource
import co.trayectago.backend.comun.migrar
import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.SigningKeyNotFoundException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Base64
import javax.sql.DataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** Exige que la URL apunte a una base *_pruebas: las pruebas truncan tablas y nunca deben tocar una base real. */
fun exigirBaseDePruebas(url: String): String {
    val nombre = url.substringAfterLast('/').substringBefore('?')
    check(nombre.endsWith("_pruebas")) { "Las pruebas solo corren contra una base *_pruebas; PRUEBAS_DB_URL apunta a '$nombre'" }
    return url
}

private val baseCompartida: DataSource by lazy {
    val url = exigirBaseDePruebas(System.getenv("PRUEBAS_DB_URL") ?: "jdbc:postgresql://localhost:5432/trayecta_pruebas")
    crearDataSource(ConfigBaseDatos(url, System.getenv("PRUEBAS_DB_USUARIO") ?: "trayecta", System.getenv("PRUEBAS_DB_CLAVE") ?: "solo-local"))
        .also(::migrar)
}

fun baseDePruebas(): DataSource = baseCompartida

fun limpiarBase(ds: DataSource) {
    ds.connection.use { it.createStatement().execute("TRUNCATE usuario CASCADE") }
}

fun configuracionDePrueba() = Configuracion(
    puerto = 0,
    entorno = "pruebas",
    version = "pruebas",
    db = ConfigBaseDatos("jdbc:postgresql://no-usada/trayecta_pruebas", "no-usado", "no-usada"),
    oidc = ConfigOidc(
        issuer = "http://keycloak.prueba/realms/trayecta-go",
        jwksUrl = "http://keycloak.prueba/realms/trayecta-go/protocol/openid-connect/certs",
        audiencia = "trayecta-go-api",
    ),
)

/** Firma tokens RS256 como los de Keycloak y entrega el JWKS que los valida, sin Keycloak real. */
object TokensDePrueba {
    const val ISSUER = "http://keycloak.prueba/realms/trayecta-go"
    private const val KID = "llave-prueba"

    val llavePrincipal: KeyPair = nuevaLlave()

    val jwkProvider = JwkProvider { kid ->
        if (kid != KID) throw SigningKeyNotFoundException("kid desconocido: $kid", null)
        val publica = llavePrincipal.public as RSAPublicKey
        Jwk.fromValues(
            mapOf("kty" to "RSA", "kid" to KID, "alg" to "RS256", "use" to "sig", "n" to base64Url(publica.modulus), "e" to base64Url(publica.publicExponent)),
        )
    }

    fun nuevaLlave(): KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    fun firmar(
        sub: String = "usuario-1",
        aud: String = "trayecta-go-api",
        iss: String = ISSUER,
        expiraEn: Duration = 5.minutes,
        llave: KeyPair = llavePrincipal,
    ): String = JWT.create()
        .withKeyId(KID)
        .withIssuer(iss)
        .withAudience(aud)
        .withSubject(sub)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plusSeconds(expiraEn.inWholeSeconds))
        .sign(Algorithm.RSA256(llave.public as RSAPublicKey, llave.private as RSAPrivateKey))

    private fun base64Url(n: BigInteger): String {
        val bytes = n.toByteArray().let { if (it[0] == 0.toByte()) it.copyOfRange(1, it.size) else it }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

/** Levanta el módulo real sobre `dataSource` y entrega un cliente que serializa JSON. */
fun pruebaDeApp(
    dataSource: DataSource = baseDePruebas(),
    jwkProvider: JwkProvider = TokensDePrueba.jwkProvider,
    rutasExtra: Route.() -> Unit = {},
    bloque: suspend ApplicationTestBuilder.(HttpClient) -> Unit,
) = testApplication {
    application {
        moduloTrayecta(configuracionDePrueba(), dataSource, jwkProvider)
        routing { rutasExtra() }
    }
    bloque(createClient { install(ContentNegotiation) { json() } })
}
