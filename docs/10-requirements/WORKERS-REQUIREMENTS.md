# Workers Requirements

## Purpose

Workers 域定义 `kuzhambu-workers/` 作为 Python 技术支撑工程的能力边界、接口形态和跨域协作规则。

Workers 是无状态执行器，负责 AI 编排执行、流式输出转发、内容生成、图片理解、文件渲染和格式加工。Workers 不拥有业务事实，不替代 Java servers 的应用服务、任务台账、权限、审计、候选区、正式内容或文件对象。

## Scope

覆盖：

- Python 3.10 worker 服务。
- 通过内部 HTTP API 向 Java servers 提供能力。
- 使用 LangGraph 作为 AI 能力执行入口。
- 使用 LangChain 作为 prompt、model adapter、structured output 和 message 组装基础。
- 支持同步响应和流式响应。
- 支持由 Java servers 编排的异步任务单元执行。
- AI 翻译、摘要、标签、问答对、查询理解、回答生成、实体关系抽取、图片理解、视觉描述、条目拆分和提示词优化建议。
- Classics 导出、三才图会静态展示页、Operations 报表等文件渲染和格式加工。
- 请求级临时文件读取、处理和清理。
- 运行日志、请求追踪标识透传、执行耗时和 token 或成本用量摘要返回。

不覆盖：

- 数据库访问。
- Redis、MQ、任务队列或分布式锁。
- LangGraph persistence、checkpointer、durable execution 持久化和 time travel。
- LangChain memory、会话记忆或跨请求上下文保存。
- 任务状态持久化。
- 业务 ID 生成。
- 用户认证、权限判断和菜单权限。
- 业务审计。
- AI 模型、能力映射、提示词版本和候选结果持久化。
- Classics、Knowledge、Discovery、Operations、Storage 或 System 业务表写入。
- Storage 文件对象创建、引用建立、删除和分片上传状态流转。

## Technology Requirements

- Workers 必须使用 Python 3.10。
- Workers 必须以 FastAPI 提供内部 HTTP API。
- Workers 必须使用 Pydantic 定义请求和响应模型。
- Workers 必须使用 LangGraph 承载 AI 执行图；即使单次 prompt 调用，也必须封装为 graph。
- Workers 应使用 LangChain 进行 prompt 模板、chat model、structured output 和 message 组装。
- Workers 可以使用 `httpx` 进行非 AI 文件读取、内部服务调用和必要的 OpenAI-compatible 直连适配。
- Workers 可以使用 `Pillow` 进行图片尺寸、格式和基础转换处理。
- Workers 可以使用 Python 标准库完成 CSV、JSON、HTML、ZIP 和临时文件处理。
- Workers 不得引入 SQLAlchemy、Alembic、Redis client、Celery、Dramatiq、RQ、Kafka client 或 RabbitMQ client 作为基础依赖。
- Workers 新增重型依赖前必须先明确触发需求和模块归属。

## Architecture Rules

- Workers 是技术支撑工程，不是业务域。
- Workers 只接受 Java servers 发起的内部调用。
- Workers 不直接面向 Admin Web 或 Portal Web。
- Workers 内部接口必须校验服务身份，不得仅依赖路径命名作为保护。
- Workers 不接收用户 access token，不做用户权限判断。
- Workers 不直接调用 Java servers 的业务写接口。
- Workers 不回调 AI 域读取模型、提示词、候选结果或任务状态；AI 域必须在请求体中提供本次执行所需 prompt/messages，AI 执行结果必须通过当前 HTTP 响应或 SSE 流返回。
- Workers 每次请求必须包含完整执行上下文，不得依赖上一次请求的内存状态。
- Workers 可以在单次请求生命周期内使用本地临时目录，处理完成后必须清理。
- Workers 可以输出技术日志，但技术日志不得替代 System 业务审计。
- Workers 不得持久化 AI Key、用户输入、候选结果、业务快照或文件内容。
- Workers 不得把底层模型错误直接透出为不稳定协议，必须归一化为稳定错误类型。

## Interface Model

Workers 对外接口按能力族组织，接口命名应表达执行动作而不是业务写入动作。

