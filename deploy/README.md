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

Nginx renders its upstream configuration from the same starter port variables that Compose passes to the Java services:

- `KUZHAMBU_ADMIN_SERVER_PORT`, default `20010`
- `KUZHAMBU_PORTAL_SERVER_PORT`, default `20020`

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

Compose builds project-prefixed business image names by default:

- `kuzhambu/admin-web:dev`
- `kuzhambu/portal-web:dev`
- `kuzhambu/admin-starter:dev`
- `kuzhambu/portal-starter:dev`
- `kuzhambu/workers:dev`

Foundation images are pulled from their upstream repositories or built as project-owned images:

- `nginx:1.27-alpine`
- `mysql:8.4`
- `redis:7.2`
- `kuzhambu/elasticsearch:8.18.8`
- `apache/rocketmq:5.3.0`

Business image names default to `${KUZHAMBU_IMAGE_REGISTRY:-kuzhambu}/*:${KUZHAMBU_IMAGE_TAG:-dev}`. Override one image with the matching `KUZHAMBU_*_IMAGE` variable when a stage needs a different tag.

Foundation image versions default to `KUZHAMBU_MYSQL_VERSION=8.4`, `KUZHAMBU_REDIS_VERSION=7.2`, `KUZHAMBU_ELASTICSEARCH_VERSION=8.18.8`, and `KUZHAMBU_ROCKETMQ_VERSION=5.3.0`. The full image reference can still be overridden with the matching `KUZHAMBU_*_IMAGE` variable. Business images and the project Elasticsearch image are produced by `docker compose --env-file .env build`. Pulled foundation images may be retagged into an offline registry when the target environment cannot access the upstream source.

`kuzhambu/elasticsearch:8.18.8` is built from an Elasticsearch `8.18.8` base image and installs the same-version `analysis-ik` plugin. Keep these values aligned:

- `KUZHAMBU_ELASTICSEARCH_VERSION=8.18.8`
- `KUZHAMBU_ELASTICSEARCH_IK_VERSION=8.18.8`
- `KUZHAMBU_ELASTICSEARCH_DOCKER_PLATFORM=linux/amd64`

When the Docker daemon cannot pull Elastic images directly, pull `container-registry-test.elastic.co/elasticsearch/elasticsearch:${KUZHAMBU_ELASTICSEARCH_VERSION}` with `crane` through the local proxy, load it, and retag it as `kuzhambu/elasticsearch-base:${KUZHAMBU_ELASTICSEARCH_VERSION}`. Compose uses that project-owned base image by default so the actual ES build does not depend on Docker daemon registry access:

```sh
HTTP_PROXY=http://127.0.0.1:1082 HTTPS_PROXY=http://127.0.0.1:1082 \
  crane pull --platform linux/amd64 \
  "container-registry-test.elastic.co/elasticsearch/elasticsearch:${KUZHAMBU_ELASTICSEARCH_VERSION}" \
  "/tmp/elasticsearch-${KUZHAMBU_ELASTICSEARCH_VERSION}.tar"
docker load -i "/tmp/elasticsearch-${KUZHAMBU_ELASTICSEARCH_VERSION}.tar"
docker tag \
  "container-registry-test.elastic.co/elasticsearch/elasticsearch:${KUZHAMBU_ELASTICSEARCH_VERSION}" \
  "kuzhambu/elasticsearch-base:${KUZHAMBU_ELASTICSEARCH_VERSION}"
```

Before building the final ES image, download `elasticsearch-analysis-ik-${KUZHAMBU_ELASTICSEARCH_IK_VERSION}.zip` into `deploy/elasticsearch/`; the plugin archive is a local build input and must not be committed. The canonical IK download URL is `https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-${KUZHAMBU_ELASTICSEARCH_IK_VERSION}.zip`:

```sh
curl --fail --location \
  --output "deploy/elasticsearch/elasticsearch-analysis-ik-${KUZHAMBU_ELASTICSEARCH_IK_VERSION}.zip" \
  "https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-${KUZHAMBU_ELASTICSEARCH_IK_VERSION}.zip"
```

Discovery search defaults to `ik_max_word` for indexing and `ik_smart` for search; override them with `KUZHAMBU_DISCOVERY_SEARCH_INDEX_ANALYZER` and `KUZHAMBU_DISCOVERY_SEARCH_SEARCH_ANALYZER` only when the target ES image provides compatible analyzers.

Offline Docker image archives belong under `deploy/image-files/`. That directory is ignored except for its README and `.gitignore`; do not commit `docker save` tar files. Save one archive per image. Business archives use names like `kuzhambu-admin-web-dev.tar`; foundation archives use names like `foundation-mysql-8.4.tar`, `foundation-redis-7.2.tar`, `foundation-elasticsearch-8.18.8.tar`, and `foundation-rocketmq-5.3.0.tar`.

Dockerfile base images are configurable because CI and deployment hosts may have different registry access. These base images are build inputs, not release artifacts. Defaults are `node:22-bookworm-slim`, `nginx:1.27-alpine`, `maven:3.9.11-eclipse-temurin-17`, `eclipse-temurin:17-jre`, and `python:3.10-slim`. Override `KUZHAMBU_WEB_BUILD_IMAGE`, `KUZHAMBU_WEB_RUNTIME_IMAGE`, `KUZHAMBU_SERVER_BUILD_IMAGE`, `KUZHAMBU_SERVER_RUNTIME_IMAGE`, or `KUZHAMBU_WORKERS_BASE_IMAGE` without changing Dockerfiles.

When a deployment host cannot pull those defaults, first rebuild or import the usable upstream image as a project-owned base image, then point compose to that `kuzhambu/*` tag:

- `KUZHAMBU_WEB_BUILD_IMAGE=kuzhambu/build-node:22`
- `KUZHAMBU_WEB_RUNTIME_IMAGE=kuzhambu/runtime-nginx:1.27`
- `KUZHAMBU_SERVER_BUILD_IMAGE=kuzhambu/build-maven-temurin17:3.9`
- `KUZHAMBU_SERVER_RUNTIME_IMAGE=kuzhambu/runtime-temurin17:17`
- `KUZHAMBU_WORKERS_BASE_IMAGE=kuzhambu/build-python:3.10`

Do not leave ad-hoc third-party image references in committed env files or repeatable deployment commands. If an image is borrowed from another local stack, retag or rebuild it under the project-owned `kuzhambu/*` name before using it in compose.

When the build host must use a local proxy, set `KUZHAMBU_BUILD_NETWORK=host` and provide standard `HTTP_PROXY` / `HTTPS_PROXY` / `NO_PROXY` values. Keep the default `KUZHAMBU_BUILD_NETWORK=default` for normal compose environments.

For Maven dependency downloads behind a proxy, also set `MAVEN_OPTS` with Java proxy properties, for example `-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=1082 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=1082`.

When the build host should prefer domestic package mirrors, set `COREPACK_NPM_REGISTRY` for the pnpm distribution download, `NPM_CONFIG_REGISTRY` for pnpm package installs, and `PIP_INDEX_URL` / `PIP_TRUSTED_HOST` for pip. Dockerfiles keep BuildKit caches for pnpm, Maven, and pip downloads so repeated compose builds do not redownload unchanged dependencies.

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
