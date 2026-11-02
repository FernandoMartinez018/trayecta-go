package co.trayectago.backend

import co.trayectago.backend.comun.Configuracion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ConfiguracionTest {
    private val minima = mapOf(
        "DB_URL" to "jdbc:postgresql://db:5432/trayecta",
        "DB_USUARIO" to "trayecta",
        "DB_CLAVE" to "clave",
        "OIDC_ISSUER" to "http://kc/realms/trayecta-go",
    )

    @Test
    fun `lee la configuración completa con valores por defecto`() {
        val c = Configuracion.desdeEntorno(minima)

        assertEquals(8080, c.puerto)
        assertEquals("local", c.entorno)
        assertEquals("dev", c.version)
        assertEquals("http://kc/realms/trayecta-go/protocol/openid-connect/certs", c.oidc.jwksUrl)
        assertEquals("trayecta-go-api", c.oidc.audiencia)
    }

    @Test
    fun `respeta OIDC_JWKS_URL y PUERTO cuando existen`() {
        val c = Configuracion.desdeEntorno(
            minima + mapOf("OIDC_JWKS_URL" to "http://keycloak:8080/certs", "PUERTO" to "9090"),
        )

        assertEquals("http://keycloak:8080/certs", c.oidc.jwksUrl)
        assertEquals(9090, c.puerto)
    }

    @Test
    fun `falla nombrando solo las variables que faltan`() {
        val error = assertFailsWith<IllegalStateException> {
            Configuracion.desdeEntorno(mapOf("DB_USUARIO" to "trayecta", "DB_CLAVE" to "secreta-123"))
        }

        assertEquals("Faltan variables de entorno: DB_URL, OIDC_ISSUER", error.message)
        assertFalse(error.message!!.contains("secreta-123"))
    }
}
