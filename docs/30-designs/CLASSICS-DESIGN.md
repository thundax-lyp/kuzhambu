# Classics Design

## Purpose

本文档定义 Classics 古籍域数据设计，覆盖三才图会、王圻文档、明代习俗、内容标签、问答对、版本、导出和 portal 在线展示。

设计约束：每个持久化字段必须能追溯到原始需求中的内容展示、筛选、状态、版本、导出或访问统计需求；不从既有错误 SQL 或初始化数据反推字段。

## Business Boundary

Classics 拥有古籍内容主数据和内容上下文内的维护数据。Storage、AI、Knowledge、Discovery、System 只通过应用服务协作，不直接写入 Classics 主表。对于通用内容标签，`classics_content_tag` 是内容绑定主事实；Knowledge 只提供统一标签解析、自动创建和内容引用投影协作。

## Cross Content Identity

公共内容表使用 `content_type + content_id`：

- `SANCAI_ENTRY`：三才图会条目。
- `WANGQI_DOCUMENT`：王圻文档。
- `MING_CUSTOMS`：明代习俗条目。

## Data Model Rules

- 排序字段统一为 `priority int`，并建立单列唯一约束。
- `priority` 只作为单表内全局排序值，不参与普通 KEY 或组合 KEY。
- 状态、类型、格式等业务枚举统一使用 `varchar`。
- 只有纯 yes/no 技术标志使用 `tinyint(1)`；业务状态、业务类型、业务快照统一使用 `varchar`。
- 绝对时间点使用 `BIGINT epoch_ms`。
- 默认不设置数据库外键。
- 操作者、创建者、更新者、删除者、发起人等审计归属不进入业务表，由 System 审计系统记录。

## Tables

### classics_sancai_category

需求来源：三才图会支持 14 个正式门类、卷首辅助内容、门类稳定排序和门类浏览。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 门类实体身份 |
| `title` | `varchar(64)` | UK | 门类浏览展示门类标题 |
| `category_type` | `varchar(16)` |  | 区分正式门类和卷首辅助内容 |
| `priority` | `int` | UK | 门类稳定排序 |

约束：`id` 主键；`title` 唯一；`priority` 唯一。

### classics_sancai_volume

需求来源：三才图会支持卷浏览、106 卷、卷首辅助内容和卷稳定排序。卷展示顺序只由 `priority` 控制。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 卷实体身份 |
| `category_id` | `bigint` | KEY | 门类到卷的三级浏览关系 |
| `title` | `varchar(128)` |  | 卷列表展示卷标题 |
| `volume_type` | `varchar(16)` |  | 区分正式卷和辅助卷首内容 |
| `priority` | `int` | UK | 卷稳定排序 |

约束：`id` 主键；`priority` 唯一。索引：`category_id`。

### classics_sancai_entry

需求来源：三才图会条目 CRUD、编辑标题/门类/卷/原文/译文/标签、状态展示、筛选、搜索、草稿/发布/下线生命周期。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 条目实体身份 |
| `volume_id` | `bigint` | KEY | 条目归属卷，支持三级浏览和编辑归属；编辑迁移时更新为目标卷 ID |
| `title` | `varchar(255)` |  | 条目标题展示、编辑、搜索 |
| `original_text` | `longtext` |  | 原文展示和编辑 |
| `translation_text` | `longtext` |  | 译文展示和编辑 |
| `summary` | `text` |  | 摘要内联查看、编辑和保存 |
| `lifecycle_status` | `varchar(16)` | KEY | 草稿、发布、下线、错误最终生命周期 |
| `transition_status` | `varchar(24)` | KEY | 发布或下线过程状态，例如 `NONE`、`PUBLISHING`、`OFFLINING` |
| `current_publication_job_id` | `bigint` | KEY | 当前发布或下线任务归属和进度跳转，不作为独立稿件锁 |
| `translation_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按翻译状态筛选 |
| `image_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按配图状态筛选 |
| `visual_asset_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按视觉资产状态筛选 |
| `refinement_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按完善状态筛选 |
| `priority` | `int` | UK | 条目列表稳定排序；跨卷迁移时写入当前全局最大 `priority + 1` |
| `current_version_id` | `bigint` | KEY | 当前正式内容版本标定 |
| `current_version_no` | `int` |  | 当前正式内容版本号展示和差异判断 |
| `current_versioned_at` | `BIGINT epoch_ms` |  | 当前正式内容版本生成时间 |
| `content_updated_at` | `BIGINT epoch_ms` |  | 内容语义更新时间，用于判断未版本化变更 |

