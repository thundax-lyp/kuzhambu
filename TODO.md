# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `workers ai usecase contract`：同步 AI usecase 接口口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U1-Usecase-Contract-Sync`
    - 范围对象：`docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md`、`docs/20-interfaces/WORKERS-AI-INTERFACE.md`、`docs/30-designs/WORKERS-DESIGN.md`、`kuzhambu-workers/README.md`
    - 处理动作：同步 usecase path、通用调试接口和真实业务接入口径
    - 验收点：文档一致说明真实业务必须使用 usecase path
    - 重要度：10/10

- [ ] `workers ai usecase registry`：实现 AI usecase 注册表
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U2-Usecase-Registry`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`、`kuzhambu-workers/tests/test_ai_usecase_registry.py`
    - 处理动作：实现 path 到 capability、stream 和 OpenAPI 描述的注册表
    - 验收点：注册表覆盖接口文档中的全部 usecase path
    - 重要度：10/10

- [ ] `workers ai shared orchestration`：抽取 AI 路由共享编排
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U3-AI-Route-Shared-Orchestration`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`、`kuzhambu-workers/tests/test_ai_routes.py`、`kuzhambu-workers/tests/test_worker_e2e_ai.py`
    - 处理动作：抽取通用 invoke/stream 与 usecase route 可复用的执行编排
    - 验收点：通用调试接口行为不变且既有 AI 路由测试通过
    - 重要度：9/10

- [ ] `workers ai classics routes`：实现 Classics AI usecase 路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U4-Classics-AI-Usecase-Routes`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`、`kuzhambu-workers/src/kuzhambu_workers/main.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
    - 处理动作：实现 Classics sancai、wangqi、ming-customs AI usecase routes
    - 验收点：Classics usecase path 可用且 path/capability/stream 不匹配会失败
    - 重要度：10/10

- [ ] `workers ai discovery routes`：实现 Discovery AI usecase 路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U5-Discovery-AI-Usecase-Routes`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_discovery.py`
    - 处理动作：实现 Discovery 查询理解、查询改写、回答生成和流式回答 usecase routes
    - 验收点：Discovery 同步和 stream usecase path 均可用
    - 重要度：9/10

- [ ] `workers ai knowledge platform routes`：实现 Knowledge 和 Platform AI usecase 路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U6-Knowledge-And-Platform-AI-Usecase-Routes`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_knowledge.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_platform.py`
    - 处理动作：实现 Knowledge 抽取和 Platform AI usecase routes
    - 验收点：Knowledge 和 Platform path/capability 校验覆盖成功和失败路径
    - 重要度：9/10

- [ ] `workers ai usecase security`：接入 AI usecase HMAC 路径授权
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U7-Security-Allowlist`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/core/security.py`、`kuzhambu-workers/tests/test_security.py`、`kuzhambu-workers/tests/test_worker_e2e_security.py`
    - 处理动作：把 AI usecase path 纳入内部服务路径授权
    - 验收点：仅 `kuzhambu-ai` 可访问 AI usecase path，业务域服务身份被拒绝
    - 重要度：10/10

- [ ] `workers ai usecase openapi`：补齐 AI usecase OpenAPI 展示
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U8-OpenAPI-Coverage`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/ai_usecase_routes.py`、`kuzhambu-workers/tests/test_openapi.py`
    - 处理动作：让 Swagger/OpenAPI 展示 AI usecase path 和边界说明
    - 验收点：OpenAPI 包含 usecase path 且通用接口仍标注调试用途
    - 重要度：8/10

- [ ] `workers ai usecase e2e`：补充 AI usecase 协议级端到端测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U9-End-To-End-Verification`
    - 范围对象：`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_classics.py`、`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_discovery.py`、`kuzhambu-workers/tests/test_worker_e2e_ai_usecase_security.py`
    - 处理动作：补充 Classics、Discovery 和 security usecase e2e 测试
    - 验收点：happy path、业务服务越权和 path/capability mismatch 被覆盖
    - 重要度：10/10

- [ ] `workers ai usecase cleanup`：完成 AI usecase 接口收口清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md#U10-Cleanup-And-PR-Readiness`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-WORKERS-AI-USECASE.md`、`kuzhambu-workers/README.md`、`.github/pull_request_template.md`
    - 处理动作：清理已完成 TODO、按规则删除或收窄 RUNBOOK 并确认 PR 准备状态
    - 验收点：TODO 只保留真实剩余任务，RUNBOOK 按收口规则处理，工作区无无关修改
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
