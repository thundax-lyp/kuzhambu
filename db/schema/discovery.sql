SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `discovery_search_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `config_key` varchar(128) NOT NULL,
    `config_value` text NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_search_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索配置表';

CREATE TABLE IF NOT EXISTS `discovery_search_query_event` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint DEFAULT NULL,
    `raw_query` varchar(512) NOT NULL,
    `normalized_query` varchar(512) DEFAULT NULL,
    `intent` varchar(32) DEFAULT NULL,
    `rewritten_query` varchar(1024) DEFAULT NULL,
    `filters_json` text DEFAULT NULL,
    `expanded_terms_json` text DEFAULT NULL,
    `linked_entities_json` text DEFAULT NULL,
    `result_count` int NOT NULL DEFAULT 0,
    `searched_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_search_query_event_user` (`user_id`, `searched_at`),
    KEY `idx_discovery_search_query_event_intent` (`intent`, `searched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索查询事件表';

CREATE TABLE IF NOT EXISTS `discovery_search_click_event` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `search_event_id` bigint NOT NULL,
    `content_domain` varchar(64) DEFAULT NULL,
    `content_type` varchar(64) NOT NULL,
    `content_id` varchar(64) NOT NULL,
    `content_title` varchar(256) DEFAULT NULL,
    `result_group_key` varchar(64) DEFAULT NULL,
    `result_rank` int DEFAULT NULL,
    `group_rank` int DEFAULT NULL,
    `target_path` varchar(512) DEFAULT NULL,
    `operator_type` varchar(32) DEFAULT NULL,
    `operator_id` varchar(64) DEFAULT NULL,
    `request_id` varchar(128) DEFAULT NULL,
    `trace_id` varchar(128) DEFAULT NULL,
    `created_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_search_click_event_event` (`search_event_id`, `created_at`),
    KEY `idx_discovery_search_click_event_content` (`content_type`, `content_id`),
    KEY `idx_discovery_search_click_event_operator` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检索点击事件表';

