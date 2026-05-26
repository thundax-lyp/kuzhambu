SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sancai_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `category_code` varchar(16) NOT NULL,
    `name` varchar(64) NOT NULL,
    `formal` tinyint(1) NOT NULL DEFAULT 1,
    `sort_order` int NOT NULL DEFAULT 0,
    `description` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_category_code` (`category_code`),
    KEY `idx_sancai_category_sort` (`formal`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会门类表';

CREATE TABLE IF NOT EXISTS `sancai_volume` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `volume_id` char(26) NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `volume_no` int DEFAULT NULL,
    `title` varchar(128) NOT NULL,
    `auxiliary` tinyint(1) NOT NULL DEFAULT 0,
    `sort_order` int NOT NULL DEFAULT 0,
    `entry_count` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_volume_id` (`volume_id`),
    UNIQUE KEY `uk_sancai_volume_category_no` (`category_code`, `volume_no`),
    KEY `idx_sancai_volume_category_sort` (`category_code`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会卷表';

CREATE TABLE IF NOT EXISTS `sancai_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `entry_id` char(26) NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `volume_id` char(26) NOT NULL,
    `entry_no` int DEFAULT NULL,
    `title` varchar(255) NOT NULL,
    `original_text` longtext DEFAULT NULL,
    `translation_text` longtext DEFAULT NULL,
    `summary` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `lifecycle_status` varchar(16) NOT NULL DEFAULT 'DRAFT',
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` char(26) NOT NULL,
    `translation_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `image_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `visual_asset_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `refinement_status` varchar(16) NOT NULL DEFAULT 'RAW',
    `current_version` int NOT NULL DEFAULT 0,
    `operated_at` datetime(3) NOT NULL,
    `autosaved_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_id` (`entry_id`),
    KEY `idx_sancai_entry_volume` (`volume_id`, `entry_no`),
    KEY `idx_sancai_entry_category_status` (`category_code`, `lifecycle_status`, `visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目表';

CREATE TABLE IF NOT EXISTS `sancai_entry_image` (
    `entry_id` char(26) NOT NULL,
    `object_id` char(26) NOT NULL,
    `image_role` varchar(16) NOT NULL,
    `current_used` tinyint(1) NOT NULL DEFAULT 0,
    `sort_order` int NOT NULL DEFAULT 0,
    `caption` varchar(512) DEFAULT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`entry_id`, `object_id`),
    KEY `idx_sancai_entry_image_object` (`object_id`),
    KEY `idx_sancai_entry_image_sort` (`entry_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目图片引用表';

CREATE TABLE IF NOT EXISTS `sancai_entry_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `qa_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    `operated_at` datetime(3) NOT NULL,
    `autosaved_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_qa_id` (`qa_id`),
    KEY `idx_sancai_entry_qa_entry_sort` (`entry_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目问答对表';

CREATE TABLE IF NOT EXISTS `sancai_entry_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_version_id` (`version_id`),
    UNIQUE KEY `uk_sancai_entry_version_no` (`entry_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目版本表';

CREATE TABLE IF NOT EXISTS `sancai_entry_draft` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `draft_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `user_id` char(26) NOT NULL,
    `draft_json` longtext NOT NULL,
    `autosaved_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_draft_id` (`draft_id`),
    UNIQUE KEY `uk_sancai_entry_draft_user_entry` (`entry_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目草稿表';

CREATE TABLE IF NOT EXISTS `sancai_visual_asset` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `asset_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `source_object_id` char(26) DEFAULT NULL,
    `generated_object_id` char(26) DEFAULT NULL,
    `image_analysis` longtext DEFAULT NULL,
    `fusion_description` longtext DEFAULT NULL,
    `visual_description` longtext DEFAULT NULL,
    `text_weight` int NOT NULL DEFAULT 50,
    `image_weight` int NOT NULL DEFAULT 50,
    `generation_params` text DEFAULT NULL,
    `current_used` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_visual_asset_id` (`asset_id`),
    UNIQUE KEY `uk_sancai_visual_asset_version` (`entry_id`, `version_no`),
    KEY `idx_sancai_visual_asset_entry_current` (`entry_id`, `current_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会视觉资产表';

CREATE TABLE IF NOT EXISTS `sancai_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` char(26) NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `entry_count` int NOT NULL DEFAULT 0,
    `asset_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_export_job_id` (`export_id`),
    KEY `idx_sancai_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会导出产物表';

CREATE TABLE IF NOT EXISTS `sancai_showcase_page` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `showcase_id` char(26) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) NOT NULL,
    `entry_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `private_risk_confirmed` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_showcase_page_id` (`showcase_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会静态展示页面表';
