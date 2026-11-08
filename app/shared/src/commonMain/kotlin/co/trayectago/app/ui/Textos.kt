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
    const val ERROR_GUARDAR_VEHICULO = "No pudimos guardar tu vehículo. Intenta de nuevo en unos segundos. Si sigue fallando, revisa tu conexión."

    const val CONTINUAR = "Continuar"

    // Pasos del primer uso: «Paso 1 de 3» (vehículo) y «Paso 2 de 3» (permisos).
    fun paso(actual: Int, total: Int) = "Paso $actual de $total"

    // Pantalla 1 (Main.dc.html): vehículo
    const val VEHICULO_TITULO = "Bienvenido a Trayecta Go"
    const val VEHICULO_SUBTITULO = "Cuéntanos en qué te mueves para darte rutas que sí te sirven."
    const val VEHICULO_SECCION = "TU VEHÍCULO"
    const val VEHICULO_MOTO = "Moto"
    const val VEHICULO_CARRO = "Carro"
    const val PROXIMAMENTE = "Próximamente"
    const val DIGITO_SECCION = "ÚLTIMO DÍGITO DE TU PLACA"
    const val DIGITO_AYUDA = "Solo guardamos este dígito, para aplicar el pico y placa."
    const val PARRILLERO_TITULO = "¿Sueles llevar parrillero?"
    const val PARRILLERO_AYUDA = "Para las restricciones de parrillero"
    fun placaTerminadaEn(digito: Int) = "Placa terminada en $digito"

    // Pantalla 2 (Permisos.dc.html): permisos graduales
    const val PERMISOS_TITULO = "Para guiarte necesitamos algunos permisos"
    const val PERMISOS_SUBTITULO = "Te explicamos para qué sirve cada uno. Puedes cambiarlos cuando quieras."
    const val PERMISO_UBICACION = "Ubicación"
    const val PERMISO_UBICACION_AYUDA = "Mientras usas la app, para guiarte"
    const val PERMISO_NOTIFICACIONES = "Notificaciones"
    const val PERMISO_NOTIFICACIONES_AYUDA = "Para avisarte a qué hora salir"
    const val PERMISO_MICROFONO = "Micrófono"
    const val PERMISO_MICROFONO_AYUDA = "Para manos libres, que viene activado"
    const val PERMISO_SEGUNDO_PLANO = "Ubicación en segundo plano"
    const val PERMISO_SEGUNDO_PLANO_AYUDA = "Solo si activas \"Aprender mis rutas\""
    const val PERMITIR = "Permitir"
    const val PERMITIDO = "Permitido"
    const val AHORA_NO = "Ahora no"

    // Tres botones «Permitir» seguidos: TalkBack necesita saber cuál es cuál.
    fun permitir(permiso: String) = "Permitir ${permiso.lowercase()}"
    fun permitido(permiso: String) = "Permitido: ${permiso.lowercase()}"

    // Inicio provisional: lo reemplaza la pantalla 4 (mapa) en H2.
    const val INICIO_TITULO = "Todo listo"
    const val INICIO_CUERPO = "Muy pronto verás aquí el mapa y tus rutas."
    const val CERRAR_SESION = "Cerrar sesión"
}
