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

## External Knowledge Base

FastGPT is intentionally kept in a separate compose project because its dependency stack is large. See `deploy/fastgpt/README.md`.

## Images

Compose uses project-prefixed image names by default:

- `kuzhambu/admin-web:dev`
- `kuzhambu/portal-web:dev`
- `kuzhambu/admin-starter:dev`
- `kuzhambu/portal-starter:dev`
- `kuzhambu/workers:dev`
- `kuzhambu/nginx:1.27-alpine`
- `kuzhambu/mysql:8.4`
- `kuzhambu/redis:7.2`
- `kuzhambu/elasticsearch:8.15.3`
- `kuzhambu/rocketmq:5.3.0`

Override the names with `KUZHAMBU_*_IMAGE` variables in `deploy/.env`. Business images are produced by `docker compose --env-file .env build`. Foundation images must be available locally under the configured names before offline smoke tests or image export.

## Backup And Restore Scripts

The compose stack mounts backup and restore assets directly into `admin-starter`.

Current layout:

- `admin-starter` and `portal-starter` share the same `storage-data` volume so local object storage has a single data root
- `admin-starter` mounts the `backup-data` volume at `${KUZHAMBU_BACKUP_ROOT_PATH}`
- `admin-starter` mounts `deploy/scripts` at `/app/ops-scripts`

The scripts currently provided are:

- `/app/ops-scripts/backup-business-data.sh`
- `/app/ops-scripts/restore-business-data.sh`
- `/app/ops-scripts/cleanup-backups.sh`
- `/app/ops-scripts/business-table-whitelist.txt`

Notes:

- The scripts run inside `admin-starter` and connect to `mysql` over the compose network.
- In `local` mode, the scripts read and restore files from the shared `/app/storage/object` path.
- In `s3` mode, the scripts expect the `aws` CLI to be available inside the `admin-starter` image. If that is not yet true, either extend the image or defer S3 backup execution until the image includes the required tooling.
