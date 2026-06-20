# Kuzhambu Docker Compose

## Services

The compose stack builds and runs:

- `nginx`: the only public HTTP entrypoint.
- `admin-web`: static admin web assets.
- `portal-web`: static portal web assets.
- `admin-starter`: Java admin API starter.
- `portal-starter`: Java portal API starter.
- `workers`: Python FastAPI workers.
- `mysql`, `redis`, `rocketmq-namesrv`, `rocketmq-broker`, `elasticsearch`: foundation services.

## Public Routes

Default HTTP port is `8080`.

- Admin web: `http://localhost:8080/kuzhambu-admin/`
- Admin API through nginx: `http://localhost:8080/kuzhambu-admin-api/api/`
- Portal web: `http://localhost:8080/kuzhambu/`
- Portal API through nginx: `http://localhost:8080/kuzhambu-api/api/`

The Java starters use API-specific context paths:

- `admin-starter`: `/kuzhambu-admin-api`
- `portal-starter`: `/kuzhambu-api`

The frontend web routes stay under `/kuzhambu-admin/` and `/kuzhambu/`.

## Internal Operations Routes

Nginx also exposes internal operations paths for health and OpenAPI inspection:

- Admin health: `/internal/admin/health`
- Admin Swagger UI: `/internal/admin/swagger-ui/`
- Admin OpenAPI JSON: `/internal/admin/v3/api-docs`
- Portal health: `/internal/portal/health`
- Portal Swagger UI: `/internal/portal/swagger-ui/`
- Portal OpenAPI JSON: `/internal/portal/v3/api-docs`
- Workers health: `/internal/workers/health`
- Workers docs: `/internal/workers/docs`
- Workers OpenAPI JSON: `/internal/workers/openapi.json`

## Run

```sh
cd deploy
cp .env.example .env
docker compose --env-file .env up --build
```
