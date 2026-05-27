# Classics Implementation Coverage

## Purpose

本文档记录 Classics 古籍域需求的当前完成状态，用于后续补充开发、跨域协作和交付验收。

本清单不替代 `docs/10-requirements/CLASSICS-REQUIREMENTS.md`、`docs/30-designs/CLASSICS-DESIGN.md` 或 `docs/30-designs/RUNBOOK-CLASSICS-DOMAIN.md`。

## Status Definition

- `已完成`：当前仓库已有可追溯交付物，且不依赖后续服务代码即可成立。例如需求文档、数据设计、schema、初始化数据或 RUNBOOK 决策。
- `部分完成`：当前仓库已有需求、设计、数据结构或 RUNBOOK 任务，但运行时代码、跨域协作或业务闭环尚未完成。
- `未完成`：当前仓库尚未形成可执行设计、数据结构或明确任务，后续必须补充。
- `外部依赖`：能力边界不属于 Classics 单域，Classics 只提供引用、入口、快照、任务记录或调用点。

## Current Baseline

已完成：

- Classics 原始需求已按三才图会、王圻文档、明代习俗和分享重新整理。
- Classics 数据设计已按需求来源重制，每个持久化字段可追溯到需求。
- Classics schema SQL 已重制。
- 三才图会初始化数据 SQL 已由 JSON 生成并导入开发数据库。
- JSON 快照和转换脚本已保留。
- Classics 服务实现 RUNBOOK 已按 `domain -> application -> infra -> interface -> starter -> verification` 拆分。
- 关键架构决策已确认：不做读写分离、Repository 统一命名、业务表不放审计字段、状态使用 `varchar`、`priority` 表内唯一且不参与 KEY、三才图会新增条目使用数据库自增主键。
- 导出和静态展示第一版只记录任务，不同步生成产物。
- 分享访问第一版返回完整内容快照。

未完成：

- Java 服务代码尚未实现。
- HTTP API 尚未实现。
- Repository、Mapper、ApplicationService、Controller、Starter 装配尚未实现。
- 自动化验证尚未实现。
- 分享完整内容快照尚未同步落入数据设计和 schema。

## Requirement Coverage Matrix

### 三才图会知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 14 个正式门类、卷首辅助内容、卷和条目三级浏览 | 部分完成 | 需求、设计、schema、初始化数据、RUNBOOK 任务已覆盖 | Java 查询服务和接口未实现 | Classics |
| 门类和卷稳定排序 | 部分完成 | `priority` 规则、schema 约束和初始化数据已完成 | Repository 和 API 排序实现未完成 | Classics |
| 条目查看、创建、编辑、删除 | 部分完成 | 条目表、领域模型任务、应用服务任务和接口任务已规划 | 运行时代码、二次确认和删除后分享占位处理未完成 | Classics |
| 编辑标题、门类、卷、原文、译文和标签 | 部分完成 | 主表和通用标签表已设计，RUNBOOK 已覆盖保存命令 | API 请求、应用服务校验和标签协作未完成 | Classics, Knowledge |
| 展示原文、译文、标签、配图和状态 | 部分完成 | 数据结构和响应任务已覆盖 | 接口响应和前端展示未完成 | Classics, Frontend |
| 多张配图、缩略预览、放大浏览 | 部分完成 | 图片引用表、Storage 引用和资产任务已覆盖 | 文件读取、缩略图和预览展示依赖 Storage/Frontend | Classics, Storage, Frontend |
| 区分原始配图和视觉资产生成图 | 部分完成 | `image_type` 和视觉资产表已设计 | 应用服务和接口未实现 | Classics |
| 从条目上下文进入视觉资产工作流 | 部分完成 | RUNBOOK 有资产服务和接口任务 | AI/Frontend 工作流未实现 | Classics, AI, Frontend |
| 原图上传、删除和预览 | 部分完成 | Storage 引用设计已完成 | 上传、删除二次确认、预览读取未实现 | Classics, Storage, Frontend |
| 图片理解、信息融合、权重调节、视觉描述、AI 生图入口 | 部分完成 | 视觉资产字段已支持权重、理解结果、融合描述、生成参数 | AI 调用执行、候选预览、确认应用和失败处理未实现 | Classics, AI |
| 视觉资产历史和当前使用版本选择 | 部分完成 | 版本号、当前使用标志和 RUNBOOK 任务已覆盖 | 当前版本切换规则和接口未实现 | Classics |
| 多选条目批量视觉资产处理 | 部分完成 | RUNBOOK 提到批量入口 | 批处理结果模型、失败原因、取消保留已完成结果未明确实现 | Classics, AI |
| 摘要、标签和问答对内联维护 | 部分完成 | 主表摘要、通用标签、问答对表和 RUNBOOK 任务已覆盖 | API、候选结果确认、版本记录未实现 | Classics, Knowledge, AI |
| 分页、筛选、当前卷搜索和多选 | 部分完成 | 状态字段、索引和查询任务已覆盖 | 分页规格 `50/100/200`、筛选参数和多选接口未实现 | Classics |
| 生命周期：草稿、发布、归档、恢复 | 部分完成 | 状态字段、规则和任务已覆盖 | 状态流转代码和版本记录未实现 | Classics |
| 公开和私有可见性管理 | 部分完成 | 可见性字段和规则已覆盖 | 权限过滤调用点、批量修改结果未实现 | Classics, System |
| 版本历史、版本对比和历史恢复 | 部分完成 | 通用版本表和 RUNBOOK 任务已覆盖 | 版本对比接口、恢复生成新版本未实现 | Classics |
| CSV、JSON、HTML 设定集导出 | 部分完成 | 导出任务表、规则和异步接入决策已完成 | 只记录任务；实际产物生成未完成 | Classics, Worker, Storage |
| HTML 视觉资产设定集导出 | 部分完成 | 视觉资产和导出任务结构已覆盖 | 只记录任务；实际生成未完成 | Classics, Worker, Storage |
| 导出记录查看、下载、删除和过期 | 部分完成 | 导出任务表支持状态、过期和 Storage 引用 | API、权限过滤和下载未实现 | Classics, System, Storage |
| 静态展示页面生成 | 部分完成 | 展示任务表和“只记录任务”决策已完成 | 实际静态页面生成、模板、搜索、筛选、响应式布局未完成 | Classics, Worker, Frontend, Storage |
| 静态展示包含私有内容确认 | 部分完成 | 需求中确认文案已存在，设计有风险状态 | 接口确认参数和固定文案校验未实现 | Classics, Frontend |

