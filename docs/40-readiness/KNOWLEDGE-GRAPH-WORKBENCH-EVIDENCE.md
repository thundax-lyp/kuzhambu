# Knowledge Graph Workbench Evidence

## Scope

- Date: 2026-08-19
- Area: knowledge graph workbench, Admin Web, and the Node 26 frontend test baseline.
- Route: `/knowledge/graph-workbench`
- Coverage: published graph overview, progressive read-only canvas, activity timeline, and browser network boundary.

## Validated Contract

| Flow | Evidence | Status |
| --- | --- | --- |
| Overview metrics | Browser test verifies `正式节点`、`关系`、`覆盖素材`、`孤立节点` and their mock response values. | Covered |
| Read-only canvas | Browser test verifies the formal graph canvas is rendered and the page has no button or textbox controls. | Covered |
| Activity timeline | Browser test verifies the published-relation activity is rendered. | Covered |
| Network boundary | Browser fixture mocks only `/knowledge/graph/**` requests and fails on unexpected backend or console errors. | Covered |
| Frontend runtime | Admin Web Vitest uses Node 26 localStorage isolation; CI uses Node 26 for frontend and database seed verification. | Covered |

## Executed Validation

| Command | Result |
| --- | --- |
| `cd kuzhambu-servers && mvn -pl starter/kuzhambu-admin-starter -am test` | Passed; includes the affected graph, AI, operations, and starter dependency closure. |
| `cd kuzhambu-apps && pnpm --filter ./admin-web run format:check` | Passed. |
| `cd kuzhambu-apps && pnpm --filter ./admin-web run lint` | Passed. |
| `cd kuzhambu-apps && pnpm --filter ./admin-web run test` | Passed; 125 files / 479 tests under Node 26. |
| `cd kuzhambu-apps && pnpm --filter ./portal-web run format:check && pnpm --filter ./portal-web run lint && pnpm --filter ./portal-web run test` | Passed; 21 files / 57 tests under Node 26. |
| `cd kuzhambu-apps/admin-web && pnpm exec playwright test e2e/knowledge/graph/graph.spec.ts` | Passed; 1 browser test. |
| `git diff --check` | Passed. |

## Not Covered

- Browser verification uses the graph fixture and does not prove a live Java server, MySQL, and Redis refresh cycle.
- The workbench intentionally excludes node and edge editing, merging, splitting, undo, and governance action controls.

## Closure

- The graph workbench delivery-verification TODO is complete and removed.
- No temporary graph workbench RUNBOOK remains in scope; the durable contract is documented in `docs/20-interfaces/KNOWLEDGE-GRAPH-INTERFACE.md` and this readiness evidence.
