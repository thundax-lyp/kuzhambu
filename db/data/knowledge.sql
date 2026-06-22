SET NAMES utf8mb4;

INSERT INTO `knowledge_tag_category` (
    `category_id`, `name`, `description`, `priority`, `status`
) VALUES
    (1001, '人物', '人物类别', 10, 'ENABLED'),
    (1002, '地点', '地理地点', 20, 'ENABLED'),
    (1003, '时代', '历史时代', 30, 'ENABLED'),
    (1004, '主题', '主题分类', 40, 'ENABLED')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `priority` = VALUES(`priority`),
    `status` = VALUES(`status`);
