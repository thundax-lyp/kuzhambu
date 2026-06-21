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
- 关键架构决策已确认：不做读写分离、Repository 统一命名、业务表不放审计字段、状态使用 `varchar`、`priority` 表内唯一且不参与 KEY、三才图会新增条目使用数据库自增主键。
- 导出和静态展示第一版只记录任务，不同步生成产物。
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
- 王圻文档初始化数据、后端接口契约、前端服务契约、页面单测、Playwright 页面闭环和 dev.env Admin API 冒烟均已通过；删除 Wangqi 文档会删除版本历史、移除 Storage reference，并释放当前原始文件对象引用状态。
- Storage 文件读取和预览最小闭环已接入 Classics：Admin Storage/Wangqi/Sancai 使用鉴权资源 URL，Wangqi 原始文件和 Sancai 图片通过业务域接口读取，Portal 分享详情动态装配资源对象，分享资源读取会校验分享链接和快照内资源 ID。

未完成：

- 复杂业务闭环未接通：权限过滤、AI/Worker 跨域链路、批量操作和部分确认流程仍需联调。
- 三才图会仍不包含标签治理、多选批量、复杂视觉资产生产、导出和静态展示能力。

## Requirement Coverage Matrix

### 三才图会知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 14 个正式门类、卷首辅助内容、卷和条目三级浏览 | 已完成 | 需求、设计、schema、初始化数据已覆盖；门类/卷/条目查询服务、条目查询 API 与 Admin Web 三级浏览页面已实现 | 无 | Classics, Admin Web |
| 门类治理 CRUD | 已完成 | 门类列表、详情、保存和删除接口已实现；Admin Web 已支持新增、编辑和删除空门类；新增门类不输入 `priority`，由后端追加到末尾；删除有关联卷的门类由后端业务规则拦截 | 无 | Classics, Admin Web |
| 门类和卷稳定排序 | 已完成 | `priority` 规则、schema 约束、service 排序参数与稳定排序 API 已支持；Admin Web 已提供门类独立排序表单，保存时提交 orderedIds | 无 | Classics, Admin Web |
| 条目查看、创建、编辑、删除 | 部分完成 | 条目查询、详情、保存、删除接口与运行时代码已到位；Admin Web 已完成列表进入详情、编辑保存和列表刷新闭环 | 删除后分享目标状态同步、风险态重算未完成；删除不纳入本轮 Admin Web 页面闭环 | Classics, Admin Web |
| 编辑标题、门类、卷、原文、译文和标签 | 部分完成 | 条目编辑核心字段（标题/门类/卷/正文等）与保存链路已实现；Admin Web 已支持标题、原文、译文、摘要、公开状态编辑保存 | 标签协作链路（通用标签联动）和入参校验规则待补齐；门类/卷迁移不纳入本轮页面闭环 | Classics, Admin Web, Knowledge |
| 展示原文、译文、标签、配图和状态 | 部分完成 | 条目详情、标签列表、配图列表均已提供独立接口；Admin Web 已展示条目列表状态、详情编辑核心文本字段和当前使用图片预览/下载入口 | Admin Web 尚未聚合标签和复杂视觉资产展示 | Classics, Admin Web |
| 多张配图、缩略预览、放大浏览 | 部分完成 | 图片保存、列表、类型、Storage 对象引用、业务上传、业务读取和 Admin Web 当前图预览/下载链路已落地；分享快照只带 `currentUsed=true` 图片并按 `priority ASC` 展示 | 缩略图生成、多图放大浏览交互和视觉资产图切换展示未闭环 | Classics, Storage, Admin Web |
| 区分原始配图和视觉资产生成图 | 部分完成 | `image_type`、图片模型、资产模型与保存入口已实现 | 视觉资产生成结果识别、AI 生图与切换展示入口未闭环 | Classics |
| 从条目上下文进入视觉资产工作流 | 部分完成 | 资产草稿、图片、展示任务接口与服务可用 | AI 生成与调用链路未形成闭环 | Classics, AI |
| 原图上传、删除和预览 | 部分完成 | 原图业务上传、列表、Storage owner/reference 绑定、业务读取、inline/attachment 响应头和 Admin Web 当前图预览/下载已实现 | 删除接口虽有应用层能力但未形成 Admin Web 删除闭环；批量图片管理未完成 | Classics, Storage, Admin Web |
| 图片理解、信息融合、权重调节、视觉描述、AI 生图入口 | 部分完成 | 视觉资产相关字段建模完成 | AI 执行、候选预览、确认应用和失败处理未实现 | Classics, AI |
| 视觉资产历史和当前使用版本选择 | 部分完成 | 视觉资产持久化与当前版本切换服务已实现，列表查询可用 | 历史列表/切换对外接口和切换策略未打通 | Classics |
| 多选条目批量视觉资产处理 | 部分完成 | 批处理需求与模型约束在需求和数据模型中存在 | 批量执行、失败结果模型、取消回滚策略未实现 | Classics, AI |
| 摘要、标签和问答对内联维护 | 部分完成 | 主表摘要、通用内容 tag/qa CRUD 已实现 | 通用内容的写入口径、候选确认与版本落库接口未完整对齐 | Classics, Knowledge, AI |
| 分页、筛选、当前卷搜索和多选 | 部分完成 | 条目分页、筛选、卷过滤、排序查询已实现；Admin Web 已支持门类/卷筛选、关键词搜索、生命周期筛选、分页和 pageSize 切换，并用 Playwright 固定请求体 | 多选结果模型、批量操作和返回聚合未实现 | Classics, Admin Web |
| 生命周期：草稿、发布、归档、恢复 | 部分完成 | 状态枚举与变更能力在服务层已有实现；Admin Web 已支持按生命周期筛选条目 | 管理接口未完整暴露、恢复策略与版本链路未闭环；本轮页面未提供生命周期编辑 | Classics, Admin Web |
| 公开和私有可见性管理 | 部分完成 | 条目可见性字段、变更能力已落地；Admin Web 已支持单条目公开状态编辑保存 | 权限过滤调用点、批量修改失败语义未实现 | Classics, Admin Web, System |
| 版本历史、版本对比和历史恢复 | 已完成 | 后端已暴露三才图会条目版本列表、版本详情和历史恢复端点，并校验版本归属；恢复采用追加式版本语义，目标卷内排序值使用当前 `max(priority) + 1`；Admin Web 已提供版本历史列表、当前/历史字段对比、恢复确认、恢复后详情刷新和成功提示 | 无 | Classics, Admin Web |
| CSV、JSON、HTML 设定集导出 | 部分完成 | 导出任务表、异步接入决策和通用导出创建能力已到位 | 仅记录任务；产物生成与下载未完成 | Classics, Worker, Storage |
| HTML 视觉资产设定集导出 | 部分完成 | 视觉资产字段与导出任务结构已覆盖 | 仅记录任务；产物生成与下载未完成 | Classics, Worker, Storage |
| 导出记录查看、下载、删除和过期 | 部分完成 | 导出任务表支持状态、过期和 storage 关联字段 | 列表/下载/权限过滤 API 与清理策略未形成闭环 | Classics, System, Storage |
| 静态展示内容生成 | 部分完成 | 展示任务记录、静态展示任务策略已落实 | 静态展示产物生成、模板、列表搜索、筛选与回源流程接口未实现 | Classics, Worker, Storage |
| 静态展示包含私有内容确认 | 已完成 | 风险状态字段与展示任务模型已具备，静态展示所需的风险信息可直接传递 | 无 | Classics |

