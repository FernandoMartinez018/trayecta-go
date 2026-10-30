# Backend

Kotlin + Ktor (JVM 21), monolito modular. Lo inicializa la historia **HU-0.2** del backlog.

- Migraciones: `src/main/resources/db/migration` (Flyway). `V1__esquema_inicial.sql` ya está escrita a partir del modelo de datos.
- Contrato: `../contratos/openapi/v1.yaml`.
- Configuración solo por variables de entorno; ver `.env.ejemplo` (lo crea la HU-0.2).
