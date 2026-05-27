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

INSERT INTO `system_role` (
    `id`, `name`, `privilege`, `status`, `priority`, `remarks`
) VALUES (
    1,
    '超级管理员',
    'SUPER',
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
        1, NULL, 1, 8, '系统管理', 'system:*', 9,
        'VISIBLE', '{"icon":"settings"}', '/system', '_self', '系统管理根菜单'
    ),
    (
        2, 1, 2, 3, '用户管理',
        'system:user:view,system:user:create,system:user:update,system:user:disable,system:user:delete',
        9, 'VISIBLE', '{"icon":"user"}', '/system/users', '_self', '后台用户管理'
    ),
    (
        3, 1, 4, 5, '角色管理',
        'system:role:view,system:role:create,system:role:update,system:role:delete,system:role:authorize',
        9, 'VISIBLE', '{"icon":"shield"}', '/system/roles', '_self', '后台角色管理'
    ),
    (
        4, 1, 6, 7, '菜单管理',
        'system:menu:view,system:menu:create,system:menu:update,system:menu:delete',
        9, 'VISIBLE', '{"icon":"menu"}', '/system/menus', '_self', '后台菜单管理'
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
    (1, 1);

INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4);

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

SET NAMES utf8mb4;

-- Audit has no required seed data.
