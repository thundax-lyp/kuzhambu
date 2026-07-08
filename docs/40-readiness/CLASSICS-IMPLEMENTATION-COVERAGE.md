# Classics Implementation Coverage

## Purpose

本文档记录 Classics 古籍域需求的当前完成状态，用于后续补充开发、跨域协作和交付验收。

本清单不替代 `docs/10-requirements/CLASSICS-REQUIREMENTS.md` 或 `docs/30-designs/CLASSICS-DESIGN.md`。

## Status Definition

- `已完成`：当前仓库已有可追溯交付物，且不依赖后续服务代码即可成立。例如需求文档、数据设计、schema、初始化数据或稳定设计决策。
- `部分完成`：当前仓库已有需求、设计、数据结构或阶段性交付物，但运行时代码、跨域协作或业务闭环尚未完成。
- `未完成`：当前仓库尚未形成可执行设计、数据结构或明确任务，后续必须补充。
- `外部依赖`：能力边界不属于 Classics 单域，Classics 只提供引用、入口、快照、任务记录或调用点。

## Current Baseline

已完成：

- Classics 原始需求已按三才图会、王圻文档、明代习俗和分享重新整理。
- Classics 数据设计已按需求来源重制，每个持久化字段可追溯到需求。
- Classics schema SQL 已重制。
- 三才图会初始化数据 SQL 已由 JSON 生成并导入开发数据库。
- JSON 快照和转换脚本已保留。
- Classics 服务实现阶段已按 `domain -> application -> infra -> interface -> starter -> verification` 拆分并交付阶段结果。
- 关键架构决策已确认：不做读写分离、Repository 统一命名、业务表不放审计字段、状态使用 `varchar`、三才图会新增条目使用数据库自增主键；其中 `classics_content_tag.priority` 已收敛为 `content_type + content_id` 作用域内唯一。
- 导出和静态展示已打通 worker 渲染并写入 Storage；当前已接入导出任务下载门禁与过期控制（过期禁读、前端状态显示），静态展示产物支持下载；底层产物对象生命周期已复用 Storage 自动 orphan 清理能力，业务侧仍待补齐删除与批量管理策略。
- 分享访问首版支持正式版本绑定和快照字段入库能力（`content_version_id`、`content_version_no`、`title_snapshot`、`content_snapshot_json`）。
- Admin/Portal starter 已扫描 Classics 的 application/infra/interface 包，启动路径与装配可用。
- 三才图会 Admin Web 最小闭环已完成：后台菜单和 `/classics/sancai` 路由可进入真实页面，支持门类 CRUD、门类独立排序表单、门类/卷目并列列表、条目列表、搜索、生命周期筛选、分页、详情打开、标题/原文/译文/摘要/公开状态编辑和保存，以及版本历史、版本字段对比和历史恢复。
- 三才图会门类、卷和条目治理状态已统一到运行时可解析的业务枚举口径，覆盖当前 schema 默认值、初始化数据和 dev 数据库取值。
- 三才图会 Admin Web 已用 Playwright 验证接口闭环和页面闭环，覆盖 categories list/detail/save/delete/sort、volumes、entries/page、entries/{id}、entries/save、entries/versions/list、entries/versions/get、entries/versions/reset 请求体。
- 明代习俗 Admin Web 最小闭环已完成：后台菜单和 `/classics/ming-customs` 路由可进入真实页面，支持标题/分类/可见性/时间筛选、关键词云、分页、详情打开、新增、编辑、删除和分享入口。
- 明代习俗分类字典和初始化数据已补齐，`CLASSICS_MING_CUSTOMS_CATEGORY` 可由运行时字典接口读取，dev.env 数据库已同步。
- 明代习俗富文本展示已通过 Admin Web 独立控件封装 Markdown/HTML 渲染，前端使用统一清洗策略处理危险内容。
- 明代习俗闭环已完成验证：数据生成校验、后端 Maven 检查和测试、前端 format/lint/test/build、Playwright 页面闭环、dev.env 登录后分页和关键词云接口冒烟均已通过。
- 王圻文档 Admin Web 管理闭环已完成：后台菜单和 `/classics/wangqi` 路由可进入真实页面，支持文档分页、关键词和可见性筛选、时间线视图、详情阅读、新增、编辑、删除、原始文件上传和读取、版本历史、版本字段对比和历史恢复。
- 王圻文档初始化数据、后端接口契约、前端服务契约、页面单测、Playwright 页面闭环和 dev.env Admin API 冒烟均已通过；删除 Wangqi 文档会删除版本历史、解绑 Storage owner，并将当前原始文件对象回落为未引用状态。
- Storage 文件读取和预览最小闭环已接入 Classics：Admin Storage/Wangqi/Sancai 使用鉴权资源 URL，Wangqi 原始文件和 Sancai 图片通过业务域接口读取，Portal 分享详情动态装配资源对象，分享资源读取会校验分享链接和快照内资源 ID。
- Classics 通用内容标签闭环已接通后端运行时：手工标签新增/更新/删除会先解析或创建 Knowledge 统一标签，再同步 Knowledge 内容引用；AI 标签确认会先清理旧 AI 引用，再经 Knowledge 协作语义回写统一标签和引用投影；标签排序已收敛为 `contentType + contentId` 作用域；通用标签接口已补齐 `contentType/contentId/tagNameSnapshot/id/orderedIds` 入参治理和 `tags/delete` 删除路由。
- Classics 标签闭环核心路径已补齐后端测试，覆盖手工标签绑定、AI 标签确认同步、删除标签同步和按内容排序请求映射。
- 三类内容页面已接入 AI 候选确认面板：当前支持按内容读取 `PENDING` 候选、前端编辑 payload、应用候选、拒绝候选，并在应用后刷新摘要/标签/问答对或主内容详情；后端已接通 `summary / translate / tags / qa` 候选应用到正式内容、版本追加和 AI 候选状态回写。
- 三类内容页面已接入需求文档要求的“内容上下文内联 AI 精修”中的任务型入口，但当前能力并不对称：Sancai 页面可创建 `translate` 与 `summary` 任务，Wangqi 与 Ming Customs 页面只接入 `summary` 任务；三页都已接入任务轮询，并在 `SUCCEEDED/PARTIAL` 后刷新详情或治理面板。
- 分享后台管理闭环已完成：分享分页列表、详情、状态更新与访问记录查询接口在 admin 侧完成闭环；Wangqi/MingCustoms/Sancai 可通过页面完成单条分享入口、批量分享入口与管理入口联动；Portal 仍复用现有 `shareToken` 读取语义，`ACTIVE` 可读，`EXPIRED/REVOKED` 维持现有错误态。
- Classics 批量操作结果模型已落地到 Java application/interface 和 Admin Web service contract，当前已覆盖批量分享与批量公开/私有状态修改的成功数、失败数和每条失败原因展示；统一入口 `POST /api/classics/content/visibility/change` 已分发到三类内容应用服务，三类 Admin Web 页面已提供当前页多选批量公开/私有入口。
- 导出和静态展示任务治理闭环收口：三类内容页面分别完成导出任务列表/创建闭环与状态可见性展示；Sancai 的静态展示列表与下载状态在页面内已收口。
- 三才图会视觉资产 AI 闭环已补齐：页面内已接入 `image_analysis / fusion / visual / image_gen` 单条任务入口、任务状态轮询、失败提示与重试入口；候选区已接通按 `objectId` 限定的候选读取、编辑、应用、拒绝和刷新联动；`image_gen` 候选应用后的 `artifact -> Storage -> 新 version -> 页面预览/下载` 链路已稳定。
- 三才图会视觉资产 AI 流式候选展示闭环已补齐：`image_analysis / image_gen` 创建后展示 `AI 流式过程` 卡片，过程卡片展示增量文本、阶段、warning 和失败原因；completed/error 后刷新任务与候选区，成功候选进入 `AI 候选确认`，失败可在流错误出现后直接重试并生成新的 `requestId/traceId`。
- 三才图会视觉资产批量闭环已补齐：页面支持批量发起图片理解与视觉资产处理任务，后端已提供批量创建、分页查询、取消、失败聚合和已完成结果保留语义；前后端与 workers 已补齐对应回归测试。
- 三才图会图片治理闭环已补齐：后端已支持图片上传、删除、当前图切换、同条目排序和全部图片 snapshot；Admin Web 已支持配图列表管理、原图上传、删除、当前图选择、缩略预览和放大浏览；Portal 分享和 Workers 静态展示/导出均使用分享或 payload 内的多图资源。
- Classics/System 权限过滤闭环已补齐：三类内容的 view/edit/export/share 权限在后端查询、详情、批量状态、分享和导出入口形成一致约束；Admin Web 三类 Classics 页面会按同一权限口径禁用分享、导出、批量公开和批量私有控件，并继续展示 `PERMISSION_DENIED` 批量失败项。
- Classics 清理入口已对接 Operations cleanup：应用层可发现并清理过期导出任务、过期分享链接和草稿分享链接；导出清理会将任务标记为过期，分享/草稿清理会将分享记录标记为过期，底层产物对象生命周期继续复用 Storage 自动 orphan 清理。
- Classics 删除内容与分享安全闭环已完成：三类内容删除后会将关联分享目标同步为 `CONTENT_DELETED`，按剩余可用目标重算 `classics_share_link.visibility_risk_status`，并且不再把已删除目标计入资源读取、公开列表和风险态。
- 结合需求文档、设计文档和代码现状，Classics 当前已实现“内容维护 + 候选确认 + 任务型 AI 入口 + 三才视觉资产流式候选展示 + 批量视觉资产治理 + 导出/批量分享治理 + 删除分享安全闭环 + 批量公开/私有分享访问 + 批量公开/私有状态修改 + 细粒度权限过滤 + 清理协作 + 跨内容批量候选治理”的主干闭环。

