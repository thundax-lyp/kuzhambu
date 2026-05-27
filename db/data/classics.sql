SET NAMES utf8mb4;

INSERT INTO `classics_sancai_category` (
    `category_code`, `name`, `formal`, `sort_order`, `description`
) VALUES
    ('js', '卷首', 0, 0, '序、凡例等卷首辅助内容'),
    ('tw', '天文', 1, 10, '三才图会正式门类'),
    ('dl', '地理', 1, 20, '三才图会正式门类'),
    ('rw', '人物', 1, 30, '三才图会正式门类'),
    ('sl', '时令', 1, 40, '三才图会正式门类'),
    ('gs', '宫室', 1, 50, '三才图会正式门类'),
    ('qy', '器用', 1, 60, '三才图会正式门类'),
    ('st', '身体', 1, 70, '三才图会正式门类'),
    ('yf', '衣服', 1, 80, '三才图会正式门类'),
    ('rs', '人事', 1, 90, '三才图会正式门类'),
    ('yz', '仪制', 1, 100, '三才图会正式门类'),
    ('zb', '珍宝', 1, 110, '三才图会正式门类'),
    ('ws', '文史', 1, 120, '三才图会正式门类'),
    ('ns', '鸟兽', 1, 130, '三才图会正式门类'),
    ('cm', '草木', 1, 140, '三才图会正式门类')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `formal` = VALUES(`formal`),
    `sort_order` = VALUES(`sort_order`),
    `description` = VALUES(`description`);

INSERT INTO `classics_sancai_volume` (
    `volume_id`, `category_code`, `volume_no`, `title`, `auxiliary`, `sort_order`, `entry_count`
) VALUES (
    '01KUZHAMBU00000000010001',
    'js',
    NULL,
    '卷首',
    1,
    0,
    0
) ON DUPLICATE KEY UPDATE
    `category_code` = VALUES(`category_code`),
    `volume_no` = VALUES(`volume_no`),
    `title` = VALUES(`title`),
    `auxiliary` = VALUES(`auxiliary`),
    `sort_order` = VALUES(`sort_order`),
    `entry_count` = VALUES(`entry_count`);
SET NAMES utf8mb4;

-- Wangqi has no required seed data.
SET NAMES utf8mb4;

-- Ming Customs has no required seed data.
SET NAMES utf8mb4;
