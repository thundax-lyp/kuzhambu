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
    `requester_user_id` bigint DEFAULT NULL,
    `requested_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_report_id` (`report_id`),
    KEY `idx_operations_report_period` (`report_type`, `period_start`, `period_end`),
    KEY `idx_operations_report_status` (`report_status`, `requested_at`),
    KEY `idx_operations_report_requester` (`requester_user_id`, `requested_at`)
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
    `started_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    `expires_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_backup_id` (`backup_id`),
    KEY `idx_operations_backup_status` (`backup_status`, `started_at`),
    KEY `idx_operations_backup_expires` (`expires_at`),
    KEY `idx_operations_backup_requester` (`requester_user_id`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维备份记录表';

CREATE TABLE IF NOT EXISTS `operations_restore` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `restore_id` bigint NOT NULL,
    `backup_id` bigint NOT NULL,
    `pre_restore_backup_id` bigint NOT NULL,
    `restore_mode` varchar(16) NOT NULL DEFAULT 'REAL',
    `restore_status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `write_block_enabled` tinyint(1) NOT NULL DEFAULT 0,
    `write_block_started_at` BIGINT DEFAULT NULL,
    `write_block_released_at` BIGINT DEFAULT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requester_user_id` bigint NOT NULL,
    `started_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_restore_id` (`restore_id`),
    KEY `idx_operations_restore_backup` (`backup_id`),
    KEY `idx_operations_restore_status` (`restore_status`, `started_at`),
    KEY `idx_operations_restore_requester` (`requester_user_id`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维恢复记录表';

CREATE TABLE IF NOT EXISTS `operations_cleanup_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `cleanup_id` bigint NOT NULL,
    `cleanup_type` varchar(32) NOT NULL,
    `cleanup_status` varchar(16) NOT NULL DEFAULT 'RUNNING',
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requester_user_id` bigint DEFAULT NULL,
    `started_at` BIGINT NOT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_cleanup_job_id` (`cleanup_id`),
    KEY `idx_operations_cleanup_job_type` (`cleanup_type`, `started_at`),
    KEY `idx_operations_cleanup_job_status` (`cleanup_status`, `started_at`),
    KEY `idx_operations_cleanup_job_requester` (`requester_user_id`, `started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维清理任务表';

CREATE TABLE IF NOT EXISTS `operations_cleanup_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `cleanup_item_id` bigint NOT NULL,
    `cleanup_id` bigint NOT NULL,
    `target_type` varchar(32) NOT NULL,
    `target_id` bigint NOT NULL,
    `item_status` varchar(16) NOT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `processed_at` BIGINT NOT NULL,
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
    `probe_source` varchar(64) NOT NULL DEFAULT 'LOCAL',
    `probe_target` varchar(128) DEFAULT NULL,
    `details_json` text DEFAULT NULL,
    `checked_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_health_check_id` (`check_id`),
    KEY `idx_operations_health_component` (`component`, `checked_at`),
    KEY `idx_operations_health_status` (`health_status`, `checked_at`),
    KEY `idx_operations_health_probe` (`probe_source`, `probe_target`, `checked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维健康检查记录表';

CREATE TABLE IF NOT EXISTS `operations_health_alert` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `alert_id` bigint NOT NULL,
    `component` varchar(128) NOT NULL,
    `alert_type` varchar(32) NOT NULL,
    `alert_level` varchar(16) NOT NULL,
    `alert_status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `source_ref_type` varchar(32) NOT NULL,
    `source_ref_id` bigint DEFAULT NULL,
    `latest_check_id` bigint DEFAULT NULL,
    `message` varchar(1024) DEFAULT NULL,
    `suggestion` varchar(1024) DEFAULT NULL,
    `recovery_action` varchar(64) DEFAULT NULL,
    `recovery_target` text DEFAULT NULL,
    `first_triggered_at` BIGINT NOT NULL,
    `last_triggered_at` BIGINT NOT NULL,
    `acked_at` BIGINT DEFAULT NULL,
    `acked_by_user_id` bigint DEFAULT NULL,
    `recovered_at` BIGINT DEFAULT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_health_alert_id` (`alert_id`),
    KEY `idx_operations_health_alert_status` (`alert_status`, `alert_level`, `last_triggered_at`),
    KEY `idx_operations_health_alert_component` (`component`, `alert_status`, `last_triggered_at`),
    KEY `idx_operations_health_alert_source` (`source_ref_type`, `source_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维健康告警表';

CREATE TABLE IF NOT EXISTS `operations_long_task_snapshot` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `snapshot_id` bigint NOT NULL,
    `source_domain` varchar(32) NOT NULL,
    `task_type` varchar(32) NOT NULL,
    `task_key` varchar(64) NOT NULL,
    `task_status` varchar(16) NOT NULL,
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `requested_by_user_id` bigint DEFAULT NULL,
    `started_at` BIGINT DEFAULT NULL,
    `completed_at` BIGINT DEFAULT NULL,
    `snapshot_at` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_operations_long_task_snapshot_id` (`snapshot_id`),
    KEY `idx_operations_long_task_snapshot_task` (`source_domain`, `task_type`, `snapshot_at`),
    KEY `idx_operations_long_task_snapshot_status` (`task_status`, `snapshot_at`),
    KEY `idx_operations_long_task_snapshot_key` (`task_key`, `snapshot_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维长任务状态快照表';