约束：`id` 主键；`priority` 唯一。索引：`current_version_id`、`volume_id`、`lifecycle_status`、`transition_status`、`current_publication_job_id`、`(translation_status, image_status, visual_asset_status, refinement_status)`。同卷编辑保留原 `priority`；跨卷迁移使用当前全局最大 `priority + 1`，使条目进入目标卷列表末尾。

### classics_sancai_entry_draft

需求来源：三才图会自动保存、手动保存和草稿恢复提示。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 草稿实体身份 |
| `entry_id` | `bigint` | KEY(entry_id, autosaved_at) | 草稿归属条目 |
| `autosaved_at` | `BIGINT epoch_ms` | KEY(entry_id, autosaved_at) | 草稿恢复提示需要自动保存时间 |
| `draft_json` | `json` |  | 自动保存内容快照 |

约束：`id` 主键。索引：`(entry_id, autosaved_at)`。

### classics_sancai_entry_image

需求来源：三才图会多张配图展示、缩略预览、放大浏览、区分原图和视觉资产生成图、原图上传删除预览、当前使用版本。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 图片引用实体身份 |
| `entry_id` | `bigint` | UK(entry_id, storage_object_id), KEY | 图片归属条目 |
| `storage_object_id` | `bigint` | UK(entry_id, storage_object_id) | 关联 Storage 对象 |
| `image_type` | `varchar(16)` |  | 区分原图和 AI 生成图 |
| `title` | `varchar(512)` |  | 图片展示标题 |
| `current_used` | `tinyint(1)` |  | 当前使用版本，纯 yes/no 技术标志 |
| `priority` | `int` | UK | 多图展示排序 |

约束：`id` 主键；`(entry_id, storage_object_id)` 唯一；`priority` 唯一。索引：`entry_id`。

### classics_sancai_visual_asset

需求来源：图片理解、信息融合、文本和图片权重调节、视觉描述生成编辑预览、AI 生图、历史产物、当前使用版本、视觉资产设定集导出。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 视觉资产实体身份 |
| `entry_id` | `bigint` | UK(entry_id, version_no), KEY(entry_id, current_used) | 视觉资产必须关联三才图会条目 |
| `version_no` | `int` | UK(entry_id, version_no) | 视觉资产历史版本 |
| `status` | `varchar(16)` | KEY | 视觉资产状态和处理结果 |
| `source_image_storage_object_id` | `bigint` |  | 原图 Storage 对象引用 |
| `generated_image_storage_object_id` | `bigint` |  | AI 生成图 Storage 对象引用 |
| `current_used` | `tinyint(1)` | KEY(entry_id, current_used) | 当前使用版本，纯 yes/no 技术标志 |
| `text_weight` | `int` |  | 文本权重调节 |
| `image_weight` | `int` |  | 图片理解权重调节 |
| `image_analysis_markdown` | `longtext` |  | 图片理解结果 Markdown 编辑预览 |
| `fusion_description` | `longtext` |  | 信息融合结果 |
| `visual_description` | `longtext` |  | 视觉描述生成、编辑和预览 |
| `generation_params_json` | `json` |  | AI 生图参数和设定集导出需要 |

约束：`id` 主键；`(entry_id, version_no)` 唯一。索引：`(entry_id, current_used)`、`status`。

### classics_sancai_portal_view

本节定义三才图会 Portal 逻辑读模型，不对应数据库表或数据库 view。Portal 在线展示不生成静态展示包，也不维护展示包任务记录。

