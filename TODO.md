# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `kuzhambu-workers/tests`：03 锁定 Classics image-gen 成功契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GEN.md`
    - 范围对象：`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`、`kuzhambu-workers/tests/test_ai_routes.py`、`kuzhambu-workers/tests/test_graph_registry.py`
    - 处理动作：更新 `CLASSICS_SANCAI_IMAGE_GEN` 测试，从失败契约改为成功 `completed` 和 artifact 下载契约。
    - 验收点：测试断言 SSE 包含 `started` 和 `completed`、不包含 `error`，并可通过 `downloadPath` 使用 HMAC 下载图片且校验响应头、bytes 和 sha256。
    - 重要度：10/10

- [ ] `kuzhambu-workers/tests`：04 锁定图片生成失败分类
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GEN.md`
    - 范围对象：`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`、`kuzhambu-workers/tests/test_ai_routes.py`
    - 处理动作：补充模型超时、5xx、空图片、非法 base64、非法 MIME 和超大图片的失败分类测试。
    - 验收点：失败流只发送 `error` 不发送 `completed`，错误类型分别归一为 `MODEL_TRANSPORT_FAILURE`、`MODEL_SEMANTIC_FAILURE`、`OUTPUT_FORMAT_FAILURE` 或 `IMAGE_INPUT_FAILURE`，且响应不泄露密钥、完整 prompt 或临时路径。
    - 重要度：9/10

- [ ] `kuzhambu-workers`：05 运行 workers 窄范围格式化和最小验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GEN.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai`、`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`、`kuzhambu-workers/tests/test_ai_routes.py`、`kuzhambu-workers/tests/test_graph_registry.py`
    - 处理动作：运行窄范围 Ruff format 后执行 Ruff check 和相关 pytest。
    - 验收点：相关格式化、lint 和 pytest 命令通过，且 `git diff` 不包含任务外文件改动。
    - 重要度：9/10

- [ ] `main`：06 同步 main 分支代码并处理冲突
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：当前分支 `feat/classics-sancai-image-gen`、上游 `origin/main`
    - 处理动作：实现和首轮验证通过后同步最新 `main` 分支代码，并按任务范围处理可能冲突。
    - 验收点：当前分支包含最新 `origin/main`，冲突已解决，且没有引入任务外变更。
    - 重要度：8/10

- [ ] `kuzhambu-workers`：07 同步 main 后复跑最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-workers/src/kuzhambu_workers/ai`、`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`、`kuzhambu-workers/tests/test_ai_routes.py`、`kuzhambu-workers/tests/test_graph_registry.py`
    - 处理动作：同步 `main` 后复跑 Ruff format check、Ruff lint 和相关 pytest。
    - 验收点：最终验证通过，且同步 `main` 未破坏 image-gen artifact 和 SSE final-state 契约。
    - 重要度：9/10

- [ ] `docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`：08 更新 workers Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GEN.md`
    - 范围对象：`docs/40-readiness/WORKERS-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步记录 `CLASSICS_SANCAI_IMAGE_GEN` 已支持真实图片生成、artifact 写入和 SSE final-state 的实现覆盖状态。
    - 验收点：覆盖文档不再把该能力描述为 unsupported，且与实际测试结果一致。
    - 重要度：7/10

- [ ] `docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GEN.md`：09 清理 RUNBOOK 并收窄 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-CLASSICS-SANCAI-IMAGE-GEN.md`、`TODO.md`
    - 处理动作：实现、验证和文档同步完成后删除临时 RUNBOOK，并删除或收窄已完成 TODO。
    - 验收点：PR 收口前无已完成任务残留在 `TODO.md`，无仍有价值耗尽的临时 RUNBOOK。
    - 重要度：8/10

## 待讨论项
