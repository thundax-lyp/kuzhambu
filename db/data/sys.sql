SET NAMES utf8mb4;

INSERT INTO `sys_user` (
    `user_id`, `name`, `email`, `mobile`, `tel`, `avatar_object_id`, `rank`,
    `privilege`, `status`, `remarks`
) VALUES (
    '01KUZHAMBU00000000000001',
    '系统管理员',
    NULL,
    NULL,
    NULL,
    NULL,
    9,
    'SUPER',
    'ENABLED',
    '系统初始化管理员'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `rank` = VALUES(`rank`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `sys_role` (
    `role_id`, `name`, `privilege`, `status`, `priority`, `remarks`
) VALUES (
    '01KUZHAMBU00000000000002',
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

INSERT INTO `sys_menu` (
    `menu_id`, `parent_menu_id`, `name`, `permission_codes`, `rank`,
    `visibility`, `display_params`, `path`, `target`, `priority`, `remarks`
) VALUES
    (
        '01KUZHAMBU00000000000003', NULL, '系统管理', 'sys:*', 9,
        'VISIBLE', '{"icon":"settings"}', '/system', '_self', 10, '系统管理根菜单'
    ),
    (
        '01KUZHAMBU00000000000004', '01KUZHAMBU00000000000003', '用户管理',
        'sys:user:view,sys:user:create,sys:user:update,sys:user:disable,sys:user:delete',
        9, 'VISIBLE', '{"icon":"user"}', '/system/users', '_self', 20, '后台用户管理'
    ),
    (
        '01KUZHAMBU00000000000005', '01KUZHAMBU00000000000003', '角色管理',
        'sys:role:view,sys:role:create,sys:role:update,sys:role:delete,sys:role:authorize',
        9, 'VISIBLE', '{"icon":"shield"}', '/system/roles', '_self', 30, '后台角色管理'
    ),
    (
        '01KUZHAMBU00000000000006', '01KUZHAMBU00000000000003', '菜单管理',
        'sys:menu:view,sys:menu:create,sys:menu:update,sys:menu:delete',
        9, 'VISIBLE', '{"icon":"menu"}', '/system/menus', '_self', 40, '后台菜单管理'
    )
ON DUPLICATE KEY UPDATE
    `parent_menu_id` = VALUES(`parent_menu_id`),
    `name` = VALUES(`name`),
    `permission_codes` = VALUES(`permission_codes`),
    `rank` = VALUES(`rank`),
    `visibility` = VALUES(`visibility`),
    `display_params` = VALUES(`display_params`),
    `path` = VALUES(`path`),
    `target` = VALUES(`target`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES
    ('01KUZHAMBU00000000000001', '01KUZHAMBU00000000000002');

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000003'),
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000004'),
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000005'),
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000006');
