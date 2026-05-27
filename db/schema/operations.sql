SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `operations_report` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `report_id` bigint NOT NULL,
    `report_type` varchar(16) NOT NULL,
    `format` varchar(16) NOT NULL,
    `period_start` date NOT NULL,
    `period_end` date NOT NULL,
    `storage_object_id` bigint DEFAULT NULL,
    `report_status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requester_user_id` bigint NOT NULL,
    `requested_at` datetime(3) NOT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_report_id` (`report_id`),
    KEY `idx_operations_report_period` (`report_type`, `period_start`, `period_end`),
    KEY `idx_operations_report_status` (`report_status`, `requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维报表记录表';

CREATE TABLE IF NOT EXISTS `operations_backup` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `backup_id` bigint NOT NULL,
    `backup_type` varchar(16) NOT NULL,
    `backup_status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `storage_object_id` bigint DEFAULT NULL,
    `file_name` varchar(512) DEFAULT NULL,
    `file_size_bytes` bigint DEFAULT NULL,
    `checksum` varchar(128) DEFAULT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requester_user_id` bigint DEFAULT NULL,
    `started_at` datetime(3) NOT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_backup_id` (`backup_id`),
    KEY `idx_operations_backup_status` (`backup_status`, `started_at`),
    KEY `idx_operations_backup_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维备份记录表';

CREATE TABLE IF NOT EXISTS `operations_restore` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `restore_id` bigint NOT NULL,
    `backup_id` bigint NOT NULL,
    `pre_restore_backup_id` bigint NOT NULL,
    `restore_status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `write_block_enabled` tinyint(1) NOT NULL DEFAULT 0,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requester_user_id` bigint NOT NULL,
    `started_at` datetime(3) NOT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_restore_id` (`restore_id`),
    KEY `idx_operations_restore_backup` (`backup_id`),
    KEY `idx_operations_restore_status` (`restore_status`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维恢复记录表';

CREATE TABLE IF NOT EXISTS `operations_cleanup_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `cleanup_id` bigint NOT NULL,
    `cleanup_type` varchar(32) NOT NULL,
    `cleanup_status` varchar(16) NOT NULL DEFAULT 'RUNNING',
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `requester_user_id` bigint DEFAULT NULL,
    `started_at` datetime(3) NOT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_cleanup_job_id` (`cleanup_id`),
    KEY `idx_operations_cleanup_job_type` (`cleanup_type`, `started_at`),
    KEY `idx_operations_cleanup_job_status` (`cleanup_status`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维清理任务表';

CREATE TABLE IF NOT EXISTS `operations_cleanup_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `cleanup_item_id` bigint NOT NULL,
    `cleanup_id` bigint NOT NULL,
    `target_type` varchar(32) NOT NULL,
    `target_id` bigint NOT NULL,
    `item_status` varchar(16) NOT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `processed_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_cleanup_item_id` (`cleanup_item_id`),
    KEY `idx_operations_cleanup_item_job` (`cleanup_id`, `item_status`),
    KEY `idx_operations_cleanup_item_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维清理单项结果表';

CREATE TABLE IF NOT EXISTS `operations_health_check` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `check_id` bigint NOT NULL,
    `component` varchar(128) NOT NULL,
    `health_status` varchar(16) NOT NULL,
    `latency_ms` int DEFAULT NULL,
    `message` varchar(1024) DEFAULT NULL,
    `checked_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_health_check_id` (`check_id`),
    KEY `idx_operations_health_component` (`component`, `checked_at`),
    KEY `idx_operations_health_status` (`health_status`, `checked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维健康检查记录表';
