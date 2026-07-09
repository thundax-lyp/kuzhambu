# RUNBOOK Classics Sancai Image Generation

## 目标

让 `CLASSICS_SANCAI_IMAGE_GEN` 成为真实可用的 workers AI usecase：

- `POST /internal/ai/classics/sancai/image-gen` 不再返回 `UNSUPPORTED_CAPABILITY`。
- `capability=image_gen` 使用真实图片生成协议调用模型服务。
- 图片结果写入 workers 临时 artifact store。
- 同步最终响应和 SSE `completed` final-state 都以 `artifactReference` 表达最终事实。

## 范围

允许修改的生产代码文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/image_generation.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/image_generation.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graph_registry.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`

允许修改的测试文件：

- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_routes.py`
- `kuzhambu-workers/tests/test_graph_registry.py`

只读依赖文件：

- `kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
- `kuzhambu-workers/src/kuzhambu_workers/api/artifact_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/events.py`
- `kuzhambu-workers/src/kuzhambu_workers/streaming/sse.py`

允许只在已有共享 artifact 能力边界内调用 `kuzhambu_workers.render.artifact_store.RequestArtifactStore`。不得改 Java servers、前端、render renderer、Storage、业务域候选区或正式落库逻辑。

## 确认决策

- 图片生成模型协议使用 OpenAI-compatible image generation endpoint。
- 优先支持模型返回 `b64_json`；临时 URL 下载可作为兼容分支，但不得把模型 URL 透传给 Java servers。
- 业务 usecase `POST /internal/ai/classics/sancai/image-gen` 只接受 `options.stream=true`。
- 通用调试接口 `/internal/ai/invoke` 可以兼容同步返回 `artifactReference`，但不作为业务入口。
- artifact 写入复用 `RequestArtifactStore`，不新增第二套临时文件托管能力。
- workers SSE 不直接传图片 bytes；成功最终事实只放在 `completed.extra.artifactReference`。
- `completed.extra.artifactReference.downloadPath` 是 Java AI 域后续下载 workers 临时图片的内部路径，可视为内部图片下载 URL；它不是模型供应商 URL，也不是前端可访问 URL。
- 默认文件名为 `sancai-image.png`；如模型明确返回 JPEG 或 WebP，则使用 `sancai-image.jpg` 或 `sancai-image.webp`。
- 文件名不得包含用户输入。

## 目标协议

`CLASSICS_SANCAI_IMAGE_GEN` 请求必须满足：

- path：`/internal/ai/classics/sancai/image-gen`
- `operation`：`CLASSICS_SANCAI_IMAGE_GEN`
- `capability`：`image_gen`
- `options.stream`：`true`
- `outputSchema.type`：`artifact` 或等价文件类输出约束

图片生成模型调用由 workers 使用请求体内 `modelConfig`、`prompt.messages`、`input.payload` 和 `outputSchema` 完成。workers 不回查 Java servers，不保存 prompt、业务快照或模型原始响应。

图片生成模型请求体字段：

- `model`：`request.modelConfig.modelName`，必填。
- `prompt`：由 `request.prompt.messages[*].role` 和 `request.prompt.messages[*].content` 按顺序合并，必填，不写日志。
- `response_format`：固定 `b64_json`。
- `n`：固定 `1`。
- `size`：可选，来自 `request.modelConfig.parameters["size"]`。
- `quality`：可选，来自 `request.modelConfig.parameters["quality"]`。
- `style`：可选，来自 `request.modelConfig.parameters["style"]`。
- `output_format`：可选，来自 `request.modelConfig.parameters["output_format"]`，只允许 `png`、`jpeg`、`webp`。
- `background`：可选，来自 `request.modelConfig.parameters["background"]`。

图片生成模型响应只读取以下字段：

- `data[0].b64_json`：base64 图片内容，首选字段。
- `data[0].url`：兼容字段；workers 下载后仍必须写入 artifact store。
- `data[0].output_format`：可选，用于推断 `contentType` 和 `filename`。
- `output_format`：可选，用于推断 `contentType` 和 `filename`。
- `usage.input_tokens`：可选，映射到 `UsageSummary.inputTokens`。
- `usage.output_tokens`：可选，映射到 `UsageSummary.outputTokens`。
- `usage.total_tokens`：只用于诊断，不进入当前 `UsageSummary` 字段。

模型供应商返回 URL 和 workers SSE 返回下载路径是两层不同协议：

- 模型供应商 URL：只允许 workers 内部读取，用于把图片 bytes 写入 `RequestArtifactStore`；不得透传给 Java servers。
- workers `downloadPath`：由 artifact store 生成，格式为 `/internal/artifacts/{artifactId}`；Java AI 域在收到 `completed` 后使用内部 HMAC 下载该临时产物，再转存到 Storage。
- 前端业务 URL：Java servers 转存 Storage 后生成；不属于本 workers RUNBOOK 的交付物。

成功结果必须满足：

- `result` 为 `null`。
- `artifactReference` 非空。
- `artifactReference.contentType` 为真实图片 MIME，例如 `image/png` 或 `image/jpeg`。
- `artifactReference.downloadPath` 指向 `/internal/artifacts/{artifactId}`。
- `artifactReference.sha256`、`sizeBytes`、`filename`、`expiresAt` 来自 artifact store 元数据。
- TTL 沿用 workers artifact store 默认 `12` 小时。

SSE 成功流必须满足：

- 先发送 `started`。
- 可以发送 `progress`、`usage` 或 `warning`，但不得通过 `delta` 或 `artifact` 事件传完整图片内容。
- 最终发送且只以 `completed` 作为成功最终事实。
- `completed.result` 为 `null`。
- `completed.extra.status` 为 `SUCCEEDED`。
- `completed.extra.artifactReference` 为最终临时产物引用。

`completed` 事件 data 结构必须精确包含：

- `eventId`
- `requestId`
- `traceId`
- `stage`：固定 `completed`
- `timestamp`
- `result`：固定 `null`
- `usage.latencyMs`
- `usage.inputTokens`
- `usage.outputTokens`
- `usage.costAmount`
- `extra.status`：固定 `SUCCEEDED`
- `extra.failureStage`：固定 `null`
- `extra.fallbackUsed`：固定 `false`
- `extra.artifactReference.artifactId`
- `extra.artifactReference.downloadPath`：Java AI 域下载 workers 临时图片的内部路径。
- `extra.artifactReference.contentType`
- `extra.artifactReference.filename`
- `extra.artifactReference.sizeBytes`
- `extra.artifactReference.sha256`
- `extra.artifactReference.expiresAt`

失败流必须满足：

- 发送 `error` 后结束。
- 不发送 `completed`。
- HTTP、超时、限流或 5xx 归一为 `MODEL_TRANSPORT_FAILURE`。
- 模型未返回图片或返回空图片归一为 `MODEL_SEMANTIC_FAILURE`。
- base64 解析失败或响应结构不符合预期归一为 `OUTPUT_FORMAT_FAILURE`。
- MIME、尺寸或大小不合法归一为 `IMAGE_INPUT_FAILURE`。
- 错误响应不得包含 API Key、完整 prompt、完整业务输入、临时文件路径或模型原始敏感响应。

## 数据结构变更

外部响应结构不新增字段。以下既有字段语义必须保持稳定：

- `AiInvokeResponse.result`：图片生成成功时固定为 `null`。
- `AiInvokeResponse.artifactReference`：图片生成成功时非空。
- `ArtifactReference.artifactId`：artifact store 生成的 `art_*` 编码。
- `ArtifactReference.downloadPath`：固定格式 `/internal/artifacts/{artifactId}`。
- `ArtifactReference.contentType`：图片 MIME，只允许 `image/png`、`image/jpeg`、`image/webp`。
- `ArtifactReference.filename`：固定安全文件名，不含用户输入。
- `ArtifactReference.sizeBytes`：图片字节数。
- `ArtifactReference.sha256`：格式 `sha256:{hex}`。
- `ArtifactReference.expiresAt`：ISO-8601 UTC 字符串。

新增内部类型放在 `kuzhambu-workers/src/kuzhambu_workers/ai/image_generation.py`：

```python
class GeneratedImageArtifact(BaseModel):
    data: bytes
    contentType: str
    filename: str
    usage: UsageSummary
