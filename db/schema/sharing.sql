SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sharing_link` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `share_id` char(26) NOT NULL,
    `share_token` varchar(96) NOT NULL,
    `owner_user_id` char(26) NOT NULL,
    `title` varchar(256) NOT NULL,
    `visibility` varchar(16) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `risk_confirmed` tinyint(1) NOT NULL DEFAULT 0,
    `risk_confirmer_user_id` char(26) DEFAULT NULL,
    `risk_confirmed_at` datetime(3) DEFAULT NULL,
    `issued_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `revoked_at` datetime(3) DEFAULT NULL,
    `restored_at` datetime(3) DEFAULT NULL,
    `last_accessed_at` datetime(3) DEFAULT NULL,
    `access_count` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sharing_link_id` (`share_id`),
    UNIQUE KEY `uk_sharing_link_token` (`share_token`),
    KEY `idx_sharing_link_owner` (`owner_user_id`, `issued_at`),
    KEY `idx_sharing_link_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享链接表';

CREATE TABLE IF NOT EXISTS `sharing_target` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `target_id` char(26) NOT NULL,
    `share_id` char(26) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` char(26) NOT NULL,
    `knowledge_base` varchar(64) NOT NULL,
    `title_snapshot` varchar(512) NOT NULL,
    `content_private_snapshot` tinyint(1) NOT NULL DEFAULT 0,
    `target_status` varchar(16) NOT NULL DEFAULT 'AVAILABLE',
    `sort_order` int NOT NULL DEFAULT 0,
    `snapshotted_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sharing_target_id` (`target_id`),
    UNIQUE KEY `uk_sharing_target_content` (`share_id`, `content_type`, `content_id`),
    KEY `idx_sharing_target_share` (`share_id`, `sort_order`),
    KEY `idx_sharing_target_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享目标表';

CREATE TABLE IF NOT EXISTS `sharing_batch` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `batch_id` char(26) NOT NULL,
    `requester_user_id` char(26) NOT NULL,
    `visibility` varchar(16) NOT NULL,
    `total_count` int NOT NULL DEFAULT 0,
    `success_count` int NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'RUNNING',
    `requested_at` datetime(3) NOT NULL,
    `cancelled_at` datetime(3) DEFAULT NULL,
    `completed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sharing_batch_id` (`batch_id`),
    KEY `idx_sharing_batch_requester` (`requester_user_id`, `requested_at`),
    KEY `idx_sharing_batch_status` (`status`, `requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量创建分享任务表';

CREATE TABLE IF NOT EXISTS `sharing_batch_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `batch_item_id` char(26) NOT NULL,
    `batch_id` char(26) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` char(26) NOT NULL,
    `share_id` char(26) DEFAULT NULL,
    `item_status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `failure_reason` varchar(1024) DEFAULT NULL,
    `processed_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sharing_batch_item_id` (`batch_item_id`),
    KEY `idx_sharing_batch_item_batch` (`batch_id`, `item_status`),
    KEY `idx_sharing_batch_item_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批量创建分享单项结果表';

CREATE TABLE IF NOT EXISTS `sharing_access_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `access_id` char(26) NOT NULL,
    `share_id` char(26) NOT NULL,
    `visitor_user_id` char(26) DEFAULT NULL,
    `access_result` varchar(32) NOT NULL,
    `failure_reason` varchar(1024) DEFAULT NULL,
    `accessed_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sharing_access_log_id` (`access_id`),
    KEY `idx_sharing_access_log_share` (`share_id`, `accessed_at`),
    KEY `idx_sharing_access_log_result` (`access_result`, `accessed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享访问日志表';
