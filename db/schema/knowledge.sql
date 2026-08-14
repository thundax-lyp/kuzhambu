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
    `created_at` BIGINT NOT NULL,
    `reviewed_at` BIGINT DEFAULT NULL,
    `merged_to_tag_id` bigint DEFAULT NULL,
    `deprecated_at` BIGINT DEFAULT NULL,
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

CREATE TABLE IF NOT EXISTS `knowledge_graph_material` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `content_type` varchar(32) NOT NULL,
    `content_ref_id` bigint NOT NULL,
    `content_title_snapshot` varchar(255) NOT NULL,
    `status` varchar(32) NOT NULL,
    `published_at` BIGINT DEFAULT NULL,
    `current_extraction_task_id` bigint DEFAULT NULL,
    `lock_version` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_material_content` (`content_type`, `content_ref_id`),
    KEY `idx_knowledge_graph_material_status_published` (`status`, `published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识图谱素材空间';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_node` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `material_id` bigint NOT NULL,
    `node_key` varchar(256) DEFAULT NULL,
    `node_type` varchar(64) NOT NULL,
    `name` varchar(255) NOT NULL,
    `source` varchar(32) NOT NULL,
    `properties_json` json DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_material_node_key` (`material_id`, `node_key`),
    KEY `idx_knowledge_graph_material_node_material_type` (`material_id`, `node_type`),
    KEY `idx_knowledge_graph_material_node_material_name` (`material_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材草稿图节点';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_edge` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `material_id` bigint NOT NULL,
    `source_material_node_id` bigint NOT NULL,
    `target_material_node_id` bigint NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `source` varchar(32) NOT NULL,
    `qualifiers_json` json DEFAULT NULL,
    `edge_key` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_material_edge_key` (`material_id`, `edge_key`),
    KEY `idx_knowledge_graph_material_edge_material` (`material_id`),
    KEY `idx_knowledge_graph_material_edge_source_target` (`source_material_node_id`, `target_material_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材草稿图关系';

