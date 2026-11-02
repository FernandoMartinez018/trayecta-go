package co.trayectago.backend.usuarios

import co.trayectago.backend.comun.jsonTrayecta
import java.sql.Connection
import java.time.OffsetDateTime
import javax.sql.DataSource
import kotlin.random.Random

// Los niveles con nombre llegan en H5 (reputación); mientras tanto todos empiezan en el 1.
private const val NIVEL_INICIAL = "1"

/** Alias público inicial: nunca el nombre ni el correo (los reportes se muestran con alias). */
internal fun aliasAleatorio(numero: Int = Random.nextInt(10_000)): String = "Usuario %04d".format(numero)

class RepositorioUsuarios(private val ds: DataSource) {

    /** Devuelve el usuario del `sub` de Keycloak y lo crea en su primer acceso, sin carreras entre peticiones simultáneas. */
    fun obtenerOCrear(keycloakId: String): Usuario = enTransaccion { c ->
        c.prepareStatement("INSERT INTO usuario (keycloak_id, alias_publico) VALUES (?, ?) ON CONFLICT (keycloak_id) DO NOTHING").use {
            it.setString(1, keycloakId)
            it.setString(2, aliasAleatorio())
            it.executeUpdate()
        }
        c.prepareStatement("INSERT INTO reputacion (usuario_id) SELECT id FROM usuario WHERE keycloak_id = ? ON CONFLICT (usuario_id) DO NOTHING").use {
            it.setString(1, keycloakId)
            it.executeUpdate()
        }
        c.prepareStatement(
            """
            SELECT u.id, u.alias_publico, u.preferencias::text, u.creado_en, r.puntos, r.aciertos
            FROM usuario u JOIN reputacion r ON r.usuario_id = u.id
            WHERE u.keycloak_id = ?
            """.trimIndent(),
        ).use {
            it.setString(1, keycloakId)
            it.executeQuery().use { r ->
                r.next()
                Usuario(
                    id = r.getString(1),
                    alias = r.getString(2),
                    nivel = NIVEL_INICIAL,
                    puntos = r.getInt(5),
                    reportesConfirmados = r.getInt(6),
                    preferencias = jsonTrayecta.decodeFromString(Preferencias.serializer(), r.getString(3)),
                    creadoEn = r.getObject(4, OffsetDateTime::class.java).toInstant().toString(),
                )
            }
        }
    }

    private fun <T> enTransaccion(bloque: (Connection) -> T): T = ds.connection.use { c ->
        c.autoCommit = false
        try {
            bloque(c).also { c.commit() }
        } catch (e: Exception) {
            c.rollback()
            throw e
        }
    }
}
