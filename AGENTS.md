# Repository Guidelines

## Read Order

- Read `docs/AGENTS.md` first for document routing.
- For implementation work, read `docs/00-governance/ARCHITECTURE.md`; for Java servers work also read `docs/00-governance/SERVERS-ARCHITECTURE.md`.
- Do not treat root `README.md` as implementation authority.

## Project Structure & Module Organization

The repository is organized into governance docs, Java servers, frontend apps, Python workers, and deployment support. Keep contributor-facing docs at the root or under `docs/`.

Use `kuzhambu-servers/` for Java backend modules, `kuzhambu-apps/` for frontend apps, `kuzhambu-workers/` for Python worker capabilities, and `deploy/` for deployment support. Use `docs/10-requirements/` for requirements, `docs/20-interfaces/` for contracts, `docs/30-designs/` for designs and temporary runbooks, and `docs/40-readiness/` for release checks.

## Build, Test, and Development Commands

Java servers use Maven under `kuzhambu-servers/`. Use Java 17 for compile, test, and local runs:

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn test
```

Java servers currently use Maven under `kuzhambu-servers/`. Local starter runs default to repo-root `dev.env`; load it before running Maven. Install reactor dependencies first when needed, then run from the starter module so Maven does not execute `spring-boot:run` on the root aggregator:

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

Before any Maven compile or package step, run formatting and static checks first:

```sh
mvn spotless:check
mvn checkstyle:check
```

Frontend apps use npm workspaces under `kuzhambu-apps/`:

```sh
cd kuzhambu-apps
npm run lint
npm run test
npm run build
```

Python workers use Python 3.10 and a repo-local virtual environment:

```sh
cd kuzhambu-workers
python3.10 -m venv .venv
.venv/bin/python -m pip install -e '.[dev]'
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

## Coding Style & Naming Conventions

Follow idiomatic Java conventions unless another language is introduced: 4-space indentation, `PascalCase` classes, `camelCase` methods and variables, and `UPPER_SNAKE_CASE` constants. Keep package names lowercase, for example `com.thundax.kuzhambu`.

Follow module-local patterns once a module establishes its own build system, style, or test layout. Avoid unrelated refactors.

## Documentation Governance

Stable rules belong in `docs/00-governance/`. Temporary execution plans belong in `docs/30-designs/RUNBOOK-*.md` and should be removed after the task closes. `docs/50-prompts/` stores manually triggered prompt templates only; `docs/60-human/` stores human-facing narrative only. Neither is default AI context.

## Testing Guidelines

Place tests under `src/test/` mirroring source structure. Name unit tests with a `Test` suffix, such as `ParserTest`, and integration tests with `IT` or `IntegrationTest`.

Run the narrowest relevant validation available. If no validation exists, document manual checks in the PR.

## Commit & Pull Request Guidelines

Use the project convention `Type(scope): 中文说明`, for example `Docs(governance): 初始化文档治理入口`. Keep each commit focused on one concrete engineering judgment.

Pull requests are stage delivery boundaries. Use `.github/pull_request_template.md`, rely on the explicit `.github/workflows/pr-verify.yml` checks, and complete documentation, TODO, and RUNBOOK cleanup before merge. Changes must go through `branch -> PR -> review -> merge`; do not push or merge work directly to `main`. Merge PRs with normal merge commits by default to preserve the small-step commit history; do not squash unless explicitly requested. Detailed rules live in `docs/00-governance/TODO-RULES.md` and `docs/40-readiness/PR-WORKFLOW.md`.

## Agent-Specific Instructions

Load the minimum docs needed for the task. Keep edits scoped, preserve user changes, and update documentation when behavior, setup, or developer workflow changes.
