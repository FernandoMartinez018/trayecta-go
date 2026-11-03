package co.trayectago.app

/** Configuración de la compilación (sabor): URL del backend y de Keycloak, redirect de AppAuth y cliente OIDC. */
data class ConfiguracionApp(
    val urlApi: String,
    val urlAuth: String,
    val redirect: String,
    val clienteId: String = "app-movil",
)