约束：Portal 条目列表和检索入口必须查询 ES 中 `publicationStatus = READY` 且 `deleted = false` 的文档；命中后通过 Classics application 只读能力组装门类、卷、条目、图片和视觉资产详情，不允许 portal-web 直接读取 Classics 数据表，也不再按主库 `lifecycle_status` 二次决定列表可见性。Portal 详情接口必须先通过 Discovery 校验同一内容仍满足 ES READY 查询条件，禁止仅凭 `contentType + contentId` 绕过 ES 可见性读取详情。

### classics_wangqi_document

需求来源：王圻文档 CRUD、原始文档 Storage 对象关联替换、全文阅读、安全展示、摘要标签问答展示、文档搜索、时间线、草稿/发布/下线生命周期、版本。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 文档实体身份 |
| `title` | `varchar(255)` |  | 文档标题展示和搜索 |
| `summary` | `text` |  | 摘要展示和内联编辑 |
| `content_format` | `varchar(16)` |  | 内容安全展示需要正文格式 |
| `content` | `longtext` |  | 全文阅读和编辑 |
| `document_time` | `BIGINT epoch_ms` | KEY | 时间线浏览 |
| `storage_object_id` | `bigint` | KEY | 原始文档 Storage 对象关联和替换 |
| `lifecycle_status` | `varchar(16)` | KEY | 草稿、发布、下线、错误最终生命周期 |
| `transition_status` | `varchar(24)` | KEY | 发布或下线过程状态，例如 `NONE`、`PUBLISHING`、`OFFLINING` |
| `current_publication_job_id` | `bigint` | KEY | 当前发布或下线任务归属和进度跳转，不作为独立稿件锁 |
| `current_version_id` | `bigint` | KEY | 当前正式内容版本标定 |
| `current_version_no` | `int` |  | 当前正式内容版本号展示和差异判断 |
| `current_versioned_at` | `BIGINT epoch_ms` |  | 当前正式内容版本生成时间 |
| `content_updated_at` | `BIGINT epoch_ms` |  | 内容语义更新时间，用于判断未版本化变更 |

约束：`id` 主键。索引：`current_version_id`、`document_time`、`lifecycle_status`、`transition_status`、`current_publication_job_id`、`storage_object_id`。

### classics_ming_customs_entry

需求来源：明代习俗 CRUD、概述、正文、分类、关键词、标签、原文摘录展示、Markdown 安全渲染、列表、搜索、详情弹窗、草稿/发布/下线生命周期、版本。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 习俗条目实体身份 |
| `title` | `varchar(255)` |  | 标题展示和搜索 |
| `category` | `varchar(128)` | KEY(category, lifecycle_status) | 分类展示和导出范围 |
| `chapter` | `varchar(128)` |  | 章节展示 |
| `section` | `varchar(128)` |  | 节展示 |
| `summary` | `text` |  | 概述和摘要展示 |
| `content_format` | `varchar(16)` |  | Markdown 安全渲染 |
| `content` | `longtext` |  | 正文展示和编辑 |
| `original_excerpts` | `longtext` |  | 原文摘录展示 |
| `lifecycle_status` | `varchar(16)` | KEY(category, lifecycle_status), KEY | 草稿、发布、下线、错误最终生命周期 |
| `transition_status` | `varchar(24)` | KEY | 发布或下线过程状态，例如 `NONE`、`PUBLISHING`、`OFFLINING` |
| `current_publication_job_id` | `bigint` | KEY | 当前发布或下线任务归属和进度跳转，不作为独立稿件锁 |
| `current_version_id` | `bigint` | KEY | 当前正式内容版本标定 |
| `current_version_no` | `int` |  | 当前正式内容版本号展示和差异判断 |
| `current_versioned_at` | `BIGINT epoch_ms` |  | 当前正式内容版本生成时间 |
| `content_updated_at` | `BIGINT epoch_ms` |  | 内容语义更新时间，用于判断未版本化变更 |

约束：`id` 主键。索引：`current_version_id`、`(category, lifecycle_status)`、`lifecycle_status`、`transition_status`、`current_publication_job_id`。

### classics_ming_customs_keyword

