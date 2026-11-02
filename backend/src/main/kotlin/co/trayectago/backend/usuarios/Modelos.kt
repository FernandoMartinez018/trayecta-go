package co.trayectago.backend.usuarios

import kotlinx.serialization.Serializable

/** Perfil del usuario, según el esquema `Usuario` del contrato. */
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
