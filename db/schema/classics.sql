SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `classics_sancai_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(64) NOT NULL COMMENT '门类名称',
    `category_type` varchar(16) NOT NULL DEFAULT 'FORMAL' COMMENT '门类类型',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_category_title` (`title`),
    UNIQUE KEY `uk_classics_sancai_category_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会门类表';

CREATE TABLE IF NOT EXISTS `classics_sancai_volume` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` bigint NOT NULL COMMENT '所属门类ID',
    `title` varchar(128) NOT NULL COMMENT '卷标题',
    `volume_type` varchar(16) NOT NULL DEFAULT 'MAIN' COMMENT '卷类型',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_volume_priority` (`priority`),
    KEY `idx_classics_sancai_volume_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会卷表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `volume_id` bigint NOT NULL COMMENT '所属卷ID',
    `title` varchar(255) NOT NULL COMMENT '条目标题',
    `original_text` longtext DEFAULT NULL COMMENT '原文',
    `translation_text` longtext DEFAULT NULL COMMENT '译文',
    `summary` text DEFAULT NULL COMMENT '摘要',
    `lifecycle_status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '生命周期状态',
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC' COMMENT '平台内可见性',
    `translation_status` varchar(16) NOT NULL DEFAULT 'MISSING' COMMENT '翻译状态',
    `image_status` varchar(16) NOT NULL DEFAULT 'MISSING' COMMENT '配图状态',
    `visual_asset_status` varchar(16) NOT NULL DEFAULT 'MISSING' COMMENT '视觉资产状态',
    `refinement_status` varchar(16) NOT NULL DEFAULT 'RAW' COMMENT '完善状态',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_priority` (`priority`),
    KEY `idx_classics_sancai_entry_volume` (`volume_id`),
    KEY `idx_classics_sancai_entry_lifecycle` (`lifecycle_status`, `visibility`),
    KEY `idx_classics_sancai_entry_status` (`translation_status`, `image_status`, `visual_asset_status`, `refinement_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_draft` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `entry_id` bigint NOT NULL COMMENT '条目ID',
    `autosaved_at` datetime(3) NOT NULL COMMENT '自动保存时间',
    `draft_json` json NOT NULL COMMENT '草稿快照',
    PRIMARY KEY (`id`),
    KEY `idx_classics_sancai_entry_draft_entry` (`entry_id`, `autosaved_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目草稿表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_image` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `entry_id` bigint NOT NULL COMMENT '条目ID',
    `storage_object_id` bigint NOT NULL COMMENT 'Storage对象ID',
    `image_type` varchar(16) NOT NULL COMMENT '图片类型',
    `title` varchar(512) DEFAULT NULL COMMENT '图片标题',
    `current_used` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否当前使用',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_image_object` (`entry_id`, `storage_object_id`),
    UNIQUE KEY `uk_classics_sancai_entry_image_priority` (`priority`),
    KEY `idx_classics_sancai_entry_image_entry` (`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目图片引用表';

CREATE TABLE IF NOT EXISTS `classics_sancai_visual_asset` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `entry_id` bigint NOT NULL COMMENT '条目ID',
    `version_no` int NOT NULL COMMENT '视觉资产版本号',
    `status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    `source_image_storage_object_id` bigint DEFAULT NULL COMMENT '原图Storage对象ID',
    `generated_image_storage_object_id` bigint DEFAULT NULL COMMENT 'AI生成图Storage对象ID',
    `current_used` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否当前使用版本',
    `text_weight` int NOT NULL DEFAULT 50 COMMENT '文本权重',
    `image_weight` int NOT NULL DEFAULT 50 COMMENT '图片理解权重',
    `image_analysis_markdown` longtext DEFAULT NULL COMMENT '图片理解结果',
    `fusion_description` longtext DEFAULT NULL COMMENT '融合描述',
    `visual_description` longtext DEFAULT NULL COMMENT '视觉描述',
    `generation_params_json` json DEFAULT NULL COMMENT '生图参数快照',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_visual_asset_version` (`entry_id`, `version_no`),
    KEY `idx_classics_sancai_visual_asset_current` (`entry_id`, `current_used`),
    KEY `idx_classics_sancai_visual_asset_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会视觉资产表';

CREATE TABLE IF NOT EXISTS `classics_sancai_showcase` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `requested_at` datetime(3) NOT NULL COMMENT '请求时间',
    `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '生成状态',
    `scope_json` json NOT NULL COMMENT '展示范围快照',
    `storage_object_id` bigint DEFAULT NULL COMMENT '生成产物Storage对象ID',
    `entry_count` int NOT NULL DEFAULT 0 COMMENT '条目数量',
    `visibility_risk_status` varchar(16) NOT NULL DEFAULT 'PUBLIC_ONLY' COMMENT '可见性风险状态',
    PRIMARY KEY (`id`),
    KEY `idx_classics_sancai_showcase_requested` (`requested_at`),
    KEY `idx_classics_sancai_showcase_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会静态展示页面表';

CREATE TABLE IF NOT EXISTS `classics_wangqi_document` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(255) NOT NULL COMMENT '标题',
    `summary` text DEFAULT NULL COMMENT '摘要',
    `content_format` varchar(16) NOT NULL DEFAULT 'MARKDOWN' COMMENT '正文格式',
    `content` longtext DEFAULT NULL COMMENT '正文',
    `document_time` datetime(3) DEFAULT NULL COMMENT '文档时间',
    `storage_object_id` bigint DEFAULT NULL COMMENT '原始文档Storage对象ID',
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC' COMMENT '平台内可见性',
    PRIMARY KEY (`id`),
    KEY `idx_classics_wangqi_document_time` (`document_time`),
    KEY `idx_classics_wangqi_document_visibility` (`visibility`),
    KEY `idx_classics_wangqi_document_storage_object` (`storage_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档表';

CREATE TABLE IF NOT EXISTS `classics_ming_customs_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(255) NOT NULL COMMENT '标题',
    `category` varchar(128) DEFAULT NULL COMMENT '分类',
    `chapter` varchar(128) DEFAULT NULL COMMENT '章节',
    `section` varchar(128) DEFAULT NULL COMMENT '节',
    `summary` text DEFAULT NULL COMMENT '概述或摘要',
    `content_format` varchar(16) NOT NULL DEFAULT 'MARKDOWN' COMMENT '正文格式',
    `content` longtext DEFAULT NULL COMMENT '正文',
    `original_excerpts` longtext DEFAULT NULL COMMENT '原文摘录',
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC' COMMENT '平台内可见性',
    PRIMARY KEY (`id`),
    KEY `idx_classics_ming_customs_category` (`category`, `visibility`),
    KEY `idx_classics_ming_customs_visibility` (`visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗条目表';

CREATE TABLE IF NOT EXISTS `classics_ming_customs_keyword` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `custom_id` bigint NOT NULL COMMENT '明代习俗条目ID',
    `keyword` varchar(128) NOT NULL COMMENT '关键词',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_ming_customs_keyword` (`custom_id`, `keyword`),
    UNIQUE KEY `uk_classics_ming_customs_keyword_priority` (`priority`),
    KEY `idx_classics_ming_customs_keyword_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗关键词表';

CREATE TABLE IF NOT EXISTS `classics_content_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `tag_id` bigint DEFAULT NULL COMMENT 'Knowledge标签ID',
    `tag_name_snapshot` varchar(128) NOT NULL COMMENT '标签名称快照',
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL' COMMENT '来源',
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_content_tag_name` (`content_type`, `content_id`, `tag_name_snapshot`),
    UNIQUE KEY `uk_classics_content_tag_priority` (`priority`),
    KEY `idx_classics_content_tag_content` (`content_type`, `content_id`),
    KEY `idx_classics_content_tag_tag` (`tag_id`, `content_type`),
    KEY `idx_classics_content_tag_name` (`tag_name_snapshot`, `content_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签引用表';

CREATE TABLE IF NOT EXISTS `classics_content_qa_pair` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `question` text NOT NULL COMMENT '问题',
    `answer` longtext NOT NULL COMMENT '答案',
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL' COMMENT '来源',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_content_qa_priority` (`priority`),
    KEY `idx_classics_content_qa_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容问答对表';

CREATE TABLE IF NOT EXISTS `classics_content_version` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `version_no` int NOT NULL COMMENT '版本号',
    `versioned_at` datetime(3) NOT NULL COMMENT '版本时间',
    `snapshot_json` json NOT NULL COMMENT '快照内容',
    `change_type` varchar(32) NOT NULL COMMENT '变更类型',
    `change_summary` varchar(512) DEFAULT NULL COMMENT '变更摘要',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_content_version_no` (`content_type`, `content_id`, `version_no`),
    KEY `idx_classics_content_version_time` (`content_type`, `content_id`, `versioned_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容版本表';

CREATE TABLE IF NOT EXISTS `classics_content_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `export_kind` varchar(32) NOT NULL COMMENT '导出类型',
    `content_type` varchar(32) NOT NULL COMMENT '内容范围类型',
    `export_format` varchar(16) NOT NULL COMMENT '导出格式',
    `scope_type` varchar(32) NOT NULL COMMENT '范围类型',
    `scope_json` json NOT NULL COMMENT '范围快照',
    `requested_at` datetime(3) NOT NULL COMMENT '请求时间',
    `expires_at` datetime(3) NOT NULL COMMENT '过期时间',
    `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `storage_object_id` bigint DEFAULT NULL COMMENT '导出产物Storage对象ID',
    `item_count` int NOT NULL DEFAULT 0 COMMENT '内容数量',
    `asset_count` int NOT NULL DEFAULT 0 COMMENT '资产数量',
    `visibility_risk_status` varchar(16) NOT NULL DEFAULT 'PUBLIC_ONLY' COMMENT '可见性风险状态',
    `content_changed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '导出后内容是否可能已变更',
    PRIMARY KEY (`id`),
    KEY `idx_classics_content_export_status` (`status`, `expires_at`),
    KEY `idx_classics_content_export_type` (`content_type`, `export_kind`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容导出任务表';

CREATE TABLE IF NOT EXISTS `classics_share_link` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `token_hash` varchar(128) NOT NULL COMMENT '分享令牌哈希',
    `title` varchar(256) NOT NULL COMMENT '分享标题',
    `visibility` varchar(16) NOT NULL COMMENT '分享可见性',
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '分享状态',
    `visibility_risk_status` varchar(16) NOT NULL DEFAULT 'PUBLIC_ONLY' COMMENT '可见性风险状态',
    `issued_at` datetime(3) NOT NULL COMMENT '创建时间',
    `expires_at` datetime(3) DEFAULT NULL COMMENT '过期时间',
    `access_count` bigint NOT NULL DEFAULT 0 COMMENT '访问次数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_share_link_token` (`token_hash`),
    KEY `idx_classics_share_link_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享链接表';

CREATE TABLE IF NOT EXISTS `classics_share_target` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `share_link_id` bigint NOT NULL COMMENT '分享链接ID',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `title_snapshot` varchar(512) NOT NULL COMMENT '标题快照',
    `content_visibility_snapshot` varchar(16) NOT NULL COMMENT '内容可见性快照',
    `target_status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '目标状态',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_share_target_content` (`share_link_id`, `content_type`, `content_id`),
    UNIQUE KEY `uk_classics_share_target_priority` (`priority`),
    KEY `idx_classics_share_target_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享目标表';

CREATE TABLE IF NOT EXISTS `classics_share_access_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `share_link_id` bigint NOT NULL COMMENT '分享链接ID',
    `share_target_id` bigint DEFAULT NULL COMMENT '分享目标ID',
    `accessed_at` datetime(3) NOT NULL COMMENT '访问时间',
    `access_result` varchar(16) NOT NULL COMMENT '访问结果',
    `client_snapshot` json DEFAULT NULL COMMENT '客户端摘要',
    PRIMARY KEY (`id`),
    KEY `idx_classics_share_access_link` (`share_link_id`, `accessed_at`),
    KEY `idx_classics_share_access_target` (`share_target_id`, `accessed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享访问记录表';
