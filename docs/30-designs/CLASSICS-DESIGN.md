# Classics Design

## Purpose

本文档定义 Classics 古籍域数据设计，覆盖三才图会、王圻文档、明代习俗、内容标签、问答对、版本、导出、静态展示页面和分享。

设计约束：每个持久化字段必须能追溯到原始需求中的内容展示、筛选、状态、版本、导出、分享或访问统计需求；不从既有错误 SQL 或初始化数据反推字段。

## Business Boundary

Classics 拥有古籍内容主数据和内容上下文内的维护数据。Storage、AI、Knowledge、Discovery、System 只通过应用服务协作，不直接写入 Classics 主表。

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
- 绝对时间点使用 `datetime(3)`。
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
| `volume_id` | `bigint` | KEY | 条目归属卷，支持三级浏览和编辑归属 |
| `title` | `varchar(255)` |  | 条目标题展示、编辑、搜索 |
| `original_text` | `longtext` |  | 原文展示和编辑 |
| `translation_text` | `longtext` |  | 译文展示和编辑 |
| `summary` | `text` |  | 摘要内联查看、编辑和保存 |
| `lifecycle_status` | `varchar(16)` | KEY(lifecycle_status, visibility) | 草稿、发布、归档生命周期 |
| `visibility` | `varchar(16)` | KEY(lifecycle_status, visibility) | 公开和私有可见性 |
| `translation_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按翻译状态筛选 |
| `image_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按配图状态筛选 |
| `visual_asset_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按视觉资产状态筛选 |
| `refinement_status` | `varchar(16)` | KEY(translation_status, image_status, visual_asset_status, refinement_status) | 按完善状态筛选 |
| `priority` | `int` | UK | 条目列表稳定排序 |

约束：`id` 主键；`priority` 唯一。索引：`volume_id`、`(lifecycle_status, visibility)`、`(translation_status, image_status, visual_asset_status, refinement_status)`。

### classics_sancai_entry_draft

需求来源：三才图会自动保存、手动保存和草稿恢复提示。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 草稿实体身份 |
| `entry_id` | `bigint` | KEY(entry_id, autosaved_at) | 草稿归属条目 |
| `autosaved_at` | `datetime(3)` | KEY(entry_id, autosaved_at) | 草稿恢复提示需要自动保存时间 |
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

### classics_sancai_showcase

需求来源：三才图会静态展示页面生成、范围、生成时间、条目数量、私有内容风险提示、产物下载。静态展示生成记录属于后台全局记录，不按用户隔离。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 静态展示生成记录身份 |
| `requested_at` | `datetime(3)` | KEY | 展示生成时间 |
| `status` | `varchar(16)` | KEY | 生成状态 |
| `scope_json` | `json` |  | 生成范围快照 |
| `storage_object_id` | `bigint` |  | 静态页面产物 Storage 对象 |
| `entry_count` | `int` |  | 条目数量展示 |
| `visibility_risk_status` | `varchar(16)` |  | 可见性风险状态，例如 `PUBLIC_ONLY`、`CONTAINS_PRIVATE` |

约束：`id` 主键。索引：`requested_at`、`status`。

### classics_wangqi_document

需求来源：王圻文档 CRUD、原始文档 Storage 对象关联替换、全文阅读、安全展示、摘要标签问答展示、文档搜索、时间线、公开私有可见性、版本。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 文档实体身份 |
| `title` | `varchar(255)` |  | 文档标题展示和搜索 |
| `summary` | `text` |  | 摘要展示和内联编辑 |
| `content_format` | `varchar(16)` |  | 内容安全展示需要正文格式 |
| `content` | `longtext` |  | 全文阅读和编辑 |
| `document_time` | `datetime(3)` | KEY | 时间线浏览 |
| `storage_object_id` | `bigint` | KEY | 原始文档 Storage 对象关联和替换 |
| `visibility` | `varchar(16)` | KEY | 公开和私有可见性 |

约束：`id` 主键。索引：`document_time`、`visibility`、`storage_object_id`。

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

约束：`id` 主键。索引：`(category, visibility)`、`visibility`。

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
| `versioned_at` | `datetime(3)` | KEY(content_type, content_id, versioned_at) | 版本时间 |
| `snapshot_json` | `json` |  | 版本快照和恢复 |
| `change_type` | `varchar(32)` |  | 手动保存、AI 应用、历史恢复等变更类型 |
| `change_summary` | `varchar(512)` |  | 版本摘要展示 |

约束：`id` 主键；`(content_type, content_id, version_no)` 唯一。索引：`(content_type, content_id, versioned_at)`。

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
| `requested_at` | `datetime(3)` |  | 生成时间 |
| `expires_at` | `datetime(3)` | KEY(status, expires_at) | 过期控制 |
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
| `issued_at` | `datetime(3)` |  | 分享创建时间 |
| `expires_at` | `datetime(3)` | KEY(status, expires_at) | 分享过期时间 |
| `access_count` | `bigint` |  | 访问统计 |

约束：`id` 主键；`token_hash` 唯一。索引：`(status, expires_at)`。

### classics_share_target

需求来源：单链接多内容、跨库分享、目标删除后占位、只读访问页排序。

| Column | Type | Key | Requirement Source |
| --- | --- | --- | --- |
| `id` | `bigint` | PK, AUTO_INCREMENT | 分享目标身份 |
| `share_link_id` | `bigint` | UK(share_link_id, content_type, content_id) | 归属分享链接 |
| `content_type` | `varchar(32)` | UK(share_link_id, content_type, content_id), KEY | 跨库内容类型 |
| `content_id` | `bigint` | UK(share_link_id, content_type, content_id), KEY | 内容身份 |
| `title_snapshot` | `varchar(512)` |  | 内容删除后占位展示 |
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
| `accessed_at` | `datetime(3)` | KEY | 访问时间 |
| `access_result` | `varchar(16)` |  | 允许、过期、撤销、无权限等结果 |
| `client_snapshot` | `json` |  | 访问统计和异常追溯摘要 |

约束：`id` 主键。索引：`(share_link_id, accessed_at)`、`(share_target_id, accessed_at)`。
