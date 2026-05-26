SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` char(26) NOT NULL,
    `name` varchar(128) NOT NULL,
    `email` varchar(512) DEFAULT NULL,
    `mobile` varchar(512) DEFAULT NULL,
    `tel` varchar(64) DEFAULT NULL,
    `avatar_object_id` char(26) DEFAULT NULL,
    `rank` int NOT NULL DEFAULT 0,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_user_id` (`user_id`),
    KEY `idx_sys_user_status` (`status`, `id`),
    KEY `idx_sys_user_privilege` (`privilege`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户主体表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `role_id` char(26) NOT NULL,
    `name` varchar(128) NOT NULL,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_role_id` (`role_id`),
    KEY `idx_sys_role_status` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `menu_id` char(26) NOT NULL,
    `parent_menu_id` char(26) DEFAULT NULL,
    `name` varchar(128) NOT NULL,
    `permission_codes` varchar(512) DEFAULT NULL,
    `rank` int NOT NULL DEFAULT 0,
    `visibility` varchar(16) NOT NULL DEFAULT 'VISIBLE',
    `display_params` text DEFAULT NULL,
    `path` varchar(512) DEFAULT NULL,
    `target` varchar(64) DEFAULT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_menu_menu_id` (`menu_id`),
    KEY `idx_sys_menu_parent` (`parent_menu_id`, `priority`),
    KEY `idx_sys_menu_visibility` (`visibility`, `rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台菜单和权限资源表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id` char(26) NOT NULL,
    `role_id` char(26) NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id` char(26) NOT NULL,
    `menu_id` char(26) NOT NULL,
    PRIMARY KEY (`role_id`, `menu_id`),
    KEY `idx_sys_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关系表';