未完成：

- 无。

## Recent Validation

- 2026-07-07：`feat/classics-ai-streaming-candidates` 已合入最新 `origin/main`。
- 2026-07-07：`cd kuzhambu-servers && mvn -pl biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-infra -am spotless:apply && mvn spotless:check && mvn checkstyle:check && mvn -pl biz/ai/kuzhambu-ai-interface,biz/ai/kuzhambu-ai-application,biz/ai/kuzhambu-ai-infra,biz/classics/kuzhambu-classics-application -am test` 通过。
- 2026-07-07：`cd kuzhambu-apps && npm run format:check && npm run lint && npm --workspace kuzhambu-admin-web run build` 通过。
- 2026-07-07：`npm --workspace kuzhambu-admin-web run test -- --maxWorkers=1` 通过，45 个 test files / 153 tests 全绿；原始 `npm --workspace kuzhambu-admin-web run test` 在全量并发下出现非本任务用例 30 秒超时波动，失败用例单独复跑通过。

## Requirement Coverage Matrix

### 三才图会知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 14 个正式门类、卷首辅助内容、卷和条目三级浏览 | 已完成 | 需求、设计、schema、初始化数据已覆盖；门类/卷/条目查询服务、条目查询 API 与 Admin Web 三级浏览页面已实现 | 无 | Classics, Admin Web |
| 门类治理 CRUD | 已完成 | 门类列表、详情、保存和删除接口已实现；Admin Web 已支持新增、编辑和删除空门类；新增门类不输入 `priority`，由后端追加到末尾；删除有关联卷的门类由后端业务规则拦截 | 无 | Classics, Admin Web |
| 门类和卷稳定排序 | 已完成 | `priority` 规则、schema 约束、service 排序参数与稳定排序 API 已支持；Admin Web 已提供门类独立排序表单，保存时提交 orderedIds | 无 | Classics, Admin Web |
| 条目查看、创建、编辑、删除 | 已完成 | 条目查询、详情、保存、删除接口与运行时代码已到位；Admin Web 已完成列表进入详情、编辑保存和列表刷新闭环；三类内容删除后会同步关联分享目标为 `CONTENT_DELETED`，并按剩余可用目标重算分享风险态 | 无 | Classics, Admin Web, Portal Web |
| 编辑标题、门类、卷、原文、译文和标签 | 部分完成 | 条目编辑核心字段（标题/门类/卷/正文等）与保存链路已实现；Admin Web 已支持标题、原文、译文、摘要、公开状态编辑保存；后端通用标签新增、更新、删除已接入 Knowledge 协作语义；Sancai 条目详情抽屉已提供“编辑标签”入口，可定位到同抽屉标签治理面板完成新增、编辑、排序和移除；通用标签接口已补齐必要入参治理 | 门类/卷迁移不纳入本轮页面闭环 | Classics, Admin Web, Knowledge |
| 展示原文、译文、标签、配图和状态 | 已完成 | 条目详情、标签列表、配图列表均已提供接口；Sancai 条目详情响应已聚合返回 `tags`；Admin Web 已在详情上下文展示原文、译文、标签、条目状态、当前使用图片预览/下载入口、视觉资产历史列表、当前版本摘要和原图/生成图预览下载入口；标签新增、编辑、排序、移除后会刷新详情聚合标签 | 无 | Classics, Admin Web |
| 多张配图、缩略预览、放大浏览 | 已完成 | 图片保存、列表、类型、Storage 对象引用、业务上传、业务读取、删除、当前图切换和同条目排序已落地；Admin Web 已提供配图列表管理、缩略预览、放大浏览抽屉、下载和当前图选择；分享快照与 Portal 分享详情保留多图并按 `priority ASC` 展示缩略图切换主图；Workers 静态展示按多图稳定渲染并标记当前图 | 无 | Classics, Storage, Admin Web, Portal Web, Worker |
| 区分原始配图和视觉资产生成图 | 已完成 | `image_type`、图片模型、资产模型与保存入口已实现；Admin Web 已区分展示视觉资产原图与生成图，并分别提供预览/下载入口；`image_gen` 结果会新建 visual asset version 并绑定正式 Storage 对象 | 无 | Classics, Admin Web |
| 从条目上下文进入视觉资产工作流 | 已完成 | 资产草稿、图片、展示任务接口与服务可用；Admin Web 已在条目详情弹窗中接入视觉资产历史列表、当前版本切换、权重与描述字段维护、原图/生成图预览下载，以及 `image_analysis / fusion / visual / image_gen` 的页面内任务入口、任务状态轮询、流式过程展示、失败提示与重试入口；后端已接通候选读取/应用/拒绝和 `image_gen` 候选应用后的版本化落库与页面展示 | 无 | Classics, Admin Web, AI |
| 原图上传、删除和预览 | 已完成 | 原图业务上传、列表、Storage owner/reference 绑定、业务读取、inline/attachment 响应头、Admin Web 上传/预览/下载/删除闭环已实现；删除当前图后按 `priority ASC` 自动补位，删除最后一张图后展示空状态 | 无 | Classics, Storage, Admin Web |
| 图片理解、信息融合、权重调节、视觉描述、AI 生图入口 | 已完成 | 视觉资产字段建模完成，三才视觉资产已接入图片理解/视觉描述/信息融合/AI 生图的任务与候选确认链路，`textWeight`、`imageWeight`、`imageAnalysisMarkdown`、`fusionDescription`、`visualDescription`、`generationParamsJson` 已可保存并按字段边界写回；`image_analysis / image_gen` 已支持流式过程卡片和失败重试；`image_gen` 候选应用已接通 `artifact -> Storage -> 新 version -> 页面预览/下载` 闭环，失败提示与重试入口已在页面内收口 | 无 | Classics, AI |
| 视觉资产历史和当前使用版本选择 | 已完成 | 视觉资产持久化、列表查询、当前版本切换服务、Admin API 和 Admin Web 已接通；条目详情内可查看历史版本、切换当前使用版本并保存基础字段 | 无 | Classics, Admin Web |
| 多选条目批量视觉资产处理 | 已完成 | Admin Web 已支持多选条目批量发起图片理解与视觉资产处理任务，并展示成功数、失败数、失败原因、运行中状态和取消入口；后端已提供批量创建、分页、取消、失败聚合和已完成结果保留语义 | 无 | Classics, AI |
| 摘要、标签和问答对内联维护 | 部分完成 | 主表摘要、通用内容 tag/qa CRUD 已实现；手工标签绑定、删除标签同步和 AI 标签确认回写已接通 Knowledge 内容引用闭环；三才图会、王圻文档、明代习俗页面已接入 `summary / tags / qa` 内联维护入口，含 AI 候选应用后的刷新联动 | 候选预览与更多确认入口仍未完整对齐 | Classics, Knowledge, AI |
| 分页、筛选、当前卷搜索和多选 | 部分完成 | 条目分页、筛选、卷过滤、排序查询已实现；Admin Web 已支持门类/卷筛选、关键词搜索、生命周期筛选、分页、pageSize 切换、批量视觉资产任务、批量分享和当前页多选批量公开/私有，并展示批量操作成功数、失败数和失败原因；权限不足时相关分享、导出、批量公开/私有控件已禁用 | 更多跨页多选策略未完成 | Classics, Admin Web |
| 生命周期：草稿、发布、归档、恢复 | 已完成 | 状态枚举、合法流转校验、Admin 生命周期变更接口、版本快照、搜索同步和 Admin Web 单条目发布/归档/恢复控件已闭环；发布、归档、恢复成功后会刷新列表、详情和版本历史，权限不足时相关控件禁用 | 无 | Classics, Admin Web |
| 公开和私有可见性管理 | 已完成 | 条目可见性字段、变更能力已落地；Admin Web 已支持单条目公开状态编辑保存和当前页多选批量公开/私有；Java interface 已通过统一入口分发到三类内容应用服务，并返回批量结果和失败明细；后端按三类内容 edit 权限过滤批量状态修改，前端按同一口径禁用控件 | 无；本轮不重算历史分享快照，`classics_share_target.content_visibility_snapshot` 继续表示创建分享时的内容可见性快照 | Classics, Admin Web, System |
| 版本历史、版本对比和历史恢复 | 已完成 | 后端已暴露三才图会条目版本列表、版本详情和历史恢复端点，并校验版本归属；恢复采用追加式版本语义，目标卷内排序值使用当前 `max(priority) + 1`；Admin Web 已提供版本历史列表、当前/历史字段对比、恢复确认、恢复后详情刷新和成功提示 | 无 | Classics, Admin Web |
| CSV、JSON、HTML 设定集导出 | 已完成 | 导出任务表、异步接入决策、通用创建能力、worker 渲染产物落库与下载闭环已打通，前端可查看任务状态与下载成功产物；Sancai JSON/ZIP 保留 `items[].images[]`，HTML 输出图片元数据，CSV 仍按内容条目一行输出；底层产物对象生命周期已复用 Storage 自动 orphan 清理 | 无 | Classics, Worker, Storage |
| HTML 视觉资产设定集导出 | 已完成 | 视觉资产字段与导出任务结构已覆盖，worker 渲染产物已写入 Storage，任务状态与下载入口已闭环；底层产物对象生命周期已复用 Storage 自动 orphan 清理 | 无 | Classics, Worker, Storage |
| 导出记录查看、下载、删除和过期 | 部分完成 | 导出任务列表、下载链接、状态与过期时间透传，后端支持过期任务 404、前端展示“已过期”并禁用下载，产物已落库存储并可读取；过期导出任务已可被 Operations cleanup 发现并标记为过期；底层产物对象生命周期已复用 Storage 自动 orphan 清理 | 用户主动删除导出记录和更细的批量管理策略未形成闭环 | Classics, System, Storage |
| 静态展示内容生成 | 部分完成 | 展示任务记录、策略与 worker 产物落库、状态回写和下载能力已完成 | 列表搜索、筛选与回源流程边界未完整 | Classics, Worker, Storage |
| 静态展示包含私有内容确认 | 已完成 | 风险状态字段与展示任务模型已具备，静态展示所需的风险信息可直接传递 | 无 | Classics |

