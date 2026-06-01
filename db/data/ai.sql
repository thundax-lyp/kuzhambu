SET NAMES utf8mb4;

INSERT INTO `ai_capability` (
    `capability`, `name`, `required_tags_json`, `output_mode`, `enabled`, `priority`
) VALUES
    ('translate', '古文翻译', '["text"]', 'TEXT', 1, 10),
    ('tags', '标签提取', '["text", "structured_output"]', 'STRUCTURED', 1, 20),
    ('visual', '视觉描述', '["text"]', 'TEXT', 1, 30),
    ('fusion', '信息融合', '["text"]', 'TEXT', 1, 40),
    ('qa', '问答生成', '["text", "structured_output"]', 'STRUCTURED', 1, 50),
    ('split', '条目拆分', '["text", "structured_output"]', 'STRUCTURED', 1, 60),
    ('image_analysis', '图片理解', '["vision"]', 'MARKDOWN', 1, 70),
    ('image_gen', '图片生成', '["image_gen"]', 'ARTIFACT', 1, 80),
    ('knowledge_graph', '知识图谱抽取', '["text", "structured_output"]', 'STRUCTURED', 1, 90),
    ('summary', '摘要生成', '["text"]', 'TEXT', 1, 100),
    ('version_summary', '版本摘要', '["text"]', 'TEXT', 1, 110),
    ('query_understanding', '查询理解', '["text", "structured_output"]', 'STRUCTURED', 1, 120),
    ('answer_generation', '回答生成', '["text", "streaming_text"]', 'TEXT', 1, 130),
    ('relation_extraction', '实体关系抽取', '["text", "structured_output"]', 'STRUCTURED', 1, 140),
    ('lineage_extraction', '世系图抽取', '["text", "structured_output"]', 'STRUCTURED', 1, 150),
    ('prompt_suggestion', '提示词优化建议', '["text"]', 'TEXT', 1, 160)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `required_tags_json` = VALUES(`required_tags_json`),
    `output_mode` = VALUES(`output_mode`),
    `enabled` = VALUES(`enabled`),
    `priority` = VALUES(`priority`);
