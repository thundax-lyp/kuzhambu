# Repository Guidelines

## Read Order

- Read `docs/AGENTS.md` first for document routing.
- For implementation work, read `docs/00-governance/ARCHITECTURE.md`; for Java servers work also read `docs/00-governance/SERVERS-ARCHITECTURE.md`.
- Do not treat root `README.md` as implementation authority.

## Project Structure & Module Organization

This repository is currently minimal: `README.md`, `LICENSE`, `.gitignore`, and governance docs under `docs/`. Keep contributor-facing docs at the root or under `docs/`.

When source code is added, prefer `src/main/java/` for application code, `src/test/java/` for tests, and `src/main/resources/` for packaged assets or configuration. Use `docs/10-requirements/` for requirements, `docs/20-interfaces/` for contracts, `docs/30-designs/` for designs and temporary runbooks, and `docs/40-readiness/` for release checks.

## Build, Test, and Development Commands

No build tool is configured yet. Add one before introducing executable code. Recommended Java examples:

```sh
./gradlew build      # compile, test, and package
./gradlew test       # run tests
./gradlew run        # start locally, if supported
```

If Maven is chosen, use `./mvnw test` and `./mvnw package`. Prefer checked-in wrappers (`gradlew` or `mvnw`).

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

## Coding Style & Naming Conventions

Follow idiomatic Java conventions unless another language is introduced: 4-space indentation, `PascalCase` classes, `camelCase` methods and variables, and `UPPER_SNAKE_CASE` constants. Keep package names lowercase, for example `com.thundax.kuzhambu`.

Follow module-local patterns once a module establishes its own build system, style, or test layout. Avoid unrelated refactors.

## Documentation Governance

Stable rules belong in `docs/00-governance/`. Temporary execution plans belong in `docs/30-designs/RUNBOOK-*.md` and should be removed after the task closes. `docs/50-prompts/` stores manually triggered prompt templates only; `docs/60-human/` stores human-facing narrative only. Neither is default AI context.

## Testing Guidelines

Place tests under `src/test/` mirroring source structure. Name unit tests with a `Test` suffix, such as `ParserTest`, and integration tests with `IT` or `IntegrationTest`.

Run the narrowest relevant validation available. If no validation exists, document manual checks in the PR.

## Commit & Pull Request Guidelines

Current history only contains `Initial commit`; use the project convention `Type(scope): 中文说明`, for example `Docs(governance): 初始化文档治理入口`. Keep each commit focused on one concrete engineering judgment.

Pull requests are stage delivery boundaries. Use `.github/pull_request_template.md`, run `scripts/verify-all.sh`, and complete documentation, TODO, and RUNBOOK cleanup before merge. Detailed rules live in `docs/00-governance/TODO-RULES.md` and `docs/40-readiness/PR-WORKFLOW.md`.

## Agent-Specific Instructions

Load the minimum docs needed for the task. Keep edits scoped, preserve user changes, and update documentation when behavior, setup, or developer workflow changes.
