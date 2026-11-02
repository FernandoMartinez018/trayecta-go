#!/bin/sh
# Crea el rol y la base de Keycloak solo si el despliegue pasa la clave; en local Keycloak usa dev-file.
set -eu

if [ -n "${KEYCLOAK_DB_PASSWORD:-}" ]; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    -v clave="$KEYCLOAK_DB_PASSWORD" <<'SQL'
CREATE ROLE keycloak LOGIN PASSWORD :'clave';
CREATE DATABASE keycloak OWNER keycloak;
SQL
fi
