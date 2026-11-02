# syntax=docker/dockerfile:1
# Photon con solo Colombia: no hay imagen oficial y la comunitaria no admite un país suelto.
FROM eclipse-temurin:21.0.13_9-jre-noble

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl zstd \
 && rm -rf /var/lib/apt/lists/*

ADD --checksum=sha256:a89707c0045e4807b2a1180e132e68e108d998709f48b6c94b98a6e281f571a5 \
    https://github.com/komoot/photon/releases/download/1.3.0/photon-1.3.0.jar /photon/photon.jar
COPY --chmod=755 arrancar.sh /photon/arrancar.sh

ENV PHOTON_DUMP_URL=https://download1.graphhopper.com/public/south-america/colombia/photon-dump-colombia-1.0-latest.jsonl.zst
ENV JAVA_OPTS=-Xmx2g
EXPOSE 2322
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -fsS http://localhost:2322/status || exit 1

ENTRYPOINT ["/photon/arrancar.sh"]