CREATE TABLE IF NOT EXISTS `knowledge_graph_extraction_task` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    -- Compatibility columns retained until the legacy graph infra is removed.
    `batch_job_id` bigint DEFAULT NULL,
    `task_type` varchar(32) DEFAULT NULL,
    `scope_type` varchar(32) DEFAULT NULL,
    `scope_json` json DEFAULT NULL,
    `trigger_source` varchar(32) DEFAULT NULL,
    `selection_scope_json` json DEFAULT NULL,
    `replace_unconfirmed_only` tinyint(1) DEFAULT NULL,
    `parent_task_id` bigint DEFAULT NULL,
    `source_content_type` varchar(32) DEFAULT NULL,
    `source_content_id` bigint DEFAULT NULL,
    `model_id` bigint DEFAULT NULL,
    `model_name` varchar(128) DEFAULT NULL,
    `prompt_version_id` bigint DEFAULT NULL,
    `request_id` varchar(128) DEFAULT NULL,
    `trace_id` varchar(128) DEFAULT NULL,
    `prompt_messages_json` json DEFAULT NULL,
    `prompt_variables_json` json DEFAULT NULL,
    `prompt_hash` varchar(128) DEFAULT NULL,
    `input_payload_json` json DEFAULT NULL,
    `output_schema_json` json DEFAULT NULL,
    `force_json` tinyint(1) DEFAULT NULL,
    `locale` varchar(16) DEFAULT NULL,
    `ai_call_id` bigint DEFAULT NULL,
    `ai_candidate_id` bigint DEFAULT NULL,
    `error_type` varchar(64) DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    `requested_by` bigint DEFAULT NULL,
    `applied_at` BIGINT DEFAULT NULL,
    `material_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_ref_id` bigint NOT NULL,
    `content_snapshot_json` json NOT NULL,
    `pipeline_version` varchar(64) NOT NULL,
    `status` varchar(32) NOT NULL,
    `current_stage` varchar(64) DEFAULT NULL,
    `progress` int NOT NULL DEFAULT 0,
    `result_summary_json` json DEFAULT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `retry_from_task_id` bigint DEFAULT NULL,
    `requested_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_graph_extraction_task_material_status` (`material_id`, `status`, `requested_at`),
    KEY `idx_knowledge_graph_extraction_task_content` (`content_type`, `content_ref_id`),
    KEY `idx_knowledge_graph_extraction_task_source` (`source_content_type`, `source_content_id`),
    KEY `idx_knowledge_graph_extraction_task_call_candidate` (`ai_call_id`, `ai_candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱抽取管道任务';

CREATE TABLE IF NOT EXISTS `knowledge_graph_extraction_stage` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `extraction_task_id` bigint NOT NULL,
    `stage_name` varchar(64) NOT NULL,
    `stage_order` int NOT NULL,
    `status` varchar(32) NOT NULL,
    `input_snapshot_json` json DEFAULT NULL,
    `output_summary_json` json DEFAULT NULL,
    `ai_call_id` bigint DEFAULT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `started_at` BIGINT DEFAULT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_extraction_stage_order` (`extraction_task_id`, `stage_order`),
    KEY `idx_knowledge_graph_extraction_stage_task_status` (`extraction_task_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图谱抽取管道阶段';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `material_id` bigint NOT NULL,
    `version_no` bigint NOT NULL,
    `snapshot_json` json NOT NULL,
    `published_by` bigint NOT NULL,
    `published_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_material_version_no` (`material_id`, `version_no`),
    KEY `idx_knowledge_graph_material_version_published` (`material_id`, `published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材发布快照';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_event` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `content_type` varchar(32) NOT NULL,
    `content_ref_id` bigint NOT NULL,
    `event_type` varchar(32) NOT NULL,
    `status` varchar(32) NOT NULL,
    `changed_at` BIGINT NOT NULL,
    `lock_version` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_graph_material_event_content` (`content_type`, `content_ref_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材变更事件';

CREATE TABLE IF NOT EXISTS `knowledge_graph_published_node` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `node_key` varchar(256) NOT NULL,
    `node_type` varchar(64) NOT NULL,
    `name` varchar(255) NOT NULL,
    `source` varchar(32) NOT NULL,
    `status` varchar(32) NOT NULL,
    `modified_at` BIGINT NOT NULL,
    `lock_version` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_published_node_key` (`node_key`),
    KEY `idx_knowledge_graph_published_node_recent` (`status`, `modified_at`),
    KEY `idx_knowledge_graph_published_node_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布空间节点';

CREATE TABLE IF NOT EXISTS `knowledge_graph_published_edge` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `edge_key` varchar(512) NOT NULL,
    `source_published_node_id` bigint NOT NULL,
    `target_published_node_id` bigint NOT NULL,
    `relation_type` varchar(64) NOT NULL,
    `source` varchar(32) NOT NULL,
    `qualifiers_json` json DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `modified_at` BIGINT NOT NULL,
    `lock_version` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_published_edge_key` (`edge_key`),
    KEY `idx_knowledge_graph_published_edge_source` (`source_published_node_id`),
    KEY `idx_knowledge_graph_published_edge_target` (`target_published_node_id`),
    KEY `idx_knowledge_graph_published_edge_recent` (`status`, `modified_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布空间关系';

CREATE TABLE IF NOT EXISTS `knowledge_graph_published_node_property` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `published_node_id` bigint NOT NULL,
    `property_name` varchar(128) NOT NULL,
    `value` varchar(512) NOT NULL,
    `preferred` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_node_property_value` (`published_node_id`, `property_name`, `value`),
    KEY `idx_knowledge_graph_node_property_preferred` (`published_node_id`, `property_name`, `preferred`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布节点多值属性';

CREATE TABLE IF NOT EXISTS `knowledge_graph_published_edge_property` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `published_edge_id` bigint NOT NULL,
    `property_name` varchar(128) NOT NULL,
    `value` varchar(512) NOT NULL,
    `preferred` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_edge_property_value` (`published_edge_id`, `property_name`, `value`),
    KEY `idx_knowledge_graph_edge_property_preferred` (`published_edge_id`, `property_name`, `preferred`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布关系多值属性';

CREATE TABLE IF NOT EXISTS `knowledge_graph_published_node_material` (
    `published_node_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_ref_id` bigint NOT NULL,
    `source_snapshot_json` json DEFAULT NULL,
    PRIMARY KEY (`published_node_id`, `content_type`, `content_ref_id`),
    KEY `idx_knowledge_graph_published_node_material_content` (`content_type`, `content_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布节点当前素材关联';

CREATE TABLE IF NOT EXISTS `knowledge_graph_published_edge_material` (
    `published_edge_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_ref_id` bigint NOT NULL,
    `source_snapshot_json` json DEFAULT NULL,
    PRIMARY KEY (`published_edge_id`, `content_type`, `content_ref_id`),
    KEY `idx_knowledge_graph_published_edge_material_content` (`content_type`, `content_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布关系当前素材关联';

CREATE TABLE IF NOT EXISTS `knowledge_graph_manual_source` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `target_type` varchar(32) NOT NULL,
    `target_id` bigint NOT NULL,
    `reason` varchar(1024) NOT NULL,
    `recorded_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_graph_manual_source_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布空间人工来源';

CREATE TABLE IF NOT EXISTS `knowledge_graph_publish_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `material_id` bigint NOT NULL,
    `status` varchar(32) NOT NULL,
    `preview_summary_json` json NOT NULL,
    `conflict_decisions_json` json DEFAULT NULL,
    `result_summary_json` json DEFAULT NULL,
    `requested_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_graph_publish_record_material_status` (`material_id`, `status`, `requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材整体发布记录';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_node_mapping` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `material_id` bigint NOT NULL,
    `material_node_id` bigint DEFAULT NULL,
    `published_node_id` bigint NOT NULL,
    `publish_record_id` bigint NOT NULL,
    `source_snapshot_json` json DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `changed_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_node_mapping_publish` (`material_node_id`, `published_node_id`, `publish_record_id`),
    KEY `idx_knowledge_graph_node_mapping_material_status` (`material_id`, `status`),
    KEY `idx_knowledge_graph_node_mapping_published_status` (`published_node_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材节点发布映射';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_edge_mapping` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `material_id` bigint NOT NULL,
    `material_edge_id` bigint DEFAULT NULL,
    `published_edge_id` bigint NOT NULL,
    `publish_record_id` bigint NOT NULL,
    `source_snapshot_json` json DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `changed_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_edge_mapping_publish` (`material_edge_id`, `published_edge_id`, `publish_record_id`),
    KEY `idx_knowledge_graph_edge_mapping_material_status` (`material_id`, `status`),
    KEY `idx_knowledge_graph_edge_mapping_published_status` (`published_edge_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材关系发布映射';

CREATE TABLE IF NOT EXISTS `knowledge_graph_governance_operation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `operation_type` varchar(32) NOT NULL,
    `target_type` varchar(32) NOT NULL,
    `target_id` bigint NOT NULL,
    `before_snapshot_json` json DEFAULT NULL,
    `after_snapshot_json` json DEFAULT NULL,
    `reason` varchar(1024) NOT NULL,
    `operated_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_graph_governance_operation_target` (`target_type`, `target_id`, `operated_at`),
    KEY `idx_knowledge_graph_governance_operation_type_time` (`operation_type`, `operated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布空间治理操作记录';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_deletion_change` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `material_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_ref_id` bigint NOT NULL,
    `material_snapshot_json` json NOT NULL,
    `decision` varchar(32) DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `result_summary_json` json DEFAULT NULL,
    `requested_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_graph_deletion_change_material_status` (`material_id`, `status`, `requested_at`),
    KEY `idx_knowledge_graph_deletion_change_content` (`content_type`, `content_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除变更快照';

CREATE TABLE IF NOT EXISTS `knowledge_graph_material_deletion_task` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `deletion_change_id` bigint NOT NULL,
    `idempotency_key` varchar(128) NOT NULL,
    `status` varchar(32) NOT NULL,
    `progress` int NOT NULL DEFAULT 0,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `result_summary_json` json DEFAULT NULL,
    `requested_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_knowledge_graph_deletion_task_idempotency` (`idempotency_key`),
    KEY `idx_knowledge_graph_deletion_task_status_requested` (`status`, `requested_at`),
    KEY `idx_knowledge_graph_deletion_task_change` (`deletion_change_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除后台任务';
