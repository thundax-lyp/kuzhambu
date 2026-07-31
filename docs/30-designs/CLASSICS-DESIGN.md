# Classics Design

## Purpose

本文档定义 Classics 古籍域数据设计，覆盖三才图会、王圻文档、明代习俗、内容标签、问答对、版本、导出、portal 在线展示和分享。

设计约束：每个持久化字段必须能追溯到原始需求中的内容展示、筛选、状态、版本、导出、分享或访问统计需求；不从既有错误 SQL 或初始化数据反推字段。

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
- 状态、类型、格式、可见性等业务枚举统一使用 `varchar`。
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

需求来源：三才图会条目 CRUD、编辑标题/门类/卷/原文/译文/标签、状态展示、筛选、搜索、生命周期、公开私有可见性。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 条目实体身份 |
| `volume_id` | `bigint` | KEY | 条目归属卷，支持三级浏览和编辑归属；编辑迁移时更新为目标卷 ID |
| `title` | `varchar(255)` |  | 条目标题展示、编辑、搜索 |
| `original_text` | `longtext` |  | 原文展示和编辑 |
| `translation_text` | `longtext` |  | 译文展示和编辑 |
| `summary` | `text` |  | 摘要内联查看、编辑和保存 |
| `lifecycle_status` | `varchar(16)` | KEY(lifecycle_status, visibility) | 草稿、发布、下线生命周期 |
| `visibility` | `varchar(16)` | KEY(lifecycle_status, visibility) | 公开和私有可见性 |
| `translation_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按翻译状态筛选 |
| `image_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按配图状态筛选 |
| `visual_asset_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按视觉资产状态筛选 |
| `refinement_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按完善状态筛选 |
| `priority` | `int` | UK | 条目列表稳定排序；跨卷迁移时写入当前全局最大 `priority + 1` |
| `current_version_id` | `bigint` | KEY | 当前正式内容版本标定 |
| `current_version_no` | `int` |  | 当前正式内容版本号展示和差异判断 |
| `current_versioned_at` | `BIGINT epoch_ms` |  | 当前正式内容版本生成时间 |
| `content_updated_at` | `BIGINT epoch_ms` |  | 内容语义更新时间，用于判断未版本化变更 |

约束：`id` 主键；`priority` 唯一。索引：`current_version_id`、`volume_id`、`(lifecycle_status, visibility)`、`(translation_status, image_status, visual_asset_status, refinement_status)`。同卷编辑保留原 `priority`；跨卷迁移使用当前全局最大 `priority + 1`，使条目进入目标卷列表末尾。

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

需求来源：三才图会 portal 在线展示。portal 直接读取公开且已发布的三才图会门类、卷和条目，不生成静态展示包，不维护展示包任务记录。

约束：portal 展示数据来自 `classics_sancai_category`、`classics_sancai_volume`、`classics_sancai_entry`、`classics_sancai_entry_image` 和视觉资产表；展示查询必须过滤 `lifecycle_status = PUBLISHED` 且 `visibility = PUBLIC`。

### classics_wangqi_document

需求来源：王圻文档 CRUD、原始文档 Storage 对象关联替换、全文阅读、安全展示、摘要标签问答展示、文档搜索、时间线、公开私有可见性、版本。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 文档实体身份 |
| `title` | `varchar(255)` |  | 文档标题展示和搜索 |
| `summary` | `text` |  | 摘要展示和内联编辑 |
| `content_format` | `varchar(16)` |  | 内容安全展示需要正文格式 |
| `content` | `longtext` |  | 全文阅读和编辑 |
| `document_time` | `BIGINT epoch_ms` | KEY | 时间线浏览 |
| `storage_object_id` | `bigint` | KEY | 原始文档 Storage 对象关联和替换 |
| `visibility` | `varchar(16)` | KEY | 公开和私有可见性 |
| `current_version_id` | `bigint` | KEY | 当前正式内容版本标定 |
| `current_version_no` | `int` |  | 当前正式内容版本号展示和差异判断 |
| `current_versioned_at` | `BIGINT epoch_ms` |  | 当前正式内容版本生成时间 |
| `content_updated_at` | `BIGINT epoch_ms` |  | 内容语义更新时间，用于判断未版本化变更 |

约束：`id` 主键。索引：`current_version_id`、`document_time`、`visibility`、`storage_object_id`。

### classics_ming_customs_entry

