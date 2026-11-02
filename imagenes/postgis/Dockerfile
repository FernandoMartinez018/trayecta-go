# PostgreSQL 16 + PostGIS para amd64 y arm64: la imagen oficial postgis/postgis solo publica amd64.
FROM postgres:16.15-trixie

ARG POSTGIS_VERSION=3.6.4+dfsg-2.pgdg13+1
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates \
      postgresql-16-postgis-3=${POSTGIS_VERSION} \
      postgresql-16-postgis-3-scripts=${POSTGIS_VERSION} \
 && rm -rf /var/lib/apt/lists/*

COPY --chmod=755 01-keycloak.sh /docker-entrypoint-initdb.d/
