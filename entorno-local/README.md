# Entorno local

Servicios que el backend necesita para desarrollar: PostGIS, RabbitMQ, Keycloak, Valhalla y Photon.

## Primer arranque

1. Descarga el extracto de OpenStreetMap de Colombia desde Geofabrik (`colombia-latest.osm.pbf`) y déjalo en `entorno-local/datos/osm/`. La carpeta `datos/` está ignorada por git.
2. Levanta los servicios:
   ```bash
   docker compose -f entorno-local/compose.yaml up -d
   ```
3. Valhalla genera sus tiles en el primer arranque; puede tardar varios minutos. Revisa el avance con `docker compose -f entorno-local/compose.yaml logs -f valhalla`.
4. Keycloak importa el realm `trayecta-go` desde `entorno-local/keycloak/` (lo crea la HU-0.1).

## Puertos

| Servicio | Puerto |
|---|---|
| PostgreSQL | 5432 |
| RabbitMQ (AMQP / consola) | 5672 / 15672 |
| Keycloak | 8081 |
| Valhalla | 8002 |
| Photon | 2322 |

Las contraseñas por defecto son solo para tu máquina. Nunca se usan en los despliegues.
