# Backend

Kotlin + Ktor (JVM 21), monolito modular por paquetes (`comun`, `usuarios`, …).

- Migraciones: `src/main/resources/db/migration` (Flyway). Una migración aplicada no se edita: se crea otra.
- Contrato: `../contratos/openapi/v1.yaml`.
- Configuración solo por variables de entorno; las de ejemplo están en `.env.ejemplo`.
- Pruebas, imagen local y arranque, siempre en Docker: ver `../entorno-local/README.md`.
