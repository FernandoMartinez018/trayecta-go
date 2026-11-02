#!/bin/bash
# Importa el volcado de Colombia la primera vez (en el volumen) y luego sirve Photon.
set -euo pipefail

DATOS=/photon/data
VOLCADO=/tmp/volcado.jsonl.zst

if [ ! -f "$DATOS/.importado" ]; then
  echo "Descargando el volcado de Photon: $PHOTON_DUMP_URL"
  curl -fsSL -o "$VOLCADO" "$PHOTON_DUMP_URL"
  # El .md5 trae el nombre del archivo remoto; se reescribe al nombre local para verificar.
  curl -fsSL "$PHOTON_DUMP_URL.md5" | awk -v f="$VOLCADO" '{print $1 "  " f}' | md5sum -c -
  rm -rf "$DATOS"/*
  zstd -dc "$VOLCADO" | java $JAVA_OPTS -jar /photon/photon.jar import -import-file - -data-dir "$DATOS" -languages es,en
  rm -f "$VOLCADO"
  touch "$DATOS/.importado"
fi

exec java $JAVA_OPTS -jar /photon/photon.jar serve -data-dir "$DATOS" -listen-ip 0.0.0.0
