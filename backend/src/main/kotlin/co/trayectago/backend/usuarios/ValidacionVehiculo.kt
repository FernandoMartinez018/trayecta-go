package co.trayectago.backend.usuarios

import co.trayectago.backend.comun.SolicitudInvalida

fun validarVehiculo(entrada: VehiculoEntrada): Vehiculo {
    if (entrada.tipo != "moto") throw SolicitudInvalida("tipo debe ser \"moto\"")
    val digito = entrada.ultimoDigitoPlaca
    if (digito == null || digito !in 0..9) {
        throw SolicitudInvalida("ultimoDigitoPlaca debe ser un número entre 0 y 9")
    }
    return Vehiculo("moto", digito, entrada.llevaParrillero ?: false)
}
