# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Discovery QA Session Context`：收口后端会话上下文校验
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-QA-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/QaApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-interface/src/test/java/com/thundax/kuzhambu/discovery/interfaces/portal/qa/controller/DiscoveryQaPortalControllerTest.java`
    - 处理动作：校验 `SINGLE_DOCUMENT` 会话上下文并锁定 Portal 请求字段映射。
    - 验收点：单文档模式仅支持 `WANGQI_DOCUMENT`，缺少内容类型或内容 ID 时抛业务异常。
    - 重要度：10/10

- [ ] `Discovery QA Provider Context`：收口后端问答 provider 上下文
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-SEARCH-QA-CLOSEOUT.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImpl.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/qa/support/QaTraceAssembler.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/qa/service/impl/KnowledgeQaApplicationServiceImplTest.java`
    - 处理动作：校验 chat metadata 与 session 上下文一致，并把上下文写入 provider options 和 trace。
    - 验收点：上下文不一致抛业务异常，trace 保留 `contextMode/contextContentType/contextContentId`。
    - 重要度：10/10

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