### 王圻文档知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 文档查看、创建、编辑、删除 | 部分完成 | 文档表、设计和 RUNBOOK 任务已覆盖 | Java 服务和接口未实现 | Classics |
| 原始文件关联和替换 | 部分完成 | `storage_object_id` 已设计 | Storage 对象校验、替换接口未实现 | Classics, Storage |
| 全文阅读和内容安全展示 | 部分完成 | `content_format` 和 `content` 已设计 | 安全渲染策略和接口约束未实现 | Classics, Frontend |
| 摘要、标签和问答对展示维护 | 部分完成 | 摘要字段、通用标签和问答对表已设计 | API 和内联维护流程未实现 | Classics, Knowledge |
| AI 摘要、标签、问答对生成入口和候选确认 | 部分完成 | 需求已记录，通用内容结构可承载确认后结果 | AI 触发入口、候选结果暂存、修改、确认和放弃未完成 | Classics, AI |
| 文档搜索和时间线浏览 | 部分完成 | 文档时间字段和索引已设计，RUNBOOK 已覆盖时间线 | 搜索实现和时间线接口未完成 | Classics, Discovery |
| 列表标题、标签预览、摘要预览和时间信息 | 部分完成 | 数据结构已支持 | 接口响应未实现 | Classics |
| 批量修改公开或私有状态 | 部分完成 | 可见性字段已设计 | 批量结果、失败原因和权限过滤未实现 | Classics, System |
| 单文档问答入口 | 部分完成 | RUNBOOK 已列入口 | 回答生成属于外部依赖，Classics 调用点未实现 | Classics, Discovery, AI |
| 版本历史、版本对比和历史恢复 | 部分完成 | 通用版本表已设计 | 版本接口和恢复规则未实现 | Classics |
| 筛选结果或选中文档导出 | 部分完成 | 导出任务表已设计 | 只记录任务；实际生成未完成 | Classics, Worker, Storage |

### 明代习俗知识库

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 习俗查看、创建、编辑、删除 | 部分完成 | 习俗表、关键词表和 RUNBOOK 任务已覆盖 | Java 服务和接口未实现 | Classics |
| 概述、正文、分类、关键词、标签、原文摘录展示 | 部分完成 | 字段设计已覆盖 | 接口响应和展示未实现 | Classics, Frontend |
| 列表浏览和关键词搜索 | 部分完成 | 关键词表和索引已设计 | 查询服务和接口未实现 | Classics |
| 详情弹窗 | 部分完成 | 数据结构支持详情内容 | 前端弹窗和接口响应未实现 | Classics, Frontend |
| Markdown 安全渲染 | 部分完成 | `content_format` 和需求已记录 | 安全渲染策略、危险 HTML 过滤未实现 | Classics, Frontend |
| 标签云筛选 | 部分完成 | 通用标签表支持标签统计 | 标签云统计接口和权限过滤未实现 | Classics, Knowledge, System |
| 批量修改公开或私有状态 | 部分完成 | 可见性字段已设计 | 批量结果、失败原因和权限过滤未实现 | Classics, System |
| 摘要、标签和问答对维护 | 部分完成 | 通用内容表已设计 | API 和内联维护流程未实现 | Classics, Knowledge |
| 版本历史、版本对比和历史恢复 | 部分完成 | 通用版本表已设计 | 版本接口和恢复规则未实现 | Classics |
| 分类、标签、筛选结果或选中条目导出 | 部分完成 | 导出任务表已设计 | 只记录任务；实际生成未完成 | Classics, Worker, Storage |

