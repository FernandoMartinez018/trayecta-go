package co.trayectago.app

import co.trayectago.app.ingreso.correoValido
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidacionCorreoTest {
    @Test
    fun `acepta un correo normal`() = assertTrue(correoValido("ana@correo.com"))

    @Test
    fun `ignora los espacios de los extremos`() = assertTrue(correoValido(" ana@correo.com "))

    @Test
    fun `rechaza un dominio sin punto`() = assertFalse(correoValido("ana@correo"))

    @Test
    fun `rechaza espacios dentro del correo`() = assertFalse(correoValido("ana correo@x.com"))

    @Test
    fun `rechaza el campo vacío`() = assertFalse(correoValido(""))
}
