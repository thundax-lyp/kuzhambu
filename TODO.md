# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Workers Discovery Contract`：收口 Workers Discovery usecase 契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-QA-CLOSEOUT.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`、`docs/20-interfaces/WORKERS-AI-INTERFACE.md`
    - 处理动作：锁定 Discovery answer-generation 单文档上下文契约并禁止新增正式 QA 会话路径。
    - 验收点：Workers 支持 `contextMode/contextContentType/contextContentId`，且不暴露 `/internal/ai/discovery/qa/session/*`。
    - 重要度：8/10

- [ ] `Discovery Coverage Update`：更新 Discovery 覆盖矩阵
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-QA-CLOSEOUT.md`
    - 范围对象：`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：按代码事实更新 Discovery Implementation Coverage。
    - 验收点：本轮搜索和问答需求项状态更新为 `已完成`。
    - 重要度：10/10

- [ ] `Discovery Runbook Cleanup`：清理 Discovery 闭环 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-QA-CLOSEOUT.md`、`TODO.md`
    - 处理动作：任务关闭时删除 RUNBOOK 并清空已完成 TODO。
    - 验收点：最终收口提交不保留 RUNBOOK，`TODO.md` 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
