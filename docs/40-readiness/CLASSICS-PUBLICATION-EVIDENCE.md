# Classics Publication Readiness Evidence

## Stage 1: Foundation

Status: PR_OPEN

### Scope

- Baseline commit: `80f1927d9`
- Functional commit range (inclusive): `722855678^..a885a4fbb`
- Changed files: 138
- Diff: 2,113 insertions, 5,833 deletions

Stage 1 固定发布契约，拆除 server sharing 能力，校准正式版本 snapshot，并建立稿件与
publication job 的状态、租约、条件更新和清理持久化协议。

### Verification

| Check | Result |
| --- | --- |
| `mvn spotless:check checkstyle:check test` | PASS；58/58 reactor modules；2026-07-31；1 min 10 s |
| admin-web format/lint/test | PASS；87 test files；376 tests |
| portal-web format/lint/test | PASS；25 test files；79 tests |
| workers Ruff format/lint/test | PASS；181 tests |
| `scripts/verify-classics.sh` | PASS |
| publication persistence focused tests | PASS；8 tests |
| server sharing residue scan | 0 files |
| admin/portal sharing source scan | 23 files；按计划保留到 Stage 4 |
| legacy search MQ scan | 18 files；按计划保留到 Stage 5 |
| combined publication visibility scan | 183 files；按后续 stage 的语义边界继续收口 |
| `git diff --check` | PASS |

完整门禁首次执行发现 publication content persistence test 位于错误包，违反 Mapper 只能由
`repository.impl` 或 mapper package 依赖的架构规则。测试归位并 amend 同一工程判断后，
聚焦架构测试和第二次完整门禁均通过。

### Delivery State

- Delivery PR: `#193` (`merge`)。
- RUNBOOK Stage 1 已压缩为 `COMPLETE` 摘要；Stage 2-6 保持 `PENDING`。
- 当前证据不包含数据库运行时 smoke；RUNBOOK 明确由最终 Stage 6 统一重建数据库并冒烟。
- Deferred implementation: frontend sharing removal (Stage 4), legacy MQ and remaining visibility removal (Stage 5).

## Stage 5: Visibility And Legacy MQ Removal

Status: LOCAL_COMPLETE_PENDING_PR

### Scope

- Baseline commit: `d7dea5f6a`
- Functional commit range (inclusive): `932453204^..a75f5c3d9`
- Changed files: 169
- Diff: 390 insertions, 3,189 deletions

Stage 5 删除三类 Classics 稿件 publication visibility、Discovery public search 旧
`visibilityScopes/contentStatuses` 契约，以及 Classics -> RocketMQ -> Discovery 旧搜索同步链路。
Portal/public search 继续只由 ES `publicationStatus = READY` 且 `deleted = false` 决定。

### Verification

| Check | Result |
| --- | --- |
| `cd kuzhambu-servers && mvn spotless:check checkstyle:check test` | PASS；58/58 reactor modules；2026-08-02；3 min 41 s |
| focused Stage 5 Java tests | PASS；Classics/Discovery/starter impacted tests |
| focused admin-web tests | PASS；Ming/Wangqi/Discovery/classics contract tests |
| `cd kuzhambu-apps && pnpm run format:check && pnpm run lint` | PASS |
| `cd kuzhambu-apps && pnpm run build` | PASS；admin-web and portal-web |
| `cd kuzhambu-apps && pnpm --filter ./portal-web run test` | PASS；21 files；57 tests |
| `cd kuzhambu-apps && pnpm --filter ./admin-web exec vitest run <failed files>` | PASS；5 files；52 tests |
| `cd kuzhambu-apps && pnpm run test` | FAIL；admin-web full Vitest had 5 failed files / 8 failed tests; the same failed files passed in focused rerun |
| legacy search sync residue scan | PASS；0 results for searchsync/index-sync/ClassicsSearchIndexSync/RocketMqDiscoverySearchIndexSync |
| public search old filter residue scan | PASS；0 results for `visibilityScopes` / `contentStatuses` |
| `git diff --check` | PASS |

The admin-web full Vitest failure was not reproduced by focused rerun of the exact failed files:
`src/app.test.tsx`, `src/pages/classics/wangqi/wangqi-page.test.tsx`,
`src/pages/discovery/qa-console/qa-console-page.test.tsx`,
`src/pages/knowledge/graph-extraction/graph-extraction-page.test.tsx`, and
`src/pages/knowledge/taxonomy/taxonomy-page.test.tsx`.

### Delivery State

- Delivery PR: pending.
- RUNBOOK Stage 5 remains `ACTIVE` until PR is opened and assigned a number.
- Remaining Stage 6 work: database reset, runtime smoke, final RUNBOOK deletion.
