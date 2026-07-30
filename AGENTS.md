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

`kuzhambu-servers/.mvn/maven.config` 默认启用 Maven 并行构建 `-T 4`，在 `kuzhambu-servers/` 下执行 `mvn` 会自动使用 4 线程。

```sh
cd kuzhambu-servers
mvn spotless:check
mvn checkstyle:check
mvn test
```

Java servers currently use Maven under `kuzhambu-servers/`. Local starter runs default to a repo-root local `dev.env`; create it from `.env.example` and keep it untracked, then load it before running Maven. Install reactor dependencies first when needed, then run from the starter module so Maven does not execute `spring-boot:run` on the root aggregator:

```sh
set -a
source dev.env
set +a
cd kuzhambu-servers
mvn -pl starter/kuzhambu-admin-starter -am -DskipTests install
cd starter/kuzhambu-admin-starter
mvn spring-boot:run
```

Before any Maven compile or package step, first run the narrowest relevant formatter on the files touched by the task, then run formatting and static checks:

```sh
mvn -pl ... spotless:apply
mvn spotless:check
mvn checkstyle:check
```

After applying formatting, inspect `git diff` and keep only task-related file changes. If `spotless:check` still fails, treat it as an unexpected issue and inspect the formatter scope, configuration, or affected files before continuing.

Frontend apps use pnpm workspaces under `kuzhambu-apps/`:

```sh
cd kuzhambu-apps
pnpm run format:check
pnpm run lint
pnpm run build
pnpm run test
```

Before any frontend build or package step, first run the narrowest relevant formatter on the files touched by the task, then run Prettier formatting checks and lint:

```sh
pnpm --filter ... run format
pnpm run format:check
pnpm run lint
```

After applying formatting, inspect `git diff` and keep only task-related file changes. If `format:check` still fails, treat it as an unexpected issue and inspect the formatter scope, configuration, or affected files before continuing.

Python workers use Python 3.10 and a repo-local virtual environment:

```sh
cd kuzhambu-workers
python3.10 -m venv .venv
.venv/bin/python -m pip install -e '.[dev]'
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture
```

Before any Python worker test, package, or runtime validation step, first run the narrowest relevant formatter on the files touched by the task, then run Ruff formatting and lint checks:

```sh
.venv/bin/python -m ruff format .
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
```

After applying formatting, inspect `git diff` and keep only task-related file changes. If `ruff format --check` still fails, treat it as an unexpected issue and inspect the formatter scope, configuration, or affected files before continuing.

## Coding Style & Naming Conventions

Follow idiomatic Java conventions unless another language is introduced: 4-space indentation, `PascalCase` classes, `camelCase` methods and variables, and `UPPER_SNAKE_CASE` constants. Keep package names lowercase, for example `com.thundax.kuzhambu`.

Follow module-local patterns once a module establishes its own build system, style, or test layout. Avoid unrelated refactors.

## Documentation Governance

Stable rules belong in `docs/00-governance/`. Temporary execution plans belong in `docs/30-designs/RUNBOOK-*.md` and should be removed after the task closes. `docs/50-prompts/` stores manually triggered prompt templates only; `docs/60-human/` stores human-facing narrative only. Neither is default AI context.

## Testing Guidelines

Place tests under `src/test/` mirroring source structure. Name unit tests with a `Test` suffix, such as `ParserTest`, and integration tests with `IT` or `IntegrationTest`.

Run the narrowest relevant validation available. If no validation exists, document manual checks in the PR.

## Code Review Guidelines

Review the complete PR diff from its merge base. Judge the final code against the delivery intent, contracts, architecture rules, and surrounding system behavior. Complete all applicable review passes before reporting findings; do not stop after the first issues.

Review:

- **Behavior:** user flows, background and asynchronous workflows, failure paths, and regressions.
- **Architecture:** module ownership, layer responsibilities, dependency direction, boundary bypasses, and duplicated capabilities.
- **Contracts:** application, domain, facade, HTTP, event, worker, persistence, and configuration compatibility.
- **Runtime integrity:** transactions, concurrency, idempotency, retries, ordering, state transitions, migrations, authorization, sensitive data, observability, and recovery where relevant.
- **Verification:** tests and static checks for critical behavior and failure paths. Passing checks are evidence, not proof.

Identify affected modules and contracts first, then follow real dependencies across Java servers, frontend apps, Python workers, deployment support, and external integrations. Do not expand scope without dependency evidence. Run the narrowest validation appropriate to the risk; broaden it only for shared contracts, common infrastructure, or cross-module behavior.

A finding must:

- be introduced, exposed, or materially worsened by the change;
- have a concrete trigger and observable impact;
- point to changed code and provide an actionable correction;
- be more than style preference or unsupported speculation.

Report all independent, actionable P0-P2 findings in one review, including severity, location, trigger, system impact, and correction direction:

- **P0:** severe security incident, data loss, system outage, or irreversible impact.
- **P1:** likely breakage of a critical flow, authorization boundary, key contract, or release.
- **P2:** functional, compatibility, or operational failure under concrete conditions.
- **P3:** maintainability, readability, or local design concern; omit from formal findings.

Put unverified concerns under validation gaps or residual risks, not findings. If there are no findings, state that clearly and list any material gaps or residual risks.

## Commit & Pull Request Guidelines

Use the project convention `Type(scope): 中文说明`, for example `Docs(governance): 初始化文档治理入口`. Keep each commit focused on one concrete engineering judgment. Keep each commit to 1-8 files; split larger changes into separate commits.

Pull requests are stage delivery boundaries. Use `.github/pull_request_template.md`, rely on the explicit `.github/workflows/pr-verify.yml` checks, and complete documentation, TODO, and RUNBOOK cleanup before merge. Changes must go through `branch -> PR -> review -> merge`; do not push or merge work directly to `main`. Merge PRs with normal merge commits by default to preserve the small-step commit history; do not squash unless explicitly requested. Detailed rules live in `docs/00-governance/TODO-RULES.md` and `docs/00-governance/PR-RULES.md`.

## Agent-Specific Instructions

Load the minimum docs needed for the task. Keep edits scoped, preserve user changes, and update documentation when behavior, setup, or developer workflow changes.
