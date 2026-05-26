SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `search_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `config_id` char(26) NOT NULL,
    `config_key` varchar(128) NOT NULL,
    `config_value` text NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `operator_user_id` char(26) NOT NULL,
    `operated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_search_config_id` (`config_id`),
    UNIQUE KEY `uk_search_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索配置表';

CREATE TABLE IF NOT EXISTS `search_query_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `query_id` char(26) NOT NULL,
    `user_id` char(26) DEFAULT NULL,
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
    UNIQUE KEY `uk_search_query_log_id` (`query_id`),
    KEY `idx_search_query_log_user` (`user_id`, `searched_at`),
    KEY `idx_search_query_log_intent` (`intent`, `searched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索查询日志表';

CREATE TABLE IF NOT EXISTS `search_click_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `click_id` char(26) NOT NULL,
    `query_id` char(26) NOT NULL,
    `user_id` char(26) DEFAULT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` char(26) NOT NULL,
    `result_rank` int NOT NULL DEFAULT 0,
    `clicked_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_search_click_log_id` (`click_id`),
    KEY `idx_search_click_log_query` (`query_id`, `clicked_at`),
    KEY `idx_search_click_log_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索点击日志表';