需求来源：明代习俗关键词展示和关键词搜索。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 关键词实体身份 |
| `custom_id` | `bigint` | UK(custom_id, keyword) | 关键词归属习俗条目 |
| `keyword` | `varchar(128)` | UK(custom_id, keyword), KEY | 关键词展示和搜索 |
| `priority` | `int` | UK | 关键词展示排序 |

约束：`id` 主键；`(custom_id, keyword)` 唯一；`priority` 唯一。索引：`keyword`。

### classics_content_tag

需求来源：三类内容标签展示、王圻标签内联维护、标签云筛选、统一标签治理协作、标签为空仍可保存。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 标签引用实体身份 |
| `content_type` | `varchar(32)` | UK(content_type, content_id, tag_name_snapshot), KEY | 三类内容通用标签引用 |
| `content_id` | `bigint` | UK(content_type, content_id, tag_name_snapshot), KEY | 内容身份 |
| `tag_id` | `bigint` | KEY(tag_id, content_type) | 对接 Knowledge 统一标签 |
| `tag_name_snapshot` | `varchar(128)` | UK(content_type, content_id, tag_name_snapshot), KEY | 标签展示和导出快照 |
| `source` | `varchar(16)` |  | 区分 AI 提取和人工维护 |
| `status` | `varchar(16)` |  | 标签引用状态 |
| `priority` | `int` | UK | 标签展示排序 |

约束：`id` 主键；`(content_type, content_id, tag_name_snapshot)` 唯一；`priority` 唯一。索引：`(content_type, content_id)`、`(tag_id, content_type)`、`(tag_name_snapshot, content_type)`。

协作规则：

- `classics_content_tag` 作为内容标签绑定主事实保存 `tag_id`、`tag_name_snapshot`、`source`、`status` 和全局排序权重。
- 手工标签新增、更新、删除必须先经过 Knowledge 统一标签协作语义，再写入 Classics 主事实。
- AI 标签确认不再直接按本地标签名重建，而是先解析或创建 Knowledge 统一标签，再写回 Classics。
- `tag_name_snapshot` 只由 Classics 保存，用于内容展示、历史快照和导出，不回写 Knowledge 引用投影。
- 内容侧标签名称编辑只改变当前内容的绑定和当前快照：先解除旧标签引用，再解析或创建新 Knowledge 标签并绑定到当前内容。该操作不得修改 `knowledge_tag` 主数据名称，也不得影响其他内容的标签绑定。
- Knowledge taxonomy 发起全局改名、合并、复制、分拆或废弃时，必须通过 Classics 协作接口更新当前内容标签绑定和当前 `tag_name_snapshot`；历史版本 `snapshot_json` 保持创建时快照，不随全局治理回写。

### classics_content_qa_pair

需求来源：三类内容问答对展示、内联新增编辑删除、AI 候选确认后应用。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 问答对实体身份 |
| `content_type` | `varchar(32)` | KEY(content_type, content_id) | 三类内容通用问答对 |
| `content_id` | `bigint` | KEY(content_type, content_id) | 内容身份 |
| `question` | `text` |  | 问题 |
| `answer` | `longtext` |  | 答案 |
| `source` | `varchar(16)` |  | 区分 AI 生成和人工维护 |
| `priority` | `int` | UK | 问答对展示排序 |

约束：`id` 主键；`priority` 唯一。索引：`(content_type, content_id)`。

### classics_content_version

需求来源：三类内容版本历史、版本对比、历史恢复、正式版本可追溯。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 版本实体身份 |
| `content_type` | `varchar(32)` | UK(content_type, content_id, version_no), KEY | 三类内容通用版本 |
| `content_id` | `bigint` | UK(content_type, content_id, version_no), KEY | 内容身份 |
| `version_no` | `int` | UK(content_type, content_id, version_no) | 版本号和对比定位 |
| `versioned_at` | `BIGINT epoch_ms` | KEY(content_type, content_id, versioned_at) | 版本时间 |
| `snapshot_json` | `json` |  | 版本快照和恢复 |
| `change_type` | `varchar(32)` |  | 手动保存、AI 应用、历史恢复等变更类型 |
| `change_summary` | `varchar(512)` |  | 版本摘要展示 |

