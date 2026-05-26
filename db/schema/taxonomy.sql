SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `taxonomy_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `category_id` char(26) NOT NULL,
    `name` varchar(128) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `sort_order` int NOT NULL DEFAULT 0,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_category_id` (`category_id`),
    UNIQUE KEY `uk_taxonomy_category_name` (`name`),
    KEY `idx_taxonomy_category_sort` (`enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签分类表';

CREATE TABLE IF NOT EXISTS `taxonomy_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tag_id` char(26) NOT NULL,
    `tag_name` varchar(128) NOT NULL,
    `category_id` char(26) DEFAULT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    `source` varchar(32) NOT NULL DEFAULT 'AI_EXTRACTED',
    `merge_target_tag_id` char(26) DEFAULT NULL,
    `operator_user_id` char(26) DEFAULT NULL,
    `reviewer_user_id` char(26) DEFAULT NULL,
    `reviewed_at` datetime(3) DEFAULT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_tag_id` (`tag_id`),
    UNIQUE KEY `uk_taxonomy_tag_name` (`tag_name`),
    KEY `idx_taxonomy_tag_category` (`category_id`, `status`),
    KEY `idx_taxonomy_tag_status` (`status`, `source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一标签表';

CREATE TABLE IF NOT EXISTS `taxonomy_tag_alias` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `alias_id` char(26) NOT NULL,
    `tag_id` char(26) NOT NULL,
    `alias_name` varchar(128) NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_tag_alias_id` (`alias_id`),
    UNIQUE KEY `uk_taxonomy_tag_alias_name` (`alias_name`),
    KEY `idx_taxonomy_tag_alias_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签别名表';

CREATE TABLE IF NOT EXISTS `taxonomy_content_tag_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `relation_id` char(26) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` char(26) NOT NULL,
    `tag_id` char(26) NOT NULL,
    `source` varchar(32) NOT NULL DEFAULT 'MANUAL',
    `operator_user_id` char(26) DEFAULT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_content_tag_relation_id` (`relation_id`),
    UNIQUE KEY `uk_taxonomy_content_tag_relation` (`content_type`, `content_id`, `tag_id`),
    KEY `idx_taxonomy_content_tag_content` (`content_type`, `content_id`),
    KEY `idx_taxonomy_content_tag_tag` (`tag_id`, `content_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签关联表';

CREATE TABLE IF NOT EXISTS `taxonomy_synonym` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `synonym_id` char(26) NOT NULL,
    `term` varchar(128) NOT NULL,
    `synonym` varchar(128) NOT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_synonym_id` (`synonym_id`),
    UNIQUE KEY `uk_taxonomy_synonym_pair` (`term`, `synonym`),
    KEY `idx_taxonomy_synonym_term` (`term`, `enabled`),
    KEY `idx_taxonomy_synonym_reverse` (`synonym`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同义词表';

CREATE TABLE IF NOT EXISTS `taxonomy_operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `operation_id` char(26) NOT NULL,
    `operation_type` varchar(32) NOT NULL,
    `tag_id` char(26) NOT NULL,
    `target_tag_id` char(26) DEFAULT NULL,
    `detail_json` text DEFAULT NULL,
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_operation_log_id` (`operation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签治理操作日志表';