### 王圻文档知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 文档查看、创建、编辑、删除 | 已完成 | 文档分页、详情、时间线、保存、删除接口与服务已实现；Admin Web 已支持列表进入详情、新增、编辑、删除和列表刷新闭环；dev.env Admin API 冒烟已通过 | 无 | Classics, Admin Web |
| 原始文件关联和替换 | 已完成 | `storage_object_id` 已设计并入参到位；Wangqi 业务接口已支持原始文件上传、元数据查询和资源流读取；上传会绑定 Storage 归属和引用，替换会追加版本；删除文档会解绑 Storage owner，并将当前对象回落为未引用状态 | 无 | Classics, Storage, Admin Web |
| 全文阅读和内容安全展示 | 已完成 | 文本内容/时间字段及阅读接口已实现；Admin Web 详情编辑页已提供正文预览，并复用统一富文本清洗控件展示 Markdown/HTML 内容 | 无 | Classics, Admin Web |
| 摘要、标签和问答对展示维护 | 部分完成 | 文档摘要、通用标签/问答对模型与 API 已实现；Admin Web 编辑页已接入标签治理面板和问答对治理面板；通用标签写路径已接通 Knowledge 协作与内容引用同步 | 问答对版本化确认链路与更多治理约束未补齐 | Classics, Knowledge |
| AI 摘要、标签、问答对生成入口和候选确认 | 部分完成 | Admin Web 编辑页已接入 AI 候选确认面板，可读取 `PENDING` 候选、编辑 payload、应用或拒绝候选；后端已接通候选应用后的正文摘要/标签/问答对写回、版本追加与 AI 候选状态回写 | AI 触发生成入口、流式过程展示和候选来源侧任务治理未实现 | Classics, AI |
| 文档搜索和时间线浏览 | 已完成 | 文档搜索与时间线接口已实现；时间线支持关键词、可见性和排序入参；Admin Web 已提供列表搜索、筛选和时间线查询闭环 | 无 | Classics, Admin Web |
| 列表标题、标签预览、摘要预览和时间信息 | 已完成 | 详情字段和查询可返回标题、摘要、时间、可见性、版本状态等信息；Admin Web 已展示列表摘要和时间信息 | 无 | Classics, Admin Web |
| 批量修改公开或私有状态 | 已完成 | 可见性变更能力已存在；应用层已补齐批量结果和失败原因模型；统一后端入口已分发到 Wangqi 应用服务，Admin Web 已支持当前页选中文档批量公开/私有并展示失败明细；后端权限过滤与前端控件禁用已按 `classics:wangqi:edit` 对齐 | 无 | Classics, Admin Web, System |
| 单文档问答入口 | 部分完成 | 问答入口需求已记录 | Discovery/AI 回答调用点及结果落库未实现 | Classics, Discovery, AI |
| 版本历史、版本对比和历史恢复 | 已完成 | Wangqi 后端已暴露版本列表、版本详情和历史恢复端点，并校验版本归属；恢复采用追加式版本语义；Admin Web 已提供版本历史列表、当前/历史字段对比和恢复动作 | 无 | Classics, Admin Web |
| 筛选结果或选中文档导出 | 已完成 | 通用导出任务创建能力已实现，字段模型完整；Wangqi 导出快照 payload 已接入 Render Worker，CSV/JSON/HTML/ZIP 产物可写入 Storage 并下载 | 无 | Classics, Worker, Storage |

