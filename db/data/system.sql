SET NAMES utf8mb4;

INSERT INTO `system_user` (
    `id`, `name`, `email`, `mobile`, `tel`, `ranks`, `privilege`, `status`, `remarks`
) VALUES (
    1,
    '系统管理员',
    NULL,
    NULL,
    NULL,
    9,
    'SUPER',
    'ENABLED',
    '系统初始化管理员'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `ranks` = VALUES(`ranks`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `system_user` (
    `id`, `name`, `email`, `mobile`, `tel`, `ranks`, `privilege`, `status`, `remarks`
) VALUES (
    2,
    '开发者',
    NULL,
    NULL,
    NULL,
    9,
    'SUPER',
    'ENABLED',
    '本地开发调试超级管理员'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `ranks` = VALUES(`ranks`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `system_role` (
    `id`, `name`, `privilege`, `status`, `priority`, `remarks`
) VALUES (
    1,
    '超级管理员',
    'ADMIN',
    'ENABLED',
    1,
    '拥有全部后台管理权限'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `system_menu` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `perms`, `ranks`,
    `visibility`, `display_params`, `url`, `target`, `remarks`
) VALUES
    (
        5, NULL, 1, 2, '仪表盘', 'user', 100,
        'VISIBLE', '{"icon":"dashboard"}', '/dashboard', '_self', '后台工作台'
    ),
    (
        1, NULL, 3, 16, '系统管理', 'sys', 90,
        'VISIBLE', '{"icon":"system"}', '/system', '_self', '系统管理根菜单'
    ),
    (
        2, 1, 4, 5, '用户管理',
        'sys:user:view,sys:user:edit',
        90, 'VISIBLE', '{"icon":"users"}', '/system/users', '_self', '后台用户管理'
    ),
    (
        6, 1, 6, 7, '部门管理',
        'sys:department:view,sys:department:edit',
        80, 'VISIBLE', '{"icon":"departments"}', '/system/departments', '_self', '后台部门管理'
    ),
    (
        3, 1, 8, 9, '角色管理',
        'sys:role:view,sys:role:edit',
        70, 'VISIBLE', '{"icon":"roles"}', '/system/roles', '_self', '后台角色管理'
    ),
    (
        4, 1, 10, 11, '菜单管理',
        'super',
        60, 'VISIBLE', '{"icon":"menus"}', '/system/menus', '_self', '后台菜单管理'
    ),
    (
        7, 1, 12, 13, '字典管理',
        'sys:dict:view,sys:dict:edit',
        50, 'VISIBLE', '{"icon":"dictionaries"}', '/system/dictionaries', '_self', '后台字典管理'
    ),
    (
        8, 1, 14, 15, '系统日志',
        'super',
        40, 'VISIBLE', '{"icon":"logs"}', '/system/logs', '_self', '后台运行日志'
    ),
    (
        20, NULL, 17, 20, '存储管理', 'storage', 80,
        'VISIBLE', '{"icon":"storage"}', '/storage', '_self', '对象存储管理根菜单'
    ),
    (
        21, 20, 18, 19, '对象管理',
        'storage:object:view,storage:object:edit,storage:storage:edit',
        80, 'VISIBLE', '{"icon":"storage-objects"}', '/storage/objects', '_self', '对象存储文件管理'
    ),
    (
        30, NULL, 21, 24, '投稿管理', 'submission', 70,
        'VISIBLE', '{"icon":"submission"}', '/submission', '_self', '投稿管理根菜单'
    ),
    (
        31, 30, 22, 23, '投稿列表',
        'submission:submission:view,submission:submission:edit',
        70, 'VISIBLE', '{"icon":"submissions"}', '/submission/submissions', '_self', '投稿内容管理'
    ),
    (
        40, NULL, 25, 28, '开放接口', 'open', 60,
        'VISIBLE', '{"icon":"open-api"}', '/open', '_self', '开放接口管理根菜单'
    ),
    (
        41, 40, 26, 27, '客户端管理',
        'open:client:view,open:client:edit',
        60, 'VISIBLE', '{"icon":"open-clients"}', '/open/clients', '_self', '开放接口客户端管理'
    ),
    (
        50, NULL, 29, 32, '审计中心', 'audit:view', 50,
        'VISIBLE', '{"icon":"audit"}', '/audit', '_self', '审计中心根菜单'
    ),
    (
        51, 50, 30, 31, '审计日志',
        'audit:view',
        50, 'VISIBLE', '{"icon":"audit-logs"}', '/audit/logs', '_self', '业务审计日志'
    )
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `lft` = VALUES(`lft`),
    `rgt` = VALUES(`rgt`),
    `name` = VALUES(`name`),
    `perms` = VALUES(`perms`),
    `ranks` = VALUES(`ranks`),
    `visibility` = VALUES(`visibility`),
    `display_params` = VALUES(`display_params`),
    `url` = VALUES(`url`),
    `target` = VALUES(`target`),
    `remarks` = VALUES(`remarks`);

INSERT IGNORE INTO `system_user_role` (`user_id`, `role_id`) VALUES
    (1, 1),
    (2, 1);

INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 5),
    (1, 6),
    (1, 7),
    (1, 8),
    (1, 20),
    (1, 21),
    (1, 30),
    (1, 31),
    (1, 40),
    (1, 41),
    (1, 50),
    (1, 51);

SET NAMES utf8mb4;

-- Initial admin account:
--   login name: admin
--   password credential value is a placeholder and must be rotated before production use.

INSERT INTO `system_auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES (
    1,
    'USER',
    1,
    'USER_ACCOUNT',
    'admin',
    'ENABLED'
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `system_auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES (
    2,
    'USER',
    2,
    'USER_ACCOUNT',
    'developer',
    'ENABLED'
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `system_auth_principal_credential` (
    `id`, `principal_type`, `principal_id`, `identity_id`,
    `credential_type`, `credential_value`, `status`, `need_change_password`,
    `failed_count`, `failed_limit`
) VALUES (
    1,
    'USER',
    1,
    1,
    'USER_PASSWORD',
    '{noop}admin',
    'ACTIVE',
    1,
    0,
    5
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`);

INSERT INTO `system_auth_principal_credential` (
    `id`, `principal_type`, `principal_id`, `identity_id`,
    `credential_type`, `credential_value`, `status`, `need_change_password`,
    `failed_count`, `failed_limit`
) VALUES (
    2,
    'USER',
    2,
    2,
    'USER_PASSWORD',
    '{noop}Q1w2e3r$',
    'ACTIVE',
    0,
    0,
    5
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`);

SET NAMES utf8mb4;

-- Audit has no required seed data.
