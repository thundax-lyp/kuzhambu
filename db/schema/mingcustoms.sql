SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ming_customs_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `custom_id` char(26) NOT NULL,
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
    `owner_user_id` char(26) NOT NULL,
    `current_version` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_entry_id` (`custom_id`),
    KEY `idx_ming_customs_entry_category` (`category`, `visibility`, `deleted_at`),
    KEY `idx_ming_customs_entry_updated` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗条目表';

CREATE TABLE IF NOT EXISTS `ming_customs_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `qa_id` char(26) NOT NULL,
    `custom_id` char(26) NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_qa_id` (`qa_id`),
    KEY `idx_ming_customs_qa_custom_sort` (`custom_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗问答对表';

CREATE TABLE IF NOT EXISTS `ming_customs_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` char(26) NOT NULL,
    `custom_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_version_id` (`version_id`),
    UNIQUE KEY `uk_ming_customs_version_no` (`custom_id`, `version_no`),
    KEY `idx_ming_customs_version_time` (`custom_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗版本表';

CREATE TABLE IF NOT EXISTS `ming_customs_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` char(26) NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `custom_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_export_job_id` (`export_id`),
    KEY `idx_ming_customs_export_job_creator` (`created_by`, `created_at`),
    KEY `idx_ming_customs_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗导出产物表';
