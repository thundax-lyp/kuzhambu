SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_refinement_candidate` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `candidate_id` char(26) NOT NULL,
    `batch_id` char(26) DEFAULT NULL,
    `operation_type` varchar(32) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` char(26) NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `result_format` varchar(16) NOT NULL,
    `result_payload` longtext DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `prompt_id` char(26) DEFAULT NULL,
    `model_name` varchar(255) DEFAULT NULL,
    `error_message` varchar(1024) DEFAULT NULL,
    `requested_by` char(26) NOT NULL,
    `applied_by` char(26) DEFAULT NULL,
    `applied_at` datetime(3) DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_refinement_candidate_id` (`candidate_id`),
    KEY `idx_ai_refinement_candidate_target` (`content_type`, `content_id`, `operation_type`),
    KEY `idx_ai_refinement_candidate_batch` (`batch_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI精修候选结果表';

CREATE TABLE IF NOT EXISTS `ai_refinement_batch` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `batch_id` char(26) NOT NULL,
    `operation_type` varchar(32) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'RUNNING',
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `cancelled_count` int NOT NULL DEFAULT 0,
    `requested_by` char(26) NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    `cancelled_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_refinement_batch_id` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI精修批量操作表';

CREATE TABLE IF NOT EXISTS `ai_refinement_batch_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `batch_item_id` char(26) NOT NULL,
    `batch_id` char(26) NOT NULL,
    `content_id` char(26) NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `candidate_id` char(26) DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `error_message` varchar(1024) DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_refinement_batch_item_id` (`batch_item_id`),
    UNIQUE KEY `uk_ai_refinement_batch_item_target` (`batch_id`, `content_id`, `object_id`),
    KEY `idx_ai_refinement_batch_item_batch` (`batch_id`, `status`),
    KEY `idx_ai_refinement_batch_item_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI精修批量明细表';

CREATE TABLE IF NOT EXISTS `image_analysis_cache` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `cache_id` char(26) NOT NULL,
    `object_id` char(26) NOT NULL,
    `content_hash` varchar(128) NOT NULL,
    `analysis_markdown` longtext NOT NULL,
    `prompt_id` char(26) DEFAULT NULL,
    `model_name` varchar(255) DEFAULT NULL,
    `requested_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_image_analysis_cache_id` (`cache_id`),
    UNIQUE KEY `uk_image_analysis_cache_object_hash` (`object_id`, `content_hash`),
    KEY `idx_image_analysis_cache_hash` (`content_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片理解分析缓存表';