### 王圻文档知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 文档查看、创建、编辑、删除 | 已完成 | 文档分页、详情、时间线、保存、删除接口与服务已实现；Admin Web 已支持列表进入详情、新增、编辑、删除和列表刷新闭环；dev.env Admin API 冒烟已通过 | 无 | Classics, Admin Web |
| 原始文件关联和替换 | 已完成 | `storage_object_id` 已设计并入参到位；Wangqi 业务接口已支持原始文件上传、元数据查询和资源流读取；上传会绑定 Storage 归属和引用，替换会追加版本；删除文档会移除引用并释放当前对象引用状态 | 无 | Classics, Storage, Admin Web |
| 全文阅读和内容安全展示 | 已完成 | 文本内容/时间字段及阅读接口已实现；Admin Web 详情编辑页已提供正文预览，并复用统一富文本清洗控件展示 Markdown/HTML 内容 | 无 | Classics, Admin Web |
| 摘要、标签和问答对展示维护 | 部分完成 | 文档摘要、通用标签/问答对模型与 API 已实现 | 文档内联维护调用链与确认/版本化链路未接通 | Classics, Knowledge |
| AI 摘要、标签、问答对生成入口和候选确认 | 部分完成 | 通用内容结构可承载候选确认后的结果 | AI 触发入口、候选暂存、修改/确认/放弃未实现 | Classics, AI |
| 文档搜索和时间线浏览 | 已完成 | 文档搜索与时间线接口已实现；时间线支持关键词、可见性和排序入参；Admin Web 已提供列表搜索、筛选和时间线查询闭环 | 无 | Classics, Admin Web |
| 列表标题、标签预览、摘要预览和时间信息 | 已完成 | 详情字段和查询可返回标题、摘要、时间、可见性、版本状态等信息；Admin Web 已展示列表摘要和时间信息 | 无 | Classics, Admin Web |
| 批量修改公开或私有状态 | 部分完成 | 可见性变更能力已存在 | 批量结果、失败原因和权限过滤未实现 | Classics, System |
| 单文档问答入口 | 部分完成 | 问答入口需求已记录 | Discovery/AI 回答调用点及结果落库未实现 | Classics, Discovery, AI |
| 版本历史、版本对比和历史恢复 | 已完成 | Wangqi 后端已暴露版本列表、版本详情和历史恢复端点，并校验版本归属；恢复采用追加式版本语义；Admin Web 已提供版本历史列表、当前/历史字段对比和恢复动作 | 无 | Classics, Admin Web |
| 筛选结果或选中文档导出 | 部分完成 | 通用导出任务创建能力已实现，字段模型完整 | 仅记录任务，产物生成与下载未完成 | Classics, Worker, Storage |