### 明代习俗知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 习俗查看、创建、编辑、删除 | 已完成 | 条目分页、详情、保存、关键词新增、关键词云、删除接口已落地；Admin Web 已支持列表进入详情、新增、编辑保存、删除和列表刷新闭环 | 无 | Classics, Admin Web |
| 概述、正文、分类、关键词、标签、原文摘录展示 | 已完成 | 核心字段与查询服务到位；Admin Web 已展示列表摘要、分类、关键词、可见性、时间和详情正文，并提供富文本预览 | 无 | Classics, Admin Web |
| 列表浏览和关键词搜索 | 已完成 | 列表、关键词/标签/可见性筛选与分页已实现；Admin Web 已支持标题/分类/可见性/时间筛选、分页和关键词云点击筛选 | 无 | Classics, Admin Web |
| 详情聚合查询 | 已完成 | 详情查询和关键词查询已实现；Admin Web 已组合详情、关键词和正文预览信息 | 无 | Classics, Admin Web |
| Markdown 安全渲染 | 已完成 | `content_format` 与内容字段模型可追踪；Admin Web 已封装富文本展示控件，使用 Markdown/HTML 渲染与内容清洗策略展示正文 | 无 | Classics, Admin Web |
| 标签云筛选 | 部分完成 | 通用标签模型、关键词云接口与状态筛选已实现；关键词云响应固定为 `List<KeywordCloudItem>`，字段为 `keyword` 和 `count` | 基于统一标签的真实标签云聚合、标签云权限过滤与输出限缩未实现 | Classics, Knowledge, System |
| 批量修改公开或私有状态 | 已完成 | 可见性枚举与变更能力已具备；应用层已补齐批量结果和失败原因模型；统一后端入口已分发到 Ming Customs 应用服务，Admin Web 已支持当前页选中习俗批量公开/私有并展示失败明细；后端权限过滤与前端控件禁用已按 `classics:mingcustoms:edit` 对齐 | 无 | Classics, Admin Web, System |
| 摘要、标签和问答对维护 | 部分完成 | 通用内容 tag/qa API 已可复用，摘要字段已覆盖；Admin Web 编辑页已接入 AI 候选确认面板、标签治理面板和问答对治理面板；通用标签写路径已接通 Knowledge 协作与内容引用同步 | 版本历史链路尚未覆盖明代习俗，问答对版本化确认仍未补齐 | Classics, Knowledge |
| 版本历史、版本对比和历史恢复 | 已完成 | 明代习俗已补齐版本列表/版本详情/历史恢复接口、版本归属校验与恢复追加式版本生成，前端已接入版本历史面板、字段级对比与恢复确认，恢复后支持列表与详情刷新 | 无 | Classics, Admin Web |
| 分类、标签、筛选结果或选中条目导出 | 已完成 | 任务创建与导出参数已支持；Ming Customs 导出快照 payload 已接入 Render Worker，CSV/JSON/HTML/ZIP 产物可写入 Storage 并下载 | 无 | Classics, Worker, Storage |

