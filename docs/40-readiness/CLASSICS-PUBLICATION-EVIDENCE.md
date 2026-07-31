# Classics Publication Readiness Evidence

## Stage 1: Foundation

Status: READY_FOR_PR

### Scope

- Baseline commit: `80f1927d9`
- Functional commit range (inclusive): `287f4ea06^..899c78b74`
- Changed files: 138
- Diff: 2,099 insertions, 5,833 deletions

Stage 1 固定发布契约，拆除 server sharing 能力，校准正式版本 snapshot，并建立稿件与
publication job 的状态、租约、条件更新和清理持久化协议。

### Verification

| Check | Result |
| --- | --- |
| `mvn spotless:check checkstyle:check test` | PASS；58/58 reactor modules；2026-07-31；1 min 10 s |
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

- RUNBOOK Stage 1 保持 `ACTIVE`，直到 delivery PR 创建、检查通过并按协议收口。
- 当前证据不包含数据库运行时 smoke；RUNBOOK 明确由最终 Stage 6 统一重建数据库并冒烟。
- Deferred implementation: frontend sharing removal (Stage 4), legacy MQ and remaining visibility removal (Stage 5).
