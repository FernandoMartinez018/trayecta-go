package co.trayectago.backend.comun

/** Configuración del backend; todo entra por variables de entorno (12-factor). */
data class Configuracion(
    val puerto: Int,
    val entorno: String,
    val version: String,
    val db: ConfigBaseDatos,
    val oidc: ConfigOidc,
) {
    companion object {
        private val REQUERIDAS = listOf("DB_URL", "DB_USUARIO", "DB_CLAVE", "OIDC_ISSUER")

        fun desdeEntorno(env: Map<String, String>): Configuracion {
            val faltan = REQUERIDAS.filter { env[it].isNullOrBlank() }
            check(faltan.isEmpty()) { "Faltan variables de entorno: ${faltan.joinToString(", ")}" }
            val issuer = env.getValue("OIDC_ISSUER").trimEnd('/')
            return Configuracion(
                puerto = env["PUERTO"]?.toInt() ?: 8080,
                entorno = env["ENTORNO"] ?: "local",
                version = env["VERSION"] ?: "dev",
                db = ConfigBaseDatos(env.getValue("DB_URL"), env.getValue("DB_USUARIO"), env.getValue("DB_CLAVE")),
                oidc = ConfigOidc(
                    issuer = issuer,
                    jwksUrl = env["OIDC_JWKS_URL"] ?: "$issuer/protocol/openid-connect/certs",
                    audiencia = env["OIDC_AUDIENCIA"] ?: "trayecta-go-api",
                ),
            )
        }
    }
}

data class ConfigBaseDatos(val url: String, val usuario: String, val clave: String) {
    // La clave nunca debe terminar en un log por un toString().
    override fun toString() = "ConfigBaseDatos(url=$url, usuario=$usuario, clave=***)"
}

data class ConfigOidc(val issuer: String, val jwksUrl: String, val audiencia: String)
