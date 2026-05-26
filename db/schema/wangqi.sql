SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `wangqi_document` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `document_id` char(26) NOT NULL,
    `title` varchar(255) NOT NULL,
    `content` longtext DEFAULT NULL,
    `content_format` varchar(16) NOT NULL DEFAULT 'MARKDOWN',
    `summary` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `file_object_id` char(26) DEFAULT NULL,
    `word_count` int NOT NULL DEFAULT 0,
    `document_time` datetime(3) DEFAULT NULL,
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` char(26) NOT NULL,
    `current_version` int NOT NULL DEFAULT 0,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_document_id` (`document_id`),
    KEY `idx_wangqi_document_file` (`file_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档表';

CREATE TABLE IF NOT EXISTS `wangqi_document_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `qa_id` char(26) NOT NULL,
    `document_id` char(26) NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_document_qa_id` (`qa_id`),
    KEY `idx_wangqi_document_qa_document_sort` (`document_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档问答对表';

CREATE TABLE IF NOT EXISTS `wangqi_document_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` char(26) NOT NULL,
    `document_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_document_version_id` (`version_id`),
    UNIQUE KEY `uk_wangqi_document_version_no` (`document_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档版本表';

CREATE TABLE IF NOT EXISTS `wangqi_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` char(26) NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `document_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_export_job_id` (`export_id`),
    KEY `idx_wangqi_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻导出产物表';
