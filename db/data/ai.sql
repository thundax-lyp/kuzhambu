SET NAMES utf8mb4;

INSERT INTO `ai_capability` (
    `capability`, `name`, `required_tags`, `enabled`, `sort_order`
) VALUES
    ('translate', '古文翻译', '["text"]', 1, 10),
    ('tags', '标签提取', '["text"]', 1, 20),
    ('visual', '视觉描述', '["text"]', 1, 30),
    ('fusion', '信息融合', '["text"]', 1, 40),
    ('qa', '问答生成', '["text"]', 1, 50),
    ('split', '条目拆分', '["text"]', 1, 60),
    ('image_analysis', '图片理解', '["vision"]', 1, 70),
    ('image_gen', '图片生成', '["image_gen"]', 1, 80),
    ('knowledge_graph', '知识图谱抽取', '["text"]', 1, 90),
    ('summary', '摘要生成', '["text"]', 1, 100),
    ('version_summary', '版本摘要', '["text"]', 1, 110)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `required_tags` = VALUES(`required_tags`),
    `enabled` = VALUES(`enabled`),
    `sort_order` = VALUES(`sort_order`);
SET NAMES utf8mb4;

-- AI Refinement has no required seed data.