AI 执行接口示例：

- `POST /internal/ai/invoke`
- `POST /internal/ai/stream`

按能力命名的路径可以作为 worker 内部路由或调试别名存在，但 Java AI 域默认只依赖统一 AI 执行接口。

文件和渲染接口示例：

- `POST /internal/render/classics-export`
- `POST /internal/render/sancai-showcase`
- `POST /internal/render/operations-report`

健康和能力接口示例：

- `GET /internal/health`
- `GET /internal/capabilities`

接口必须支持请求追踪字段：

- `requestId`
- `traceId`
- `callerDomain`
- `operation`

AI 接口请求应包含：

- `modelConfig`：由 AI 域提供的模型、服务地址、能力和调用参数。
- `prompt`：由 AI 域选择并校验后的提示词内容或消息模板。
- `variables`：已经由 Java servers 组装的提示词变量。
- `input`：执行所需完整业务上下文快照。
- `outputSchema`：需要结构化输出时使用的 schema 描述。
- `stream`：是否需要流式输出。

AI 接口响应应包含：

- `status`：`SUCCEEDED`、`FAILED` 或 `PARTIAL`。
- `result`：文本、Markdown、结构化对象或文件描述。
- `usage`：token、耗时和成本估算摘要。
- `warnings`：格式修复、部分降级或非阻断问题。
- `error`：失败时的稳定错误对象。

文件和渲染接口请求应包含：

- 已完成权限过滤和风险确认的内容快照。
- 输出格式。
- 模板版本或模板标识。
- 生成参数。

文件和渲染接口响应应返回：

- 文件 bytes、stream 或 base64 内容。
- 文件名建议。
- 内容类型。
- 文件大小。
- 生成摘要。

## Stream Requirements

Workers 必须支持流式输出能力，用于 AI 回答、长文本生成、图片理解说明、视觉描述生成、报表生成进度和大型 HTML 生成进度。

流式输出必须采用稳定事件协议，使用 Server-Sent Events。

流事件必须至少支持：

- `started`：执行开始。
- `delta`：增量文本或增量结构化片段。
- `progress`：进度、阶段名称、已处理数量和总数量。
- `artifact`：中间产物或最终产物元信息。
- `usage`：阶段性 token、耗时和成本摘要。
- `warning`：非阻断问题。
- `error`：失败信息。
- `completed`：执行完成。

流事件必须包含：

- `eventId`
- `requestId`
- `traceId`
- `stage`
- `timestamp`

流式 AI 输出不得要求 Java servers 在流未完成时写入正式内容。Java servers 可以把流式片段转发给前端展示，但候选结果、问答消息或导出记录的最终落库必须以 `completed` 或明确失败结果为准。

流式输出中断时，Workers 不负责恢复。Java servers 负责记录失败、保留用户输入、决定是否重试，并在重试时重新发起完整请求。

## Async Flow

Workers 不拥有异步任务状态。异步流程由 Java servers 编排，Workers 只执行单次无状态调用。

推荐流程：

1. Java servers 校验用户权限、业务状态和操作参数。
2. Java servers 创建业务任务、候选记录或导出记录。
3. Java servers 读取提示词、模型能力映射和内容快照。
4. Java servers 调用 Workers 同步接口或 stream 接口。
5. Workers 通过 LangGraph 执行单元能力，并返回最终结果或流事件。
6. Java servers 记录调用结果、失败原因、用量和任务进度。
7. Java servers 将 AI 结果写入候选区，或将生成文件交给 Storage 保存。
8. 用户确认后，Java servers 才写入正式业务内容、版本和审计。

批量任务规则：

- Java servers 负责把批量任务拆成多个 worker 调用。
- Java servers 负责取消语义；取消后停止继续派发未开始的 worker 调用。
- Workers 正在执行的单次调用可以返回取消失败或超时失败，但不保存取消状态。
- 单项失败不得影响 Java servers 继续处理其他项。
- 已完成结果由 Java servers 保留。

## Cross-Domain Interfaces

调用规则总览：

