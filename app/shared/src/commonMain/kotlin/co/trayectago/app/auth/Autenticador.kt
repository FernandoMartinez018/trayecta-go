package co.trayectago.app.auth

sealed interface OpcionIngreso {
    /** Va directo a Google (kc_idp_hint=google). */
    data object Google : OpcionIngreso

    /** Abre Keycloak con el correo ya escrito (login_hint). */
    data class Correo(val correo: String) : OpcionIngreso

    /** Abre la página de Keycloak sin pistas. */
    data object General : OpcionIngreso
}

sealed interface ResultadoIngreso {
    data object Exito : ResultadoIngreso
    data object Cancelado : ResultadoIngreso
    data class Fallo(val sinConexion: Boolean) : ResultadoIngreso
}

/** Sesión OIDC del usuario; en Android la implementa AppAuth. */
interface Autenticador {
    fun haySesion(): Boolean

    suspend fun iniciarSesion(opcion: OpcionIngreso): ResultadoIngreso

    /**
     * Token de acceso vigente. Devuelve null si la sesión murió (invalid_grant, revocada o inexistente).
     * Lanza ErrorApi.SinConexion si la renovación falló por red o porque Keycloak no respondió bien.
     */
    suspend fun tokenDeAcceso(forzarRenovacion: Boolean = false): String?

    suspend fun cerrarSesion()
}
