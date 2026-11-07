package co.trayectago.backend.usuarios

import co.trayectago.backend.comun.SolicitudInvalida
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

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

@Serializable
data class Vehiculo(val tipo: String, val ultimoDigitoPlaca: Int, val llevaParrillero: Boolean = false)

object DigitoPlacaSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DigitoPlaca", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeInt()
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonPrimitive || element.isString) {
            throw SolicitudInvalida("ultimoDigitoPlaca debe ser un número entre 0 y 9")
        }
        return element.intOrNull ?: throw SolicitudInvalida("ultimoDigitoPlaca debe ser un número entre 0 y 9")
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}

@Serializable
data class VehiculoEntrada(
    val tipo: String? = null,
    @Serializable(with = DigitoPlacaSerializer::class)
    val ultimoDigitoPlaca: Int? = null,
    val llevaParrillero: Boolean? = null,
)
