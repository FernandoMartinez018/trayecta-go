# App

Kotlin Multiplatform + Compose Multiplatform: `shared/`, `androidApp/` y, en la fase de producción, `iosApp/`. La inicializa la historia **HU-0.4** del backlog.

Variantes de compilación (flavors):

| Flavor | Backend | Distribución |
|---|---|---|
| `pruebas` | Despliegue gratuito (Oracle) | Firebase App Distribution |
| `produccion` | GCP | Google Play |

Las URL de cada entorno se inyectan en la compilación; no se escriben en el código.
