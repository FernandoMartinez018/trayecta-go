package co.trayectago.app.ingreso

private val FORMATO_CORREO = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** Validación mínima antes de abrir Keycloak; Keycloak vuelve a validar el correo. */
fun correoValido(texto: String): Boolean = FORMATO_CORREO.matches(texto.trim())
