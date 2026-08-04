# Scripts

This directory stores repository-level automation entry points. Keep scripts here only when they are repeatable, documented and useful across modules or deployment steps.

## Verification

- `verify-all.sh`: local aggregate verification entry. It checks governance files, then runs backend, frontend manifest and worker manifest checks.

## Data Generation

- `import-seed-data.sh`: stable external seed import entry. It defaults to repo-root `dev.env`, regenerates temporary seed SQL products under `build/seed-sql/` from JSON sources, imports schema and seed data into MySQL through `scripts/seed/import-to-database.mjs`, and supports `--rebuild`, table-batch transactions, `--jobs 4`, and `--include-test`.
- `package.json`: independent Node package for repository scripts. It is not part of `kuzhambu-apps` pnpm workspace; install script dependencies with `pnpm --dir scripts install`.
- `seed/import-to-database.mjs`: internal DB importer used by `import-seed-data.sh`.
- `seed/generate-system-sql.mjs`: regenerates `build/seed-sql/system.sql` from `db/data-source/system.json`.
- `seed/generate-ai-sql.mjs`: regenerates `build/seed-sql/ai.sql` from `db/data-source/ai-prompts/`.
- `seed/generate-classics-sql.mjs`: regenerates `build/seed-sql/classics.sql` from Sancai, Wangqi and Ming JSON sources.
- `seed/generate-sancai-knowledge-sql.mjs`: regenerates `build/seed-sql/knowledge.sql` from Sancai tag and manuscript sources.
- `seed/generate-test-sql.mjs`: regenerates `build/seed-sql/test.sql` from `db/data-source/test/` when `--include-test` is used.
- `seed/collect-ming-customs-source.mjs`: collects and normalizes Ming customs source data into `db/data-source/ming-customs.json`.

## Docker Smoke

Docker smoke scripts live under `scripts/smoke/`.

- `smoke/load-image-files.sh`: loads `deploy/image-files/*.tar` on a deploy host.
- `smoke/fastgpt-smoke.sh`: verifies FastGPT bootstrap records and publication-critical dataset operations.
- `smoke/full-smoke.sh`: orchestrates image loading, shared smoke network setup, FastGPT bootstrap/smoke, Kuzhambu compose startup, seed import through `scripts/import-seed-data.sh`, and HTTP health checks. It passes the FastGPT-generated Kuzhambu env fragment into compose automatically. Set `KUZHAMBU_SMOKE_LOAD_IMAGES=false` only when the smoke host already has every required image loaded.

## Rules

- Prefer adding scripts under a purpose-specific subdirectory when a category grows, for example `scripts/smoke/`.
- Root-level scripts are stable external entry points and should normally be `.sh` wrappers. Implementation `.mjs` files should live in a purpose-specific subdirectory such as `scripts/seed/`.
- Script entry files under `scripts/` must use `.mjs` or `.sh` only. `README.md`, `package.json`, and `pnpm-lock.yaml` are the only repository metadata exceptions.
- Add or update this README whenever adding, renaming or deleting a script.
- Do not keep one-off local repair commands here. Put temporary execution steps in a RUNBOOK and remove them when the task closes.
- Scripts must not print secrets or require committed environment files with real credentials.
