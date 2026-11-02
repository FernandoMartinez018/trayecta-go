package co.trayectago.backend

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MigracionTest {
    @Test
    fun `flyway aplica V1 en la base de pruebas`() {
        baseDePruebas().connection.use { c ->
            c.createStatement().executeQuery("SELECT to_regclass('public.usuario') IS NOT NULL").use { r ->
                assertTrue(r.next() && r.getBoolean(1), "falta la tabla usuario")
            }
            c.createStatement().executeQuery("SELECT success FROM flyway_schema_history WHERE version = '1'").use { r ->
                assertTrue(r.next() && r.getBoolean(1), "V1 no quedó aplicada")
            }
        }
    }

    @Test
    fun `las pruebas se niegan a usar una base que no sea de pruebas`() {
        assertFailsWith<IllegalStateException> { exigirBaseDePruebas("jdbc:postgresql://db:5432/trayecta") }
    }
}
