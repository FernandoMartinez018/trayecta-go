# Diseño

`tokens.json` es la copia del sistema de diseño "Trayecta Go" (artefacto en claude.ai). Es la fuente única de colores (temas Día y Noche), tipografía, espacios, radios, tamaños y sombras.

`generar-tema.mjs` (Node, sin dependencias) convierte los tokens en `app/shared/src/commonMain/kotlin/co/trayectago/app/tema/TemaGenerado.kt`: colores de día y noche, estilos de texto, espacios, radios y tamaños. Las sombras se agregan cuando haya hojas inferiores.

Si un token cambia en el sistema de diseño, se actualiza `tokens.json` y se regenera el tema; **nunca se edita `TemaGenerado.kt` a mano**. El CI regenera el tema y falla si no coincide con el archivo del repositorio.

```bash
# Pruebas del generador y regeneración del tema (en Docker)
docker compose -f entorno-local/compose.yaml --profile herramientas run --rm nodo node --test 'diseno/*.test.mjs'
docker compose -f entorno-local/compose.yaml --profile herramientas run --rm nodo node diseno/generar-tema.mjs
```