```

字段约束：

- `data`：非空图片 bytes，不写日志。
- `contentType`：只允许 `image/png`、`image/jpeg`、`image/webp`。
- `filename`：`sancai-image.png`、`sancai-image.jpg` 或 `sancai-image.webp`。
- `usage`：缺失 token 时使用 `UsageSummary()` 默认值。

`kuzhambu-workers/src/kuzhambu_workers/ai/graphs/image_generation.py` graph result 字段：

- `format`：固定 `ARTIFACT`。
- `payload.data`：`GeneratedImageArtifact.data`。
- `payload.contentType`：`GeneratedImageArtifact.contentType`。
- `payload.filename`：`GeneratedImageArtifact.filename`。
- `usage.latencyMs`：模型调用耗时毫秒。
- `usage.inputTokens`：来自模型 `usage.input_tokens`，缺失为 `0`。
- `usage.outputTokens`：来自模型 `usage.output_tokens`，缺失为 `0`。
- `usage.costAmount`：当前 workers 不计算费用，保持默认值。

`kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py` 写入 artifact 后生成 `ArtifactReference` 字段：

- `artifactId <- ArtifactMetadata.artifact_id`
- `downloadPath <- ArtifactMetadata.download_path`
- `contentType <- ArtifactMetadata.content_type`
- `filename <- ArtifactMetadata.filename`
- `sizeBytes <- ArtifactMetadata.size_bytes`
- `sha256 <- ArtifactMetadata.sha256`
- `expiresAt <- ArtifactMetadata.expires_at`

SSE `error` 事件 data 字段：

- `eventId`
- `requestId`
- `traceId`
- `stage`：固定 `error`
- `timestamp`
- `error.type`
- `error.code`
- `error.message`
- `error.retryable`
- `extra.status`：固定 `FAILED`
- `extra.failureStage`：模型调用、解析和 artifact 写入失败均按现有 route 映射输出。
- `extra.fallbackUsed`：固定 `false`
- `extra.artifactReference`：固定 `null`
- `extra.errorType`
- `extra.errorMessage`

## 实现计划

### 任务一：图片模型适配和 graph

涉及文件：

- `kuzhambu-workers/src/kuzhambu_workers/ai/image_generation.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graphs/image_generation.py`
- `kuzhambu-workers/src/kuzhambu_workers/ai/graph_registry.py`

改动要求：

- 新增 `generate_image(request: AiInvokeRequest) -> GeneratedImageArtifact`。
- 新增 `_build_image_prompt(messages: list[AiMessage]) -> str`，只拼接 role 和 content，不写日志。
- 新增 `_decode_image_response(payload: dict[str, Any]) -> GeneratedImageArtifact`。
- 新增 `build_image_generation_graph()`，graph state 输入为 `{"request": AiInvokeRequest}`。
- `GraphRegistry.build_default()` 对 `AiCapability.IMAGE_GEN` 注册 `build_image_generation_graph()`，不能继续使用 basic text graph。
- 模型调用只读取 `modelConfig`、`prompt.messages`、`input.payload` 和 `outputSchema`。
- graph result 必须包含：
  - `format`: `ARTIFACT`
  - `payload.data`: 图片 bytes
  - `payload.contentType`: 图片 MIME
  - `payload.filename`: 安全文件名
  - `usage`: `UsageSummary` JSON

### 任务二：AI route 写入 artifact 并输出 final-state

涉及文件：

- `kuzhambu-workers/src/kuzhambu_workers/api/ai_routes.py`
- `kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`

改动要求：

- 删除 `stream_ai_graph()` 中 `if request.capability.value == "image_gen": raise unsupported_capability(...)`。
- 新增 `_artifact_reference_from_image_result(request: AiInvokeRequest, result: AiResult) -> ArtifactReference`，将 image graph result 写入 `RequestArtifactStore`。
- store 参数使用：
  - `request_id=request.requestId`
  - `root_dir=load_settings().temp_dir`
  - `chunk_bytes=load_settings().artifact_chunk_bytes`
  - `ttl_hours=load_settings().artifact_ttl_hours`
- artifact metadata 映射为 `ArtifactReference`：
  - `artifactId <- metadata.artifact_id`
  - `downloadPath <- metadata.download_path`
  - `contentType <- metadata.content_type`
  - `filename <- metadata.filename`
  - `sizeBytes <- metadata.size_bytes`
  - `sha256 <- metadata.sha256`
  - `expiresAt <- metadata.expires_at`
- SSE 成功完成事件调用 `final_state_extra(status="SUCCEEDED", failure_stage=None, fallback_used=False, artifact_reference=artifact_reference)`。
- 同步响应也必须返回 `artifactReference`，`result` 固定为 `null`。
- 不修改 `kuzhambu-workers/src/kuzhambu_workers/api/artifact_routes.py`；下载能力复用现有实现。

### 任务三：Classics usecase contract tests

涉及文件：

- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_routes.py`
- `kuzhambu-workers/tests/test_graph_registry.py`

