# Workers Design

## Purpose

本文档定义 `kuzhambu-workers/` 的目标工程设计。Workers 是 Python 技术支撑工程，负责无状态 AI graph 执行、流式输出转发、文件渲染和格式加工。

Workers 不拥有业务事实，不连接数据库、Redis 或 MQ，不保存任务状态，不替代 Java servers 的权限、审计、候选区、正式内容、文件对象和任务台账。

## Module

```text
kuzhambu-workers/
  pyproject.toml
  README.md
  src/kuzhambu_workers/
    main.py
    api/
      health_routes.py
      ai_routes.py
      render_routes.py
    core/
      config.py
      logging.py
      security.py
      errors.py
      tempfiles.py
    schemas/
      common.py
      ai.py
      render.py
      stream.py
    ai/
      graphs/
      graph_registry.py
      model_adapters.py
      prompt_messages.py
      structured_output.py
    render/
      templates/
      classics_export.py
      sancai_showcase.py
      operations_report.py
      browser_pool.py
      artifact_store.py
    streaming/
      sse.py
      events.py
    tests/
```

目录职责：

- `api/`：FastAPI 路由入口，只做协议解析、服务认证和调用编排。
- `core/`：配置、日志、安全、错误归一和临时文件生命周期。
- `schemas/`：Pydantic 请求、响应、错误和 SSE 事件模型。
- `ai/`：LangGraph graph 构建、能力注册、LangChain message 组装、模型适配和结构化输出。
- `render/`：文件渲染、HTML/CSV/JSON/ZIP/PDF 能力、模板加载、Browser Pool 和请求级产物暂存。
- `streaming/`：SSE 事件编码、增量输出和最终事件包装。
- `tests/`：单元测试、协议测试和路由级测试。

## Runtime

- Python 版本固定为 `>=3.10,<3.11`。
- HTTP 服务使用 FastAPI。
- 请求和响应模型使用 Pydantic。
- AI 执行图使用 LangGraph；即使单次 prompt 调用，也封装为 graph。
- Prompt、message、model adapter 和 structured output 使用 LangChain。
- 内部 HTTP、临时 URL 或模型兼容接口访问使用 `httpx`。
- 图片尺寸、格式和基础转换使用 `Pillow`。
- CSV、JSON、HTML、ZIP 和临时文件处理优先使用 Python 标准库。

禁止作为基础依赖引入：

- SQLAlchemy、Alembic 或其他数据库 ORM/migration 工具。
- Redis client。
- Celery、Dramatiq、RQ 或其他任务队列框架。
- Kafka、RabbitMQ 或其他 MQ client。
- LangGraph persistence、checkpointer、durable execution 和 time travel。
- LangChain memory 或跨请求会话记忆。

## Application Boundary

Workers 只接受 Java servers 发起的内部调用。

调用规则：

| 调用方 | AI 接口 | Render 接口 |
| --- | --- | --- |
| AI 域 | 可以 | 不建议 |
| Classics | 不可以，必须通过 AI 域 | 可以 |
| Knowledge | 不可以，必须通过 AI 域 | 暂无 |
| Discovery | 不可以，必须通过 AI 域 | 暂无 |
| Operations | 不可以；需要 AI 摘要时必须通过 AI 域 | 可以 |
| Storage | 不可以 | 不建议 |
| System | 不可以 | 不可以 |
| Admin Web / Portal Web | 不可以 | 不可以 |

AI 能力必须经由 AI 域治理入口。凡是涉及模型、提示词、能力映射、用量统计、候选结果或 AI 失败分类的调用，都不得绕过 AI 域。

Workers 的 AI 对外接口按 usecase 建模。`/internal/ai/invoke` 和 `/internal/ai/stream` 只作为调试、平台联调和协议验证接口，不作为真实业务域长期集成入口。真实业务入口必须由 AI 域或业务域定义稳定 usecase path、请求模型、权限边界、审计语义和失败分类。

Render 能力只处理调用方已经完成权限过滤、风险确认和数据快照准备后的内容。Workers 返回的文件在进入 Storage 前只是临时产物。

