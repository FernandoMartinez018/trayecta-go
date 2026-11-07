package co.trayectago.backend

import co.trayectago.backend.comun.SolicitudInvalida
import co.trayectago.backend.usuarios.Vehiculo
import co.trayectago.backend.usuarios.VehiculoEntrada
import co.trayectago.backend.usuarios.validarVehiculo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ValidacionVehiculoTest {
    @Test
    fun `valor ausente de parrillero usa false`() {
        assertEquals(Vehiculo("moto", 4, false), validarVehiculo(VehiculoEntrada("moto", 4, null)))
    }

    @Test
    fun `tipo distinto de moto se rechaza`() {
        assertEquals("tipo debe ser \"moto\"", assertFailsWith<SolicitudInvalida> {
            validarVehiculo(VehiculoEntrada("carro", 4))
        }.message)
    }

    @Test
    fun `tipo ausente se rechaza`() {
        assertEquals("tipo debe ser \"moto\"", assertFailsWith<SolicitudInvalida> {
            validarVehiculo(VehiculoEntrada(null, 4))
        }.message)
    }

    @Test
    fun `digitos fuera de rango y ausente se rechazan`() {
        listOf(10, -1, null).forEach { digito ->
            assertEquals("ultimoDigitoPlaca debe ser un número entre 0 y 9", assertFailsWith<SolicitudInvalida> {
                validarVehiculo(VehiculoEntrada("moto", digito))
            }.message)
        }
    }
}
