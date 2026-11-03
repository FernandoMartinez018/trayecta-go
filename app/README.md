# App

Kotlin Multiplatform + Compose Multiplatform. Por ahora solo Android; iOS se suma en la fase de producción.

- `shared/`: pantallas (Compose Multiplatform), estado, cliente de la API e interfaz de autenticación.
- `androidApp/`: actividad, inicio de sesión con AppAuth (Custom Tab + PKCE) y sabores.

| Sabor | Backend | Distribución |
|---|---|---|
| `pruebas` (`co.trayectago.app.pruebas`) | Despliegue gratuito (Oracle) | Firebase App Distribution |
| `produccion` (`co.trayectago.app`) | GCP | Google Play |

Las URL de cada entorno entran en la compilación (`-PurlApi`/`-PurlAuth`, o las variables `URL_API_PRUEBAS`/`URL_AUTH_PRUEBAS`); nunca se escriben en el código. AppAuth solo acepta HTTPS, así que la app inicia sesión contra el despliegue de pruebas, no contra el Keycloak local.

Compilar y probar, siempre en Docker: ver `../entorno-local/README.md`.
