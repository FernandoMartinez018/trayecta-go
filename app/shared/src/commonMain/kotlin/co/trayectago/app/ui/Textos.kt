package co.trayectago.app.ui

/** Textos de la app (español de Colombia, tuteo). */
object Textos {
    const val NOMBRE_APP = "Trayecta Go"

    const val ERROR_INGRESO = "No pudimos iniciar sesión. Intenta de nuevo en unos segundos. Si sigue fallando, revisa tu conexión."

    // Mismo patrón del estado de error del prototipo (pantalla 20).
    const val SIN_CONEXION_TITULO = "No pudimos conectarnos"
    const val SIN_CONEXION_CUERPO = "Intenta de nuevo en unos segundos. Si sigue fallando, revisa tu conexión."
    const val REINTENTAR = "Reintentar"

    const val FALTA_CONFIGURACION = "Falta la configuración del servidor en esta compilación."

    // Inicio provisional: lo reemplaza la pantalla 4 (mapa) en H2.
    const val INICIO_TITULO = "Todo listo"
    const val INICIO_CUERPO = "Muy pronto verás aquí el mapa y tus rutas."
    const val CERRAR_SESION = "Cerrar sesión"

    // Registro mínimo: la HU-1.1 lo completa con el diseño de la pantalla 12.
    const val REGISTRO_TITULO = "Crea tu cuenta"
    const val CONTINUAR_GOOGLE = "Continuar con Google"
    const val INICIA_SESION = "Inicia sesión"
}