## API Layer

固定入口：

- `GET /internal/openapi.json`
- `GET /internal/docs`
- `GET /internal/redoc`
- `GET /internal/health`
- `GET /internal/capabilities`
- `POST /internal/ai/invoke`
- `POST /internal/ai/stream`
- `POST /internal/render/classics-export`
- `POST /internal/render/sancai-showcase`
- `POST /internal/render/operations-report`
- `POST /internal/render/classics-export/stream`
- `POST /internal/render/sancai-showcase/stream`
- `POST /internal/render/operations-report/stream`

AI 接口契约见 [`WORKERS-AI-INTERFACE.md`](../20-interfaces/WORKERS-AI-INTERFACE.md)。

AI usecase 接口契约见 [`WORKERS-AI-USECASE-INTERFACE.md`](../20-interfaces/WORKERS-AI-USECASE-INTERFACE.md)。

Render 接口契约见 [`WORKERS-RENDER-INTERFACE.md`](../20-interfaces/WORKERS-RENDER-INTERFACE.md)。

OpenAPI 和 Swagger UI 仅作为内部开发、联调和排查入口，固定挂载在 `/internal/*` 路径下，不作为公网接口入口。

`/internal/health` 返回 worker 进程状态、版本、启动时间和基础依赖可用性，不访问数据库、Redis 或 MQ。

`/internal/capabilities` 返回当前 worker 支持的 AI capability、render type、输出格式、stream 支持情况、PDF 引擎、Browser Pool 限制和最大请求或分片大小，用于 Java servers 启动检查和运维排查。

## Security

Workers 内部接口必须经过服务身份校验，不得仅依赖路径命名作为保护。

内部接口使用 HMAC 签名：

- `X-Kuzhambu-Service`
- `X-Kuzhambu-Request-Id`
- `X-Kuzhambu-Trace-Id`
- `X-Kuzhambu-Timestamp`
- `X-Kuzhambu-Signature`

`core/security.py` 负责：

- 校验服务名白名单。
- 校验请求时间偏差。
- 校验请求头 `requestId`、`traceId` 与请求体一致。
- 校验 HMAC 签名。
- 按路径判断服务是否允许访问。

Workers 不接收用户 access token，不判断用户权限，不调用 System 做用户权限校验。内部服务认证只证明调用方服务可信，不证明最终用户有业务权限。

## AI Execution

AI 执行入口由 `ai/graph_registry.py` 管理。

Graph registry 必须使用 canonical capability matrix，不得新增未登记别名。

| Capability | 名称 | Scope | Model tags | Output | Stream | Artifact |
| --- | --- | --- | --- | --- | --- | --- |
| `translate` | 古文翻译 | Classics | `text` | `TEXT` | 否 | 否 |
| `tags` | 标签提取 | Classics/Knowledge | `text`, `structured_output` | `STRUCTURED` | 否 | 否 |
| `visual` | 视觉描述 | Classics | `text` | `TEXT` | 否 | 否 |
| `fusion` | 信息融合 | Classics | `text` | `TEXT`/`MARKDOWN` | 否 | 否 |
| `qa` | 问答生成 | Classics/Discovery | `text`, `structured_output` | `STRUCTURED` | 否 | 否 |
| `split` | 条目拆分 | Classics | `text`, `structured_output` | `STRUCTURED` | 否 | 否 |
| `image_analysis` | 图片理解 | Classics | `vision` | `MARKDOWN` | 是 | 否 |
| `image_gen` | 图片生成 | Classics | `image_gen` | `ARTIFACT` | 是 | 是 |
| `knowledge_graph` | 知识图谱抽取 | Knowledge | `text`, `structured_output` | `STRUCTURED` | 否 | 否 |
| `summary` | 摘要生成 | Classics/Discovery/Operations | `text` | `TEXT` | 否 | 否 |
| `version_summary` | 版本摘要 | Classics/Operations | `text` | `TEXT` | 否 | 否 |
| `query_understanding` | 查询理解 | Discovery | `text`, `structured_output` | `STRUCTURED` | 否 | 否 |
| `answer_generation` | 回答生成 | Discovery | `text`, `streaming_text` | `TEXT` | 是 | 否 |
| `relation_extraction` | 实体关系抽取 | Knowledge | `text`, `structured_output` | `STRUCTURED` | 否 | 否 |
| `lineage_extraction` | 世系图抽取 | Knowledge | `text`, `structured_output` | `STRUCTURED` | 否 | 否 |
| `prompt_suggestion` | 提示词优化建议 | AI | `text` | `TEXT`/`STRUCTURED` | 否 | 否 |

