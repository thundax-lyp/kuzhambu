# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Java 三才与 AI 接口测试文件`：补齐字段写回、版本追加、批量取消与接口契约测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/test/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/controller/AiInvocationControllerTest.java`
    - 处理动作：补齐 Java 侧正式字段写回、候选应用、版本追加、批量取消和接口契约测试
    - 验收点：后端测试锁定四类 capability 的正式写回规则、版本行为和批量任务契约
    - 重要度：9/10

- [ ] `workers 三才 AI 测试文件`：补齐 capability、final-state 与 artifact 元信息测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`、`kuzhambu-workers/tests/test_ai_usecase_registry.py`、`kuzhambu-workers/tests/test_artifact_store.py`
    - 处理动作：补齐 workers 侧四类 capability、stream final-state 和 artifact 元信息测试
    - 验收点：workers 测试覆盖 `image_analysis`、`fusion`、`visual`、`image_gen` 的最终态、失败路径和产物元信息
    - 重要度：8/10

- [ ] `Implementation Coverage 文档`：同步三才 AI + Workers + Classics 闭环完成口径
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：按本轮实际交付结果更新 Classics、AI、Workers 三份 Implementation Coverage 的完成口径
    - 验收点：三份覆盖文档准确反映四类 capability、候选治理、批量处理、正式写回与测试收口状态
    - 重要度：7/10

- [ ] `kuzhambu-servers/`：按 format -> checkstyle -> compile -> test 顺序完成 Java 收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/`
    - 处理动作：按 Java formatter、静态检查、编译、测试顺序执行 servers 全域验证
    - 验收点：`kuzhambu-servers/` 按 `format -> checkstyle -> compile -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `kuzhambu-apps/`：按 format -> lint -> build -> test 顺序完成前端收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/`
    - 处理动作：按前端 formatter、lint、build、test 顺序执行 apps 全域验证
    - 验收点：`kuzhambu-apps/` 按 `format -> lint -> build -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `kuzhambu-workers/`：按 format -> lint -> test 顺序完成 workers 收口验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/40-readiness/PR-WORKFLOW.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`kuzhambu-workers/`
    - 处理动作：按 Ruff format、Ruff check、pytest 顺序执行 workers 全域验证
    - 验收点：`kuzhambu-workers/` 按 `format -> lint -> test` 顺序通过，且结果可写入 PR 验证记录
    - 重要度：10/10

- [ ] `RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`：在本轮需求关闭后清理执行手册
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-SANCAI-AI-WORKERS-FULL-CLOSURE.md`
    - 处理动作：在功能、测试、文档和验证全部完成后删除本轮 RUNBOOK
    - 验收点：PR 收口前该 RUNBOOK 已清理，且 TODO 仅保留真实剩余未完成任务
    - 重要度：8/10

## 待讨论项
