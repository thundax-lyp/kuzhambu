SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `system_user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `department_id` bigint DEFAULT NULL,
    `name` varchar(128) NOT NULL,
    `email` varchar(512) DEFAULT NULL,
    `mobile` varchar(512) DEFAULT NULL,
    `tel` varchar(64) DEFAULT NULL,
    `ranks` int NOT NULL DEFAULT 0,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_system_user_department` (`department_id`, `id`),
    KEY `idx_system_user_status` (`status`, `id`),
    KEY `idx_system_user_privilege` (`privilege`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户主体表';

CREATE TABLE IF NOT EXISTS `system_role` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(128) NOT NULL,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_system_role_status` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS `system_menu` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `parent_id` bigint DEFAULT NULL,
    `lft` int NOT NULL DEFAULT 0,
    `rgt` int NOT NULL DEFAULT 0,
    `name` varchar(128) NOT NULL,
    `perms` varchar(512) DEFAULT NULL,
    `ranks` int NOT NULL DEFAULT 0,
    `visibility` varchar(16) NOT NULL DEFAULT 'VISIBLE',
    `display_params` text DEFAULT NULL,
    `url` varchar(512) DEFAULT NULL,
    `target` varchar(64) DEFAULT NULL,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_system_menu_parent` (`parent_id`, `lft`),
    KEY `idx_system_menu_visibility` (`visibility`, `ranks`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台菜单和权限资源表';

CREATE TABLE IF NOT EXISTS `system_user_role` (
    `user_id` bigint NOT NULL,
    `role_id` bigint NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_system_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS `system_role_menu` (
    `role_id` bigint NOT NULL,
    `menu_id` bigint NOT NULL,
    PRIMARY KEY (`role_id`, `menu_id`),
    KEY `idx_system_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关系表';

CREATE TABLE IF NOT EXISTS `system_auth_principal_identity` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` bigint NOT NULL,
    `identity_type` varchar(32) NOT NULL,
    `identity_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_auth_principal_identity_type_value` (`identity_type`, `identity_value`),
    KEY `idx_system_auth_principal_identity_principal` (`principal_type`, `principal_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证主体登录标识表';

CREATE TABLE IF NOT EXISTS `system_auth_principal_credential` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` bigint NOT NULL,
    `identity_id` bigint NOT NULL,
    `credential_type` varchar(32) NOT NULL,
    `credential_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `need_change_password` tinyint(1) NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `failed_limit` int NOT NULL DEFAULT 5,
    `locked_until` datetime(3) DEFAULT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `last_verified_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_auth_principal_credential_identity_type` (`identity_id`, `credential_type`),
    KEY `idx_system_auth_principal_credential_principal` (`principal_type`, `principal_id`, `status`),
    KEY `idx_system_auth_principal_credential_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证主体凭据表';

CREATE TABLE IF NOT EXISTS `system_auth_principal_login_event` (
    `id` varchar(64) NOT NULL,
    `principal_type` varchar(32) DEFAULT NULL,
    `principal_id` bigint DEFAULT NULL,
    `client_id` varchar(64) NOT NULL,
    `event_type` varchar(32) NOT NULL,
    `authentication_method` varchar(32) NOT NULL,
    `identity_type` varchar(32) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `ip` varchar(64) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `reason` varchar(128) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_system_auth_principal_login_event_principal_time` (`principal_type`, `principal_id`, `occurred_at`),
    KEY `idx_system_auth_principal_login_event_client_time` (`client_id`, `occurred_at`),
    KEY `idx_system_auth_principal_login_event_type_time` (`event_type`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证事件表';

CREATE TABLE IF NOT EXISTS `system_audit_meta` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `object_type` varchar(128) NOT NULL,
    `object_id` varchar(64) NOT NULL,
    `version` bigint NOT NULL,
    `last_log_id` bigint NOT NULL,
    `last_action` varchar(32) NOT NULL,
    `last_operator_type` varchar(32) NOT NULL,
    `last_operator_id` varchar(64) DEFAULT NULL,
    `last_operator_name` varchar(128) DEFAULT NULL,
    `last_operated_at` datetime(3) NOT NULL,
    `created_log_id` bigint NOT NULL,
    `created_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_audit_meta_object` (`object_type`, `object_id`),
    KEY `idx_system_audit_meta_last_operated` (`last_operated_at`),
    KEY `idx_system_audit_meta_last_operator` (`last_operator_type`, `last_operator_id`, `last_operated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务对象审计元数据表';

CREATE TABLE IF NOT EXISTS `system_audit_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `meta_id` bigint NOT NULL,
    `object_type` varchar(128) NOT NULL,
    `object_id` varchar(64) NOT NULL,
    `version` bigint NOT NULL,
    `previous_version` bigint NOT NULL,
    `action` varchar(32) NOT NULL,
    `idempotency_key` varchar(128) NOT NULL,
    `operator_type` varchar(32) NOT NULL,
    `operator_id` varchar(64) DEFAULT NULL,
    `operator_name` varchar(128) DEFAULT NULL,
    `source` varchar(64) NOT NULL,
    `request_id` varchar(128) DEFAULT NULL,
    `trace_id` varchar(128) DEFAULT NULL,
    `remote_addr` varchar(64) DEFAULT NULL,
    `summary` varchar(512) DEFAULT NULL,
    `snapshot_schema_version` int NOT NULL DEFAULT 1,
    `before_snapshot` longtext DEFAULT NULL,
    `after_snapshot` longtext DEFAULT NULL,
    `changed_fields` longtext DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_audit_log_idempotency` (`idempotency_key`),
    KEY `idx_system_audit_log_object` (`object_type`, `object_id`, `occurred_at`),
    KEY `idx_system_audit_log_meta_version` (`meta_id`, `version`),
    KEY `idx_system_audit_log_operator` (`operator_type`, `operator_id`, `occurred_at`),
    KEY `idx_system_audit_log_action` (`action`, `occurred_at`),
    KEY `idx_system_audit_log_request` (`request_id`),
    KEY `idx_system_audit_log_trace` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务对象审计日志表';