AI 域 `ai_capability` seed、workers `graph_registry.py`、`/internal/capabilities` 和 `WORKERS-AI-INTERFACE.md` 必须使用同一组 capability 编码。`image_generation` 不作为接口编码使用。

AI 执行流程：

1. `api/ai_routes.py` 校验内部服务身份和请求模型。
2. 根据 `capability` 从 graph registry 选择 graph。
3. 使用 AI 域传入的 `modelConfig`、`prompt.messages`、`input.payload` 和 `outputSchema` 构造 graph state。
4. graph 内部使用 LangChain 组装 message、调用 chat model 或 vision model。
5. 同步调用返回稳定 JSON 响应。
6. 流式调用通过 SSE 返回 `started`、`delta`、`progress`、`usage`、`warning`、`error` 和 `completed`。

Workers 不得根据 `templateId`、`promptVersionId` 或业务 ID 回调 Java servers 读取提示词、模型配置或业务内容。每次请求必须包含完整执行上下文。

## Render Execution

Render 执行入口由 `render_routes.py` 分发到具体 renderer。

Renderer：

- `classics_export`：生成 Classics CSV、JSON、HTML 或 ZIP 导出产物。
- `sancai_showcase`：生成三才图会静态展示页面。
- `operations_report`：生成 Operations 周报、月报 HTML 或 PDF 产物。

PDF 生成使用 Playwright/Chromium print。`render/browser_pool.py` 负责 Chromium Browser Pool，复用浏览器进程并限制并发页面数。PDF 渲染不得在每次请求中无控制地启动独立 Chromium 进程。

Browser Pool 规则：

- worker 进程启动时按配置懒加载或预热 Chromium。
- 单次 PDF 渲染从 pool 获取 page/context，请求结束后归还或关闭。
- 页面必须禁用外网任意资源加载，只允许使用请求快照中的内联资源或 Java servers 提供的临时可读资源。
- 渲染超时必须释放 page/context。
- Browser Pool 指标必须进入 health 或 capabilities 诊断信息。

Artifact store 由 AI 和 render 共享，只保存当前请求生命周期内的临时产物和分片读取状态。它不提供跨请求下载，不生成可复用 artifact URL；请求结束后立即清理。

Render 执行流程：

1. Java servers 完成权限校验、内容可见性过滤、私有内容风险确认和内容快照准备。
2. Workers 校验内部服务身份和请求模型。
3. renderer 在请求级临时目录中生成文件。
4. Workers 返回文件 text、base64 或 SSE 分片，以及文件名建议、内容类型、文件大小和生成摘要。
5. Java servers 将返回文件交给 Storage 创建文件对象并建立引用。
6. 请求结束后 Workers 清理临时目录。

Workers 不保存 Storage object id，不建立文件引用，不删除文件对象。

## Streaming

SSE 事件由 `streaming/events.py` 定义，`streaming/sse.py` 负责编码。

所有流事件必须包含：

- `eventId`
- `requestId`
- `traceId`
- `stage`
- `timestamp`

AI 流式片段只用于展示过程，不是业务提交事实。Java servers 只能以 `completed.result` 或同步最终响应生成候选结果、问答消息、导出记录终态或调用记录终态。

HTTP 连接中断且未收到 `completed` 时，Workers 不负责恢复。Java servers 负责记录失败或部分失败，并在重试时重新发起完整请求。

大型 AI 产物和 render 产物必须在当前 SSE 连接内通过 `artifact` 事件分片传输。Java servers 按 `artifactId`、`chunkIndex` 和 `sha256` 组装校验；收到 `completed` 前不得把产物视为业务事实。

