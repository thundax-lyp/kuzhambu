SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `data_refinement_work_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `work_item_id` char(26) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` char(26) NOT NULL,
    `category_code` varchar(16) DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `quality_sample` tinyint(1) NOT NULL DEFAULT 0,
    `entity_count` int NOT NULL DEFAULT 0,
    `relation_count` int NOT NULL DEFAULT 0,
    `verified_entity_count` int NOT NULL DEFAULT 0,
    `verified_relation_count` int NOT NULL DEFAULT 0,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_refinement_work_item_id` (`work_item_id`),
    UNIQUE KEY `uk_data_refinement_work_item_content` (`content_type`, `content_id`),
    KEY `idx_data_refinement_work_item_filter` (`category_code`, `status`, `quality_sample`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据精修工作项表';

CREATE TABLE IF NOT EXISTS `data_refinement_entity_annotation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `annotation_id` char(26) NOT NULL,
    `work_item_id` char(26) NOT NULL,
    `content_id` char(26) NOT NULL,
    `entity_name` varchar(255) NOT NULL,
    `entity_type` varchar(32) NOT NULL,
    `normalized_name` varchar(255) DEFAULT NULL,
    `source_text` text DEFAULT NULL,
    `confidence` decimal(5, 4) NOT NULL DEFAULT 0.0000,
    `verified` tinyint(1) NOT NULL DEFAULT 0,
    `source` varchar(32) NOT NULL DEFAULT 'AI_EXTRACTED',
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `kg_entity_id` char(26) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_refinement_entity_annotation_id` (`annotation_id`),
    KEY `idx_data_refinement_entity_work_item` (`work_item_id`, `status`),
    KEY `idx_data_refinement_entity_content` (`content_id`, `entity_type`),
    KEY `idx_data_refinement_entity_kg` (`kg_entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据精修实体标注表';

CREATE TABLE IF NOT EXISTS `data_refinement_relation_annotation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `annotation_id` char(26) NOT NULL,
    `work_item_id` char(26) NOT NULL,
    `content_id` char(26) NOT NULL,
    `source_entity_annotation_id` char(26) NOT NULL,
    `target_entity_annotation_id` char(26) NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `confidence` decimal(5, 4) NOT NULL DEFAULT 0.0000,
    `verified` tinyint(1) NOT NULL DEFAULT 0,
    `source` varchar(32) NOT NULL DEFAULT 'AI_EXTRACTED',
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `kg_relation_id` char(26) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_refinement_relation_annotation_id` (`annotation_id`),
    UNIQUE KEY `uk_data_refinement_relation_unique` (`work_item_id`, `source_entity_annotation_id`, `target_entity_annotation_id`, `relation_type`),
    KEY `idx_data_refinement_relation_work_item` (`work_item_id`, `status`),
    KEY `idx_data_refinement_relation_content` (`content_id`, `relation_type`),
    KEY `idx_data_refinement_relation_kg` (`kg_relation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据精修关系标注表';
