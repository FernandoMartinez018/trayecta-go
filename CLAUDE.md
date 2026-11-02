# Trayecta Go — guía para Claude Code

Lee este archivo completo antes de cada tarea. Las decisiones aquí escritas ya fueron aprobadas por el dueño del producto (Fernando Martínez); no las cambies sin preguntar.

## Qué es el producto

Trayecta Go ("Sal a tiempo, llega seguro") es una app móvil de rutas para **motociclistas y domiciliarios**, con piloto en **Bogotá y municipios vecinos**. Diferenciadores:

1. **Rutas por hora de llegada**: el usuario dice a qué hora quiere llegar; el sistema calcula cuándo salir con el tráfico y avisa. El widget muestra "Iniciar" 5 minutos antes de la salida.
2. **Rutas para moto**: pico y placa (por último dígito de placa), restricciones de parrillero y vías prohibidas para motos.
3. **Alertas comunitarias** (control vial, accidente, hueco, vía inundada, vía cerrada, zona de riesgo) con confirmación, vencimiento y reputación.
4. **Manos libres activado por defecto**; el usuario puede desactivarlo.
5. **Modo domiciliario**: varias paradas con orden optimizado.

## Documentación (fuera del repositorio)

Toda la documentación del proyecto vive **solo** en la carpeta del PC de Fernando:

`C:\proyectosPersonales\contextoa\optimus routes`

Antes de cada tarea:

1. Lee `output/estado.md` de esa carpeta (qué está hecho, qué sigue, bloqueos). No releas todo.
2. Usa el índice de `README.md` de esa carpeta para abrir solo lo que la tarea necesita.

Al terminar cada tarea (aunque quede incompleta):

1. Agrega una entrada al inicio de `output/registro.md` con el formato de `output/README.md`.
2. Reescribe `output/estado.md`.
3. Actualiza los documentos afectados (`01-producto/backlog.md`, `00-contexto/decisiones.md`, `02-diseno/diseno.md`, etc.).
4. Si el código y la documentación no coinciden, no decidas tú: anótalo en `output/estado.md` → Inconsistencias y pregunta a Fernando.

**No crees documentación dentro del repositorio** (nada de `docs/`). En el repositorio solo van código, contratos, migraciones, configuración y este archivo. Si la carpeta no es accesible (por ejemplo, Claude Code corre en otro equipo), detente y avísale a Fernando.

Fuentes de verdad dentro de este repositorio:

- `contratos/openapi/v1.yaml` y `contratos/asyncapi/v1.yaml`: API y eventos.
- `backend/src/main/resources/db/migration/`: esquema de base de datos.
- `diseno/tokens.json`: tokens del sistema de diseño (colores, tipografía, espacios, radios, tamaños).

## Estructura

```
contratos/openapi/v1.yaml     API REST (fuente de verdad)
contratos/asyncapi/v1.yaml    Eventos de RabbitMQ (CloudEvents)
backend/                      Kotlin + Ktor, monolito modular
app/                          Kotlin Multiplatform: shared/, androidApp/ (iosApp/ en producción)
identidad/                    Imagen de Keycloak: realm, tema y extensión del enlace mágico
imagenes/                     Imágenes propias: PostGIS (amd64 + arm64) y Photon (solo Colombia)
pruebas-e2e/                  Pruebas de extremo a extremo con Playwright
web/                          Landing y política de privacidad (Vercel)
diseno/                       tokens.json y script de conversión a temas
entorno-local/                Docker Compose para desarrollo y pruebas
```

La infraestructura y los despliegues viven en otro repositorio: `trayecta-go-infra`.

## Stack (no cambiar sin aprobación)

- **Backend**: Kotlin + Ktor, JVM 21, Gradle (Kotlin DSL). Monolito modular por paquetes: `usuarios`, `rutas`, `programadas`, `alertas`, `telemetria`, `notificaciones`, `privacidad`, `reglas`. Cada módulo expone una interfaz; nada cruza módulos por la base de datos.
- **Base de datos**: PostgreSQL 16 + PostGIS. Migraciones con **Flyway** en `backend/src/main/resources/db/migration`. **Nunca edites una migración ya aplicada**: crea una nueva.
- **Mensajería**: RabbitMQ, eventos en formato CloudEvents 1.0 según `contratos/asyncapi/v1.yaml`.
- **Rutas**: Valhalla (perfil `motorcycle`), VROOM para varias paradas, Photon para direcciones. El backend es el único que les habla; la app nunca llama a Valhalla directamente.
- **Identidad**: Keycloak (OIDC + PKCE). El backend valida JWT con el JWKS de Keycloak.
- **App**: Kotlin Multiplatform + Compose Multiplatform. Mapa con MapLibre Native, navegación con Ferrostar, widget Android con Glance. **Android primero**; iOS se activa en la fase de producción.
- **Contratos primero**: todo endpoint nuevo o cambiado se escribe primero en `contratos/openapi/v1.yaml`; el servidor y el cliente se generan o ajustan desde ahí.

