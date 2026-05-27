SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `discovery_search_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `config_id` bigint NOT NULL,
    `config_key` varchar(128) NOT NULL,
    `config_value` text NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_search_config_id` (`config_id`),
    UNIQUE KEY `uk_discovery_search_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索配置表';

CREATE TABLE IF NOT EXISTS `discovery_search_query_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `query_id` bigint NOT NULL,
    `user_id` bigint DEFAULT NULL,
    `raw_query` varchar(512) NOT NULL,
    `normalized_query` varchar(512) DEFAULT NULL,
    `intent` varchar(32) DEFAULT NULL,
    `rewritten_query` varchar(1024) DEFAULT NULL,
    `filters_json` text DEFAULT NULL,
    `expanded_terms_json` text DEFAULT NULL,
    `linked_entities_json` text DEFAULT NULL,
    `result_count` int NOT NULL DEFAULT 0,
    `searched_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_search_query_log_id` (`query_id`),
    KEY `idx_discovery_search_query_log_user` (`user_id`, `searched_at`),
    KEY `idx_discovery_search_query_log_intent` (`intent`, `searched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索查询日志表';

CREATE TABLE IF NOT EXISTS `discovery_search_click_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `click_id` bigint NOT NULL,
    `query_id` bigint NOT NULL,
    `user_id` bigint DEFAULT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` bigint NOT NULL,
    `result_rank` int NOT NULL DEFAULT 0,
    `clicked_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_search_click_log_id` (`click_id`),
    KEY `idx_discovery_search_click_log_query` (`query_id`, `clicked_at`),
    KEY `idx_discovery_search_click_log_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索点击日志表';
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `discovery_qa_session` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `session_id` bigint NOT NULL,
    `owner_user_id` bigint NOT NULL,
    `title` varchar(256) NOT NULL,
    `scope` varchar(32) NOT NULL,
    `context_mode` varchar(32) NOT NULL,
    `context_content_type` varchar(32) DEFAULT NULL,
    `context_content_id` bigint DEFAULT NULL,
    `status` varchar(32) NOT NULL,
    `opened_at` datetime(3) NOT NULL,
    `last_message_at` datetime(3) DEFAULT NULL,
    `removed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_qa_session_id` (`session_id`),
    KEY `idx_discovery_qa_session_owner` (`owner_user_id`, `last_message_at`),
    KEY `idx_discovery_qa_session_context` (`context_content_type`, `context_content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答会话表';

CREATE TABLE IF NOT EXISTS `discovery_qa_message` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `message_id` bigint NOT NULL,
    `session_id` bigint NOT NULL,
    `role` varchar(32) NOT NULL,
    `content` mediumtext NOT NULL,
    `message_status` varchar(32) NOT NULL,
    `context_turn_count` int NOT NULL DEFAULT 0,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `sent_at` datetime(3) NOT NULL,
    `answered_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_qa_message_id` (`message_id`),
    KEY `idx_discovery_qa_message_session` (`session_id`, `sent_at`),
    KEY `idx_discovery_qa_message_status` (`message_status`, `sent_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答消息表';

CREATE TABLE IF NOT EXISTS `discovery_qa_message_source` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `source_id` bigint NOT NULL,
    `message_id` bigint NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` bigint NOT NULL,
    `knowledge_base` varchar(64) NOT NULL,
    `title_snapshot` varchar(512) NOT NULL,
    `location_label` varchar(256) DEFAULT NULL,
    `snippet` text DEFAULT NULL,
    `source_rank` int NOT NULL DEFAULT 0,
    `score` decimal(10,6) DEFAULT NULL,
    `source_status` varchar(32) NOT NULL,
    `referenced_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_qa_message_source_id` (`source_id`),
    KEY `idx_discovery_qa_message_source_message` (`message_id`, `source_rank`),
    KEY `idx_discovery_qa_message_source_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答来源引用表';

CREATE TABLE IF NOT EXISTS `discovery_qa_retrieval_trace` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `trace_id` bigint NOT NULL,
    `message_id` bigint NOT NULL,
    `raw_question` varchar(1024) NOT NULL,
    `rewritten_question` varchar(2048) DEFAULT NULL,
    `scope` varchar(32) NOT NULL,
    `filters_json` text DEFAULT NULL,
    `expanded_terms_json` text DEFAULT NULL,
    `linked_entities_json` text DEFAULT NULL,
    `candidate_count` int NOT NULL DEFAULT 0,
    `context_snapshot` mediumtext DEFAULT NULL,
    `retrieved_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_qa_retrieval_trace_id` (`trace_id`),
    KEY `idx_discovery_qa_retrieval_trace_message` (`message_id`),
    KEY `idx_discovery_qa_retrieval_trace_scope` (`scope`, `retrieved_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答检索追溯表';

CREATE TABLE IF NOT EXISTS `discovery_qa_session_export` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` bigint NOT NULL,
    `session_id` bigint NOT NULL,
    `format` varchar(32) NOT NULL,
    `storage_object_id` bigint DEFAULT NULL,
    `export_status` varchar(32) NOT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requester_user_id` bigint NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_discovery_qa_session_export_id` (`export_id`),
    KEY `idx_discovery_qa_session_export_session` (`session_id`, `requested_at`),
    KEY `idx_discovery_qa_session_export_status` (`export_status`, `requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答会话导出表';