约束：`id` 主键；`(content_type, content_id, version_no)` 唯一。索引：`(content_type, content_id, versioned_at)`。

主内容版本标定规则：`classics_sancai_entry`、`classics_wangqi_document`、`classics_ming_customs_entry` 统一使用 `current_version_id/current_version_no/current_versioned_at/content_updated_at` 表达当前正式版本和内容语义更新时间。正式版本只由用户确认动作产生，例如手动保存、AI 应用和历史恢复；自动保存草稿、排序、状态刷新、访问统计等非内容确认动作不得生成 `classics_content_version`。

### classics_publication_job

需求来源：发布和下线是跨 ES 和 FastGPT 的异步状态机操作，需要专门只读菜单入口查看状态、端侧引用和失败诊断。一个发布或下线任务只处理一个稿件；批量操作创建多个独立任务；每个稿件只保留最新一条任务。失败任务不在任务菜单重试，用户只能从稿件页面重新发起新任务。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 发布任务身份 |
| `job_type` | `varchar(16)` | KEY(job_type, job_result_status) | 任务类型，例如 `PUBLISH`、`OFFLINE` |
| `content_type` | `varchar(32)` | UK(content_type, content_id) | 内容类型 |
| `content_id` | `bigint` | UK(content_type, content_id) | 内容身份 |
| `content_title_snapshot` | `varchar(255)` |  | 发起任务时的稿件标题快照，稿件删除后继续用于任务展示 |
| `content_deleted_at` | `BIGINT epoch_ms` | KEY | 稿件被业务正常删除的时间；非空时作为已删除稿件端侧清理墓碑 |
| `source_lifecycle_status` | `varchar(16)` |  | 发起任务时的生命周期 |
| `target_lifecycle_status` | `varchar(16)` |  | 目标生命周期，发布为 `PUBLISHED`，下线为 `OFFLINE` |
| `content_version_id` | `bigint` |  | 发布绑定的正式版本快照 |
| `content_version_no` | `int` |  | 发布绑定的正式版本号 |
| `job_status` | `varchar(32)` | KEY(job_result_status, job_status, expires_at), KEY(job_result_status, job_status, next_retry_at) | 最后一个成功完成的状态机里程碑，不承载 `FAILED` 或 `SUCCEEDED` |
| `job_result_status` | `varchar(16)` | KEY(job_type, job_result_status), KEY(job_result_status, job_status, expires_at), KEY(job_result_status, job_status, next_retry_at) | 整体任务结果，例如 `RUNNING`、`FAILED`、`SUCCEEDED` |
| `execution_token` | `varchar(64)` |  | 当前切片执行令牌；每次抢占生成新值，使过期线程不能推进任务 |
| `expires_at` | `BIGINT epoch_ms` | KEY(job_status, expires_at) | 当前切片执行过期时间 |
| `next_retry_at` | `BIGINT epoch_ms` | KEY(job_status, next_retry_at) | `nextStep(job_status)` 失败后的下次重试时间 |
| `attempt_count` | `int` |  | `nextStep(job_status)` 已尝试次数，包含初次执行 |
| `max_attempts` | `int` |  | `nextStep(job_status)` 最大尝试次数，默认 `4`，即初次执行后最多重试 3 次 |
| `es_document_id` | `varchar(256)` |  | ES 文档 ID |
| `fastgpt_collection_id` | `varchar(256)` |  | FastGPT 稿件 collection ID |
| `fastgpt_data_ids_json` | `json` |  | 可选诊断字段；仅保存当前 job 能取得的 FastGPT data ID，不作为进度完成条件或历史碎片清理清单 |
| `es_cleanup_status` | `varchar(16)` | KEY(es_cleanup_status, es_cleanup_expires_at) | ES 残留清理状态，例如 `NONE`、`PENDING`、`RUNNING`、`FAILED`、`SUCCEEDED` |
| `es_cleanup_token` | `varchar(64)` |  | ES 清理执行令牌 |
| `es_cleanup_expires_at` | `BIGINT epoch_ms` | KEY(es_cleanup_status, es_cleanup_expires_at) | ES 清理租约过期时间 |
| `fastgpt_cleanup_status` | `varchar(16)` | KEY(fastgpt_cleanup_status, fastgpt_cleanup_expires_at) | FastGPT 残留清理状态，例如 `NONE`、`PENDING`、`RUNNING`、`FAILED`、`SUCCEEDED` |
| `fastgpt_cleanup_token` | `varchar(64)` |  | FastGPT 清理执行令牌 |
| `fastgpt_cleanup_expires_at` | `BIGINT epoch_ms` | KEY(fastgpt_cleanup_status, fastgpt_cleanup_expires_at) | FastGPT 清理租约过期时间 |
| `detail_json` | `json` |  | 端侧响应摘要、探测结果和清理信息 |
| `requested_at` | `BIGINT epoch_ms` | KEY | 发起时间 |
| `started_at` | `BIGINT epoch_ms` |  | 开始时间 |
| `finished_at` | `BIGINT epoch_ms` |  | 结束时间 |
| `failure_reason` | `varchar(1024)` |  | 目标失败原因 |

