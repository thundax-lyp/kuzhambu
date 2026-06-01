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

- [ ] `workers classics export`：实现 Classics 导出渲染
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W13-Classics-Export-Renderer`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/classics_export.py`、`kuzhambu-workers/src/kuzhambu_workers/render/templates/classics_export.html`、`kuzhambu-workers/tests/test_classics_export.py`
    - 处理动作：实现 Classics CSV、JSON、HTML、ZIP 产物生成
    - 验收点：产物包含 filename、contentType、sizeBytes、sha256，HTML/ZIP 可通过 artifact chunk 输出
    - 重要度：8/10

- [ ] `workers sancai showcase`：实现三才图会静态展示渲染
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W14-Sancai-Showcase-Renderer`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/sancai_showcase.py`、`kuzhambu-workers/src/kuzhambu_workers/render/templates/sancai_showcase.html`、`kuzhambu-workers/tests/test_sancai_showcase.py`
    - 处理动作：实现三才图会静态展示页面渲染
    - 验收点：支持数据集元信息、目录、正文和图片引用快照，不回查 Storage 或数据库
    - 重要度：8/10

- [ ] `workers browser pool`：实现 Playwright Browser Pool
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W15-Browser-Pool`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/browser_pool.py`、`kuzhambu-workers/tests/test_browser_pool.py`、`kuzhambu-workers/README.md`
    - 处理动作：实现 Playwright/Chromium Browser Pool 和运行说明
    - 验收点：支持 pool size、max pages、page timeout，超时和异常路径释放 page/context
    - 重要度：9/10

- [ ] `workers operations report`：实现 Operations 报表渲染
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W16-Operations-Report-Renderer`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/render/operations_report.py`、`kuzhambu-workers/src/kuzhambu_workers/render/templates/operations_report.html`、`kuzhambu-workers/src/kuzhambu_workers/render/browser_pool.py`、`kuzhambu-workers/tests/test_operations_report.py`
    - 处理动作：实现 Operations HTML/PDF 报表渲染
    - 验收点：HTML 可生成，PDF 使用 Browser Pool 和 Chromium print，PDF 通过 SSE artifact chunk 输出
    - 重要度：9/10

- [ ] `workers render routes`：实现 render 同步和 stream 路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W17-Render-Routes`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/api/render_routes.py`、`kuzhambu-workers/src/kuzhambu_workers/main.py`、`kuzhambu-workers/tests/test_render_routes.py`
    - 处理动作：实现 render 同步和 stream 路由
    - 验收点：同步支持小产物，stream 支持 progress、artifact chunk 和 completed，HMAC 授权生效
    - 重要度：10/10

- [ ] `workers packaging`：补齐开发命令和 PR 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WORKERS-MODULE.md#W18-Packaging-And-Dev-Commands`
    - 范围对象：`kuzhambu-workers/pyproject.toml`、`kuzhambu-workers/README.md`、`docs/40-readiness/PR-WORKFLOW.md`、`.github/workflows/pr-verify.yml`
    - 处理动作：补齐 Python package、ruff、pytest、运行命令和 PR workflow
    - 验收点：PR workflow 显式包含 workers 的 ruff format check、ruff check 和 pytest
    - 重要度：9/10

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
