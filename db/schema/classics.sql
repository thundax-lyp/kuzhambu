SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `classics_sancai_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `category_code` varchar(16) NOT NULL,
    `name` varchar(64) NOT NULL,
    `formal` tinyint(1) NOT NULL DEFAULT 1,
    `sort_order` int NOT NULL DEFAULT 0,
    `description` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_category_code` (`category_code`),
    KEY `idx_classics_sancai_category_sort` (`formal`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会门类表';

CREATE TABLE IF NOT EXISTS `classics_sancai_volume` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `volume_id` bigint NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `volume_no` int DEFAULT NULL,
    `title` varchar(128) NOT NULL,
    `auxiliary` tinyint(1) NOT NULL DEFAULT 0,
    `sort_order` int NOT NULL DEFAULT 0,
    `entry_count` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_volume_id` (`volume_id`),
    UNIQUE KEY `uk_classics_sancai_volume_category_no` (`category_code`, `volume_no`),
    KEY `idx_classics_sancai_volume_category_sort` (`category_code`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会卷表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `entry_id` bigint NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `volume_id` bigint NOT NULL,
    `entry_no` int DEFAULT NULL,
    `title` varchar(255) NOT NULL,
    `original_text` longtext DEFAULT NULL,
    `translation_text` longtext DEFAULT NULL,
    `summary` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `lifecycle_status` varchar(16) NOT NULL DEFAULT 'DRAFT',
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` bigint NOT NULL,
    `translation_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `image_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `visual_asset_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `refinement_status` varchar(16) NOT NULL DEFAULT 'RAW',
    `current_version` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_id` (`entry_id`),
    KEY `idx_classics_sancai_entry_volume` (`volume_id`, `entry_no`),
    KEY `idx_classics_sancai_entry_category_status` (`category_code`, `lifecycle_status`, `visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_image` (
    `entry_id` bigint NOT NULL,
    `object_id` bigint NOT NULL,
    `image_role` varchar(16) NOT NULL,
    `current_used` tinyint(1) NOT NULL DEFAULT 0,
    `sort_order` int NOT NULL DEFAULT 0,
    `caption` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`entry_id`, `object_id`),
    KEY `idx_classics_sancai_entry_image_object` (`object_id`),
    KEY `idx_classics_sancai_entry_image_sort` (`entry_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目图片引用表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `discovery_qa_id` bigint NOT NULL,
    `entry_id` bigint NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_qa_id` (`discovery_qa_id`),
    KEY `idx_classics_sancai_entry_qa_entry_sort` (`entry_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目问答对表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` bigint NOT NULL,
    `entry_id` bigint NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `versioned_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_version_id` (`version_id`),
    UNIQUE KEY `uk_classics_sancai_entry_version_no` (`entry_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目版本表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_draft` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `draft_id` bigint NOT NULL,
    `entry_id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `draft_json` longtext NOT NULL,
    `autosaved_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_draft_id` (`draft_id`),
    UNIQUE KEY `uk_classics_sancai_entry_draft_user_entry` (`entry_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目草稿表';

CREATE TABLE IF NOT EXISTS `classics_sancai_visual_asset` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `asset_id` bigint NOT NULL,
    `entry_id` bigint NOT NULL,
    `version_no` int NOT NULL,
    `source_object_id` bigint DEFAULT NULL,
    `generated_object_id` bigint DEFAULT NULL,
    `image_analysis` longtext DEFAULT NULL,
    `fusion_description` longtext DEFAULT NULL,
    `visual_description` longtext DEFAULT NULL,
    `text_weight` int NOT NULL DEFAULT 50,
    `image_weight` int NOT NULL DEFAULT 50,
    `generation_params` text DEFAULT NULL,
    `current_used` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_visual_asset_id` (`asset_id`),
    UNIQUE KEY `uk_classics_sancai_visual_asset_version` (`entry_id`, `version_no`),
    KEY `idx_classics_sancai_visual_asset_entry_current` (`entry_id`, `current_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会视觉资产表';

CREATE TABLE IF NOT EXISTS `classics_sancai_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` bigint NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` bigint DEFAULT NULL,
    `entry_count` int NOT NULL DEFAULT 0,
    `asset_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `content_changed` tinyint(1) NOT NULL DEFAULT 0,
    `requester_user_id` bigint NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_export_job_id` (`export_id`),
    KEY `idx_classics_sancai_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会导出产物表';

CREATE TABLE IF NOT EXISTS `classics_sancai_showcase_page` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `showcase_id` bigint NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` bigint NOT NULL,
    `entry_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `private_risk_confirmed` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `requester_user_id` bigint NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_showcase_page_id` (`showcase_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会静态展示页面表';
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `classics_wangqi_document` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `document_id` bigint NOT NULL,
    `title` varchar(255) NOT NULL,
    `content` longtext DEFAULT NULL,
    `content_format` varchar(16) NOT NULL DEFAULT 'MARKDOWN',
    `summary` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `file_object_id` bigint DEFAULT NULL,
    `word_count` int NOT NULL DEFAULT 0,
    `document_time` datetime(3) DEFAULT NULL,
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` bigint NOT NULL,
    `current_version` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_wangqi_document_id` (`document_id`),
    KEY `idx_classics_wangqi_document_file` (`file_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档表';

CREATE TABLE IF NOT EXISTS `classics_wangqi_document_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `discovery_qa_id` bigint NOT NULL,
    `document_id` bigint NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_wangqi_document_qa_id` (`discovery_qa_id`),
    KEY `idx_classics_wangqi_document_qa_document_sort` (`document_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档问答对表';

CREATE TABLE IF NOT EXISTS `classics_wangqi_document_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` bigint NOT NULL,
    `document_id` bigint NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `versioned_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_wangqi_document_version_id` (`version_id`),
    UNIQUE KEY `uk_classics_wangqi_document_version_no` (`document_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档版本表';

CREATE TABLE IF NOT EXISTS `classics_wangqi_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` bigint NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` bigint DEFAULT NULL,
    `document_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `content_changed` tinyint(1) NOT NULL DEFAULT 0,
    `requester_user_id` bigint NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_wangqi_export_job_id` (`export_id`),
    KEY `idx_classics_wangqi_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻导出产物表';
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `classics_ming_customs_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `custom_id` bigint NOT NULL,
    `title` varchar(255) NOT NULL,
    `summary` text DEFAULT NULL,
    `content` longtext DEFAULT NULL,
    `category` varchar(128) DEFAULT NULL,
    `chapter` varchar(128) DEFAULT NULL,
    `section` varchar(128) DEFAULT NULL,
    `keywords_snapshot` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `original_excerpts` longtext DEFAULT NULL,
    `word_count` int NOT NULL DEFAULT 0,
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` bigint NOT NULL,
    `current_version` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_ming_customs_entry_id` (`custom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗条目表';

CREATE TABLE IF NOT EXISTS `classics_ming_customs_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `discovery_qa_id` bigint NOT NULL,
    `custom_id` bigint NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_ming_customs_qa_id` (`discovery_qa_id`),
    KEY `idx_classics_ming_customs_qa_custom_sort` (`custom_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗问答对表';

CREATE TABLE IF NOT EXISTS `classics_ming_customs_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` bigint NOT NULL,
    `custom_id` bigint NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `versioned_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_ming_customs_version_id` (`version_id`),
    UNIQUE KEY `uk_classics_ming_customs_version_no` (`custom_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗版本表';

CREATE TABLE IF NOT EXISTS `classics_ming_customs_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` bigint NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` bigint DEFAULT NULL,
    `custom_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `content_changed` tinyint(1) NOT NULL DEFAULT 0,
    `requester_user_id` bigint NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_ming_customs_export_job_id` (`export_id`),
    KEY `idx_classics_ming_customs_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗导出产物表';
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `classics_sharing_link` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `share_id` bigint NOT NULL,
    `share_token` varchar(96) NOT NULL,
    `owner_user_id` bigint NOT NULL,
    `title` varchar(256) NOT NULL,
    `visibility` varchar(16) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `risk_confirmed` tinyint(1) NOT NULL DEFAULT 0,
    `issued_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `access_count` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sharing_link_id` (`share_id`),
    UNIQUE KEY `uk_classics_sharing_link_token` (`share_token`),
    KEY `idx_classics_sharing_link_owner` (`owner_user_id`, `issued_at`),
    KEY `idx_classics_sharing_link_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享链接表';

CREATE TABLE IF NOT EXISTS `classics_sharing_target` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `target_id` bigint NOT NULL,
    `share_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` bigint NOT NULL,
    `knowledge_base` varchar(64) NOT NULL,
    `title_snapshot` varchar(512) NOT NULL,
    `content_private_snapshot` tinyint(1) NOT NULL DEFAULT 0,
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sharing_target_id` (`target_id`),
    UNIQUE KEY `uk_classics_sharing_target_content` (`share_id`, `content_type`, `content_id`),
    KEY `idx_classics_sharing_target_share` (`share_id`, `sort_order`),
    KEY `idx_classics_sharing_target_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享目标表';