CREATE TABLE IF NOT EXISTS `discovery_search_event` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `query_text` varchar(512) NOT NULL,
    `normalized_query_text` varchar(512) DEFAULT NULL,
    `display_query_text` varchar(512) DEFAULT NULL,
    `intent_type` varchar(32) DEFAULT NULL,
    `search_scopes_json` text DEFAULT NULL,
    `result_total_count` int DEFAULT NULL,
    `group_total_count` int DEFAULT NULL,
    `search_latency_ms` bigint DEFAULT NULL,
    `search_status` varchar(32) NOT NULL,
    `failure_code` varchar(64) DEFAULT NULL,
    `failure_message` varchar(1024) DEFAULT NULL,
    `operator_type` varchar(32) DEFAULT NULL,
    `operator_id` varchar(64) DEFAULT NULL,
    `request_id` varchar(128) DEFAULT NULL,
    `trace_id` varchar(128) DEFAULT NULL,
    `created_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_search_event_status` (`search_status`, `created_at`),
    KEY `idx_discovery_search_event_operator` (`operator_id`, `created_at`),
    KEY `idx_discovery_search_event_intent` (`intent_type`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检索统计事件表';

CREATE TABLE IF NOT EXISTS `discovery_query_understanding` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `search_event_id` bigint DEFAULT NULL,
    `query_text` varchar(512) NOT NULL,
    `normalized_query_text` varchar(512) DEFAULT NULL,
    `rewritten_query_text` varchar(1024) DEFAULT NULL,
    `intent_type` varchar(32) DEFAULT NULL,
    `recognized_entities_json` text DEFAULT NULL,
    `understanding_status` varchar(32) NOT NULL,
    `failure_code` varchar(64) DEFAULT NULL,
    `failure_message` varchar(1024) DEFAULT NULL,
    `request_id` varchar(128) DEFAULT NULL,
    `trace_id` varchar(128) DEFAULT NULL,
    `created_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_query_understanding_event` (`search_event_id`),
    KEY `idx_discovery_query_understanding_status` (`understanding_status`, `created_at`),
    KEY `idx_discovery_query_understanding_intent` (`intent_type`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='查询理解记录表';
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `discovery_qa_session` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `owner_type` varchar(32) NOT NULL,
    `owner_id` varchar(64) NOT NULL,
    `title` varchar(256) NOT NULL,
    `scope` varchar(32) NOT NULL,
    `context_mode` varchar(32) NOT NULL,
    `context_content_type` varchar(32) DEFAULT NULL,
    `context_content_id` bigint DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `opened_at` BIGINT NOT NULL,
    `last_message_at` BIGINT DEFAULT NULL,
    `removed_at` BIGINT DEFAULT NULL,
    `knowledge_base_name` varchar(128) NOT NULL DEFAULT 'kuzhambu-qa',
    PRIMARY KEY (`id`),
    KEY `idx_discovery_qa_session_owner` (`owner_type`, `owner_id`, `last_message_at`),
    KEY `idx_discovery_qa_session_context` (`context_content_type`, `context_content_id`),
    KEY `idx_discovery_qa_session_removed_opened` (`removed_at`, `opened_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答会话表';

CREATE TABLE IF NOT EXISTS `discovery_qa_message` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `session_id` bigint NOT NULL,
    `role` varchar(32) NOT NULL,
    `content` mediumtext NOT NULL,
    `answer_status` varchar(32) NOT NULL,
    `model` varchar(128) NOT NULL DEFAULT 'kuzhambu-qa',
    `context_turn_count` int NOT NULL DEFAULT 0,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `provider_chat_id` varchar(128) DEFAULT NULL,
    `finish_reason` varchar(32) DEFAULT NULL,
    `sent_at` BIGINT NOT NULL,
    `answered_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_qa_message_session` (`session_id`, `sent_at`),
    KEY `idx_discovery_qa_message_status` (`answer_status`, `sent_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答消息表';

CREATE TABLE IF NOT EXISTS `discovery_qa_message_source` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `source_business_id` varchar(128) NOT NULL,
    `message_id` bigint NOT NULL,
    `content_type` varchar(64) NOT NULL,
    `content_id` bigint NOT NULL,
    `knowledge_base` varchar(64) NOT NULL,
    `title_snapshot` varchar(256) NOT NULL,
    `location_label` varchar(256) DEFAULT NULL,
    `snippet` text DEFAULT NULL,
    `source_path` varchar(512) DEFAULT NULL,
    `source_rank` int NOT NULL DEFAULT 0,
    `score` decimal(10,6) DEFAULT NULL,
    `source_status` varchar(32) NOT NULL,
    `referenced_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_qa_message_source_message` (`message_id`, `source_rank`),
    KEY `idx_discovery_qa_message_source_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答来源引用表';

CREATE TABLE IF NOT EXISTS `discovery_qa_retrieval_trace` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `message_id` bigint NOT NULL,
    `raw_question` varchar(1024) NOT NULL,
    `provider` varchar(64) DEFAULT NULL,
    `external_knowledge_base_id` varchar(128) NOT NULL,
    `external_knowledge_item_ids` text DEFAULT NULL,
    `external_chat_id` varchar(128) DEFAULT NULL,
    `provider_request_id` varchar(128) DEFAULT NULL,
    `latency_ms` bigint DEFAULT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `raw` mediumtext DEFAULT NULL,
    `retrieved_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_qa_retrieval_trace_message` (`message_id`),
    KEY `idx_discovery_qa_retrieval_trace_provider` (`provider`, `retrieved_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答检索追溯表';

CREATE TABLE IF NOT EXISTS `discovery_qa_knowledge_sync_batch` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `trigger_type` varchar(32) NOT NULL,
    `provider` varchar(64) NOT NULL,
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failure_count` int NOT NULL DEFAULT 0,
    `started_at` BIGINT NOT NULL,
    `finished_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_qa_knowledge_sync_batch_trigger` (`trigger_type`, `started_at`),
    KEY `idx_discovery_qa_knowledge_sync_batch_provider` (`provider`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答知识同步批次表';

CREATE TABLE IF NOT EXISTS `discovery_qa_knowledge_sync_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `source_id` varchar(128) NOT NULL,
    `content_type` varchar(64) NOT NULL,
    `content_id` bigint NOT NULL,
    `knowledge_base_name` varchar(128) NOT NULL DEFAULT 'kuzhambu-qa',
    `current_version_no` int NOT NULL DEFAULT 0,
    `knowledge_revision` varchar(128) DEFAULT NULL,
    `provider` varchar(64) NOT NULL,
    `external_knowledge_base_id` varchar(128) DEFAULT NULL,
    `external_knowledge_item_id` varchar(128) DEFAULT NULL,
    `sync_status` varchar(32) NOT NULL DEFAULT 'PENDING',
    `failure_reason` varchar(1024) DEFAULT NULL,
    `synced_at` BIGINT DEFAULT NULL,
    `created_at` BIGINT NOT NULL,
    `updated_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_qa_knowledge_sync_item_source` (`source_id`),
    KEY `idx_discovery_qa_knowledge_sync_item_type` (`content_type`, `content_id`),
    KEY `idx_discovery_qa_knowledge_sync_item_status` (`sync_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答知识同步项表';

CREATE TABLE IF NOT EXISTS `discovery_qa_session_export` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `session_id` bigint NOT NULL,
    `format` varchar(32) NOT NULL,
    `storage_object_id` bigint DEFAULT NULL,
    `export_status` varchar(32) NOT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requester_user_id` bigint NOT NULL,
    `requested_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_discovery_qa_session_export_session` (`session_id`, `requested_at`),
    KEY `idx_discovery_qa_session_export_status` (`export_status`, `requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答会话导出表';
