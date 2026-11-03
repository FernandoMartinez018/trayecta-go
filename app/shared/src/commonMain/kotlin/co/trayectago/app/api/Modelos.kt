package co.trayectago.app.api

import kotlinx.serialization.Serializable

// Modelos según contratos/openapi/v1.yaml.

@Serializable
data class Salud(val estado: String, val version: String)

@Serializable
data class Usuario(
    val id: String,
    val alias: String,
    val nivel: String,
    val puntos: Int,
    val reportesConfirmados: Int,
    val preferencias: Preferencias,
    val creadoEn: String,
)

@Serializable
data class Preferencias(
    val manosLibres: Boolean = true,
    val modoSeguro: Boolean = false,
    val avisosLluvia: Boolean = true,
    val anticipacionAvisoMin: Int = 5,
    val noMolestar: NoMolestar? = null,
)

@Serializable
data class NoMolestar(val activo: Boolean? = null, val desde: String? = null, val hasta: String? = null)

/** Problem Details (RFC 9457). */
@Serializable
data class Problema(val type: String? = null, val title: String, val status: Int, val detail: String? = null)

sealed class ErrorApi : Exception() {
    /** Sin red, timeout o Keycloak caído: la sesión se conserva. */
    data object SinConexion : ErrorApi()

    /** La sesión murió: hay que volver a iniciar sesión. */
    data object NoAutenticado : ErrorApi()

    data class Servidor(val estado: Int, val problema: Problema?) : ErrorApi()
}
