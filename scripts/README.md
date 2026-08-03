# Scripts

This directory stores repository-level automation entry points. Keep scripts here only when they are repeatable, documented and useful across modules or deployment steps.

## Verification

- `verify-all.sh`: local aggregate verification entry. It checks governance files, delegates Classics verification, then runs backend, frontend manifest and worker manifest checks.
- `verify-classics.sh`: Classics schema, generated SQL and JSON source consistency checks.

## Data Generation

- `generate-system-data-sql.ts`: regenerates `db/data/system.sql` from `db/data-source/system.json`.
- `generate-ai-data-sql.ts`: regenerates `db/data/ai.sql` from `db/data-source/ai-prompts/`.
- `generate-sancai-knowledge-data-sql.mjs`: regenerates `db/data/knowledge.sql` from Sancai tag and manuscript sources.
- `classics-json-to-sql.sh`: regenerates `db/data/classics.sql` from Classics JSON sources.
- `collect-ming-customs.mjs`: collects and normalizes Ming customs source data into `db/data-source/ming-customs.json`.

## Docker Smoke

Docker smoke scripts live under `scripts/smoke/`.

- `smoke/docker-load-image-files.sh`: loads `deploy/image-files/*.tar` on a deploy host.
- `smoke/docker-fastgpt-smoke.sh`: verifies FastGPT bootstrap records and publication-critical dataset operations.
- `smoke/docker-full-smoke.sh`: orchestrates image loading, shared smoke network setup, FastGPT bootstrap/smoke, Kuzhambu compose startup, database initialization and HTTP health checks. It passes the FastGPT-generated Kuzhambu env fragment into compose automatically. Set `KUZHAMBU_SMOKE_LOAD_IMAGES=false` only when the smoke host already has every required image loaded.

## Rules

- Prefer adding scripts under a purpose-specific subdirectory when a category grows, for example `scripts/smoke/`.
- Add or update this README whenever adding, renaming or deleting a script.
- Do not keep one-off local repair commands here. Put temporary execution steps in a RUNBOOK and remove them when the task closes.
- Scripts must not print secrets or require committed environment files with real credentials.
