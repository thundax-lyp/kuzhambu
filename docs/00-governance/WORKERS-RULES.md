# Workers Rules

## Purpose

本文档固定 `kuzhambu-workers` 的 Python worker 治理规则。

本文件覆盖：

- Python workers 架构与分层
- 命名与目录归属
- API、schema、stream、AI 和 render 边界
- 安全、依赖和测试门禁

## Scope

当前范围：

- `kuzhambu-workers/src/kuzhambu_workers`
- `kuzhambu-workers/tests`
- Python 3.10、FastAPI、Pydantic、LangGraph、LangChain、Playwright 和 Ruff 代码

不在范围内：

- Java servers 业务域规则
- 前端应用规则
- AI 提示词内容治理
- 生产部署拓扑和运维手册

## Principles

- Workers 是无状态技术执行器，不是业务事实拥有方。
- 一致性优先于灵活性。
- 能机器门禁的规则优先机器门禁。
- API 契约、schema、stream 事件和安全边界必须稳定。
- 不依赖开发者记忆规则；规则应尽量沉淀到 Ruff、mypy、import-linter、pytest 和 Code Review。

## Rule Structure

规则分为两个层级：

- `Hard Rules`：必须可由 Ruff、mypy、import-linter、pytest 或依赖扫描稳定门禁。
- `Review Rules`：由 AI 或人工审阅执行，暂不强制门禁。

同一条规则只归入一个层级。已由 `Hard Rules` 稳定门禁的内容不得在 `Review Rules` 中重复表述；当 `Review Rules` 被沉淀为门禁后，必须从 `Review Rules` 删除或改写为未被门禁覆盖的语义审阅点。

新增规则应先归入以下主题之一：

- `Architecture`
- `Placement`
- `Naming`
- `API`
- `Schema`
- `Streaming`
- `AI`
- `Render`
- `Security`
- `Dependency`
- `Code Quality`
- `Testing`
- `Forbidden Defaults`

门禁报错信息应包含本文件中的规则标签，例如 `WORKERS_CORE_NO_FEATURE_DEPENDENCY`。

## Hard Rules

### Architecture

- `WORKERS_ROOT_PACKAGE`：生产代码必须位于 `src/kuzhambu_workers/`。
- `WORKERS_PACKAGE_GROUPS`：`kuzhambu_workers` 下工程组固定为 `api/`、`core/`、`schemas/`、`ai/`、`render/`、`streaming/`。
- `WORKERS_CORE_NO_FEATURE_DEPENDENCY`：`core/` 不得依赖 `api/`、`ai/`、`render/`、`streaming/` 或 `schemas/`。
- `WORKERS_SCHEMA_NO_RUNTIME_DEPENDENCY`：`schemas/` 不得依赖 `api/`、`ai/`、`render/` 或 `streaming/`。
- `WORKERS_FEATURE_NO_API_DEPENDENCY`：`ai/`、`render/` 和 `streaming/` 不得依赖 `api/`。
- `WORKERS_AI_NO_RENDER_DEPENDENCY`：`ai/` 不得依赖 `render/`。
- `WORKERS_RENDER_NO_AI_DEPENDENCY`：`render/` 不得依赖 `ai/`。
- `WORKERS_STREAMING_NO_FEATURE_DEPENDENCY`：`streaming/` 不得依赖 `ai/` 或 `render/`。
- `WORKERS_MAIN_ASSEMBLY_ONLY`：`main.py` 只负责 FastAPI 应用创建、路由挂载和 OpenAPI 内部路径配置。

### Placement

- `WORKERS_PATH_API_ROUTES`：FastAPI 路由只放在 `api/*_routes.py`。
- `WORKERS_PATH_CORE`：配置、日志、安全、错误归一和请求级生命周期支撑只放在 `core/`。
- `WORKERS_PATH_SCHEMAS`：Pydantic 请求、响应、错误和 SSE 事件模型只放在 `schemas/`。
- `WORKERS_PATH_AI_GRAPHS`：LangGraph graph 定义放在 `ai/graphs/`。
- `WORKERS_PATH_RENDER_TEMPLATES`：渲染 HTML 模板放在 `render/templates/`。
- `WORKERS_PATH_TESTS`：测试放在 `kuzhambu-workers/tests/`。

