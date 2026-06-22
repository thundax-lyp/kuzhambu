SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_tag_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `category_id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `priority` int NOT NULL,
    `status` varchar(32) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_tag_category_category_id` (`category_id`),
    UNIQUE KEY `uk_knowledge_tag_category_name` (`name`),
    UNIQUE KEY `uk_knowledge_tag_category_priority` (`priority`),
    KEY `idx_knowledge_tag_category_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签分类表';

CREATE TABLE IF NOT EXISTS `knowledge_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tag_id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `category_id` bigint DEFAULT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `source` varchar(32) NOT NULL,
    `review_status` varchar(32) NOT NULL,
    `review_note` varchar(512) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `reviewed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_tag_tag_id` (`tag_id`),
    UNIQUE KEY `uk_knowledge_tag_name` (`name`),
    KEY `idx_knowledge_tag_category_status` (`category_id`, `status`),
    KEY `idx_knowledge_tag_review_status` (`review_status`, `created_at`),
    KEY `idx_knowledge_tag_source_status` (`source`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一标签表';

CREATE TABLE IF NOT EXISTS `knowledge_tag_alias` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `alias_id` bigint NOT NULL,
    `tag_id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `source` varchar(32) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_tag_alias_alias_id` (`alias_id`),
    UNIQUE KEY `uk_knowledge_tag_alias_name` (`name`),
    KEY `idx_knowledge_tag_alias_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签别名表';

CREATE TABLE IF NOT EXISTS `knowledge_tag_content_ref` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `ref_id` bigint NOT NULL,
    `tag_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` bigint NOT NULL,
    `content_title` varchar(255) NOT NULL,
    `source` varchar(32) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_tag_content_ref_ref_id` (`ref_id`),
    UNIQUE KEY `uk_knowledge_tag_content_ref_unique` (`tag_id`, `content_type`, `content_id`),
    KEY `idx_knowledge_tag_content_ref_tag_id` (`tag_id`),
    KEY `idx_knowledge_tag_content_ref_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签关联表';

CREATE TABLE IF NOT EXISTS `knowledge_synonym` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `synonym_id` bigint NOT NULL,
    `term` varchar(128) NOT NULL,
    `synonym` varchar(128) NOT NULL,
    `status` varchar(32) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_synonym_synonym_id` (`synonym_id`),
    UNIQUE KEY `uk_knowledge_synonym_pair` (`term`, `synonym`),
    KEY `idx_knowledge_synonym_term_status` (`term`, `status`),
    KEY `idx_knowledge_synonym_synonym_status` (`synonym`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同义词表';
