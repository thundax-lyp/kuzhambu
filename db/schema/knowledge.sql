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
    `merged_to_tag_id` bigint DEFAULT NULL,
    `deprecated_at` datetime(3) DEFAULT NULL,
    `deprecated_by` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_tag_tag_id` (`tag_id`),
    UNIQUE KEY `uk_knowledge_tag_name` (`name`),
    KEY `idx_knowledge_tag_category_status` (`category_id`, `status`),
    KEY `idx_knowledge_tag_review_status` (`review_status`, `created_at`),
    KEY `idx_knowledge_tag_source_status` (`source`, `status`),
    KEY `idx_knowledge_tag_merged_to_tag_id` (`merged_to_tag_id`),
    KEY `idx_knowledge_tag_deprecated_at` (`deprecated_at`)
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

CREATE TABLE IF NOT EXISTS `knowledge_graph_extraction_task` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `task_id` bigint NOT NULL,
    `task_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) DEFAULT NULL,
    `scope_json` json DEFAULT NULL,
    `source_content_type` varchar(32) NOT NULL,
    `source_content_id` bigint NOT NULL,
    `ai_call_id` bigint DEFAULT NULL,
    `ai_candidate_id` bigint DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `error_type` varchar(64) DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    `requested_by` bigint DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    `applied_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_extraction_task_task_id` (`task_id`),
    KEY `idx_knowledge_graph_extraction_task_status_requested` (`status`, `requested_at`),
    KEY `idx_knowledge_graph_extraction_task_source` (`source_content_type`, `source_content_id`),
    KEY `idx_knowledge_graph_extraction_task_call_candidate` (`ai_call_id`, `ai_candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱抽取任务表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` bigint NOT NULL,
    `task_id` bigint NOT NULL,
    `candidate_id` bigint NOT NULL,
    `task_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) DEFAULT NULL,
    `scope_json` json DEFAULT NULL,
    `source_content_type` varchar(32) NOT NULL,
    `source_content_id` bigint NOT NULL,
    `version_no` int NOT NULL,
    `status` varchar(32) NOT NULL,
    `applied_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_version_version_id` (`version_id`),
    UNIQUE KEY `uk_knowledge_graph_version_task_candidate` (`task_id`, `candidate_id`),
    UNIQUE KEY `uk_knowledge_graph_version_source_version` (`task_type`, `source_content_type`, `source_content_id`, `version_no`),
    KEY `idx_knowledge_graph_version_source_status` (`task_type`, `source_content_type`, `source_content_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱正式版本表';

CREATE TABLE IF NOT EXISTS `knowledge_entity` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `entity_id` bigint NOT NULL,
    `entity_key` varchar(160) NOT NULL,
    `name` varchar(128) NOT NULL,
    `entity_type` varchar(64) NOT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `confirmation_status` varchar(32) NOT NULL,
    `latest_version_id` bigint NOT NULL,
    `source_refs_json` json DEFAULT NULL,
    `first_extracted_at` datetime(3) NOT NULL,
    `last_extracted_at` datetime(3) NOT NULL,
    `confirmed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_entity_entity_id` (`entity_id`),
    UNIQUE KEY `uk_knowledge_entity_entity_key` (`entity_key`),
    KEY `idx_knowledge_entity_latest_version` (`latest_version_id`),
    KEY `idx_knowledge_entity_confirmation_status` (`confirmation_status`, `last_extracted_at`),
    KEY `idx_knowledge_entity_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱实体表';

CREATE TABLE IF NOT EXISTS `knowledge_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `relation_id` bigint NOT NULL,
    `relation_key` varchar(256) NOT NULL,
    `source_entity_key` varchar(160) NOT NULL,
    `target_entity_key` varchar(160) NOT NULL,
    `source_name` varchar(128) NOT NULL,
    `target_name` varchar(128) NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `evidence` varchar(1024) DEFAULT NULL,
    `confirmation_status` varchar(32) NOT NULL,
    `latest_version_id` bigint NOT NULL,
    `source_refs_json` json DEFAULT NULL,
    `first_extracted_at` datetime(3) NOT NULL,
    `last_extracted_at` datetime(3) NOT NULL,
    `confirmed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_relation_relation_id` (`relation_id`),
    UNIQUE KEY `uk_knowledge_relation_relation_key` (`relation_key`),
    KEY `idx_knowledge_relation_latest_version` (`latest_version_id`),
    KEY `idx_knowledge_relation_confirmation_status` (`confirmation_status`, `last_extracted_at`),
    KEY `idx_knowledge_relation_source_target` (`source_entity_key`, `target_entity_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱关系表';

CREATE TABLE IF NOT EXISTS `knowledge_lineage_node` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `node_id` bigint NOT NULL,
    `node_key` varchar(160) NOT NULL,
    `name` varchar(128) NOT NULL,
    `node_type` varchar(64) NOT NULL,
    `generation` int DEFAULT NULL,
    `gender` varchar(32) DEFAULT NULL,
    `confirmation_status` varchar(32) NOT NULL,
    `latest_version_id` bigint NOT NULL,
    `source_refs_json` json DEFAULT NULL,
    `first_extracted_at` datetime(3) NOT NULL,
    `last_extracted_at` datetime(3) NOT NULL,
    `confirmed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_lineage_node_node_id` (`node_id`),
    UNIQUE KEY `uk_knowledge_lineage_node_node_key` (`node_key`),
    KEY `idx_knowledge_lineage_node_latest_version` (`latest_version_id`),
    KEY `idx_knowledge_lineage_node_confirmation_status` (`confirmation_status`, `last_extracted_at`),
    KEY `idx_knowledge_lineage_node_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='世系节点表';

CREATE TABLE IF NOT EXISTS `knowledge_lineage_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `relation_id` bigint NOT NULL,
    `relation_key` varchar(256) NOT NULL,
    `source_node_key` varchar(160) NOT NULL,
    `target_node_key` varchar(160) NOT NULL,
    `source_name` varchar(128) NOT NULL,
    `target_name` varchar(128) NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `evidence` varchar(1024) DEFAULT NULL,
    `confirmation_status` varchar(32) NOT NULL,
    `latest_version_id` bigint NOT NULL,
    `source_refs_json` json DEFAULT NULL,
    `first_extracted_at` datetime(3) NOT NULL,
    `last_extracted_at` datetime(3) NOT NULL,
    `confirmed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_lineage_relation_relation_id` (`relation_id`),
    UNIQUE KEY `uk_knowledge_lineage_relation_relation_key` (`relation_key`),
    KEY `idx_knowledge_lineage_relation_latest_version` (`latest_version_id`),
    KEY `idx_knowledge_lineage_relation_confirmation_status` (`confirmation_status`, `last_extracted_at`),
    KEY `idx_knowledge_lineage_relation_source_target` (`source_node_key`, `target_node_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='世系关系表';