### 明代习俗知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 习俗查看、创建、编辑、删除 | 已完成 | 条目分页、详情、保存、关键词新增、关键词云、删除接口已落地；Admin Web 已支持列表进入详情、新增、编辑保存、删除和列表刷新闭环 | 无 | Classics, Admin Web |
| 概述、正文、分类、关键词、标签、原文摘录展示 | 已完成 | 核心字段与查询服务到位；Admin Web 已展示列表摘要、分类、关键词、可见性、时间和详情正文，并提供富文本预览 | 无 | Classics, Admin Web |
| 列表浏览和关键词搜索 | 已完成 | 列表、关键词/标签/可见性筛选与分页已实现；Admin Web 已支持标题/分类/可见性/时间筛选、分页和关键词云点击筛选 | 无 | Classics, Admin Web |
| 详情聚合查询 | 已完成 | 详情查询和关键词查询已实现；Admin Web 已组合详情、关键词和正文预览信息 | 无 | Classics, Admin Web |
| Markdown 安全渲染 | 已完成 | `content_format` 与内容字段模型可追踪；Admin Web 已封装富文本展示控件，使用 Markdown/HTML 渲染与内容清洗策略展示正文 | 无 | Classics, Admin Web |
| 标签云筛选 | 部分完成 | 通用标签模型、关键词云接口与状态筛选已实现；关键词云响应固定为 `List<KeywordCloudItem>`，字段为 `keyword` 和 `count` | 标签云权限过滤与输出限缩未实现 | Classics, Knowledge, System |
| 批量修改公开或私有状态 | 部分完成 | 可见性枚举与变更能力已具备 | 批量结果、失败原因和权限过滤未实现 | Classics, System |
| 摘要、标签和问答对维护 | 部分完成 | 通用内容 tag/qa API 已可复用，摘要字段已覆盖 | 内联维护链路与版本确认未完整接入 | Classics, Knowledge |
| 版本历史、版本对比和历史恢复 | 部分完成 | 通用版本模型已建 | 版本对比接口与恢复生成策略未实现 | Classics |
| 分类、标签、筛选结果或选中条目导出 | 部分完成 | 任务创建与导出参数已支持 | 仅记录任务，产物生成与下载未完成 | Classics, Worker, Storage |