约束：`id` 主键；`(content_type, content_id)` 唯一，每个稿件最多保留一条最新发布或下线任务。发起新任务时必须在同一事务内对旧 job 执行 `SELECT ... FOR UPDATE`，只继承 `es_document_id` 和 `fastgpt_collection_id`、确认两个清理状态均不为 `RUNNING`、删除旧任务并插入新任务；新任务的碎片 ID 为空，清理状态初始化为 `NONE`。稿件删除不得级联删除本表记录；删除 `ERROR/OFFLINE` 稿件时写入 `content_deleted_at` 并保留端侧引用，供清理 Schedule 在稿件不存在后继续处理。发起人属于审计关系，由 System 审计记录任务创建动作，不在本业务表重复保存。索引：`(job_result_status, job_status, expires_at)`、`(job_result_status, job_status, next_retry_at)`、`(job_type, job_result_status)`、`(es_cleanup_status, es_cleanup_expires_at)`、`(fastgpt_cleanup_status, fastgpt_cleanup_expires_at)`、`content_deleted_at`、`requested_at`。

发布任务状态规则详见 [CLASSICS-PUBLICATION-SPECIAL-DESIGN.md](./CLASSICS-PUBLICATION-SPECIAL-DESIGN.md)。本表只定义任务持久化结构；状态机切片、线程执行、定时扫描接管、失败回填为 `ERROR`、FastGPT enable/disable 和垃圾同步均以专项设计为准。

### classics_content_export_job

需求来源：三类内容和视觉资产设定集导出、范围、格式、生成时间、数量、过期、非发布内容风险提示、内容变更提示、后台按权限查看下载删除导出记录。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 导出记录身份 |
| `export_kind` | `varchar(32)` | KEY(content_type, export_kind) | 内容设定集或视觉资产设定集 |
| `content_type` | `varchar(32)` | KEY(content_type, export_kind) | 导出内容范围 |
| `export_format` | `varchar(16)` |  | CSV、JSON、HTML |
| `scope_type` | `varchar(32)` |  | 门类、卷、筛选结果、选中内容等范围类型 |
| `scope_json` | `json` |  | 范围快照 |
| `requested_at` | `BIGINT epoch_ms` |  | 生成时间 |
| `expires_at` | `BIGINT epoch_ms` | KEY(status, expires_at) | 过期控制 |
| `status` | `varchar(16)` | KEY(status, expires_at) | 导出状态 |
| `storage_object_id` | `bigint` |  | 导出产物 Storage 对象 |
| `item_count` | `int` |  | 内容数量展示 |
| `asset_count` | `int` |  | 视觉资产数量展示 |
| `lifecycle_risk_status` | `varchar(16)` |  | 生命周期风险状态，例如 `PUBLISHED_ONLY`、`CONTAINS_NON_PUBLISHED` |
| `content_changed` | `tinyint(1)` |  | 内容可能已变更提示，纯 yes/no 标志 |

约束：`id` 主键。索引：`(status, expires_at)`、`(content_type, export_kind)`。
