package co.trayectago.app.ui

/** Textos de la app (español de Colombia, tuteo). Los de las pantallas salen del prototipo. */
object Textos {
    const val NOMBRE_APP = "Trayecta Go"

    // Pantalla 0 (Splash.dc.html)
    const val LOGO_TRAYECTA = "Trayecta"
    const val LOGO_GO = "Go"
    const val ESLOGAN = "Sal a tiempo, llega seguro"

    // Pantalla 12 (Login.dc.html). «Continuar con Apple» es solo para iOS.
    const val REGISTRO_TITULO = "Crea tu cuenta"
    const val REGISTRO_SUBTITULO = "Guarda tus rutas, recibe tu hora de salida y suma puntos por tus reportes."
    const val CONTINUAR_GOOGLE = "Continuar con Google"
    const val O_CON_CORREO = "o con tu correo"
    const val CORREO_ETIQUETA = "Correo electrónico"
    const val CORREO_MARCADOR = "nombre@correo.com"

    // «Enviarme un enlace»: Keycloak abre con el correo ya escrito (login_hint) y el usuario toca enviar
    // (Keycloak no lo envía solo, por seguridad); luego abre el correo, toca el enlace, vuelve a
    // Trayecta Go y toca «Continuar».
    const val ENVIARME_ENLACE = "Enviarme un enlace"
    const val ENLACE_AYUDA = "Te enviamos un enlace para entrar, sin contraseñas."

    // Sin enlaces en este ciclo: la web con los términos y la política llega antes de producción.
    const val TERMINOS = "Al continuar aceptas los Términos de uso y la Política de privacidad."
    const val YA_TIENES_CUENTA = "¿Ya tienes cuenta? "
    const val INICIA_SESION = "Inicia sesión"

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
}
