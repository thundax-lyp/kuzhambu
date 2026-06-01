SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_service_config` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `service_id` bigint NOT NULL,
    `service_role` varchar(16) NOT NULL,
    `api_source` varchar(32) NOT NULL,
    `base_url` varchar(512) NOT NULL,
    `encrypted_api_key` varchar(2048) DEFAULT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `status` varchar(16) NOT NULL DEFAULT 'UNAVAILABLE',
    `last_checked_at` datetime(3) DEFAULT NULL,
    `configured_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_service_config_id` (`service_id`),
    UNIQUE KEY `uk_ai_service_config_role` (`service_role`),
    KEY `idx_ai_service_config_status` (`enabled`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI服务配置表';

CREATE TABLE IF NOT EXISTS `ai_model` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `model_id` bigint NOT NULL,
    `service_id` bigint NOT NULL,
    `model_name` varchar(255) NOT NULL,
    `display_name` varchar(255) NOT NULL,
    `capability_tags_json` json NOT NULL,
    `default_params_json` json DEFAULT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `registered_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_model_id` (`model_id`),
    UNIQUE KEY `uk_ai_model_service_name` (`service_id`, `model_name`),
    KEY `idx_ai_model_enabled` (`service_id`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

CREATE TABLE IF NOT EXISTS `ai_model_check_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `check_id` bigint NOT NULL,
    `model_id` bigint NOT NULL,
    `service_id` bigint NOT NULL,
    `model_name` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL,
    `latency_ms` int DEFAULT NULL,
    `error_type` varchar(32) DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    `checked_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_model_check_record_id` (`check_id`),
    KEY `idx_ai_model_check_record_model` (`model_id`, `checked_at`),
    KEY `idx_ai_model_check_record_status` (`status`, `checked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型检测记录表';

CREATE TABLE IF NOT EXISTS `ai_capability` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `capability` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `required_tags_json` json NOT NULL,
    `output_mode` varchar(32) NOT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `priority` int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_capability` (`capability`),
    UNIQUE KEY `uk_ai_capability_priority` (`priority`),
    KEY `idx_ai_capability_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI能力定义表';

CREATE TABLE IF NOT EXISTS `ai_capability_mapping` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `mapping_id` bigint NOT NULL,
    `scope` varchar(32) NOT NULL,
    `capability` varchar(64) NOT NULL,
    `model_id` bigint NOT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `configured_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_capability_mapping_id` (`mapping_id`),
    UNIQUE KEY `uk_ai_capability_mapping_scope` (`scope`, `capability`),
    KEY `idx_ai_capability_mapping_model` (`model_id`),
    KEY `idx_ai_capability_mapping_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI能力模型映射表';

CREATE TABLE IF NOT EXISTS `ai_prompt_template` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `template_id` bigint NOT NULL,
    `scope` varchar(32) NOT NULL,
    `capability` varchar(64) NOT NULL,
    `name` varchar(255) NOT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `current_version_no` int DEFAULT NULL,
    `registered_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_prompt_template_id` (`template_id`),
    UNIQUE KEY `uk_ai_prompt_template_scope` (`scope`, `capability`),
    KEY `idx_ai_prompt_template_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示词模板表';

CREATE TABLE IF NOT EXISTS `ai_prompt_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `prompt_version_id` bigint NOT NULL,
    `template_id` bigint NOT NULL,
    `version_no` int NOT NULL,
    `message_templates_json` json NOT NULL,
    `variables_snapshot_json` json NOT NULL,
    `output_schema_json` json DEFAULT NULL,
    `current_key` varchar(160) DEFAULT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `registered_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_prompt_version_id` (`prompt_version_id`),
    UNIQUE KEY `uk_ai_prompt_version_no` (`template_id`, `version_no`),
    UNIQUE KEY `uk_ai_prompt_version_current` (`current_key`),
    KEY `idx_ai_prompt_version_template` (`template_id`, `registered_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示词版本表';

CREATE TABLE IF NOT EXISTS `ai_prompt_variable` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `variable_id` bigint NOT NULL,
    `template_id` bigint NOT NULL,
    `variable_name` varchar(128) NOT NULL,
    `required` tinyint(1) NOT NULL DEFAULT 1,
    `description` varchar(512) DEFAULT NULL,
    `priority` int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_prompt_variable_id` (`variable_id`),
    UNIQUE KEY `uk_ai_prompt_variable_name` (`template_id`, `variable_name`),
    UNIQUE KEY `uk_ai_prompt_variable_priority` (`priority`),
    KEY `idx_ai_prompt_variable_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示词变量表';

CREATE TABLE IF NOT EXISTS `ai_action_status` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `action_status_id` bigint NOT NULL,
    `scope` varchar(32) NOT NULL,
    `capability` varchar(64) NOT NULL,
    `available` tinyint(1) NOT NULL DEFAULT 0,
    `unavailable_reason` varchar(1024) DEFAULT NULL,
    `checked_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_action_status_id` (`action_status_id`),
    UNIQUE KEY `uk_ai_action_status_scope` (`scope`, `capability`),
    KEY `idx_ai_action_status_available` (`available`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI功能动作状态表';

CREATE TABLE IF NOT EXISTS `ai_call_record` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `call_id` bigint NOT NULL,
    `batch_id` bigint DEFAULT NULL,
    `scope` varchar(32) DEFAULT NULL,
    `capability` varchar(64) NOT NULL,
    `content_type` varchar(32) DEFAULT NULL,
    `content_id` bigint DEFAULT NULL,
    `object_id` bigint DEFAULT NULL,
    `service_id` bigint DEFAULT NULL,
    `service_role` varchar(16) DEFAULT NULL,
    `model_id` bigint DEFAULT NULL,
    `model_name` varchar(255) DEFAULT NULL,
    `prompt_version_id` bigint DEFAULT NULL,
    `request_id` varchar(128) DEFAULT NULL,
    `trace_id` varchar(128) DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'RUNNING',
    `stream_used` tinyint(1) NOT NULL DEFAULT 0,
    `stream_completed` tinyint(1) NOT NULL DEFAULT 0,
    `fallback_used` tinyint(1) NOT NULL DEFAULT 0,
    `latency_ms` int DEFAULT NULL,
    `input_tokens` int NOT NULL DEFAULT 0,
    `output_tokens` int NOT NULL DEFAULT 0,
    `cost_amount` decimal(18, 6) NOT NULL DEFAULT 0.000000,
    `error_type` varchar(32) DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    `warnings_json` json DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_call_record_id` (`call_id`),
    KEY `idx_ai_call_record_batch` (`batch_id`, `status`),
    KEY `idx_ai_call_record_target` (`content_type`, `content_id`, `capability`),
    KEY `idx_ai_call_record_status` (`status`, `requested_at`),
    KEY `idx_ai_call_record_trace` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用记录表';

CREATE TABLE IF NOT EXISTS `ai_candidate` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `candidate_id` bigint NOT NULL,
    `call_id` bigint DEFAULT NULL,
    `batch_id` bigint DEFAULT NULL,
    `capability` varchar(64) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` bigint NOT NULL,
    `object_id` bigint DEFAULT NULL,
    `result_format` varchar(32) NOT NULL,
    `result_payload` longtext DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `prompt_version_id` bigint DEFAULT NULL,
    `model_name` varchar(255) DEFAULT NULL,
    `error_type` varchar(32) DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    `applied_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_candidate_id` (`candidate_id`),
    KEY `idx_ai_candidate_target` (`content_type`, `content_id`, `capability`),
    KEY `idx_ai_candidate_batch` (`batch_id`, `status`),
    KEY `idx_ai_candidate_call` (`call_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI候选结果表';

CREATE TABLE IF NOT EXISTS `ai_batch_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `batch_id` bigint NOT NULL,
    `scope` varchar(32) DEFAULT NULL,
    `capability` varchar(64) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'RUNNING',
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `cancelled_count` int NOT NULL DEFAULT 0,
    `failure_summary_json` json DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    `cancelled_at` datetime(3) DEFAULT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_batch_job_id` (`batch_id`),
    KEY `idx_ai_batch_job_status` (`status`, `requested_at`),
    KEY `idx_ai_batch_job_capability` (`content_type`, `capability`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI批量任务表';

CREATE TABLE IF NOT EXISTS `ai_image_understanding` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `understanding_id` bigint NOT NULL,
    `storage_object_id` bigint NOT NULL,
    `content_hash` varchar(128) NOT NULL,
    `analysis_markdown` longtext NOT NULL,
    `call_id` bigint DEFAULT NULL,
    `prompt_version_id` bigint DEFAULT NULL,
    `model_name` varchar(255) DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_image_understanding_id` (`understanding_id`),
    UNIQUE KEY `uk_ai_image_understanding_object_hash` (`storage_object_id`, `content_hash`),
    KEY `idx_ai_image_understanding_hash` (`content_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI图片理解结果表';

CREATE TABLE IF NOT EXISTS `ai_entry_split_candidate` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `split_candidate_id` bigint NOT NULL,
    `candidate_id` bigint NOT NULL,
    `parent_content_type` varchar(32) NOT NULL,
    `parent_content_id` bigint NOT NULL,
    `title` varchar(255) NOT NULL,
    `original_text` longtext NOT NULL,
    `translation_text` longtext DEFAULT NULL,
    `target_volume_id` bigint DEFAULT NULL,
    `priority` int NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_entry_split_candidate_id` (`split_candidate_id`),
    UNIQUE KEY `uk_ai_entry_split_candidate_priority` (`priority`),
    KEY `idx_ai_entry_split_candidate_parent` (`parent_content_type`, `parent_content_id`),
    KEY `idx_ai_entry_split_candidate_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI条目拆分候选表';
