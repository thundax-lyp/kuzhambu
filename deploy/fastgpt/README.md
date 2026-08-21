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

For a standalone manual environment, log in to FastGPT with user `root` and
`FASTGPT_DEFAULT_ROOT_PSW`, then configure at least the language model and vector model.

For a Kuzhambu smoke or release environment, do not rely on manual FastGPT setup. Enable bootstrap
in `.env` and provide the LLM, embedding, OpenAPI key, dataset name and app name settings:

```sh
FASTGPT_BOOTSTRAP_ENABLED=true
FASTGPT_BOOTSTRAP_LLM_MODEL=<llm-model>
FASTGPT_BOOTSTRAP_LLM_BASE_URL=<openai-compatible-base-url>
FASTGPT_BOOTSTRAP_LLM_API_KEY=<llm-api-key>
FASTGPT_BOOTSTRAP_EMBEDDING_MODEL=<embedding-model>
FASTGPT_BOOTSTRAP_EMBEDDING_BASE_URL=<openai-compatible-base-url>
FASTGPT_BOOTSTRAP_EMBEDDING_API_KEY=<embedding-api-key>
FASTGPT_KUZHAMBU_OPENAPI_KEY=<fastgpt-openapi-key-for-kuzhambu>
FASTGPT_KUZHAMBU_DATASET_NAME=kuzhambu
FASTGPT_KUZHAMBU_APP_NAME=kuzhambu-qa
FASTGPT_KUZHAMBU_BASE_URL=http://fastgpt-app:3000
```

`.env` 会被 bootstrap 和 smoke 脚本用 shell 读取；包含空格、`$` 或其他 shell 特殊字符的值
必须加引号。

Then run:

```sh
cd deploy/fastgpt
./bootstrap-fastgpt.sh .env
```

The bootstrap is idempotent. It writes or updates:

- FastGPT `system_models` records for LLM and embedding.
- One OpenAPI key for Kuzhambu publication and QA integration.
- One dataset for Kuzhambu publication fragments.
- One app entry for Kuzhambu QA routing.

`FASTGPT_KUZHAMBU_APP_ID` and `FASTGPT_KUZHAMBU_DATASET_ID` are optional. If omitted,
bootstrap creates or reuses records by `FASTGPT_KUZHAMBU_APP_NAME` and
`FASTGPT_KUZHAMBU_DATASET_NAME`, then emits the actual dynamic IDs to
`deploy/fastgpt/generated/kuzhambu-fastgpt.env`. Include that generated env fragment when
starting Kuzhambu compose so `KUZHAMBU_KNOWLEDGE_ENABLED=true`,
`KUZHAMBU_KNOWLEDGE_FASTGPT_APPID`, and
`KUZHAMBU_KNOWLEDGE_FASTGPT_KNOWLEDGE_BASE_ID` match the running FastGPT cluster.

`scripts/smoke/fastgpt-smoke.sh` remains the standalone production verification for the bootstrap
result. It checks active LLM and embedding records, OpenAPI key health, dataset visibility, and the
publication-critical collection operations. It is deliberately not invoked by the Kuzhambu full smoke.

The recommended Docker startup order for an isolated full-stack smoke is to run the repository-level
orchestrator from the repository root:

```sh
scripts/smoke/full-smoke.sh deploy/.env deploy/fastgpt/.env
```

The script creates a shared smoke network, removes `container_name` from the FastGPT compose override,
bootstraps FastGPT, loads Kuzhambu image files, initializes the database and executes the Kuzhambu
full-smoke flow. FastGPT is a publication runtime dependency, not an independently tested smoke target.

If you run the two compose projects manually, use this order:

1. Set `FASTGPT_KUZHAMBU_BASE_URL` to a FastGPT URL reachable from Kuzhambu containers, such as a
   public reverse-proxy URL or the deploy host LAN URL with `FASTGPT_HTTP_PORT`.
2. Start the blank FastGPT compose cluster.
3. Run `deploy/fastgpt/bootstrap-fastgpt.sh` to configure LLM, embedding, OpenAPI key,
   dataset and app.
4. Optionally run `scripts/smoke/fastgpt-smoke.sh` against the generated env as standalone FastGPT
   production verification.
5. Use `deploy/fastgpt/generated/kuzhambu-fastgpt.env` as an additional Kuzhambu compose
   env source.
6. Start the Kuzhambu compose cluster.

Example Kuzhambu startup after bootstrap:

```sh
docker compose \
  --env-file deploy/.env \
  --env-file deploy/fastgpt/generated/kuzhambu-fastgpt.env \
  -f deploy/docker-compose.yml up -d
```

When `FASTGPT_KUZHAMBU_BASE_URL=http://fastgpt-app:3000`, both compose projects must join the same
explicit Docker network so Kuzhambu containers can resolve `fastgpt-app`. If the projects do not share
a Docker network, set `FASTGPT_KUZHAMBU_BASE_URL` to a public or host-reachable FastGPT URL before
running bootstrap.

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
- Keep FastGPT Mongo, Redis, Pgvector, MinIO, backup, restore, image upgrade, and data volume operations separate from the main Kuzhambu compose stack. FastGPT and Kuzhambu may share a Docker network for HTTP calls, but must not share base service containers or volumes.
- If FastGPT and Kuzhambu run as separate compose projects, connect them through an explicit shared Docker network or public URL. For Docker-only smoke, use `FASTGPT_KUZHAMBU_BASE_URL=http://fastgpt-app:3000` from the shared network and keep the generated `KUZHAMBU_KNOWLEDGE_*` env fragment outside Git.

## Stop

```sh
cd deploy/fastgpt
docker compose --env-file .env down
```
