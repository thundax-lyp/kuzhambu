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
    `issued_at` datetime(3) NOT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
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
    `sort_order` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sharing_target_id` (`target_id`),
    UNIQUE KEY `uk_sharing_target_content` (`share_id`, `content_type`, `content_id`),
    KEY `idx_sharing_target_share` (`share_id`, `sort_order`),
    KEY `idx_sharing_target_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享目标表';
