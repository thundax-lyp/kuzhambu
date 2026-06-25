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

-- Test seed derived from ../down.s/KB_HTML/db/sancai_kb.db.
-- Keep this section AI-owned: service/model wiring, capability mapping and prompt templates only.

INSERT INTO `ai_service_config` (
    `service_id`, `service_role`, `api_source`, `base_url`, `encrypted_api_key`,
    `enabled`, `status`, `last_checked_at`, `configured_at`
) VALUES
    (
        900001, 'PRIMARY', 'ctyun', 'https://worker-ai.local/mock', NULL,
        1, 'AVAILABLE', '2026-02-27 04:00:00.000', '2026-02-27 04:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `api_source` = VALUES(`api_source`),
    `base_url` = VALUES(`base_url`),
    `encrypted_api_key` = VALUES(`encrypted_api_key`),
    `enabled` = VALUES(`enabled`),
    `status` = VALUES(`status`),
    `last_checked_at` = VALUES(`last_checked_at`),
    `configured_at` = VALUES(`configured_at`);

INSERT INTO `ai_model` (
    `model_id`, `service_id`, `model_name`, `display_name`, `capability_tags_json`,
    `default_params_json`, `description`, `enabled`, `registered_at`
) VALUES
    (
        900101, 900001, 'CTYUN-CX-Qwen3.5-397B-A17B', 'CTYUN Qwen3.5 397B',
        '["text", "vision", "structured_output", "streaming_text"]',
        '{"temperature": 0.2, "max_tokens": 4096}',
        'KB_HTML image_analysis sample model for classics AI tests.', 1, '2026-02-27 04:00:00.000'
    ),
    (
        900102, 900001, 'CTYUN-CX-DeepSeek-V3.1', 'CTYUN DeepSeek V3.1',
        '["text", "structured_output", "streaming_text"]',
        '{"temperature": 0.2, "max_tokens": 4096}',
        'KB_HTML image_analysis sample model for text and structured AI tests.', 1, '2026-02-27 04:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `service_id` = VALUES(`service_id`),
    `display_name` = VALUES(`display_name`),
    `capability_tags_json` = VALUES(`capability_tags_json`),
    `default_params_json` = VALUES(`default_params_json`),
    `description` = VALUES(`description`),
    `enabled` = VALUES(`enabled`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_capability_mapping` (
    `mapping_id`, `scope`, `capability`, `model_id`, `enabled`, `configured_at`
) VALUES
    (910101, 'classics', 'summary', 900102, 1, '2026-02-27 04:00:00.000'),
    (910102, 'classics', 'tags', 900102, 1, '2026-02-27 04:00:00.000'),
    (910103, 'classics', 'qa', 900102, 1, '2026-02-27 04:00:00.000'),
    (910104, 'classics', 'image_analysis', 900101, 1, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `model_id` = VALUES(`model_id`),
    `enabled` = VALUES(`enabled`),
    `configured_at` = VALUES(`configured_at`);

INSERT INTO `ai_action_status` (
    `action_status_id`, `scope`, `capability`, `available`, `unavailable_reason`, `checked_at`
) VALUES
    (920101, 'classics', 'summary', 1, NULL, '2026-02-27 04:00:00.000'),
    (920102, 'classics', 'tags', 1, NULL, '2026-02-27 04:00:00.000'),
    (920103, 'classics', 'qa', 1, NULL, '2026-02-27 04:00:00.000'),
    (920104, 'classics', 'image_analysis', 1, NULL, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `available` = VALUES(`available`),
    `unavailable_reason` = VALUES(`unavailable_reason`),
    `checked_at` = VALUES(`checked_at`);

INSERT INTO `ai_prompt_template` (
    `template_id`, `scope`, `capability`, `name`, `description`, `status`,
    `current_version_no`, `registered_at`
) VALUES
    (
        930101, 'classics', 'summary', '王圻文档摘要', '从 KB_HTML prompts 表导入的摘要测试提示词。',
        'ACTIVE', 1, '2026-02-27 04:00:00.000'
    ),
    (
        930102, 'classics', 'tags', '王圻文档标签', '从 KB_HTML prompts 表导入的标签测试提示词。',
        'ACTIVE', 1, '2026-02-27 04:00:00.000'
    ),
    (
        930103, 'classics', 'qa', '王圻文档问答', '从 KB_HTML prompts 表导入的问答测试提示词。',
        'ACTIVE', 1, '2026-02-27 04:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `status` = VALUES(`status`),
    `current_version_no` = VALUES(`current_version_no`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_prompt_version` (
    `prompt_version_id`, `template_id`, `version_no`, `message_templates_json`,
    `variables_snapshot_json`, `output_schema_json`, `current_key`, `change_summary`, `registered_at`
) VALUES
    (
        940101, 930101, 1,
        '[{"role":"system","content":"你是熟悉王圻与古籍整理的中文文献助手。"},{"role":"user","content":"请为以下关于王圻的文档生成一个简洁的摘要，包含文档的核心内容、主要观点和重要信息。摘要应准确反映文档内容，长度适中，便于快速了解文档主题。文档内容：{{document}}"}]',
        '[{"name":"document","required":true,"description":"待摘要的文档内容"}]',
        '{"type":"object","properties":{"summary":{"type":"string"}},"required":["summary"]}',
        '930101:current', 'Imported from KB_HTML prompts.summary.', '2026-02-27 04:00:00.000'
    ),
    (
        940102, 930102, 1,
        '[{"role":"system","content":"你是熟悉王圻与古籍整理的中文标签助手。"},{"role":"user","content":"请从以下关于王圻的文档中提取3-5个最能代表文档主题的标签。标签应简洁明了，能够准确反映文档内容的核心主题和关键词。文档内容：{{document}}"}]',
        '[{"name":"document","required":true,"description":"待提取标签的文档内容"}]',
        '{"type":"object","properties":{"tags":{"type":"array","items":{"type":"string"},"minItems":3,"maxItems":5}},"required":["tags"]}',
        '930102:current', 'Imported from KB_HTML prompts.tags.', '2026-02-27 04:00:00.000'
    ),
    (
        940103, 930103, 1,
        '[{"role":"system","content":"你是熟悉王圻与古籍整理的中文问答助手。"},{"role":"user","content":"请根据以下关于王圻的文档内容，生成3-5个有价值的问答对。问题应具有针对性，答案应准确反映文档内容。文档内容：{{document}}"}]',
        '[{"name":"document","required":true,"description":"待生成问答的文档内容"}]',
        '{"type":"object","properties":{"qa_pairs":{"type":"array","items":{"type":"object","properties":{"question":{"type":"string"},"answer":{"type":"string"}},"required":["question","answer"]},"minItems":3,"maxItems":5}},"required":["qa_pairs"]}',
        '930103:current', 'Imported from KB_HTML prompts.qa.', '2026-02-27 04:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `message_templates_json` = VALUES(`message_templates_json`),
    `variables_snapshot_json` = VALUES(`variables_snapshot_json`),
    `output_schema_json` = VALUES(`output_schema_json`),
    `current_key` = VALUES(`current_key`),
    `change_summary` = VALUES(`change_summary`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_prompt_variable` (
    `variable_id`, `template_id`, `variable_name`, `required`, `description`, `priority`
) VALUES
    (950101, 930101, 'document', 1, '待摘要的文档内容', 1000),
    (950102, 930102, 'document', 1, '待提取标签的文档内容', 1010),
    (950103, 930103, 'document', 1, '待生成问答的文档内容', 1020)
ON DUPLICATE KEY UPDATE
    `required` = VALUES(`required`),
    `description` = VALUES(`description`),
    `priority` = VALUES(`priority`);

INSERT INTO `ai_capability_mapping` (
    `mapping_id`, `scope`, `capability`, `model_id`, `enabled`, `configured_at`
) VALUES
    (910105, 'discovery', 'query_understanding', 900102, 1, '2026-02-27 04:00:00.000'),
    (910106, 'discovery', 'answer_generation', 900102, 1, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `model_id` = VALUES(`model_id`),
    `enabled` = VALUES(`enabled`),
    `configured_at` = VALUES(`configured_at`);

INSERT INTO `ai_action_status` (
    `action_status_id`, `scope`, `capability`, `available`, `unavailable_reason`, `checked_at`
) VALUES
    (920105, 'discovery', 'query_understanding', 1, NULL, '2026-02-27 04:00:00.000'),
    (920106, 'discovery', 'answer_generation', 1, NULL, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `available` = VALUES(`available`),
    `unavailable_reason` = VALUES(`unavailable_reason`),
    `checked_at` = VALUES(`checked_at`);

INSERT INTO `ai_prompt_template` (
    `template_id`, `scope`, `capability`, `name`, `description`, `status`,
    `current_version_no`, `registered_at`
) VALUES
    (
        930104,
        'discovery',
        'query_understanding',
        'Discovery 查询理解',
        '用于将用户检索词规范化并扩展同义词。',
        'ACTIVE',
        1,
        '2026-02-27 04:00:00.000'
    ),
    (
        930105,
        'discovery',
        'answer_generation',
        'Discovery 回答生成',
        '用于将检索到的来源与问句融合为最终回答。',
        'ACTIVE',
        1,
        '2026-02-27 04:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `status` = VALUES(`status`),
    `current_version_no` = VALUES(`current_version_no`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_prompt_version` (
    `prompt_version_id`, `template_id`, `version_no`, `message_templates_json`,
    `variables_snapshot_json`, `output_schema_json`, `current_key`, `change_summary`, `registered_at`
) VALUES
    (
        940104,
        930104,
        1,
        '[{"role":"system","content":"你是熟悉古籍检索的中文查询理解助手。"},{"role":"user","content":"请将用户查询规范化、扩展同义词，并识别可能的实体。查询：{{query}}"}]',
        '[{"name":"query","required":true,"description":"用户原始查询"}]',
        '{"type":"object","properties":{"normalizedQueryText":{"type":"string"},"rewrittenQueryText":{"type":"string"},"expandedSynonyms":{"type":"array","items":{"type":"string"}},"recognizedEntities":{"type":"array","items":{"type":"string"}},"intentType":{"type":"string"}},"required":["normalizedQueryText","rewrittenQueryText","expandedSynonyms","recognizedEntities","intentType"]}',
        '930104:current',
        'Imported for discovery query understanding seed.',
        '2026-02-27 04:00:00.000'
    ),
    (
        940105,
        930105,
        1,
        '[{"role":"system","content":"你是熟悉古籍来源引用的中文问答助手。"},{"role":"user","content":"请根据下列来源与问题生成简洁、带来源意识的回答。问题：{{question}}；来源：{{sources}}"}]',
        '[{"name":"question","required":true,"description":"用户问句"},{"name":"sources","required":true,"description":"来源列表"}]',
        '{"type":"object","properties":{"answer":{"type":"string"},"answerStatus":{"type":"string"},"sourceSummaries":{"type":"array","items":{"type":"string"}}},"required":["answer","answerStatus","sourceSummaries"]}',
        '930105:current',
        'Imported for discovery answer generation seed.',
        '2026-02-27 04:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `message_templates_json` = VALUES(`message_templates_json`),
    `variables_snapshot_json` = VALUES(`variables_snapshot_json`),
    `output_schema_json` = VALUES(`output_schema_json`),
    `current_key` = VALUES(`current_key`),
    `change_summary` = VALUES(`change_summary`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_prompt_variable` (
    `variable_id`, `template_id`, `variable_name`, `required`, `description`, `priority`
) VALUES
    (950104, 930104, 'query', 1, '用户原始查询', 1030),
    (950105, 930105, 'question', 1, '用户问句', 1040),
    (950106, 930105, 'sources', 1, '来源列表', 1050)
ON DUPLICATE KEY UPDATE
    `required` = VALUES(`required`),
    `description` = VALUES(`description`),
    `priority` = VALUES(`priority`);