需求来源：明代习俗 CRUD、概述、正文、分类、关键词、标签、原文摘录展示、Markdown 安全渲染、列表、搜索、详情弹窗、公开私有可见性、版本。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 习俗条目实体身份 |
| `title` | `varchar(255)` |  | 标题展示和搜索 |
| `category` | `varchar(128)` | KEY(category, visibility) | 分类展示和导出范围 |
| `chapter` | `varchar(128)` |  | 章节展示 |
| `section` | `varchar(128)` |  | 节展示 |
| `summary` | `text` |  | 概述和摘要展示 |
| `content_format` | `varchar(16)` |  | Markdown 安全渲染 |
| `content` | `longtext` |  | 正文展示和编辑 |
| `original_excerpts` | `longtext` |  | 原文摘录展示 |
| `visibility` | `varchar(16)` | KEY(category, visibility), KEY | 公开和私有可见性 |
| `current_version_id` | `bigint` | KEY | 当前正式内容版本标定 |
| `current_version_no` | `int` |  | 当前正式内容版本号展示和差异判断 |
| `current_versioned_at` | `BIGINT epoch_ms` |  | 当前正式内容版本生成时间 |
| `content_updated_at` | `BIGINT epoch_ms` |  | 内容语义更新时间，用于判断未版本化变更 |

约束：`id` 主键。索引：`current_version_id`、`(category, visibility)`、`visibility`。

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

### classics_content_export_job

需求来源：三类内容和视觉资产设定集导出、范围、格式、生成时间、数量、过期、私有内容风险提示、内容变更提示、后台按权限查看下载删除导出记录。

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
| `visibility_risk_status` | `varchar(16)` |  | 可见性风险状态，例如 `PUBLIC_ONLY`、`CONTAINS_PRIVATE` |
| `content_changed` | `tinyint(1)` |  | 内容可能已变更提示，纯 yes/no 标志 |

约束：`id` 主键。索引：`(status, expires_at)`、`(content_type, export_kind)`。

### classics_share_link

需求来源：分享链接、公开分享、私有分享、撤销、恢复、过期、访问统计、私有内容风险提示。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 分享链接身份 |
| `token_hash` | `varchar(128)` | UK | 分享访问令牌哈希，明文不落库 |
| `title` | `varchar(256)` |  | 集中查看分享链接 |
| `visibility` | `varchar(16)` |  | 公开或私有分享 |
| `status` | `varchar(16)` | KEY(status, expires_at) | 活跃、撤销等状态 |
| `visibility_risk_status` | `varchar(16)` |  | 可见性风险状态，例如 `PUBLIC_ONLY`、`CONTAINS_PRIVATE` |
| `created_by_user_id` | `bigint` | KEY(created_by_user_id, visibility) | 私有分享访问校验所需创建者 |
| `issued_at` | `BIGINT epoch_ms` |  | 分享创建时间 |
| `expires_at` | `BIGINT epoch_ms` | KEY(status, expires_at) | 分享过期时间 |
| `access_count` | `bigint` |  | 访问统计 |

约束：`id` 主键；`token_hash` 唯一。索引：`(status, expires_at)`、`(created_by_user_id, visibility)`。

### classics_share_target

需求来源：单链接多内容、跨库分享、目标删除后占位、只读访问页排序。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 分享目标身份 |
| `share_link_id` | `bigint` | UK(share_link_id, content_type, content_id) | 归属分享链接 |
| `content_type` | `varchar(32)` | UK(share_link_id, content_type, content_id), KEY | 跨库内容类型 |
| `content_id` | `bigint` | UK(share_link_id, content_type, content_id), KEY | 内容身份 |
| `title_snapshot` | `varchar(512)` |  | 内容删除后占位展示 |
| `content_snapshot_json` | `json` |  | 分享访问第一版返回完整内容快照 |
| `content_visibility_snapshot` | `varchar(16)` |  | 创建分享时内容可见性快照 |
| `target_status` | `varchar(16)` |  | 目标可用或内容已删除占位 |
| `priority` | `int` | UK | 分享页展示排序 |

约束：`id` 主键；`(share_link_id, content_type, content_id)` 唯一；`priority` 唯一。索引：`(content_type, content_id)`。

### classics_share_access_record

需求来源：分享访问统计和异常追溯。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 访问记录身份 |
| `share_link_id` | `bigint` | KEY(share_link_id, accessed_at) | 归属分享链接 |
| `share_target_id` | `bigint` | KEY(share_target_id, accessed_at) | 访问目标 |
| `accessed_at` | `BIGINT epoch_ms` | KEY | 访问时间 |
| `access_result` | `varchar(16)` |  | 允许、过期、撤销、无权限等结果 |
| `client_snapshot` | `json` |  | 访问统计和异常追溯摘要 |

约束：`id` 主键。索引：`(share_link_id, accessed_at)`、`(share_target_id, accessed_at)`。
