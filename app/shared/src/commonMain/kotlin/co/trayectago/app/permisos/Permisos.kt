package co.trayectago.app.permisos

/** Permisos del sistema que la app pide en el primer uso. La ubicación en segundo plano no se pide aquí. */
enum class Permiso { UBICACION, NOTIFICACIONES, MICROFONO }

/** Acceso a los permisos del sistema. La implementación de Android vive en androidApp. */
interface GestorPermisos {
    fun concedido(p: Permiso): Boolean

    /** Muestra el diálogo del sistema (si hace falta) y devuelve si el permiso quedó concedido. */
    suspend fun solicitar(p: Permiso): Boolean
}
