# Diseño

`tokens.json` es una copia del sistema de diseño "Trayecta Go" (artefacto en claude.ai). Es la fuente única de colores (temas Día y Noche), tipografía, espacios, radios, tamaños y sombras.

La historia **HU-0.5** agrega aquí un script que genera desde este archivo:

- el tema de Compose (`ColorScheme` día/noche, `Typography`, `Shapes`, dimensiones);
- más adelante, las constantes de SwiftUI.

Si un token cambia en el sistema de diseño, se actualiza este archivo y se vuelve a generar el tema; nunca se edita el tema generado a mano.