### 跨知识库分享

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 选择三类内容生成分享链接 | 已完成 | 分享链接创建、状态变更与目标写入服务链路已实现，目标含内容快照字段；Wangqi/MingCustoms/Sancai 页面已提供单内容分享入口和当前页多选批量分享入口，Portal 分享详情可展示固化快照和资源预览 | 无 | Classics, Admin Web, Portal Web |
| 分享后台管理（列表、详情、状态、访问记录） | 已完成 | 分享分页列表/详情、状态更新、访问记录分页查询与 Admin Web 页面闭环已完成，支持目标快照与状态来源校验；批量创建出的 `ACTIVE/EXPIRED/REVOKED` 分享记录仍可复用现有状态更新行为 | 无 | Classics, Admin Web, System |
| 单链接多个内容 | 部分完成 | 目标关系支持一对多，创建流程可写入多个 target；Portal 详情按 target 展示多内容快照和资源对象 | target 重复去重与回写策略未实现 | Classics, Portal Web |
| 批量创建分享链接 | 已完成 | 后端已提供批量分享创建接口，每个 target 创建独立 share link 和 share target；返回成功数、失败数和每条失败原因；Admin Web 三类内容页已接入当前页多选批量分享并展示聚合结果；Portal Web 复用现有读取状态语义 | 无 | Classics, Admin Web, Portal Web |
| 分享链接公开或私有 | 部分完成 | 可见性字段与管理接口（创建/状态变更）已实现；批量分享创建沿用 `privateContentConfirmed` 私有内容确认语义；Portal 公开分享访问无需登录；私有分享已按创建者或 `classics:sharing:view` 管理权限开放详情和资源读取，未登录时由 Portal Web 展示登录引导；过期、撤销、不存在统一按不可访问处理 | 管理侧恢复策略未实现 | Classics, System, Portal Web |
| 过期时间、撤销和恢复 | 部分完成 | 过期时间与状态更新字段已实现；过期分享和草稿分享已可被 Operations cleanup 发现并标记为过期 | 恢复/恢复到位自动流未实现 | Classics |
| 只读访问页 | 已完成 | Portal 已提供公开分享列表、详情和分享资源读取端点；Portal Web 已提供首页、分享列表和分享详情路由，展示固化快照、Wangqi 原始文件资源和 Sancai 图片资源 | 无 | Classics, System, Portal Web |
| 访问统计 | 部分完成 | 访问记录实体与应用服务接口（写入/分页查询）已实现；分享资源读取成功会写入访问记录 | 分享详情浏览统计和对外统计 API 未接通 | Classics |
| 分享完整内容快照 | 已完成 | 分享创建时先确保正式内容版本，再将 `classics_content_version.snapshot_json` 固化到 `classics_share_target.content_snapshot_json`；target 记录 `content_version_id/content_version_no`；三类正式版本快照 schema 已沉淀到 `docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`；Sancai 快照包含全部图片资源 ID，使用 `currentUsed` 标识当前图，Portal 响应层动态补资源对象 | 无 | Classics |
| 私有内容分享确认文案 | 已完成 | 分享创建与状态模型已支持风险状态表达，确认文案由前端按风险状态渲染 | 无 | Classics |
| 目标被删除后占位展示 | 已完成 | 三类内容删除会触发分享目标状态同步和风险态重算；Admin Web 分享详情保留目标行、标题快照、内容类型、内容 ID 和“内容已删除”状态；Portal Web 分享详情保留原顺序标题占位并隐藏正文、图片、文件和预览/下载控件；Portal 公开分享列表防御过滤 `CONTENT_DELETED` 目标 | 无 | Classics, Admin Web, Portal Web |