### 跨知识库分享

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 选择三类内容生成分享链接 | 部分完成 | 分享链接创建、状态变更与目标写入服务链路已实现，目标含内容快照字段；Portal 分享详情可展示固化快照和资源预览 | 内容选择 UI、冲突校验和创建幂等性未完成 | Classics, Portal Web |
| 单链接多个内容 | 部分完成 | 目标关系支持一对多，创建流程可写入多个 target；Portal 详情按 target 展示多内容快照和资源对象 | target 重复去重与回写策略未实现 | Classics, Portal Web |
| 批量创建分享链接 | 部分完成 | 分享模型与接口需求已覆盖批量创建入口 | 批量结果模型、失败原因与回滚策略未实现 | Classics |
| 分享链接公开或私有 | 部分完成 | 可见性字段与管理接口（创建/状态变更）已实现；Portal 公开分享访问无需登录，过期、撤销、不存在统一按 404 处理 | 私有分享访问分支、管理侧恢复策略未实现 | Classics, System |
| 过期时间、撤销和恢复 | 部分完成 | 过期时间与状态更新字段已实现 | 过期清理、恢复/恢复到位自动流未实现 | Classics |
| 只读访问页 | 已完成 | Portal 已提供公开分享列表、详情和分享资源读取端点；Portal Web 已提供首页、分享列表和分享详情路由，展示固化快照、Wangqi 原始文件资源和 Sancai 图片资源 | 无 | Classics, System, Portal Web |
| 访问统计 | 部分完成 | 访问记录实体与应用服务接口（写入/分页查询）已实现；分享资源读取成功会写入访问记录 | 分享详情浏览统计和对外统计 API 未接通 | Classics |
| 分享完整内容快照 | 已完成 | 分享创建时先确保正式内容版本，再将 `classics_content_version.snapshot_json` 固化到 `classics_share_target.content_snapshot_json`；target 记录 `content_version_id/content_version_no`；三类正式版本快照 schema 已沉淀到 `docs/20-interfaces/CLASSICS-CONTENT-VERSION-SNAPSHOT-INTERFACE.md`；Sancai 快照包含当前使用图片资源 ID，Portal 响应层动态补资源对象 | 无 | Classics |
| 私有内容分享确认文案 | 已完成 | 分享创建与状态模型已支持风险状态表达，确认文案由前端按风险状态渲染 | 无 | Classics |
| 目标被删除后占位展示 | 已完成 | 目标快照和目标状态可持久化，查询接口可返回状态供前端按状态展示 | 无 | Classics |

### 通用内容和跨域能力

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 权限不足用户看不到私有内容 | 部分完成 | 可见性字段与规则已设计 | 权限策略来自 System，调用点和过滤实现未完成 | Classics, System |
| 批量状态修改成功数、失败数和失败原因 | 未完成 | 需求已记录 | 批处理结果模型和接口响应未形成可执行任务粒度 | Classics |
| AI 生成候选预览、修改、确认和放弃 | 未完成 | 需求已记录 | 候选结果承载结构、接口和 AI 协作协议未设计 | Classics, AI |
| Knowledge 标签治理 | 外部依赖 | Classics 设计了标签引用和标签名快照 | 标签合并、同义词、治理规则不属于 Classics | Knowledge |
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

状态：未完成。

需要补充：

- 定义 Classics 到 AI 的触发入口。
- 定义候选结果暂存、预览、修改、确认和放弃模型。
- 明确确认后如何写入摘要、标签、问答对和版本历史。

### B3 批量操作结果模型

状态：未完成。

需要补充：

- 批量公开私有修改结果。
- 批量视觉资产处理结果。
- 批量分享创建结果。
- 统一成功数、失败数、失败原因和取消保留已完成结果语义。

### B4 权限接入

状态：未完成。

需要补充：

- 权限不足时的错误语义和过滤策略。

### B5 导出和静态展示 Worker 对接

状态：未完成。

需要补充：

- 导出任务状态机和 Worker 消费协议。
- 静态展示任务状态机和 Worker 消费协议。
- 产物 Storage 对象写入和过期清理策略。

### B6 安全渲染和内容展示策略

状态：已完成。

已补充：

- 明代习俗 Admin Web 已提供独立富文本展示控件，基于 Markdown/HTML 渲染和清洗策略展示正文。
- 王圻文档 Admin Web 已复用独立富文本展示控件，基于 Markdown/HTML 渲染和清洗策略展示正文预览。
- Portal Web 分享详情已从固化快照渲染只读内容，并对 Wangqi 原始文件与 Sancai 图片使用分享资源读取接口展示。