| 调用方 | AI 接口 | Render 接口 |
| --- | --- | --- |
| AI | 可以 | 不建议 |
| Classics | 不可以，必须通过 AI 域 | 可以 |
| Knowledge | 不可以，必须通过 AI 域 | 暂无 |
| Discovery | 不可以，必须通过 AI 域 | 暂无 |
| Operations | 不可以；需要 AI 摘要时必须通过 AI 域 | 可以 |
| Storage | 不可以 | 不建议 |
| System | 不可以 | 不可以 |
| Admin Web / Portal Web | 不可以 | 不可以 |

凡是需要模型、提示词、AI 能力映射、用量统计、候选结果或 AI 失败分类的能力，必须经由 AI 域调用 workers。凡是纯文件渲染、格式加工或报表生成能力，可以由拥有对应业务事实的业务域直接调用 render workers。

### AI

AI 域是所有 AI 能力调用的治理入口。

- AI 域负责模型配置、主备服务选择、能力映射、提示词版本、变量校验、功能动作状态、调用记录、候选结果和成本统计。
- Workers 负责根据 AI 域提供的模型配置和提示词执行 LangGraph。
- 其他业务域不得直接绕过 AI 域调用 workers 的 AI 接口。
- AI 语义失败不得由 Workers 自动无限重试。
- 网络传输失败可以由 AI 域决定是否切换备用服务后再次调用 Workers。

### Classics

Classics 消费 AI 域和 Workers 生成能力，但拥有正式古籍内容。

- 翻译、摘要、标签、问答对、图片理解、视觉资产和条目拆分结果必须先进入 AI 候选区。
- Classics 负责用户确认、拒绝、编辑后应用、正式内容写入、版本记录和业务审计。
- Classics 负责导出范围、权限过滤、私有内容确认和导出记录。
- Workers 只生成导出文件、视觉描述、图片理解 Markdown、静态展示页面或其他产物内容。

### Knowledge

Knowledge 消费 AI 抽取能力，但拥有标签、实体、关系、图谱版本和质量指标。

- Knowledge 负责图谱提取任务、人工精修优先级、质量指标和世系图正式结果。
- Workers 只返回实体、关系、来源片段、置信度和抽取说明等候选结构。
- Knowledge 不得直接绕过 AI 域调用 Workers 的 AI 接口。

### Discovery

Discovery 消费 AI 回答生成和查询理解能力，但拥有搜索和问答业务事实。

- Discovery 负责权限过滤、检索、上下文组装、来源引用、会话、消息和调试信息。
- Workers 只返回查询理解结果、改写建议、回答文本或流式回答片段。
- Workers 不保存会话历史，不做权限过滤，不判断来源可见性。
- Discovery 不得直接绕过 AI 域调用 Workers 的 AI 接口。

### Operations

Operations 可以消费 Workers 的文件渲染能力。

- Operations 负责报表记录、维护记录、备份恢复入口、长任务快照和看板聚合。
- Workers 可以根据 Operations 提供的数据快照生成 HTML 或 PDF 报表。
- Workers 不拥有备份、恢复、清理或长任务状态。

### Storage

Storage 拥有文件对象和引用事实。

- Java servers 可以向 Workers 提供临时读取 URL、bytes 或 multipart 文件。
- Workers 可以读取文件内容并生成新文件内容。
- Workers 返回的文件必须由 Java servers 交给 Storage 创建文件对象。
- Workers 不保存 Storage object id，不建立文件引用，不删除文件对象。

### System

System 拥有认证、权限和业务审计。

- Workers 不接收用户 token。
- Workers 不判断用户权限。
- Workers 不写业务审计。
- Java servers 调用 Workers 前完成权限判断，调用后按业务域规则写审计。

## Functional Requirements