改动要求：

- 将 `CLASSICS_SANCAI_IMAGE_GEN` 成功路径从 `UNSUPPORTED_CAPABILITY` 改为 `completed`。
- mock 模型返回 1x1 PNG base64。
- 断言 SSE：
  - 包含 `event: started`
  - 包含 `event: completed`
  - 不包含 `event: error`
  - `completed.result is None`
  - `completed.extra.status == "SUCCEEDED"`
  - `completed.extra.artifactReference.contentType == "image/png"`
  - `completed.extra.artifactReference.filename == "sancai-image.png"`
- 使用 `completed.extra.artifactReference.downloadPath` 再发起 HMAC 下载请求，断言：
  - HTTP 200
  - response body 等于 mock PNG bytes
  - response header `content-type` 以 `image/png` 开头
  - `X-Kuzhambu-Artifact-Id` 等于 `artifactId`
  - `X-Kuzhambu-Artifact-Sha256` 等于 `artifactReference.sha256`
  - sha256 与 `artifactReference.sha256` 一致
- 保留未知 capability 返回 `UNSUPPORTED_CAPABILITY` 的 registry 测试。

### 任务四：失败分类测试

涉及文件：

- `kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
- `kuzhambu-workers/tests/test_ai_routes.py`

改动要求：

- 模型 HTTP 超时或 5xx：断言 `event:error`，`errorType == "MODEL_TRANSPORT_FAILURE"`，无 `event:completed`。
- 模型返回空 `data`：断言 `errorType == "MODEL_SEMANTIC_FAILURE"`。
- 模型返回非法 base64：断言 `errorType == "OUTPUT_FORMAT_FAILURE"`。
- 模型返回非图片 MIME 或超过 `KUZHAMBU_WORKER_MAX_ARTIFACT_BYTES`：断言 `errorType == "IMAGE_INPUT_FAILURE"`。
- 所有失败断言响应文本不包含 `process-only`、完整 prompt、临时文件绝对路径。

## 前端联调说明

本任务不修改前端文件。前端可见行为由 Java AI 域转发结果决定，workers 只保证以下接口语义。前端涉及的控件和操作颗粒度如下：

- 控件：三才图会条目编辑页或视觉资产面板中的“生成图片”按钮。
  - 用户点击后，按钮进入 loading 状态。
  - loading 期间按钮禁用，避免重复提交同一个 image-gen 任务。
  - loading 期间保留现有图片预览，不用空白占位覆盖旧图。
- 控件：生成进度区域。
  - Java AI 域转发 `started` 或任务已创建状态后，显示“生成中”。
  - 如 Java AI 域转发 `progress`，只展示文本进度或进度条。
  - 不展示 workers `downloadPath`。
  - 不把 workers SSE `delta`、`artifact` 或模型临时 URL 当作图片预览。
- 控件：图片预览。
  - 只有 Java AI 域收到 workers `completed.extra.artifactReference`、完成内部下载、转存 Storage 并返回业务图片 URL 后，才刷新预览图。
  - 预览图 `src` 只能使用 Storage 或 Java 业务接口返回的正式 URL。
  - 预览成功后按钮退出 loading 状态。
- 控件：错误提示。
  - workers 返回 `error` 或 Java AI 域转存失败时，按钮退出 loading 状态并恢复可点击。
  - 显示 Java AI 域映射后的用户可读错误。
  - 不展示 `error.detail`、workers 内部路径、模型供应商 URL、API Key 或 prompt。
- 控件：重试操作。
  - 用户再次点击“生成图片”时，前端应发起新的 Java AI 域请求。
  - 前端不得复用上一次 workers `artifactId` 或 `downloadPath`。

## 验收标准

- `CLASSICS_SANCAI_IMAGE_GEN` 流式调用返回 `200 text/event-stream`。
- 成功流包含 `event: started` 和 `event: completed`，不包含 `event: error`。
- `completed.extra.artifactReference` 字段完整，且 `downloadPath` 可通过内部 HMAC 下载。
- 下载内容与 `artifactReference.sha256`、`sizeBytes`、`contentType` 一致。
- 通用 `/internal/capabilities` 仍声明 `image_gen` 和 `ARTIFACT`。
- workers 不新增数据库、Redis、MQ、跨请求状态或业务回调。
- 测试中不再把 `CLASSICS_SANCAI_IMAGE_GEN` 成功路径视为 `UNSUPPORTED_CAPABILITY`。
- 前端联调时，图片预览只来自 Storage 转存后的业务 URL，不来自 workers `downloadPath`。

## 验证命令

在 `kuzhambu-workers/` 下执行：

```sh
.venv/bin/python -m ruff format src/kuzhambu_workers/ai tests/test_ai_usecase_routes_classics.py tests/test_ai_routes.py
.venv/bin/python -m ruff format --check .
.venv/bin/python -m ruff check .
.venv/bin/python -m pytest -p no:capture tests/test_ai_usecase_routes_classics.py tests/test_ai_routes.py tests/test_graph_registry.py
```

如本地 `.venv` 不存在，先按 workers 治理规则创建 Python 3.10 虚拟环境并安装 `.[dev]`。

## 收口

实现通过审核后删除本 RUNBOOK。行为或协议若发生稳定变化，同步更新：

- `docs/20-interfaces/WORKERS-AI-INTERFACE.md`
- `docs/20-interfaces/WORKERS-AI-USECASE-INTERFACE.md`
- `docs/30-designs/WORKERS-DESIGN.md`
