SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_service_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `service_id` char(26) NOT NULL,
    `api_source` varchar(16) NOT NULL,
    `base_url` varchar(512) NOT NULL,
    `encrypted_api_key` varchar(2048) DEFAULT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `status` varchar(16) NOT NULL DEFAULT 'UNAVAILABLE',
    `last_checked_at` datetime(3) DEFAULT NULL,
    `tested_at` datetime(3) NOT NULL,
    `configured_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_service_config_id` (`service_id`),
    UNIQUE KEY `uk_ai_service_config_source` (`api_source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI服务配置表';

CREATE TABLE IF NOT EXISTS `ai_model` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `model_id` char(26) NOT NULL,
    `api_source` varchar(16) NOT NULL,
    `model_name` varchar(255) NOT NULL,
    `display_name` varchar(255) NOT NULL,
    `capability_tags` text NOT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `authored_at` datetime(3) NOT NULL,
    `configured_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_model_id` (`model_id`),
    UNIQUE KEY `uk_ai_model_source_name` (`api_source`, `model_name`),
    KEY `idx_ai_model_enabled` (`api_source`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

CREATE TABLE IF NOT EXISTS `ai_model_test_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `test_id` char(26) NOT NULL,
    `model_id` char(26) NOT NULL,
    `api_source` varchar(16) NOT NULL,
    `model_name` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL,
    `latency_ms` int DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    `recorded_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_model_test_record_id` (`test_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型检测记录表';

CREATE TABLE IF NOT EXISTS `ai_capability` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `capability` varchar(32) NOT NULL,
    `name` varchar(128) NOT NULL,
    `required_tags` text NOT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_capability` (`capability`),
    KEY `idx_ai_capability_sort` (`enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI能力定义表';

CREATE TABLE IF NOT EXISTS `ai_capability_mapping` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `mapping_id` char(26) NOT NULL,
    `capability` varchar(32) NOT NULL,
    `model_id` char(26) NOT NULL,
    `operator_user_id` char(26) NOT NULL,
    `configured_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_capability_mapping_id` (`mapping_id`),
    UNIQUE KEY `uk_ai_capability_mapping_capability` (`capability`),
    KEY `idx_ai_capability_mapping_model` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI能力模型映射表';

CREATE TABLE IF NOT EXISTS `ai_prompt` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `prompt_id` char(26) NOT NULL,
    `scope` varchar(32) NOT NULL,
    `capability` varchar(32) NOT NULL,
    `version_no` int NOT NULL,
    `content` longtext NOT NULL,
    `variables_snapshot` text NOT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `active` tinyint(1) NOT NULL DEFAULT 1,
    `author_user_id` char(26) NOT NULL,
    `registered_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_prompt_id` (`prompt_id`),
    UNIQUE KEY `uk_ai_prompt_version` (`scope`, `capability`, `version_no`),
    KEY `idx_ai_prompt_current` (`scope`, `capability`, `active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示词版本表';

CREATE TABLE IF NOT EXISTS `ai_call_metric` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `metric_id` char(26) NOT NULL,
    `scope` varchar(32) DEFAULT NULL,
    `capability` varchar(32) NOT NULL,
    `api_source` varchar(16) NOT NULL,
    `model_id` char(26) DEFAULT NULL,
    `model_name` varchar(255) NOT NULL,
    `success` tinyint(1) NOT NULL DEFAULT 1,
    `fallback_used` tinyint(1) NOT NULL DEFAULT 0,
    `latency_ms` int DEFAULT NULL,
    `input_tokens` int NOT NULL DEFAULT 0,
    `output_tokens` int NOT NULL DEFAULT 0,
    `cost_amount` decimal(18, 6) NOT NULL DEFAULT 0.000000,
    `error_message` varchar(1024) DEFAULT NULL,
    `registered_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_call_metric_id` (`metric_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用指标表';