- 必须提供内部 HTTP API。
- 必须支持同步 JSON 响应。
- 必须支持 SSE 流式响应。
- 必须支持请求追踪字段透传。
- 必须通过 LangGraph 执行 AI 能力。
- 必须支持 LangChain prompt 和 structured output。
- 必须支持 OpenAI-compatible 模型调用。
- 必须支持由 AI 域传入主服务或备用服务配置。
- 必须支持文本模型、视觉模型和结构化输出模型。
- 必须支持 AI 翻译、摘要、标签、问答对、图片理解、视觉描述、条目拆分、查询理解、回答生成、实体关系抽取和提示词优化建议。
- 必须支持 CSV、JSON、HTML、ZIP 文件生成。
- 必须支持 PDF 报表生成；运行依赖必须隔离在 render 能力内，不得影响 AI graph 执行。
- 必须返回稳定错误类型和可读失败原因。
- 必须返回执行耗时和用量摘要。
- 必须在请求结束后清理临时文件。

## Business Rules

- Workers 不拥有任何业务主数据。
- Workers AI 接口只允许 AI 域服务身份调用。
- Workers 不得连接业务数据库。
- Workers 不得连接 Redis 或 MQ。
- Workers 不得保存任务状态。
- Workers 不得保存跨请求会话。
- Workers 不得直接写入正式内容、候选结果、审计日志或文件对象。
- Workers 不得回调 AI 域业务接口。
- AI Key 不得写入日志、响应、错误详情或临时文件。
- 请求中的敏感字段必须在日志中脱敏。
- Workers 返回结果必须可被 Java servers 重放校验和持久化。
- Workers 失败不得导致 Java servers 丢失用户已输入内容。
- Workers 的流式片段只是展示过程，不是业务提交事实。
- Workers 生成的文件在进入 Storage 前只是临时产物。
- Java servers 调用 Workers 前必须完成用户认证、权限、业务状态和内容可见性校验。

## Acceptance Criteria

- Java servers 能调用 Workers 完成一次无状态 AI prompt 执行。
- Java servers 能调用 Workers 获取 SSE 增量输出，并在完成后收到最终结果。
- Java servers 能在 stream 中断后记录失败并重新发起完整请求。
- Workers 不需要数据库、Redis 或 MQ 即可启动和完成核心能力。
- Workers 单次请求不依赖历史请求即可复现执行。
- AI 翻译、摘要、标签、问答对、图片理解和条目拆分均能通过 LangGraph 执行。
- Discovery 能通过 AI 域间接调用 Workers 完成流式问答回答生成。
- Knowledge 能通过 AI 域间接调用 Workers 完成实体关系候选抽取。
- Classics 能调用文件渲染能力生成 HTML 或 ZIP 产物，并由 Java servers 交给 Storage 保存。
- Operations 能调用文件渲染能力生成报表产物。
- Workers 日志中不出现 AI Key、token、密码或完整敏感输入。

## Related Documents

- [ARCHITECTURE.md](../00-governance/ARCHITECTURE.md)：定义 `kuzhambu-workers/` 工程组和 Python 3.10 基线。
- [AI-REQUIREMENTS.md](./AI-REQUIREMENTS.md)：AI 配置、提示词、候选结果和调用统计归 AI 域。
- [CLASSICS-REQUIREMENTS.md](./CLASSICS-REQUIREMENTS.md)：古籍正式内容、导出、静态展示和分享归 Classics 域。
- [KNOWLEDGE-REQUIREMENTS.md](./KNOWLEDGE-REQUIREMENTS.md)：标签、实体关系和图谱正式结果归 Knowledge 域。
- [DISCOVERY-REQUIREMENTS.md](./DISCOVERY-REQUIREMENTS.md)：搜索、问答会话、来源和调试信息归 Discovery 域。
- [OPERATIONS-REQUIREMENTS.md](./OPERATIONS-REQUIREMENTS.md)：报表、长任务和维护记录归 Operations 域。
- [STORAGE-REQUIREMENTS.md](./STORAGE-REQUIREMENTS.md)：文件对象、引用和内容读取归 Storage 域。
- [SYSTEM-REQUIREMENTS.md](./SYSTEM-REQUIREMENTS.md)：认证、权限和业务审计归 System 域。
- [WORKERS-AI-INTERFACE.md](../20-interfaces/WORKERS-AI-INTERFACE.md)：定义 AI 域与 workers 之间的 HTTP、SSE、请求响应和错误协议。
