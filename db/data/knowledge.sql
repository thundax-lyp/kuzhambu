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

INSERT INTO `knowledge_tag` (
    `tag_id`, `name`, `category_id`, `description`, `status`, `source`,
    `review_status`, `review_note`, `created_at`, `reviewed_at`, `merged_to_tag_id`,
    `deprecated_at`, `deprecated_by`
) VALUES (
    500001,
    '世系图',
    1004,
    '用于知识问答和跨库检索的世系图主题标签',
    'ENABLED',
    'MANUAL',
    'APPROVED',
    '联通 Discovery 查询理解与来源引用',
    '2026-02-27 04:00:00.000',
    '2026-02-27 04:00:00.000',
    NULL,
    NULL,
    NULL
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `category_id` = VALUES(`category_id`),
    `description` = VALUES(`description`),
    `status` = VALUES(`status`),
    `source` = VALUES(`source`),
    `review_status` = VALUES(`review_status`),
    `review_note` = VALUES(`review_note`),
    `created_at` = VALUES(`created_at`),
    `reviewed_at` = VALUES(`reviewed_at`),
    `merged_to_tag_id` = VALUES(`merged_to_tag_id`),
    `deprecated_at` = VALUES(`deprecated_at`),
    `deprecated_by` = VALUES(`deprecated_by`);

INSERT INTO `knowledge_tag_alias` (
    `alias_id`, `tag_id`, `name`, `source`
) VALUES (
    510001,
    500001,
    '世系图谱',
    'MANUAL'
) ON DUPLICATE KEY UPDATE
    `tag_id` = VALUES(`tag_id`),
    `name` = VALUES(`name`),
    `source` = VALUES(`source`);

INSERT INTO `knowledge_synonym` (
    `synonym_id`, `term`, `synonym`, `status`
) VALUES (
    520001,
    '世系',
    '世系图',
    'ENABLED'
) ON DUPLICATE KEY UPDATE
    `term` = VALUES(`term`),
    `synonym` = VALUES(`synonym`),
    `status` = VALUES(`status`);

INSERT INTO `knowledge_tag_content_ref` (
    `ref_id`, `tag_id`, `content_type`, `content_id`, `content_title`, `source`
) VALUES
    (
        530001,
        500001,
        'SANCAI_ENTRY',
        300000000604,
        '上古帝王及世系图',
        'MANUAL'
    ),
    (
        530002,
        500001,
        'SANCAI_ENTRY',
        300000000609,
        '天文历法',
        'MANUAL'
    )
ON DUPLICATE KEY UPDATE
    `tag_id` = VALUES(`tag_id`),
    `content_type` = VALUES(`content_type`),
    `content_id` = VALUES(`content_id`),
    `content_title` = VALUES(`content_title`),
    `source` = VALUES(`source`);
