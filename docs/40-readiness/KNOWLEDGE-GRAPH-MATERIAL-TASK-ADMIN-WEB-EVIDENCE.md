# Knowledge Graph Material Task Admin Web Evidence

## Scope

- Date: 2026-08-17
- Branch: `docs/admin-web-dag-worktree-plan`
- Area: `kuzhambu-apps/admin-web`
- Routes: `/knowledge/graph-material`, `/knowledge/graph-extraction`
- Source runbook: migrated into this readiness evidence and removed from `docs/30-designs/`

## Current Status

- Knowledge HTTP service is complete enough for Admin Web end-to-end integration and co-debugging.
- Knowledge HTTP service implementation is still under active development.
- Current Admin Web evidence covers the frontend service switch, mocked browser E2E, lint, build, and Vitest validation. It does not claim a completed real backend browser smoke.

## Validated Frontend Coverage

| Flow | Evidence | Status |
| --- | --- | --- |
| Material page | Playwright opens `/knowledge/graph-material`, renders the composite material table, and verifies business network paths | Covered by E2E |
| Material drawer | Playwright opens a material detail drawer and switches task/publication sections | Covered by E2E |
| Single material extraction | Playwright selects one material row and verifies `/knowledge/graph/task/batch/create` receives one `contentRef` | Covered by E2E |
| Task drawer | Playwright opens task list and task segmented detail drawer from `/knowledge/graph-extraction` | Covered by E2E |
| Failed retry | Playwright opens failed task disposition and verifies `/knowledge/graph/task/retry` with expected task state and lock version | Covered by E2E |
| Candidate adoption | Playwright opens candidate disposition and verifies `/knowledge/graph/task/candidate/apply` with `MERGE`, expected state, and lock version | Covered by E2E |
| Batch partial failure | Playwright verifies partial batch create result shows preserved success and failure rows | Covered by E2E |
| Network boundary | Playwright asserts business requests are only `/knowledge/graph/**`; shell `/sys/**` and `/auth/**` requests are excluded | Covered by E2E |

## Commands

Executed from `kuzhambu-apps/` unless noted.

| Command | Result |
| --- | --- |
| `pnpm --filter ./admin-web exec vitest run src/pages/knowledge/graph-material/graph-material-service-contract.test.ts src/pages/knowledge/graph-material/graph-material-page.test.tsx src/pages/knowledge/graph-material/material-detail-drawer/material-detail-drawer.test.tsx src/pages/knowledge/graph-extraction/graph-extraction-service-contract.test.ts src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx src/pages/knowledge/graph-extraction/graph-extraction-task-table/graph-extraction-task-table.test.tsx` | Passed, 6 files / 39 tests |
| `pnpm --filter ./admin-web run format:check` | Passed |
| `pnpm --filter ./admin-web run lint` | Passed |
| `pnpm --filter ./admin-web run build` | Passed |
| `pnpm --filter ./admin-web run test` | Passed, 130 files / 493 tests |
| `git diff --check main...HEAD` from repo root | Passed |

## Runtime Notes

- A stale Vite dev server from `/Volumes/storage/workspace/kuzhambu-task-one` was listening on port `5173`; it was stopped before running the final Playwright verification so the E2E used this branch's current Admin Web source.
- Playwright emitted Ant Design deprecation warnings for `Spin.tip` and `Timeline.items.children`. They do not fail the readiness evidence but remain cleanup candidates outside this task.
- The task E2E deliberately grants task `edit/apply` permissions without `view` permission when opening `/knowledge/graph-extraction`, so the legacy workbench tree query is not triggered before the legacy cleanup task.

## Remaining Admin Web Work

- Run the same flows against the real Knowledge HTTP service runtime and attach runtime Network evidence.
- Run the same flows against the real Knowledge HTTP service runtime and attach runtime Network evidence after the backend implementation is ready for live smoke.