### Naming

- `WORKERS_NAME_FILE_SNAKE_CASE`：Python 文件名使用 snake_case。
- `WORKERS_NAME_ROUTE_FILE`：路由文件命名为 `<capability>_routes.py`。
- `WORKERS_NAME_SCHEMA_CLASS`：Pydantic 模型使用 PascalCase，并按语义以 `Request`、`Response`、`Result`、`Error`、`Event` 或 `Metadata` 结尾。
- `WORKERS_NAME_ENUM_CLASS`：枚举类型使用 PascalCase，枚举值使用稳定大写编码或接口约定编码。
- `WORKERS_NAME_FUNCTION_VERB`：公开函数命名应以动作表达能力，例如 `build_*`、`create_*`、`render_*`、`encode_*`、`verify_*`。

### API

- `WORKERS_API_INTERNAL_PATH_ONLY`：HTTP API 路径必须位于 `/internal/*`。
- `WORKERS_API_OPENAPI_INTERNAL_ONLY`：OpenAPI、Swagger UI 和 ReDoc 必须挂载在 `/internal/*`。
- `WORKERS_API_ROUTE_AUTH_REQUIRED`：除 health、capabilities 和 OpenAPI 开发入口外，内部执行接口必须校验服务身份。
- `WORKERS_API_NO_USER_TOKEN`：Workers API 不得接收用户 access token。
- `WORKERS_API_NO_BUSINESS_WRITE`：Workers 不得调用 Java servers 业务写接口。

### Schema

- `WORKERS_SCHEMA_PYDANTIC_ONLY`：请求、响应和事件协议必须使用 Pydantic 模型表达。
- `WORKERS_SCHEMA_STABLE_FIELD_ALIAS`：对外 JSON 字段名必须保持稳定，不得依赖 Python 内部重命名破坏契约。
- `WORKERS_SCHEMA_ERROR_STABLE_TYPE`：失败响应必须使用稳定错误类型，不得直接透出底层异常类名。

### Streaming

- `WORKERS_STREAM_EVENT_REQUIRED_FIELDS`：SSE 事件必须包含 `eventId`、`requestId`、`traceId`、`stage` 和 `timestamp`。
- `WORKERS_STREAM_EVENT_TYPE_SET`：SSE 事件类型必须属于 `started`、`delta`、`progress`、`artifact`、`usage`、`warning`、`error`、`completed`。
- `WORKERS_STREAM_COMPLETED_AS_FINAL_FACT`：流式结果只有 `completed` 可作为最终事实。

### AI

- `WORKERS_AI_GRAPH_REQUIRED`：AI 能力必须通过 LangGraph graph 执行。
- `WORKERS_AI_CANONICAL_CAPABILITY`：AI capability 必须来自 canonical capability matrix。
- `WORKERS_AI_NO_CALLBACK_PROMPT`：Workers 不得根据业务 ID 回调 Java servers 读取模型配置、提示词、候选结果或任务状态。
- `WORKERS_AI_SERVICE_BOUNDARY`：AI 统一执行接口只允许 AI 域服务身份调用。

### Render

- `WORKERS_RENDER_REQUEST_SNAPSHOT_ONLY`：Render 能力只能使用请求体或临时可读资源中的内容快照。
- `WORKERS_RENDER_NO_STORAGE_FACT`：Render 返回的文件不得保存 Storage object id，不得建立或删除文件引用。
- `WORKERS_RENDER_BROWSER_POOL_REQUIRED`：PDF 渲染必须通过 Browser Pool 复用 Chromium。
- `WORKERS_RENDER_TEMPLATE_LOCAL`：渲染模板必须来自 `render/templates/` 或明确的请求快照，不得任意读取外部模板路径。

### Security