## Error Handling

`core/errors.py` 负责把异常归一为稳定错误类型。

通用错误类型：

- `WORKER_PROTOCOL_FAILURE`
- `WORKER_TIMEOUT`
- `WORKER_UNAVAILABLE`
- `MODEL_TRANSPORT_FAILURE`
- `MODEL_SEMANTIC_FAILURE`
- `OUTPUT_FORMAT_FAILURE`
- `IMAGE_INPUT_FAILURE`
- `RENDER_INPUT_FAILURE`
- `RENDER_TEMPLATE_FAILURE`
- `UNSUPPORTED_CAPABILITY`
- `INTERNAL_FAILURE`

错误响应不得暴露：

- AI Key。
- 用户 token。
- 完整 prompt。
- 完整业务输入。
- 临时文件路径。
- 模型原始敏感响应。

## Temporary Files

所有文件处理必须使用请求级临时目录。

规则：

- 临时目录由 `core/tempfiles.py` 创建。
- 目录名必须关联 `requestId`，但不得包含用户输入。
- 请求完成、失败或取消后必须清理。
- AI Key、用户 token、完整 prompt 和完整业务输入不得写入临时文件。
- 需要排查问题时只记录脱敏摘要、文件类型、文件大小和阶段信息。

## Observability

Workers 只输出技术日志和运行指标，不替代 System 业务审计。

日志必须包含：

- `requestId`
- `traceId`
- `callerDomain`
- `operation`
- `capability`
- `stage`
- `latencyMs`
- `status`
- 稳定错误类型

日志必须脱敏：

- `modelConfig.apiKey`
- Authorization 类请求头。
- 用户 token。
- 密码、密钥和内部签名。
- 完整 prompt、完整业务快照和模型原始响应。

## Configuration

环境变量：

- `KUZHAMBU_WORKER_ALLOWED_SERVICES`
- `KUZHAMBU_WORKER_INTERNAL_SECRET`
- `KUZHAMBU_WORKER_MAX_CLOCK_SKEW_MS`
- `KUZHAMBU_WORKER_LOG_LEVEL`
- `KUZHAMBU_WORKER_TEMP_DIR`
- `KUZHAMBU_WORKER_MAX_REQUEST_BYTES`
- `KUZHAMBU_WORKER_DEFAULT_TIMEOUT_MS`
- `KUZHAMBU_WORKER_MAX_ARTIFACT_BYTES`
- `KUZHAMBU_WORKER_ARTIFACT_CHUNK_BYTES`
- `KUZHAMBU_WORKER_BROWSER_POOL_SIZE`
- `KUZHAMBU_WORKER_BROWSER_MAX_PAGES`
- `KUZHAMBU_WORKER_BROWSER_PAGE_TIMEOUT_MS`
- `KUZHAMBU_WORKER_RENDER_TIMEOUT_MS`

默认值：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `KUZHAMBU_WORKER_MAX_CLOCK_SKEW_MS` | `300000` | HMAC 请求时间偏差，5 分钟。 |
| `KUZHAMBU_WORKER_LOG_LEVEL` | `INFO` | 运行日志级别。 |
| `KUZHAMBU_WORKER_TEMP_DIR` | 系统临时目录下的 `kuzhambu-workers` | 请求级临时目录根路径。 |
| `KUZHAMBU_WORKER_MAX_REQUEST_BYTES` | `10485760` | 单次请求体最大 10 MiB。 |
| `KUZHAMBU_WORKER_DEFAULT_TIMEOUT_MS` | `60000` | AI 或 render 默认执行超时 60 秒。 |
| `KUZHAMBU_WORKER_MAX_ARTIFACT_BYTES` | `104857600` | 单个产物最大 100 MiB。 |
| `KUZHAMBU_WORKER_ARTIFACT_CHUNK_BYTES` | `262144` | SSE artifact 分片大小 256 KiB。 |
| `KUZHAMBU_WORKER_BROWSER_POOL_SIZE` | `1` | Chromium browser 进程数量。 |
| `KUZHAMBU_WORKER_BROWSER_MAX_PAGES` | `4` | Browser Pool 最大并发 page 数。 |
| `KUZHAMBU_WORKER_BROWSER_PAGE_TIMEOUT_MS` | `30000` | 单页 PDF 渲染超时 30 秒。 |
| `KUZHAMBU_WORKER_RENDER_TIMEOUT_MS` | `120000` | 单次 render 总超时 120 秒。 |

