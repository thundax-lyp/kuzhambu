SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `auth_principal_identity` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `identity_id` char(26) NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` char(26) NOT NULL,
    `identity_type` varchar(32) NOT NULL,
    `identity_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_identity_id` (`identity_id`),
    UNIQUE KEY `uk_auth_principal_identity_type_value` (`identity_type`, `identity_value`),
    KEY `idx_auth_principal_identity_principal` (`principal_type`, `principal_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证主体登录标识表';

CREATE TABLE IF NOT EXISTS `auth_principal_credential` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `credential_id` char(26) NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` char(26) NOT NULL,
    `identity_id` char(26) NOT NULL,
    `credential_type` varchar(32) NOT NULL,
    `credential_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `need_change_password` tinyint(1) NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `failed_limit` int NOT NULL DEFAULT 5,
    `locked_until` datetime(3) DEFAULT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `last_verified_at` datetime(3) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_credential_id` (`credential_id`),
    UNIQUE KEY `uk_auth_principal_credential_identity_type` (`identity_id`, `credential_type`),
    KEY `idx_auth_principal_credential_principal` (`principal_type`, `principal_id`, `status`),
    KEY `idx_auth_principal_credential_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证主体凭据表';

CREATE TABLE IF NOT EXISTS `auth_principal_login_event` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `event_id` char(26) NOT NULL,
    `principal_type` varchar(32) DEFAULT NULL,
    `principal_id` char(26) DEFAULT NULL,
    `client_id` varchar(64) NOT NULL,
    `event_type` varchar(32) NOT NULL,
    `authentication_method` varchar(32) NOT NULL,
    `identity_type` varchar(32) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `ip` varchar(64) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `reason` varchar(128) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_login_event_id` (`event_id`),
    KEY `idx_auth_principal_login_event_principal_time` (`principal_type`, `principal_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_client_time` (`client_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_type_time` (`event_type`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证事件表';