### 通用内容和跨域能力

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 权限不足用户看不到私有内容 | 已完成 | 可见性字段与规则已设计；三类内容查询、详情、导出、分享与批量状态入口已按 System 权限上下文过滤或拒绝；Admin Web 控件状态已按后端权限口径对齐 | 无 | Classics, System |
| 批量状态修改成功数、失败数和失败原因 | 已完成 | 通用批量操作结果模型已落地，批量分享与批量公开/私有状态修改均已返回成功数、失败数和失败原因；三类内容应用层批量可见性修改已具备同一结果语义，Java interface 与 Admin Web 当前页多选入口已完成；权限不足项以 `PERMISSION_DENIED` 进入失败明细并可在前端展示 | 无 | Classics, Admin Web |
| AI 生成候选预览、修改、确认和放弃 | 已完成 | Classics 已接入 AI 候选列表读取、payload 编辑、应用和拒绝闭环；候选应用支持 `translate / summary / tags / qa / image_analysis` 写回正式内容，并追加版本和回写 AI 候选状态；三才视觉资产支持按 `objectId` 的候选限定与刷新联动，且已补齐 `fusion / visual / image_gen` 候选应用规则、流式过程展示、失败提示和重试入口；跨内容批量候选治理已补齐（含批量应用、批量拒绝、失败明细） | 无 | Classics, AI |
| Knowledge 标签治理 | 外部依赖 | Classics 已保存标签主事实、标签名快照，并通过 Knowledge 协作语义解析统一标签、自动创建标签和同步内容引用投影 | 标签合并、同义词治理、分类运营规则仍不属于 Classics | Knowledge |
| Storage 对象管理 | 外部依赖 | Classics 只保存 `storage_object_id`，但业务域已负责 Wangqi 原始文件和 Sancai 图片的上传入口、归属校验、读取 URL 和 Portal 分享资源访问控制 | 底层对象生命周期、物理删除、清理策略和通用对象管理仍由 Storage 实现 | Classics, Storage |
| Discovery 搜索和问答 | 外部依赖 | Classics 可提供内容上下文和入口 | 索引、召回、问答生成由 Discovery/AI 实现 | Discovery, AI |
| System 审计 | 外部依赖 | 业务表不保存审计字段 | 操作者和关键操作日志由 System 审计系统实现 | System |
| Worker 异步任务执行 | 外部依赖 | Classics 记录导出和静态展示任务 | 产物生成、失败重试和任务调度由 Worker 实现 | Worker, Storage |

