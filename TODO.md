# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
