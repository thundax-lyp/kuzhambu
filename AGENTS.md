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

## Code Review Rules

Review the complete PR diff from its merge base. Judge the final code against the delivery intent, contracts, architecture rules, and surrounding system behavior. Complete all applicable review passes before reporting findings; do not stop after the first issues.

### Review Exclusions

- Exclude every file under `scripts/smoke/**` from PR code review. These scripts are intentionally mutable delivery executors and must not produce review findings, inline comments, or follow-up review work.
- Mark changed `scripts/smoke/**` files as `not-applicable` or `excluded` in review coverage instead of reviewing their implementation.
- Validate smoke behavior only through the documented smoke execution and its generated evidence. Record an unavailable runtime as a validation gap; do not convert inspection of the excluded scripts into code-review findings.
- Modify `scripts/smoke/**` only when the user explicitly requests smoke implementation work or asks to fix a concrete smoke execution failure.

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

Repository-specific checks:

- **AI business capability values:** Backend business capability codes are Java enum names such as `CLASSICS_TAG_EXTRACT`, not worker canonical values such as `tags`. When capability values are renamed or normalized, review every persistence surface that can store them: seed/config tables, runtime history tables, candidates, invocation logs, batch jobs, frontend request constants, worker-usecase mappings, and docs.
- **AI worker/backend boundary:** Workers are capability executors and must not parse business JSON protocols. Java backend owns prompt rendering, JSON compatibility extraction, structured output validation, and business protocol parsing before results become successful candidates.
- **Queued AI task snapshots:** Async or batch AI tasks must execute against the model, prompt version, prompt messages, variables, schema, and params captured at submit time. Execution must not silently re-resolve mutable business config unless the submitted command is missing a snapshot.
- **Seed/import/deploy compatibility:** `db/data-source/**` is the seed source of truth. Generated SQL under `build/seed-sql/` is temporary and must not be treated as a durable engineering asset. Import or deploy changes that normalize seed/config data must also account for runtime tables that are not rebuilt.
- **Admin business pages:** First classify a changed page/component as orchestration or capability. Orchestration components should mostly manage layout and high-level composition; capability components should own their own state, service calls, lifecycle, and protocol details. Do not keep child business logic in parent drawers/panels unless there is a real cross-child coordination requirement.
- **Admin tables:** Table action columns must use the project action structure rather than ad-hoc render output. A table must keep at least one flexible data column without explicit width; checkbox/options columns may specify width.
- **Frontend reusable components:** Extract common business components only when there are multiple repeated usages under the same business rule. Avoid fragmenting page-local UI into generic components just because two places look similar.

## Commit & Pull Request Guidelines

Use the project convention `Type(scope): 中文说明`, for example `Docs(governance): 初始化文档治理入口`.

- **Boundary:** one commit records one engineering decision that reviewers can understand and verify independently. Include the code, tests, contracts, and mechanical cleanup required to complete that decision.
- **Size heuristic:** 2-8 files is the expected review size, not a limit.
- **When larger:** recheck that every file is required by the same decision and that the commit remains independently verifiable. Keep the commit intact when splitting would create incomplete states or separate mechanical parts from the behavior they support.
- **Split when:** changes express independent decisions, can be verified separately, or include unrelated cleanup.

Pull requests are stage delivery boundaries. Use `.github/pull_request_template.md`, rely on the explicit `.github/workflows/pr-verify.yml` checks, and complete documentation, TODO, and RUNBOOK cleanup before merge. Changes must go through `branch -> PR -> review -> merge`; do not push or merge work directly to `main`. Merge PRs with normal merge commits by default to preserve the small-step commit history; do not squash unless explicitly requested. Detailed rules live in `docs/00-governance/TODO-RULES.md` and `docs/00-governance/PR-RULES.md`.

## Agent-Specific Instructions

Load the minimum docs needed for the task. Keep edits scoped, preserve user changes, and update documentation when behavior, setup, or developer workflow changes.
