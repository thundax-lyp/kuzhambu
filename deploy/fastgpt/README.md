# FastGPT Docker Compose

This directory runs FastGPT as an independent compose project. It is intentionally separate from `deploy/docker-compose.yml` because FastGPT has its own Mongo, Redis, MinIO, Pgvector, plugin, code sandbox, MCP server, and AIProxy services.

## Run

```sh
cd deploy/fastgpt
cp .env.example .env
docker compose --env-file .env up -d
```

Default public port:

- FastGPT web/API: `http://localhost:13000`

MinIO and MCP services are internal-only by default. Do not expose MinIO directly unless a later integration explicitly needs browser-facing object URLs.

After first startup, log in to FastGPT with user `root` and `FASTGPT_DEFAULT_ROOT_PSW`, then configure at least the language model and vector model.

## API Endpoints

FastGPT OpenAPI base URL:

```text
http://localhost:13000/api
```

OpenAI-compatible chat API:

```text
POST http://localhost:13000/api/v1/chat/completions
```

Dataset APIs for kuzhambu backend synchronization:

```text
http://localhost:13000/api/core/dataset
```

## Deployment Notes

- Change all `FASTGPT_*_TOKEN`, `FASTGPT_*_KEY`, passwords, and `FASTGPT_DEFAULT_ROOT_PSW` before shared or production deployment.
- `FASTGPT_STORAGE_EXTERNAL_ENDPOINT` defaults to the internal MinIO service URL. If browser-facing file URLs are required later, expose object storage deliberately through a reverse proxy and set this to that public URL.
- Keep FastGPT backup, restore, image upgrade, and data volume operations separate from the main kuzhambu compose stack.
- If FastGPT needs to call kuzhambu APIs later, connect the two projects through an explicit public URL or a deliberately shared Docker network rather than relying on implicit compose project networking.

## Stop

```sh
cd deploy/fastgpt
docker compose --env-file .env down
```
