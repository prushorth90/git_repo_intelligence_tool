# Infrastructure

Local infrastructure is defined in `docker/compose.yml` and currently consists of PostgreSQL and Redis.

- PostgreSQL stores connected repositories. The API owns schema evolution through Flyway migrations in `backend/api/src/main/resources/db/migration`.
- Redis stores API cache entries and coordinates pending analysis jobs through the `analysis:pending` list.
- Named Docker volumes preserve both data stores between restarts.

Future production infrastructure definitions, observability configuration, and secret management belong in this directory. Do not add database schema scripts here; Flyway remains the schema source of truth.
