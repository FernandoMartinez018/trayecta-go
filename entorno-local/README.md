# Entorno local

Servicios que el backend y las pruebas necesitan, todos en Docker. No hace falta instalar nada más que Docker Desktop.

| Servicio | Puerto | Para qué |
|---|---|---|
| PostgreSQL 16 + PostGIS 3.6.4 | 5432 | Bases `trayecta` y `trayecta_pruebas` (usuario `trayecta`, clave `solo-local`) |
| Keycloak 26.8.1 + enlace mágico | 8081 | Realm `trayecta-go`; consola en http://host.docker.internal:8081/admin (`admin` / `solo-local`) |
| Mailpit | 8025 (web) · 1025 (SMTP) | Atrapa los correos de Keycloak: http://localhost:8025 |
| RabbitMQ | 5672 · 15672 (consola) | Mensajería (`trayecta` / `solo-local`) |
| Valhalla | 8002 | Rutas (perfil `motorcycle`) con el mapa de Colombia |
| Photon | 2322 | Búsqueda de direcciones en Colombia |

Las contraseñas por defecto son solo para tu máquina; nunca se usan en los despliegues.

## Antes del primer arranque

Docker Desktop necesita **10 GB de memoria o más** para WSL. Revísalo con:

```bash
docker info --format '{{.MemTotal}}'
```

Si da menos, sube `memory` en `%UserProfile%\.wslconfig` (por ejemplo `memory=12GB` en la sección `[wsl2]`) y reinicia Docker Desktop.

## Primer arranque

```bash
docker compose -f entorno-local/compose.yaml up -d --build
```

- **Valhalla** descarga el extracto de Colombia de Geofabrik (unos 320 MB) y genera sus tiles. En un PC de 14 núcleos tomó unos 8 minutos, con un pico de 2,4 GB de RAM; después usa unos 0,5 GB.
- **Photon** descarga el volcado de Colombia (unos 94 MB), verifica su MD5 y lo importa. Tomó unos 7 minutos, con un pico de 2,1 GB; después usa unos 0,4 GB.
- Los datos quedan en **volúmenes de Docker con nombre** (no en carpetas de Windows, que rompen los permisos de PostgreSQL). Los siguientes arranques tardan segundos.
- **Keycloak** usa una base interna que se borra con el contenedor: cada contenedor nuevo vuelve a importar `identidad/realm/realm-trayecta-go.json`.

Para ver el avance: `docker compose -f entorno-local/compose.yaml ps` (todo debe quedar `healthy`).

## Pruebas

En Git Bash, antepón `MSYS_NO_PATHCONV=1` a los comandos que pasan rutas del contenedor.

```bash
# Backend: pruebas (usan la base trayecta_pruebas, que se trunca)
docker compose -f entorno-local/compose.yaml --profile herramientas run --rm herramientas ./gradlew test

# Backend: imagen local (una sola arquitectura) y arranque en http://localhost:8080
docker compose -f entorno-local/compose.yaml --profile herramientas run --rm herramientas ./gradlew jibBuildTar -Djib.from.platforms=linux/amd64 -Djib.to.image=trayecta-go-backend:local -PversionApp=local
docker load -i backend/build/jib-image.tar
docker compose -f entorno-local/compose.yaml --profile app up -d backend

# App Android: pruebas, lint y APK de pruebas (la primera vez construye la imagen con el Android SDK, ~1,5 GB)
docker compose -f entorno-local/compose.yaml --profile herramientas run --rm android ./gradlew :shared:testAndroidHostTest :androidApp:lintPruebasDebug :androidApp:assemblePruebasDebug -PurlApi=https://api.ejemplo.invalid -PurlAuth=https://auth.ejemplo.invalid

# E2E de identidad y API (Keycloak, Mailpit y, para la API, el backend levantados)
docker compose -f entorno-local/compose.yaml --profile e2e run --rm e2e
```

## Apagar

```bash
docker compose -f entorno-local/compose.yaml down
```

Sin `-v`: así se conservan el mapa, los tiles y las bases. `down -v` lo borra todo y el próximo arranque vuelve a descargar.

Para actualizar el mapa de OSM: borra el volumen `trayecta-go-local_valhalla-datos` y vuelve a levantar Valhalla.
