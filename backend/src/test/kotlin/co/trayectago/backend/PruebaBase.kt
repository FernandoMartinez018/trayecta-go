package co.trayectago.backend

import co.trayectago.backend.comun.ConfigBaseDatos
import co.trayectago.backend.comun.ConfigOidc
import co.trayectago.backend.comun.Configuracion
import co.trayectago.backend.comun.crearDataSource
import co.trayectago.backend.comun.migrar
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import javax.sql.DataSource

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

/** Levanta el módulo real sobre `dataSource` y entrega un cliente que serializa JSON. */
fun pruebaDeApp(
    dataSource: DataSource = baseDePruebas(),
    rutasExtra: Route.() -> Unit = {},
    bloque: suspend ApplicationTestBuilder.(HttpClient) -> Unit,
) = testApplication {
    application {
        moduloTrayecta(configuracionDePrueba(), dataSource)
        routing { rutasExtra() }
    }
    bloque(createClient { install(ContentNegotiation) { json() } })
}