## Un solo código, dos despliegues

El mismo código corre en dos entornos; **solo cambia la configuración**:

| | `pruebas` (gratis) | `produccion` |
|---|---|---|
| Servidores | VM Oracle Cloud Always Free (Docker Compose) | GCP us-east1 (Cloud Run + VM) |
| Almacenamiento | Object Storage de Oracle, vía API S3 | Cloudflare R2 / GCS, vía API S3 |
| HTTPS | Caddy + subdominio DuckDNS | Dominio propio + Cloudflare |
| App | APK por Firebase App Distribution | Google Play / App Store |

Reglas para que esto funcione:

- Toda configuración entra por **variables de entorno** (12-factor). Nada de URLs, credenciales ni nombres de entorno en el código.
- El backend se empaqueta como **una sola imagen de contenedor** que sirve para ambos entornos.
- Almacenamiento de archivos siempre por **API S3**.
- Funciones con interruptor (`FEATURE_*`): `FEATURE_ALERTAS_POLICIA`, `FEATURE_IA_NOTICIAS`, `FEATURE_PUSH_IOS`.

## Diseño

- Usa **solo** los tokens de `diseno/tokens.json`; nunca colores, tamaños o radios sueltos en el código.
- Marca: petróleo (`brand`). Alertas viales: **ámbar con ícono**, nunca el color de marca. Rojo (`critical`) solo para errores de la app.
- Tipografía: Plus Jakarta Sans en títulos, cifras y botones; fuente del sistema en el texto.
- Controles táctiles de **52 dp mínimo**; 64 dp para acciones en movimiento.
- Íconos: Lucide; las 6 alertas tienen íconos propios (ver `02-diseno/diseno.md` y `02-diseno/iconos-alertas/` en la carpeta de documentación).
- Tráfico en la ruta por **patrón**, no por color: fluido continuo, lento con puntos, trancón con rayas, siempre con el retraso en texto.
- Tema Día y Noche en todo componente. Textos en **español de Colombia**, tuteo, horas en formato 12 h ("8:00 a. m."), decimales con coma ("1,2 km").

## Privacidad y seguridad (Ley 1581 de 2012)

- Del vehículo solo se guarda el **último dígito de la placa**.
- Recorridos GPS detallados: se borran a los **30 días** y quedan solo agregados anónimos. Historial de viajes: 12 meses.
- La telemetría usa identificadores seudónimos rotativos; nunca el id del usuario.
- Consentimientos separados: navegación, aprender rutas, aportar al tráfico, comunicaciones comerciales.
- Logs sin datos personales (sin coordenadas exactas, correos ni tokens).
- Secretos solo en variables de entorno o gestores de secretos. **Nunca** en el repositorio.
- Toda acción administrativa queda en la tabla `auditoria`.

## Cómo trabajar

1. Toma la siguiente historia de `01-producto/backlog.md` en la carpeta de documentación (o la que indique Fernando).
2. Crea una rama `hX/descripcion-corta` (X = número de hito).
3. Si toca la API o los eventos, actualiza primero los contratos.
4. Implementa con pruebas unitarias para la lógica crítica: cálculo de hora de salida, reglas de pico y placa, confianza y vencimiento de alertas.
5. Verifica que compila y que las pruebas pasan antes de pedir revisión.
6. Abre un pull request en español, con qué cambió, cómo probarlo y la historia que cierra. Commits con Conventional Commits (`feat:`, `fix:`, `docs:`…).
7. Al cerrar, marca la historia `[x]` en `01-producto/backlog.md` y actualiza `output/registro.md` y `output/estado.md` en la carpeta de documentación.

## Lo que no debes hacer

- No cambiar el stack, la paleta, el modelo de despliegue ni las reglas de privacidad sin aprobación.
- No agregar dependencias de pago ni servicios en la nube no listados.
- No llamar a APIs de Google Maps ni de otros proveedores de mapas comerciales.
- No usar verde/amarillo/rojo para el tráfico ni el color de marca para alertas.
- No hacer `push` directo a `main`.
- No escribir datos personales reales en pruebas, ejemplos ni logs.

## Comandos

Todo corre en Docker; el PC solo necesita Docker Desktop. En Git Bash, antepón `MSYS_NO_PATHCONV=1` a los comandos que pasan rutas del contenedor. Primer arranque y tiempos: `entorno-local/README.md`.

```bash
# Entorno local
docker compose -f entorno-local/compose.yaml up -d --build

# Pruebas E2E (identidad y API)
docker compose -f entorno-local/compose.yaml --profile e2e run --rm e2e

# Contratos
docker compose -f entorno-local/compose.yaml --profile herramientas run --rm nodo npx --yes @redocly/cli@1.25.11 lint contratos/openapi/v1.yaml

# Apagar (sin -v, para conservar mapas y bases)
docker compose -f entorno-local/compose.yaml down
```

Los comandos del backend, la app y el tema se agregan con sus historias (HU-0.2, HU-0.4 y HU-0.5).
