SET NAMES utf8mb4;

INSERT INTO `system_department` (
    `id`, `parent_id`, `lft`, `rgt`, `name`, `short_name`, `remarks`
) VALUES
    (
        1000000000000000001, NULL, 1, 40, 'GitHub', 'GitHub',
        '系统初始化根部门'
    ),
    (
        1000000000000000002, 1000000000000000001, 2, 7, '总裁办', 'CEO Office',
        '公司治理、战略决策和综合协调'
    ),
    (
        1000000000000000003, 1000000000000000002, 3, 4, '战略发展部', 'Strategy',
        '战略规划、行业研究和重点项目跟进'
    ),
    (
        1000000000000000004, 1000000000000000002, 5, 6, '法务合规部', 'Legal',
        '合同、合规、风险控制和制度审查'
    ),
    (
        1000000000000000005, 1000000000000000001, 8, 21, '研发中心', 'R&D',
        '产品研发、工程交付和技术平台建设'
    ),
    (
        1000000000000000006, 1000000000000000005, 9, 10, '平台架构部', 'Platform',
        '基础架构、公共组件和技术规范'
    ),
    (
        1000000000000000007, 1000000000000000005, 11, 12, '后端研发部', 'Backend',
        '后端服务、业务接口和系统集成'
    ),
    (
        1000000000000000008, 1000000000000000005, 13, 14, '前端体验部', 'Frontend',
        '管理台、前台应用和用户体验实现'
    ),
    (
        1000000000000000009, 1000000000000000005, 15, 16, '测试质量部', 'QA',
        '测试体系、质量保障和发布验收'
    ),
    (
        1000000000000000010, 1000000000000000005, 17, 18, '运维效能部', 'DevOps',
        '环境、部署、监控和研发效能'
    ),
    (
        1000000000000000011, 1000000000000000005, 19, 20, '数据智能部', 'Data',
        '数据分析、报表建设和智能能力探索'
    ),
    (
        1000000000000000012, 1000000000000000001, 22, 29, '产品中心', 'Product',
        '产品规划、体验设计和需求管理'
    ),
    (
        1000000000000000013, 1000000000000000012, 23, 24, '产品规划部', 'PM',
        '产品路线图、需求评审和版本规划'
    ),
    (
        1000000000000000014, 1000000000000000012, 25, 26, '交互设计部', 'Design',
        '交互设计、视觉规范和体验走查'
    ),
    (
        1000000000000000015, 1000000000000000012, 27, 28, '用户研究部', 'UXR',
        '用户研究、反馈分析和可用性验证'
    ),
    (
        1000000000000000016, 1000000000000000001, 30, 35, '商业化中心', 'Business',
        '市场增长、客户成功和商业化运营'
    ),
    (
        1000000000000000017, 1000000000000000016, 31, 32, '市场运营部', 'Marketing',
        '市场活动、内容运营和品牌传播'
    ),
    (
        1000000000000000018, 1000000000000000016, 33, 34, '客户成功部', 'CS',
        '客户交付、续约支持和服务质量'
    ),
    (
        1000000000000000019, 1000000000000000001, 36, 39, '财务人事中心', 'FAHR',
        '财务核算、人事行政和组织支持'
    ),
    (
        1000000000000000020, 1000000000000000019, 37, 38, '财务管理部', 'Finance',
        '预算、核算、报销和经营分析'
    )
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `lft` = VALUES(`lft`),
    `rgt` = VALUES(`rgt`),
    `name` = VALUES(`name`),
    `short_name` = VALUES(`short_name`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `system_user` (
    `id`, `department_id`, `name`, `email`, `mobile`, `tel`, `ranks`, `privilege`, `status`, `remarks`
) VALUES (
    1,
    1000000000000000001,
    '系统管理员',
    NULL,
    NULL,
    NULL,
    9,
    'SUPER',
    'ENABLED',
    '系统初始化管理员'
) ON DUPLICATE KEY UPDATE
    `department_id` = VALUES(`department_id`),
    `name` = VALUES(`name`),
    `ranks` = VALUES(`ranks`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `system_user` (
    `id`, `department_id`, `name`, `email`, `mobile`, `tel`, `ranks`, `privilege`, `status`, `remarks`
) VALUES (
    2,
    1000000000000000001,
    '开发者',
    NULL,
    NULL,
    NULL,
    9,
    'SUPER',
    'ENABLED',
    '本地开发调试超级管理员'
) ON DUPLICATE KEY UPDATE
    `department_id` = VALUES(`department_id`),
    `name` = VALUES(`name`),
    `ranks` = VALUES(`ranks`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `system_user` (
    `id`, `department_id`, `email`, `mobile`, `tel`, `name`, `ranks`,
    `privilege`, `status`, `remarks`
) VALUES
    (1000000000000000102, 1000000000000000002, NULL, NULL, '8001', '林知远', 3, 'ADMIN', 'ENABLED', '总裁办系统管理员，负责后台基础权限配置'),
    (1000000000000000103, 1000000000000000003, NULL, NULL, '8101', '周承策', 2, 'NORMAL', 'ENABLED', '战略发展部组织协调人'),
    (1000000000000000104, 1000000000000000004, NULL, NULL, '8201', '顾清禾', 2, 'NORMAL', 'ENABLED', '法务合规部审计观察员'),
    (1000000000000000105, 1000000000000000006, NULL, NULL, '8301', '许慕白', 3, 'ADMIN', 'ENABLED', '平台架构部系统管理员'),
    (1000000000000000106, 1000000000000000007, NULL, NULL, '8302', '陈泊舟', 2, 'NORMAL', 'ENABLED', '后端研发部组织管理员'),
    (1000000000000000107, 1000000000000000008, NULL, NULL, '8303', '叶晚晴', 2, 'NORMAL', 'ENABLED', '前端体验部配置管理员'),
    (1000000000000000108, 1000000000000000009, NULL, NULL, '8304', '唐以宁', 3, 'NORMAL', 'ENABLED', '测试质量部审计观察员'),
    (1000000000000000109, 1000000000000000010, NULL, NULL, '8305', '沈嘉木', 2, 'NORMAL', 'DISABLED', '运维效能部临时停用账号'),
    (1000000000000000110, 1000000000000000011, NULL, NULL, '8306', '陆景明', 2, 'NORMAL', 'ENABLED', '数据智能部配置管理员'),
    (1000000000000000111, 1000000000000000013, NULL, NULL, '8401', '姜书瑶', 2, 'NORMAL', 'ENABLED', '产品规划部运营支持'),
    (1000000000000000112, 1000000000000000014, NULL, NULL, '8402', '宋亦安', 3, 'NORMAL', 'ENABLED', '交互设计部审计观察员'),
    (1000000000000000113, 1000000000000000017, NULL, NULL, '8501', '韩星野', 2, 'NORMAL', 'ENABLED', '市场运营部运营支持'),
    (1000000000000000114, 1000000000000000018, NULL, NULL, '8502', '秦若川', 2, 'NORMAL', 'ENABLED', '客户成功部运营支持'),
    (1000000000000000115, 1000000000000000020, NULL, NULL, '8601', '程予墨', 2, 'NORMAL', 'ENABLED', '财务管理部审计观察员'),
    (1000000000000000116, 1000000000000000002, NULL, NULL, '8002', '赵北辰', 3, 'ADMIN', 'ENABLED', '总裁办组织权限管理员'),
    (1000000000000000117, 1000000000000000003, NULL, NULL, '8102', '何望舒', 2, 'NORMAL', 'ENABLED', '战略发展部项目协同负责人'),
    (1000000000000000118, 1000000000000000004, NULL, NULL, '8202', '邱明澈', 3, 'NORMAL', 'ENABLED', '法务合规部审计专员'),
    (1000000000000000119, 1000000000000000006, NULL, NULL, '8307', '梁思衡', 2, 'NORMAL', 'ENABLED', '平台架构部配置管理员'),
    (1000000000000000120, 1000000000000000007, NULL, NULL, '8308', '罗子衿', 2, 'NORMAL', 'ENABLED', '后端研发部服务治理负责人'),
    (1000000000000000121, 1000000000000000008, NULL, NULL, '8309', '苏云舟', 2, 'NORMAL', 'ENABLED', '前端体验部页面配置负责人'),
    (1000000000000000122, 1000000000000000009, NULL, NULL, '8310', '孟青岚', 3, 'NORMAL', 'ENABLED', '测试质量部回归验证负责人'),
    (1000000000000000123, 1000000000000000010, NULL, NULL, '8311', '白景行', 2, 'NORMAL', 'ENABLED', '运维效能部发布支持'),
    (1000000000000000124, 1000000000000000011, NULL, NULL, '8312', '夏安歌', 2, 'NORMAL', 'DISABLED', '数据智能部暂停访问账号'),
    (1000000000000000125, 1000000000000000013, NULL, NULL, '8403', '季星河', 2, 'NORMAL', 'ENABLED', '产品规划部需求运营支持'),
    (1000000000000000126, 1000000000000000014, NULL, NULL, '8404', '余清越', 3, 'NORMAL', 'ENABLED', '交互设计部体验审阅员'),
    (1000000000000000127, 1000000000000000017, NULL, NULL, '8503', '冯若谷', 2, 'NORMAL', 'ENABLED', '市场运营部活动配置支持'),
    (1000000000000000128, 1000000000000000018, NULL, NULL, '8504', '魏南风', 2, 'NORMAL', 'ENABLED', '客户成功部客户资料维护'),
    (1000000000000000129, 1000000000000000020, NULL, NULL, '8602', '丁晓棠', 3, 'NORMAL', 'ENABLED', '财务管理部报表查看员'),
    (1000000000000000130, 1000000000000000005, NULL, NULL, '8300', '马砚秋', 3, 'ADMIN', 'ENABLED', '技术中心系统管理员'),
    (1000000000000000131, 1000000000000000012, NULL, NULL, '8400', '谢长风', 2, 'NORMAL', 'ENABLED', '产品体验中心组织管理员'),
    (1000000000000000132, 1000000000000000015, NULL, NULL, '8500', '蒋南星', 2, 'NORMAL', 'ENABLED', '商业增长中心运营管理员'),
    (1000000000000000133, 1000000000000000016, NULL, NULL, '8505', '袁初夏', 2, 'NORMAL', 'DISABLED', '销售拓展部离岗保留账号'),
    (1000000000000000134, 1000000000000000019, NULL, NULL, '8600', '曹远山', 2, 'NORMAL', 'ENABLED', '财务人事中心组织支持'),
    (1000000000000000135, 1000000000000000001, NULL, NULL, '8003', '任澜溪', 2, 'NORMAL', 'ENABLED', '总部综合事务支持')
ON DUPLICATE KEY UPDATE
    `department_id` = VALUES(`department_id`),
    `tel` = VALUES(`tel`),
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

INSERT INTO `system_role` (
    `id`, `name`, `privilege`, `status`, `priority`, `remarks`
) VALUES
    (1000000000000000401, '超级管理员', 'ADMIN', 'ENABLED', 10, '系统初始化角色，拥有全部后台菜单和权限资源'),
    (1000000000000000402, '系统管理员', 'ADMIN', 'ENABLED', 20, '负责用户、角色、部门和字典等后台基础配置'),
    (1000000000000000403, '组织管理员', 'NORMAL', 'ENABLED', 30, '负责用户与部门维护，不管理角色授权'),
    (1000000000000000404, '配置管理员', 'NORMAL', 'ENABLED', 40, '负责字典、存储对象和常规配置维护'),
    (1000000000000000405, '审计观察员', 'NORMAL', 'ENABLED', 50, '只读查看系统、用户、角色、部门和字典配置'),
    (1000000000000000406, '运营支持', 'NORMAL', 'ENABLED', 60, '负责运营支持场景下的用户查询和字典查看')
ON DUPLICATE KEY UPDATE
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
        100, NULL, 3, 14, '古籍管理', 'classics', 95,
        'VISIBLE', '{"icon":"dictionaries"}', '/classics', '_self', '古籍内容管理根菜单'
    ),
    (
        101, 100, 4, 5, '三才图会',
        'classics:sancai:view,classics:sancai:edit,classics:sancai:delete',
        95, 'VISIBLE', '{"icon":"dictionaries"}', '/classics/sancai', '_self', '三才图会知识库'
    ),
    (
        102, 100, 6, 7, '王圻文档',
        'classics:wangqi:view,classics:wangqi:edit,classics:wangqi:delete',
        90, 'VISIBLE', '{"icon":"dictionaries"}', '/classics/wangqi', '_self', '王圻文档知识库'
    ),
    (
        103, 100, 8, 9, '明代习俗',
        'classics:mingcustoms:view,classics:mingcustoms:edit,classics:mingcustoms:delete',
        85, 'VISIBLE', '{"icon":"dictionaries"}', '/classics/ming-customs', '_self', '明代习俗知识库'
    ),
    (
        104, 100, 10, 11, '内容导出',
        'classics:content:view,classics:content:edit,classics:content:export',
        80, 'VISIBLE', '{"icon":"submissions"}', '/classics/exports', '_self', '古籍内容和视觉资产导出'
    ),
    (
        105, 100, 12, 13, '分享管理',
        'classics:sharing:view,classics:sharing:edit',
        75, 'VISIBLE', '{"icon":"open-api"}', '/classics/shares', '_self', '古籍内容分享管理'
    ),
    (
        110, NULL, 15, 24, 'AI 管理', 'ai', 90,
        'VISIBLE', '{"icon":"system"}', '/ai', '_self', 'AI 配置和调用治理根菜单'
    ),
    (
        111, 110, 16, 17, 'AI 配置',
        'ai:config:view,ai:config:edit',
        90, 'VISIBLE', '{"icon":"system"}', '/ai/config', '_self', 'AI 服务、模型和能力映射'
    ),
    (
        112, 110, 18, 19, '提示词管理',
        'ai:prompt:view,ai:prompt:edit',
        85, 'VISIBLE', '{"icon":"dictionaries"}', '/ai/prompts', '_self', 'AI 提示词模板和版本'
    ),
    (
        113, 110, 20, 21, 'AI 精修',
        'ai:refinement:edit',
        80, 'VISIBLE', '{"icon":"submissions"}', '/ai/refinement', '_self', 'AI 内容精修能力入口'
    ),
    (
        114, 110, 22, 23, '调用记录',
        'ai:invocation:view,ai:invocation:edit',
        75, 'VISIBLE', '{"icon":"logs"}', '/ai/invocations', '_self', 'AI 调用记录、候选和批量任务'
    ),
    (
        120, NULL, 25, 34, '知识管理', 'knowledge', 85,
        'VISIBLE', '{"icon":"dictionaries"}', '/knowledge', '_self', '标签、同义词和知识图谱根菜单'
    ),
    (
        121, 120, 26, 27, '标签治理',
        'knowledge:tag:view,knowledge:tag:edit',
        85, 'VISIBLE', '{"icon":"dictionaries"}', '/knowledge/tags', '_self', '跨知识库标签治理'
    ),
    (
        122, 120, 28, 29, '同义词词典',
        'knowledge:synonym:view,knowledge:synonym:edit',
        80, 'VISIBLE', '{"icon":"dictionaries"}', '/knowledge/synonyms', '_self', '同义词维护和检索扩展'
    ),
    (
        123, 120, 30, 31, '数据精修',
        'knowledge:refinement:view,knowledge:refinement:edit',
        75, 'VISIBLE', '{"icon":"submissions"}', '/knowledge/refinement', '_self', '实体和关系精修工作台'
    ),
    (
        124, 120, 32, 33, '知识图谱',
        'knowledge:graph:view,knowledge:graph:edit',
        70, 'VISIBLE', '{"icon":"open-api"}', '/knowledge/graph', '_self', '三才图会知识图谱'
    ),
    (
        130, NULL, 35, 42, '搜索问答', 'discovery', 80,
        'VISIBLE', '{"icon":"open-api"}', '/discovery', '_self', '跨库搜索和智能问答根菜单'
    ),
    (
        131, 130, 36, 37, '跨库搜索',
        'discovery:search:view',
        80, 'VISIBLE', '{"icon":"open-api"}', '/discovery/search', '_self', '三类古籍跨库搜索'
    ),
    (
        132, 130, 38, 39, '智能问答',
        'discovery:qa:view,discovery:qa:edit',
        75, 'VISIBLE', '{"icon":"open-api"}', '/discovery/qa', '_self', '跨知识库智能问答'
    ),
    (
        133, 130, 40, 41, '问答调试',
        'discovery:debug:view',
        70, 'VISIBLE', '{"icon":"logs"}', '/discovery/debug', '_self', '问答上下文调试'
    ),
    (
        140, NULL, 43, 54, '运营运维', 'operations', 70,
        'VISIBLE', '{"icon":"dashboard"}', '/operations', '_self', '运营运维根菜单'
    ),
    (
        141, 140, 44, 45, '运营看板',
        'operations:dashboard:view',
        70, 'VISIBLE', '{"icon":"dashboard"}', '/operations/dashboard', '_self', '内容、AI、搜索和问答统计'
    ),
    (
        142, 140, 46, 47, '报表记录',
        'operations:report:view,operations:report:edit',
        65, 'VISIBLE', '{"icon":"submissions"}', '/operations/reports', '_self', '周报月报生成记录'
    ),
    (
        143, 140, 48, 49, '任务台账',
        'operations:task:view',
        60, 'VISIBLE', '{"icon":"logs"}', '/operations/tasks', '_self', '长任务和批量操作运行状态'
    ),
    (
        144, 140, 50, 51, '备份恢复',
        'operations:backup:view,operations:backup:edit',
        55, 'VISIBLE', '{"icon":"storage"}', '/operations/backups', '_self', '备份列表、手动备份和恢复入口'
    ),
    (
        145, 140, 52, 53, '清理维护',
        'operations:cleanup:view,operations:cleanup:edit',
        50, 'VISIBLE', '{"icon":"menus"}', '/operations/cleanup', '_self', '维护清理任务入口'
    ),
    (
        50, NULL, 55, 58, '审计中心', 'audit:view', 65,
        'VISIBLE', '{"icon":"audit"}', '/audit', '_self', '审计中心根菜单'
    ),
    (
        51, 50, 56, 57, '审计日志',
        'audit:view',
        65, 'VISIBLE', '{"icon":"audit-logs"}', '/audit/logs', '_self', '业务审计日志'
    ),
    (
        1, NULL, 59, 76, '系统管理', 'sys', 60,
        'VISIBLE', '{"icon":"system"}', '/system', '_self', '系统管理根菜单'
    ),
    (
        2, 1, 60, 61, '用户管理',
        'sys:user:view,sys:user:edit',
        60, 'VISIBLE', '{"icon":"users"}', '/system/users', '_self', '后台用户管理'
    ),
    (
        6, 1, 62, 63, '部门管理',
        'sys:department:view,sys:department:edit',
        55, 'VISIBLE', '{"icon":"departments"}', '/system/departments', '_self', '后台部门管理'
    ),
    (
        3, 1, 64, 65, '角色管理',
        'sys:role:view,sys:role:edit',
        50, 'VISIBLE', '{"icon":"roles"}', '/system/roles', '_self', '后台角色管理'
    ),
    (
        4, 1, 66, 67, '菜单管理',
        'super',
        45, 'VISIBLE', '{"icon":"menus"}', '/system/menus', '_self', '后台菜单管理'
    ),
    (
        7, 1, 68, 69, '字典管理',
        'sys:dict:view,sys:dict:edit',
        40, 'VISIBLE', '{"icon":"dictionaries"}', '/system/dictionaries', '_self', '后台字典管理'
    ),
    (
        20, 1, 70, 73, '存储管理', 'storage', 35,
        'VISIBLE', '{"icon":"storage"}', '/storage', '_self', '对象存储管理'
    ),
    (
        21, 20, 71, 72, '对象管理',
        'storage:object:view,storage:object:edit',
        35, 'VISIBLE', '{"icon":"storage-objects"}', '/storage/objects', '_self', '对象存储文件管理'
    ),
    (
        8, 1, 74, 75, '系统日志',
        'super',
        30, 'VISIBLE', '{"icon":"logs"}', '/system/logs', '_self', '后台运行日志'
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

INSERT INTO `system_user_role` (`user_id`, `role_id`) VALUES
    (1000000000000000102, 1000000000000000402),
    (1000000000000000103, 1000000000000000403),
    (1000000000000000104, 1000000000000000405),
    (1000000000000000105, 1000000000000000402),
    (1000000000000000106, 1000000000000000403),
    (1000000000000000107, 1000000000000000404),
    (1000000000000000108, 1000000000000000405),
    (1000000000000000109, 1000000000000000404),
    (1000000000000000110, 1000000000000000404),
    (1000000000000000111, 1000000000000000406),
    (1000000000000000112, 1000000000000000405),
    (1000000000000000113, 1000000000000000406),
    (1000000000000000114, 1000000000000000406),
    (1000000000000000115, 1000000000000000405),
    (1000000000000000116, 1000000000000000402),
    (1000000000000000117, 1000000000000000403),
    (1000000000000000118, 1000000000000000405),
    (1000000000000000119, 1000000000000000404),
    (1000000000000000120, 1000000000000000403),
    (1000000000000000121, 1000000000000000404),
    (1000000000000000122, 1000000000000000405),
    (1000000000000000123, 1000000000000000404),
    (1000000000000000124, 1000000000000000405),
    (1000000000000000125, 1000000000000000406),
    (1000000000000000126, 1000000000000000405),
    (1000000000000000127, 1000000000000000406),
    (1000000000000000128, 1000000000000000406),
    (1000000000000000129, 1000000000000000405),
    (1000000000000000130, 1000000000000000402),
    (1000000000000000131, 1000000000000000403),
    (1000000000000000132, 1000000000000000406),
    (1000000000000000133, 1000000000000000406),
    (1000000000000000134, 1000000000000000403),
    (1000000000000000135, 1000000000000000406)
ON DUPLICATE KEY UPDATE
    `role_id` = VALUES(`role_id`);

INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`) VALUES
    (1, 5),
    (1, 100),
    (1, 101),
    (1, 102),
    (1, 103),
    (1, 104),
    (1, 105),
    (1, 110),
    (1, 111),
    (1, 112),
    (1, 113),
    (1, 114),
    (1, 120),
    (1, 121),
    (1, 122),
    (1, 123),
    (1, 124),
    (1, 130),
    (1, 131),
    (1, 132),
    (1, 133),
    (1, 140),
    (1, 141),
    (1, 142),
    (1, 143),
    (1, 144),
    (1, 145),
    (1, 50),
    (1, 51),
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 6),
    (1, 7),
    (1, 8),
    (1, 20),
    (1, 21);

INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`) VALUES
    (1000000000000000401, 5),
    (1000000000000000401, 100),
    (1000000000000000401, 101),
    (1000000000000000401, 102),
    (1000000000000000401, 103),
    (1000000000000000401, 104),
    (1000000000000000401, 105),
    (1000000000000000401, 110),
    (1000000000000000401, 111),
    (1000000000000000401, 112),
    (1000000000000000401, 113),
    (1000000000000000401, 114),
    (1000000000000000401, 120),
    (1000000000000000401, 121),
    (1000000000000000401, 122),
    (1000000000000000401, 123),
    (1000000000000000401, 124),
    (1000000000000000401, 130),
    (1000000000000000401, 131),
    (1000000000000000401, 132),
    (1000000000000000401, 133),
    (1000000000000000401, 140),
    (1000000000000000401, 141),
    (1000000000000000401, 142),
    (1000000000000000401, 143),
    (1000000000000000401, 144),
    (1000000000000000401, 145),
    (1000000000000000401, 50),
    (1000000000000000401, 51),
    (1000000000000000401, 1),
    (1000000000000000401, 2),
    (1000000000000000401, 6),
    (1000000000000000401, 3),
    (1000000000000000401, 4),
    (1000000000000000401, 7),
    (1000000000000000401, 8),
    (1000000000000000401, 20),
    (1000000000000000401, 21),
    (1000000000000000402, 5),
    (1000000000000000402, 1),
    (1000000000000000402, 2),
    (1000000000000000402, 6),
    (1000000000000000402, 3),
    (1000000000000000402, 4),
    (1000000000000000402, 7),
    (1000000000000000402, 20),
    (1000000000000000402, 21),
    (1000000000000000402, 8),
    (1000000000000000402, 50),
    (1000000000000000402, 51),
    (1000000000000000403, 5),
    (1000000000000000403, 1),
    (1000000000000000403, 2),
    (1000000000000000403, 6),
    (1000000000000000404, 5),
    (1000000000000000404, 110),
    (1000000000000000404, 111),
    (1000000000000000404, 112),
    (1000000000000000404, 113),
    (1000000000000000404, 114),
    (1000000000000000404, 1),
    (1000000000000000404, 7),
    (1000000000000000404, 20),
    (1000000000000000404, 21),
    (1000000000000000405, 5),
    (1000000000000000405, 100),
    (1000000000000000405, 101),
    (1000000000000000405, 102),
    (1000000000000000405, 103),
    (1000000000000000405, 104),
    (1000000000000000405, 105),
    (1000000000000000405, 120),
    (1000000000000000405, 121),
    (1000000000000000405, 122),
    (1000000000000000405, 123),
    (1000000000000000405, 124),
    (1000000000000000405, 130),
    (1000000000000000405, 131),
    (1000000000000000405, 132),
    (1000000000000000405, 133),
    (1000000000000000405, 140),
    (1000000000000000405, 141),
    (1000000000000000405, 142),
    (1000000000000000405, 143),
    (1000000000000000405, 50),
    (1000000000000000405, 51),
    (1000000000000000405, 1),
    (1000000000000000405, 2),
    (1000000000000000405, 6),
    (1000000000000000405, 3),
    (1000000000000000405, 7),
    (1000000000000000405, 20),
    (1000000000000000405, 21),
    (1000000000000000406, 1),
    (1000000000000000406, 5),
    (1000000000000000406, 100),
    (1000000000000000406, 101),
    (1000000000000000406, 102),
    (1000000000000000406, 103),
    (1000000000000000406, 104),
    (1000000000000000406, 105),
    (1000000000000000406, 130),
    (1000000000000000406, 131),
    (1000000000000000406, 132),
    (1000000000000000406, 140),
    (1000000000000000406, 141),
    (1000000000000000406, 142),
    (1000000000000000406, 143);

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

INSERT INTO `system_auth_principal_identity` (
    `id`, `principal_type`, `principal_id`, `identity_type`, `identity_value`, `status`
) VALUES
    (1000000000000010103, 'USER', 1000000000000000102, 'USER_ACCOUNT', 'lin.zhiyuan', 'ENABLED'),
    (1000000000000010104, 'USER', 1000000000000000103, 'USER_ACCOUNT', 'zhou.chengce', 'ENABLED'),
    (1000000000000010105, 'USER', 1000000000000000104, 'USER_ACCOUNT', 'gu.qinghe', 'ENABLED'),
    (1000000000000010106, 'USER', 1000000000000000105, 'USER_ACCOUNT', 'xu.mubai', 'ENABLED'),
    (1000000000000010107, 'USER', 1000000000000000106, 'USER_ACCOUNT', 'chen.bozhou', 'ENABLED'),
    (1000000000000010108, 'USER', 1000000000000000107, 'USER_ACCOUNT', 'ye.wanqing', 'ENABLED'),
    (1000000000000010109, 'USER', 1000000000000000108, 'USER_ACCOUNT', 'tang.yining', 'ENABLED'),
    (1000000000000010110, 'USER', 1000000000000000109, 'USER_ACCOUNT', 'shen.jiamu', 'DISABLED'),
    (1000000000000010111, 'USER', 1000000000000000110, 'USER_ACCOUNT', 'lu.jingming', 'ENABLED'),
    (1000000000000010112, 'USER', 1000000000000000111, 'USER_ACCOUNT', 'jiang.shuyao', 'ENABLED'),
    (1000000000000010113, 'USER', 1000000000000000112, 'USER_ACCOUNT', 'song.yian', 'ENABLED'),
    (1000000000000010114, 'USER', 1000000000000000113, 'USER_ACCOUNT', 'han.xingye', 'ENABLED'),
    (1000000000000010115, 'USER', 1000000000000000114, 'USER_ACCOUNT', 'qin.ruochuan', 'ENABLED'),
    (1000000000000010116, 'USER', 1000000000000000115, 'USER_ACCOUNT', 'cheng.yumo', 'ENABLED'),
    (1000000000000010117, 'USER', 1000000000000000116, 'USER_ACCOUNT', 'zhao.beichen', 'ENABLED'),
    (1000000000000010118, 'USER', 1000000000000000117, 'USER_ACCOUNT', 'he.wangshu', 'ENABLED'),
    (1000000000000010119, 'USER', 1000000000000000118, 'USER_ACCOUNT', 'qiu.mingche', 'ENABLED'),
    (1000000000000010120, 'USER', 1000000000000000119, 'USER_ACCOUNT', 'liang.siheng', 'ENABLED'),
    (1000000000000010121, 'USER', 1000000000000000120, 'USER_ACCOUNT', 'luo.zijin', 'ENABLED'),
    (1000000000000010122, 'USER', 1000000000000000121, 'USER_ACCOUNT', 'su.yunzhou', 'ENABLED'),
    (1000000000000010123, 'USER', 1000000000000000122, 'USER_ACCOUNT', 'meng.qinglan', 'ENABLED'),
    (1000000000000010124, 'USER', 1000000000000000123, 'USER_ACCOUNT', 'bai.jingxing', 'ENABLED'),
    (1000000000000010125, 'USER', 1000000000000000124, 'USER_ACCOUNT', 'xia.ange', 'DISABLED'),
    (1000000000000010126, 'USER', 1000000000000000125, 'USER_ACCOUNT', 'ji.xinghe', 'ENABLED'),
    (1000000000000010127, 'USER', 1000000000000000126, 'USER_ACCOUNT', 'yu.qingyue', 'ENABLED'),
    (1000000000000010128, 'USER', 1000000000000000127, 'USER_ACCOUNT', 'feng.ruogu', 'ENABLED'),
    (1000000000000010129, 'USER', 1000000000000000128, 'USER_ACCOUNT', 'wei.nanfeng', 'ENABLED'),
    (1000000000000010130, 'USER', 1000000000000000129, 'USER_ACCOUNT', 'ding.xiaotang', 'ENABLED'),
    (1000000000000010131, 'USER', 1000000000000000130, 'USER_ACCOUNT', 'ma.yanqiu', 'ENABLED'),
    (1000000000000010132, 'USER', 1000000000000000131, 'USER_ACCOUNT', 'xie.changfeng', 'ENABLED'),
    (1000000000000010133, 'USER', 1000000000000000132, 'USER_ACCOUNT', 'jiang.nanxing', 'ENABLED'),
    (1000000000000010134, 'USER', 1000000000000000133, 'USER_ACCOUNT', 'yuan.chuxia', 'DISABLED'),
    (1000000000000010135, 'USER', 1000000000000000134, 'USER_ACCOUNT', 'cao.yuanshan', 'ENABLED'),
    (1000000000000010136, 'USER', 1000000000000000135, 'USER_ACCOUNT', 'ren.lanxi', 'ENABLED')
ON DUPLICATE KEY UPDATE
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

INSERT INTO `system_auth_principal_credential` (
    `id`, `principal_type`, `principal_id`, `identity_id`,
    `credential_type`, `credential_value`, `status`, `need_change_password`,
    `failed_count`, `failed_limit`
) VALUES
    (1000000000000010203, 'USER', 1000000000000000102, 1000000000000010103, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010204, 'USER', 1000000000000000103, 1000000000000010104, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010205, 'USER', 1000000000000000104, 1000000000000010105, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010206, 'USER', 1000000000000000105, 1000000000000010106, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010207, 'USER', 1000000000000000106, 1000000000000010107, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010208, 'USER', 1000000000000000107, 1000000000000010108, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010209, 'USER', 1000000000000000108, 1000000000000010109, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010210, 'USER', 1000000000000000109, 1000000000000010110, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'LOCKED', 1, 0, 5),
    (1000000000000010211, 'USER', 1000000000000000110, 1000000000000010111, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010212, 'USER', 1000000000000000111, 1000000000000010112, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010213, 'USER', 1000000000000000112, 1000000000000010113, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010214, 'USER', 1000000000000000113, 1000000000000010114, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010215, 'USER', 1000000000000000114, 1000000000000010115, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010216, 'USER', 1000000000000000115, 1000000000000010116, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010217, 'USER', 1000000000000000116, 1000000000000010117, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010218, 'USER', 1000000000000000117, 1000000000000010118, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010219, 'USER', 1000000000000000118, 1000000000000010119, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010220, 'USER', 1000000000000000119, 1000000000000010120, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010221, 'USER', 1000000000000000120, 1000000000000010121, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010222, 'USER', 1000000000000000121, 1000000000000010122, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010223, 'USER', 1000000000000000122, 1000000000000010123, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010224, 'USER', 1000000000000000123, 1000000000000010124, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010225, 'USER', 1000000000000000124, 1000000000000010125, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'LOCKED', 1, 0, 5),
    (1000000000000010226, 'USER', 1000000000000000125, 1000000000000010126, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010227, 'USER', 1000000000000000126, 1000000000000010127, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010228, 'USER', 1000000000000000127, 1000000000000010128, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010229, 'USER', 1000000000000000128, 1000000000000010129, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010230, 'USER', 1000000000000000129, 1000000000000010130, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010231, 'USER', 1000000000000000130, 1000000000000010131, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010232, 'USER', 1000000000000000131, 1000000000000010132, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010233, 'USER', 1000000000000000132, 1000000000000010133, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010234, 'USER', 1000000000000000133, 1000000000000010134, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'LOCKED', 1, 0, 5),
    (1000000000000010235, 'USER', 1000000000000000134, 1000000000000010135, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5),
    (1000000000000010236, 'USER', 1000000000000000135, 1000000000000010136, 'USER_PASSWORD', '{noop}Q1w2e3r$', 'ACTIVE', 1, 0, 5)
ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_id` = VALUES(`identity_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`);

SET NAMES utf8mb4;

-- Audit has no required seed data.