- `WORKERS_SECURITY_HMAC_REQUIRED`：内部执行接口必须使用 HMAC 请求签名。
- `WORKERS_SECURITY_TRACE_HEADER_MATCH_BODY`：请求头中的 request id 和 trace id 必须与请求体一致。
- `WORKERS_SECURITY_SERVICE_ALLOWLIST`：服务身份必须经过白名单校验。
- `WORKERS_SECURITY_PATH_PERMISSION`：服务身份必须按路径校验访问权限。

### Dependency

- `WORKERS_DEPENDENCY_PYTHON310`：Python 版本固定为 `>=3.10,<3.11`。
- `WORKERS_DEPENDENCY_NO_DATABASE`：不得引入 SQLAlchemy、Alembic 或数据库驱动作为基础依赖。
- `WORKERS_DEPENDENCY_NO_REDIS`：不得引入 Redis client 作为基础依赖。
- `WORKERS_DEPENDENCY_NO_QUEUE`：不得引入 Celery、Dramatiq、RQ、Kafka client 或 RabbitMQ client 作为基础依赖。
- `WORKERS_DEPENDENCY_NO_STATEFUL_AI`：不得引入 LangGraph persistence、checkpointer、durable execution、time travel 或 LangChain memory 作为跨请求状态能力。

### Code Quality

- `WORKERS_CODE_RUFF_FORMAT`：Python 代码必须通过 Ruff formatter。
- `WORKERS_CODE_RUFF_LINT`：Python 代码必须通过 Ruff lint。
- `WORKERS_CODE_NO_PRINT`：生产代码禁止 `print`。
- `WORKERS_CODE_PUBLIC_FUNCTION_TYPED`：生产代码函数必须声明参数和返回类型。
- `WORKERS_CODE_NO_IMPLICIT_OPTIONAL`：禁止隐式 Optional。
- `WORKERS_CODE_NO_ANY_RETURN`：禁止返回 `Any`。

### Testing

- `WORKERS_TEST_PYTEST`：Python 测试必须通过 pytest。
- `WORKERS_TEST_ARCHITECTURE_CONTRACT`：架构依赖必须通过 import-linter 或等价 pytest contract 测试。
- `WORKERS_TEST_API_CONTRACT`：OpenAPI 内部路径、服务身份和稳定 schema 必须有测试覆盖。
- `WORKERS_TEST_STREAM_CONTRACT`：SSE 事件类型和必填字段必须有测试覆盖。

### Forbidden Defaults

- `WORKERS_FORBID_STATEFUL_STORAGE`：Workers 不得持久化 AI Key、用户输入、候选结果、业务快照或文件内容。
- `WORKERS_FORBID_TASK_STATE`：Workers 不得保存任务状态、取消状态或跨请求执行状态。
- `WORKERS_FORBID_BUSINESS_AUDIT`：Workers 不得写业务审计。
- `WORKERS_FORBID_USER_AUTHORIZATION`：Workers 不得判断最终用户权限。
- `WORKERS_FORBID_BUCKET_DIR`：生产代码不得新增 `common/`、`utils/`、`misc/`、`helpers/` 兜底目录。

## Review Rules

### Architecture

- 新增 worker 能力应先判断属于 `ai/`、`render/`、`streaming/`、`core/` 还是 `schemas/`，不要为了复用方便穿透目录边界。
- 复杂流程应在 feature package 内清晰分解，`api/` 只做协议、认证和编排。

### API

- 路径命名应表达技术执行能力，不表达正式业务写入结果。
- 新增接口应同步更新 `docs/20-interfaces/` 中对应契约。

### AI

- Prompt、模型配置和业务上下文应由 Java servers 在请求体中提供，Workers 不应隐式假设某个业务域的内部状态。
- AI graph 应按能力复用基础节点，但不得把业务域确认、候选区、审计或落库语义放入 Workers。

### Render

- Render 模板应优先使用明确输入快照和本地模板，避免引入外部资源加载导致渲染不可重复。
- PDF 渲染新增能力时，应评估 Browser Pool 并发、超时和资源释放风险。

### Testing

- 新增 API 或 schema 字段时，应优先补契约测试，再补实现测试。
- 新增依赖前应说明为什么标准库或既有依赖不足以完成需求。
