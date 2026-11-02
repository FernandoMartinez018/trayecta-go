# syntax=docker/dockerfile:1
# Keycloak con el enlace mágico de Phase Two (Elastic License 2.0), el tema trayecta y el realm.
# Local: start-dev (ver entorno-local/compose.yaml). Pruebas: start --optimized con PostgreSQL.
FROM quay.io/keycloak/keycloak:26.8.1-2@sha256:3f3c5f8e03c614774b4bd9173b88391fefa4153bfc010d8012d59ad825377332 AS builder

ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ADD --chown=keycloak:keycloak --chmod=644 \
    --checksum=sha256:e9e7c3df0c0b36fd72ed5dfbcd305d281b4a18ffd88cdff7685103194e9072e1 \
    https://repo1.maven.org/maven2/io/phasetwo/keycloak/keycloak-magic-link/0.94/keycloak-magic-link-0.94.jar \
    /opt/keycloak/providers/
COPY --chown=keycloak:keycloak temas/trayecta /opt/keycloak/themes/trayecta
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.8.1-2@sha256:3f3c5f8e03c614774b4bd9173b88391fefa4153bfc010d8012d59ad825377332
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --chown=keycloak:keycloak realm/realm-trayecta-go.json /opt/keycloak/data/import/
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized", "--import-realm"]