模型服务地址、模型名、API Key、提示词和业务上下文均由 AI 域在请求体中传入，Workers 不保存这些配置。

## System Dependencies

PDF 渲染运行基线：

- Python package：`playwright==1.59.0`。
- Browser：Playwright-managed Chromium，随 `playwright==1.59.0` 安装的 Chromium 版本。
- Linux 基线：Ubuntu 22.04 LTS Jammy。
- 容器基线：`mcr.microsoft.com/playwright/python:v1.59.0-jammy`。
- 安装命令：`python -m playwright install --with-deps chromium`。

规则：

- 不使用系统包管理器安装 `chromium` 作为运行浏览器。
- Playwright Python package 版本必须与容器或浏览器安装版本一致。
- 构建日志必须记录 `python -m playwright install --list` 输出，用于确认 Chromium revision。
- 容器运行 Chromium 必须配置 `--ipc=host` 或等价共享内存策略，避免 PDF 渲染时 Chromium 因共享内存不足崩溃。
- Playwright 官方镜像用于承载浏览器和系统依赖；应用依赖仍由 `pyproject.toml` 安装。

## Testing

测试范围：

- HMAC 签名校验：成功、签名失败、过期时间戳、服务名不允许。
- Pydantic schema：AI invoke、AI stream、render 请求和错误响应。
- Graph registry：已支持能力能找到 graph，未知能力返回 `UNSUPPORTED_CAPABILITY`。
- SSE 编码：事件名、JSON data、completed 事件和 error 事件格式稳定。
- 临时目录：成功、失败和异常路径都清理。
- 日志脱敏：AI Key、token、签名和完整输入不出现在日志中。
- Render renderer：HTML/JSON/ZIP 产物包含元信息、内容类型和生成摘要。
- Artifact store：AI 和 render 产物只在当前请求生命周期内暂存，SSE 分片顺序、摘要和中断清理可验证。
- Browser Pool：PDF 渲染复用 Chromium，超时和异常路径都释放 page/context。

## Code Quality

Python lint 和 formatter 统一使用 `ruff`。

本地验证命令：

```sh
ruff format --check .
ruff check .
python -m pytest -p no:capture
```

格式化命令：

```sh
ruff format .
ruff check --fix .
```

Workers 不引入 Black、isort、flake8 或 Prettier 作为 Python 格式化链路的一部分。

## Acceptance

- Workers 可以无数据库、无 Redis、无 MQ 启动。
- Java AI 域可以通过 `/internal/ai/invoke` 完成一次无状态 prompt 执行。
- Java AI 域可以通过 `/internal/ai/stream` 获取 SSE 增量输出，并在 `completed` 中拿到最终结果。
- Classics 可以调用 render workers 生成导出或静态展示产物，并交给 Storage 保存。
- Operations 可以调用 render workers 生成报表产物，并交给 Storage 保存。
- Workers 日志不出现 AI Key、token、密码、签名、完整 prompt 或完整敏感输入。
- Workers 单次请求不依赖历史请求即可复现执行。

## Related Documents

- [WORKERS-REQUIREMENTS.md](../10-requirements/WORKERS-REQUIREMENTS.md)：workers 需求和跨域边界。
- [WORKERS-AI-INTERFACE.md](../20-interfaces/WORKERS-AI-INTERFACE.md)：AI 域到 workers 的内部 AI 执行接口。
- [WORKERS-RENDER-INTERFACE.md](../20-interfaces/WORKERS-RENDER-INTERFACE.md)：业务域到 workers 的内部 render 执行接口。
- [AI-DESIGN.md](./AI-DESIGN.md)：AI 域设计和 `WorkerAiClient` 归属。