### 跨知识库分享

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 选择三类内容生成分享链接 | 部分完成 | 分享链接、分享目标表和 RUNBOOK 任务已覆盖 | 管理接口和内容选择校验未实现 | Classics |
| 单链接多个内容 | 部分完成 | 分享目标表支持多个目标 | 应用服务和接口未实现 | Classics |
| 批量创建分享链接 | 部分完成 | RUNBOOK 已覆盖批量创建 | 批量结果、失败原因、取消保留已完成结果未实现 | Classics |
| 分享链接公开或私有 | 部分完成 | 分享可见性字段已设计 | 权限判断和访问接口未实现 | Classics, System |
| 过期时间、撤销和恢复 | 部分完成 | 状态、过期字段和任务已覆盖 | 状态流转代码未实现 | Classics |
| 只读访问页 | 部分完成 | Portal 接口任务已覆盖 | 接口、前端页面和权限处理未实现 | Classics, Frontend, System |
| 访问统计 | 部分完成 | 访问记录表和 `access_count` 已设计 | 访问记录写入和统计接口未实现 | Classics |
| 分享完整内容快照 | 部分完成 | 决策已写入 RUNBOOK | Design 和 SQL 缺少完整内容快照字段，运行时代码未实现 | Classics |
| 私有内容分享确认文案 | 部分完成 | 需求中固定文案已存在 | 接口确认参数和固定文案校验未实现 | Classics, Frontend |
| 目标被删除后占位展示 | 部分完成 | 分享目标表有标题快照和目标状态 | 删除联动和访问页占位响应未实现 | Classics |

### 通用内容和跨域能力

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 权限不足用户看不到私有内容 | 部分完成 | 可见性字段和规则已设计 | 权限策略来自 System，调用点和过滤实现未完成 | Classics, System |
| 批量状态修改成功数、失败数和失败原因 | 未完成 | 需求已记录 | 批处理结果模型和接口响应未设计到 RUNBOOK 粒度 | Classics |
| 删除前二次确认 | 部分完成 | 需求已记录 | 接口确认参数和前端确认未实现 | Classics, Frontend |
| AI 生成候选预览、修改、确认和放弃 | 未完成 | 需求已记录 | 候选结果承载结构、接口和 AI 协作协议未设计 | Classics, AI |
| Knowledge 标签治理 | 外部依赖 | Classics 设计了标签引用和标签名快照 | 标签合并、同义词、治理规则不属于 Classics | Knowledge |
| Storage 对象管理 | 外部依赖 | Classics 只保存 `storage_object_id` | 上传、读取、下载、删除对象由 Storage 实现 | Storage |
| Discovery 搜索和问答 | 外部依赖 | Classics 可提供内容上下文和入口 | 索引、召回、问答生成由 Discovery/AI 实现 | Discovery, AI |
| System 审计 | 外部依赖 | 业务表不保存审计字段 | 操作者和关键操作日志由 System 审计系统实现 | System |
| Worker 异步任务执行 | 外部依赖 | Classics 记录导出和静态展示任务 | 产物生成、失败重试和任务调度由 Worker 实现 | Worker, Storage |

## Follow-up Backlog

### B1 分享完整内容快照设计补齐

状态：未完成。

需要补充：

- `CLASSICS-DESIGN.md` 增加完整内容快照字段或独立快照表。
- `db/schema/classics.sql` 同步 schema。
- RUNBOOK 中 Sharing Domain、Persistence、Application 和 Interface 任务补充快照读写要求。

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
- 统一成功数、失败数、失败原因和取消后保留已完成结果语义。

### B4 权限和确认文案接入

状态：未完成。

需要补充：

- 私有内容分享确认。
- 私有内容静态展示确认。
- 删除内容、删除原图、删除视觉资产产物确认。
- 权限不足时的错误语义和过滤策略。

### B5 导出和静态展示 Worker 对接

状态：未完成。

需要补充：

- 导出任务状态机和 Worker 消费协议。
- 静态展示任务状态机和 Worker 消费协议。
- 产物 Storage 对象写入和过期清理策略。

### B6 安全渲染和内容展示策略

状态：未完成。

需要补充：

- 王圻文档内容安全展示策略。
- 明代习俗 Markdown 安全渲染策略。
- 分享快照内容只读展示策略。