## Follow-up Backlog

### B1 分享完整内容快照设计补齐

状态：已完成。

已补充：

- 分享创建时由后端读取 `Versionable` 主内容，调用正式版本创建/复用流程。
- `ClassicsShareTarget` 写入 `titleSnapshot`、`contentVisibilitySnapshot`、`contentVersionId`、`contentVersionNo` 和 `contentSnapshotJson`。
- `contentSnapshotJson` 直接来自绑定的 `classics_content_version.snapshot_json`。
- Portal API 使用后端生成的 `shareToken` 查询，不暴露 `token_hash`；公开访问失败原因在 Portal 侧统一为 404。
- 三类正式版本快照字段固定在 `docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`。

### B2 AI 候选结果协作设计

状态：已完成。

已补充：

- 跨内容候选治理已补齐，三类页面接入统一批量候选治理抽屉，支持批量应用、批量拒绝与按候选维度返回失败明细。

### B3 批量操作结果模型

状态：已完成。

已补充：

- Java application/interface 已新增通用批量操作结果模型，字段覆盖成功数、失败数、成功明细和失败明细。
- 批量分享创建已返回 `successCount/failureCount/successes/failures`，Admin Web 已在三类内容页展示聚合结果和失败原因。
- 三类内容应用层批量可见性修改已具备同一结果语义，Java interface 统一入口和三类 Admin Web 当前页多选入口已接通。
- 本轮不重算历史分享快照；`classics_share_target.content_visibility_snapshot` 继续表示创建分享时的内容可见性快照。

