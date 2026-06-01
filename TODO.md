# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。
- Workers 执行顺序遵循 `docs/30-designs/RUNBOOK-WORKERS-MODULE.md`，每项范围对象保持 2-5 个关联文件。
- 每完成一个 TODO，先运行该项最小验证；涉及 workers 代码时至少运行 `cd kuzhambu-workers && ruff format --check . && ruff check . && pytest`。
- 每个完成项必须小步提交，并在同一提交中删除或收窄对应 TODO。
- PR 收口前必须执行最终清理：删除或收窄 `TODO.md`，按规则删除 `docs/30-designs/RUNBOOK-WORKERS-MODULE.md`，并确认 `.github/workflows/pr-verify.yml` 显式验证 workers。

## 当前任务项

- [ ] `workers e2e tests`：补充 workers 协议级端到端测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W19-End-To-End-Worker-Verification`
    - 范围对象：`kuzhambu-workers/tests/test_worker_e2e_ai.py`、`kuzhambu-workers/tests/test_worker_e2e_render.py`、`kuzhambu-workers/tests/test_worker_e2e_security.py`
    - 处理动作：补充 AI、render 和 security 协议级端到端测试
    - 验收点：AI happy path、render artifact chunk、未签名、错误签名、越权和 stream 中断路径被覆盖
    - 重要度：10/10

- [ ] `workers cleanup`：完成 PR 前现场清理
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W20-Cleanup-And-PR-Readiness`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-WORKERS-MODULE.md`、`kuzhambu-workers/README.md`、`.github/workflows/pr-verify.yml`
    - 处理动作：清理已完成 TODO、删除或收窄 RUNBOOK、确认 PR 验证说明
    - 验收点：TODO 只保留真实剩余任务，RUNBOOK 按收口规则清理，工作区无无关修改
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
