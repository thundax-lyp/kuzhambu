SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `category_id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `sort_order` int NOT NULL DEFAULT 0,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_category_id` (`category_id`),
    UNIQUE KEY `uk_knowledge_category_name` (`name`),
    KEY `idx_knowledge_category_sort` (`enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签分类表';

CREATE TABLE IF NOT EXISTS `knowledge_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tag_id` bigint NOT NULL,
    `tag_name` varchar(128) NOT NULL,
    `category_id` bigint DEFAULT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    `source` varchar(32) NOT NULL DEFAULT 'AI_EXTRACTED',
    `merge_target_tag_id` bigint DEFAULT NULL,
    `extracted_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_tag_id` (`tag_id`),
    UNIQUE KEY `uk_knowledge_tag_name` (`tag_name`),
    KEY `idx_knowledge_tag_category` (`category_id`, `status`),
    KEY `idx_knowledge_tag_status` (`status`, `source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一标签表';

CREATE TABLE IF NOT EXISTS `knowledge_tag_alias` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `alias_id` bigint NOT NULL,
    `tag_id` bigint NOT NULL,
    `alias_name` varchar(128) NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_tag_alias_id` (`alias_id`),
    UNIQUE KEY `uk_knowledge_tag_alias_name` (`alias_name`),
    KEY `idx_knowledge_tag_alias_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签别名表';

CREATE TABLE IF NOT EXISTS `knowledge_content_tag_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `relation_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` bigint NOT NULL,
    `tag_id` bigint NOT NULL,
    `source` varchar(32) NOT NULL DEFAULT 'MANUAL',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_content_tag_relation_id` (`relation_id`),
    UNIQUE KEY `uk_knowledge_content_tag_relation` (`content_type`, `content_id`, `tag_id`),
    KEY `idx_knowledge_content_tag_content` (`content_type`, `content_id`),
    KEY `idx_knowledge_content_tag_tag` (`tag_id`, `content_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签关联表';

CREATE TABLE IF NOT EXISTS `knowledge_synonym` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `synonym_id` bigint NOT NULL,
    `term` varchar(128) NOT NULL,
    `synonym` varchar(128) NOT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_synonym_id` (`synonym_id`),
    UNIQUE KEY `uk_knowledge_synonym_pair` (`term`, `synonym`),
    KEY `idx_knowledge_synonym_term` (`term`, `enabled`),
    KEY `idx_knowledge_synonym_reverse` (`synonym`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同义词表';
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_refinement_work_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `work_item_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` bigint NOT NULL,
    `category_code` varchar(16) DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `quality_sample` tinyint(1) NOT NULL DEFAULT 0,
    `entity_count` int NOT NULL DEFAULT 0,
    `relation_count` int NOT NULL DEFAULT 0,
    `verified_entity_count` int NOT NULL DEFAULT 0,
    `verified_relation_count` int NOT NULL DEFAULT 0,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_refinement_work_item_id` (`work_item_id`),
    UNIQUE KEY `uk_knowledge_refinement_work_item_content` (`content_type`, `content_id`),
    KEY `idx_knowledge_refinement_work_item_filter` (`category_code`, `status`, `quality_sample`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据精修工作项表';

CREATE TABLE IF NOT EXISTS `knowledge_refinement_entity_annotation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `annotation_id` bigint NOT NULL,
    `work_item_id` bigint NOT NULL,
    `content_id` bigint NOT NULL,
    `entity_name` varchar(255) NOT NULL,
    `entity_type` varchar(32) NOT NULL,
    `normalized_name` varchar(255) DEFAULT NULL,
    `source_text` text DEFAULT NULL,
    `confidence` decimal(5, 4) NOT NULL DEFAULT 0.0000,
    `verified` tinyint(1) NOT NULL DEFAULT 0,
    `source` varchar(32) NOT NULL DEFAULT 'AI_EXTRACTED',
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `kg_entity_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_refinement_entity_annotation_id` (`annotation_id`),
    KEY `idx_knowledge_refinement_entity_work_item` (`work_item_id`, `status`),
    KEY `idx_knowledge_refinement_entity_content` (`content_id`, `entity_type`),
    KEY `idx_knowledge_refinement_entity_kg` (`kg_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据精修实体标注表';

CREATE TABLE IF NOT EXISTS `knowledge_refinement_relation_annotation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `annotation_id` bigint NOT NULL,
    `work_item_id` bigint NOT NULL,
    `content_id` bigint NOT NULL,
    `source_entity_annotation_id` bigint NOT NULL,
    `target_entity_annotation_id` bigint NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `confidence` decimal(5, 4) NOT NULL DEFAULT 0.0000,
    `verified` tinyint(1) NOT NULL DEFAULT 0,
    `source` varchar(32) NOT NULL DEFAULT 'AI_EXTRACTED',
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `kg_relation_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_refinement_relation_annotation_id` (`annotation_id`),
    UNIQUE KEY `uk_knowledge_refinement_relation_unique` (`work_item_id`, `source_entity_annotation_id`, `target_entity_annotation_id`, `relation_type`),
    KEY `idx_knowledge_refinement_relation_work_item` (`work_item_id`, `status`),
    KEY `idx_knowledge_refinement_relation_content` (`content_id`, `relation_type`),
    KEY `idx_knowledge_refinement_relation_kg` (`kg_relation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据精修关系标注表';
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_graph_entity` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `entity_id` bigint NOT NULL,
    `name` varchar(255) NOT NULL,
    `entity_type` varchar(32) NOT NULL,
    `category_code` varchar(16) DEFAULT NULL,
    `description` text DEFAULT NULL,
    `confidence` decimal(5, 4) NOT NULL DEFAULT 0.0000,
    `verified` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `extract_version` int NOT NULL DEFAULT 0,
    `last_extracted_at` datetime(3) DEFAULT NULL,
    `last_refined_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_entity_id` (`entity_id`),
    UNIQUE KEY `uk_knowledge_graph_entity_name_type` (`name`, `entity_type`),
    KEY `idx_knowledge_graph_entity_category` (`category_code`, `status`),
    KEY `idx_knowledge_graph_entity_type` (`entity_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱实体表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `relation_id` bigint NOT NULL,
    `source_entity_id` bigint NOT NULL,
    `target_entity_id` bigint NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `description` text DEFAULT NULL,
    `confidence` decimal(5, 4) NOT NULL DEFAULT 0.0000,
    `verified` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `extract_version` int NOT NULL DEFAULT 0,
    `last_extracted_at` datetime(3) DEFAULT NULL,
    `last_refined_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_relation_id` (`relation_id`),
    UNIQUE KEY `uk_knowledge_graph_relation_pair` (`source_entity_id`, `target_entity_id`, `relation_type`),
    KEY `idx_knowledge_graph_relation_source` (`source_entity_id`, `status`),
    KEY `idx_knowledge_graph_relation_target` (`target_entity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱关系表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_source_ref` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `source_ref_id` bigint NOT NULL,
    `graph_object_type` varchar(16) NOT NULL,
    `graph_object_id` bigint NOT NULL,
    `entry_id` bigint NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `source_status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_source_ref_id` (`source_ref_id`),
    UNIQUE KEY `uk_knowledge_graph_source_ref_object_entry` (`graph_object_type`, `graph_object_id`, `entry_id`),
    KEY `idx_knowledge_graph_source_ref_entry` (`entry_id`, `source_status`),
    KEY `idx_knowledge_graph_source_ref_category` (`category_code`, `source_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱来源引用表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_extraction_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `job_id` bigint NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'RUNNING',
    `extract_version` int NOT NULL DEFAULT 0,
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `requester_user_id` bigint NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    `started_at` datetime(3) DEFAULT NULL,
    `finished_at` datetime(3) DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_extraction_job_id` (`job_id`),
    KEY `idx_knowledge_graph_extraction_job_status` (`status`, `requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱提取任务表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_extraction_job_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `job_item_id` bigint NOT NULL,
    `job_id` bigint NOT NULL,
    `entry_id` bigint NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `entity_count` int NOT NULL DEFAULT 0,
    `relation_count` int NOT NULL DEFAULT 0,
    `error_message` varchar(1024) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_extraction_job_item_id` (`job_item_id`),
    UNIQUE KEY `uk_knowledge_graph_extraction_job_item_entry` (`job_id`, `entry_id`),
    KEY `idx_knowledge_graph_extraction_job_item_status` (`job_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱提取任务明细表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_quality_metric` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `metric_id` bigint NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `extract_version` int NOT NULL DEFAULT 0,
    `entity_coverage_rate` decimal(7, 4) NOT NULL DEFAULT 0.0000,
    `relation_accuracy_rate` decimal(7, 4) NOT NULL DEFAULT 0.0000,
    `completeness_rate` decimal(7, 4) NOT NULL DEFAULT 0.0000,
    `sampled_relation_count` int NOT NULL DEFAULT 0,
    `verified_relation_count` int NOT NULL DEFAULT 0,
    `last_extracted_at` datetime(3) DEFAULT NULL,
    `calculated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_quality_metric_id` (`metric_id`),
    UNIQUE KEY `uk_knowledge_graph_quality_metric_category` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱质量指标表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_lineage_node` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `lineage_node_id` bigint NOT NULL,
    `polity_name` varchar(128) NOT NULL,
    `ruler_name` varchar(128) NOT NULL,
    `title` varchar(128) DEFAULT NULL,
    `reign_label` varchar(255) DEFAULT NULL,
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_lineage_node_id` (`lineage_node_id`),
    KEY `idx_knowledge_graph_lineage_node_polity` (`polity_name`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='世系图节点表';

CREATE TABLE IF NOT EXISTS `knowledge_graph_lineage_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `lineage_relation_id` bigint NOT NULL,
    `source_node_id` bigint NOT NULL,
    `target_node_id` bigint NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_lineage_relation_id` (`lineage_relation_id`),
    UNIQUE KEY `uk_knowledge_graph_lineage_relation_pair` (`source_node_id`, `target_node_id`, `relation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='世系图关系表';