需要补充：

- 后续如新增非 AI 批处理场景，可复用当前批量结果模型。

### B4 权限接入

状态：已完成。

已补充：

- 三类 Classics 内容 view/edit/export/share 权限过滤。
- 批量公开/私有和批量分享的 `PERMISSION_DENIED` 失败项。
- Admin Web 三类内容页面的分享、导出、批量公开和批量私有控件禁用。

### B5 导出和静态展示 Worker 对接

状态：已完成。

已补充：

- 导出任务状态机和 Worker 消费协议。 [已完成]
- 静态展示任务状态机和 Worker 消费协议。 [已完成]
- 产物 Storage 对象写入。 [已完成]
- 产物对象生命周期已复用 Storage 自动 orphan 清理。 [已完成]

### B6 安全渲染和内容展示策略

状态：已完成。

已补充：

- 明代习俗 Admin Web 已提供独立富文本展示控件，基于 Markdown/HTML 渲染和清洗策略展示正文。
- 王圻文档 Admin Web 已复用独立富文本展示控件，基于 Markdown/HTML 渲染和清洗策略展示正文预览。
- Portal Web 分享详情已从固化快照渲染只读内容，并对 Wangqi 原始文件与 Sancai 图片使用分享资源读取接口展示；已删除分享目标只展示标题快照和“内容已删除”占位，不再渲染正文、图片、文件或资源操作。
