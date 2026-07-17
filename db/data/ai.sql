SET NAMES utf8mb4;

INSERT INTO `ai_model` (
    `id`, `api_source`, `base_url`, `encrypted_api_key`, `model_name`, `display_name`, `capabilities_json`,
    `default_params_json`, `description`, `enabled`, `registered_at`
) VALUES
    (900101, 'OPENAI', '', NULL, 'CTYUN-CX-Qwen3.5-397B-A17B', 'CTYUN Qwen3.5 397B', '["TEXT2TEXT","IMAGE2TEXT"]', '{"temperature":0.2,"max_tokens":4096}', 'Default OpenAI-compatible vision-capable model for classics AI.', 1, '2026-02-27 04:00:00.000'),
    (900102, 'OPENAI', '', NULL, 'CTYUN-bot-DeepSeek-V3.2-pro', 'CTYUN DeepSeek V3.2 Pro', '["TEXT2TEXT"]', '{"temperature":0.2,"max_tokens":4096}', 'Default OpenAI-compatible LLM from local server configuration.', 1, '2026-02-27 04:00:00.000'),
    (900201, 'BYTEDANCE', '', NULL, 'doubao-seedream-5-0-pro-260628', 'Doubao Seedream 5.0 Pro', '["TEXT2IMAGE"]', '{"response_format":"url","size":"2K","stream":false,"watermark":true}', 'Default ByteDance text-to-image model.', 1, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `api_source` = VALUES(`api_source`),
    `base_url` = VALUES(`base_url`),
    `encrypted_api_key` = VALUES(`encrypted_api_key`),
    `display_name` = VALUES(`display_name`),
    `capabilities_json` = VALUES(`capabilities_json`),
    `default_params_json` = VALUES(`default_params_json`),
    `description` = VALUES(`description`),
    `enabled` = VALUES(`enabled`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_capability_mapping` (
    `mapping_id`, `scope`, `capability`, `model_id`, `enabled`, `configured_at`
) VALUES
    (910101, 'classics', 'classics_summary', 900102, 1, '2026-02-27 04:00:00.000'),
    (910102, 'classics', 'classics_tags', 900102, 1, '2026-02-27 04:00:00.000'),
    (910103, 'classics', 'classics_qa', 900102, 1, '2026-02-27 04:00:00.000'),
    (910104, 'classics', 'classics_image_describe', 900101, 1, '2026-02-27 04:00:00.000'),
    (910105, 'classics', 'classics_translate', 900102, 1, '2026-02-27 04:00:00.000'),
    (910106, 'classics', 'classics_image_generate', 900201, 1, '2026-02-27 04:00:00.000'),
    (910107, 'classics', 'classics_image_prompt_fusion', 900102, 1, '2026-02-27 04:00:00.000'),
    (910108, 'classics', 'classics_visual_describe', 900102, 1, '2026-02-27 04:00:00.000'),
    (910201, 'discovery', 'discovery_query_understanding', 900102, 1, '2026-02-27 04:00:00.000'),
    (910202, 'discovery', 'discovery_answer_generation', 900102, 1, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `model_id` = VALUES(`model_id`),
    `enabled` = VALUES(`enabled`),
    `configured_at` = VALUES(`configured_at`);

INSERT INTO `ai_action_status` (
    `action_status_id`, `scope`, `capability`, `available`, `unavailable_reason`, `checked_at`
) VALUES
    (920101, 'classics', 'classics_summary', 1, NULL, '2026-02-27 04:00:00.000'),
    (920102, 'classics', 'classics_tags', 1, NULL, '2026-02-27 04:00:00.000'),
    (920103, 'classics', 'classics_qa', 1, NULL, '2026-02-27 04:00:00.000'),
    (920104, 'classics', 'classics_image_describe', 1, NULL, '2026-02-27 04:00:00.000'),
    (920105, 'classics', 'classics_translate', 1, NULL, '2026-02-27 04:00:00.000'),
    (920106, 'classics', 'classics_image_generate', 1, NULL, '2026-02-27 04:00:00.000'),
    (920107, 'classics', 'classics_image_prompt_fusion', 1, NULL, '2026-02-27 04:00:00.000'),
    (920108, 'classics', 'classics_visual_describe', 1, NULL, '2026-02-27 04:00:00.000'),
    (920201, 'discovery', 'discovery_query_understanding', 1, NULL, '2026-02-27 04:00:00.000'),
    (920202, 'discovery', 'discovery_answer_generation', 1, NULL, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `available` = VALUES(`available`),
    `unavailable_reason` = VALUES(`unavailable_reason`),
    `checked_at` = VALUES(`checked_at`);

INSERT INTO `ai_prompt_template` (
    `id`, `capability`, `name`, `description`, `status`,
    `current_version_no`, `registered_at`
) VALUES
    (930101, 'classics_summary', 'Classics Default Summary', '古籍内容默认摘要提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930102, 'classics_tags', 'Classics Default Tags', '古籍内容默认标签抽取提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930103, 'classics_qa', 'Classics Default QA', '古籍内容默认问答生成提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930104, 'discovery_query_understanding', 'Discovery Default Query Understanding', '知识发现默认查询理解提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930105, 'discovery_answer_generation', 'Discovery Default Answer Generation', '知识发现默认来源融合回答提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930106, 'classics_translate', 'Classics Default Translate', '古籍文言文默认现代中文翻译提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930107, 'classics_image_describe', 'Classics Default Image Analysis', '古籍图像默认解读提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930108, 'classics_image_generate', 'Classics Default Image Generation', '古籍视觉资产默认文生图提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930109, 'classics_image_prompt_fusion', 'Classics Default Visual Fusion', '古籍图文信息融合默认提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000'),
    (930110, 'classics_visual_describe', 'Classics Default Visual Description', '古籍视觉描述默认提示词。', 'ACTIVE', 1, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `capability` = VALUES(`capability`),
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `status` = VALUES(`status`),
    `current_version_no` = VALUES(`current_version_no`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_prompt_version` (
    `id`, `template_id`, `version_no`, `message_templates_json`,
    `variables_snapshot_json`, `output_schema_json`, `change_summary`, `registered_at`
) VALUES
    (940101, 930101, 1, '[{"role":"system","content":"你是古籍整理助手，负责为古籍条目生成可直接展示的中文摘要。\\n\\n规则：\\n1. 只依据输入内容总结，不得编造未出现的人名、地名、年代、关系或结论。\\n2. 摘要应简洁、准确、可读，优先保留条目的核心对象、事件、属性和用途。\\n3. 如果输入信息不足，只概括已知信息，不补充背景常识。\\n4. 不输出“摘要：”“以下是”等解释性前缀。\\n5. 输出纯文本，不使用 Markdown 列表。"},{"role":"user","content":"内容类型：{{contentType}}\\n标题：{{title}}\\n分类路径：{{categoryPath}}\\n\\n原文：\\n{{originalText}}\\n\\n已有译文：\\n{{translationText}}\\n\\n正文：\\n{{bodyText}}\\n\\n已有摘要：\\n{{existingSummary}}\\n\\n请生成一段中文摘要。若已有摘要质量较好，可以在保留事实的基础上压缩和润色；若内容为空或不足，请直接说明“暂无足够内容可摘要”。"}]', '[{"name":"contentType","required":true,"description":"内容类型"},{"name":"title","required":false,"description":"标题"},{"name":"categoryPath","required":false,"description":"门类或卷册路径"},{"name":"originalText","required":false,"description":"原文"},{"name":"translationText","required":false,"description":"已有译文"},{"name":"bodyText","required":false,"description":"正文或补充内容"},{"name":"existingSummary","required":false,"description":"已有摘要"}]', '{"type":"text"}', 'Initial classics default summary prompt.', '2026-02-27 04:00:00.000'),
    (940102, 930102, 1, '[{"role":"system","content":"你是古籍知识组织助手，负责从古籍条目中抽取稳定、可复用的中文标签。\\n\\n规则：\\n1. 标签应短小明确，优先选择人物、地点、时代、主题、器物、制度、文献类型。\\n2. 不输出过长短语，不使用解释句。\\n3. 不发明输入中没有依据的标签。\\n4. 输出必须是 JSON，字段为 tags。"},{"role":"user","content":"标题：{{title}}\\n分类路径：{{categoryPath}}\\n\\n内容：\\n{{document}}\\n\\n请抽取 3 到 8 个中文标签，返回 JSON：\\n{\\"tags\\":[\\"标签1\\",\\"标签2\\",\\"标签3\\"]}"}]', '[{"name":"title","required":false,"description":"标题"},{"name":"categoryPath","required":false,"description":"分类路径"},{"name":"document","required":true,"description":"待抽取标签的内容"}]', '{"type":"object","properties":{"tags":{"type":"array","items":{"type":"string"},"minItems":3,"maxItems":8}},"required":["tags"]}', 'Initial classics default tags prompt.', '2026-02-27 04:00:00.000'),
    (940103, 930103, 1, '[{"role":"system","content":"你是古籍问答生成助手，负责根据给定内容生成可用于知识问答的中文问答对。\\n\\n规则：\\n1. 问题必须能从输入内容中找到答案。\\n2. 答案应简洁、准确，不扩展输入之外的知识。\\n3. 问题应覆盖对象、关系、特征、用途或上下文，不要重复同一种问法。\\n4. 输出必须是 JSON，字段为 qa_pairs。"},{"role":"user","content":"标题：{{title}}\\n\\n内容：\\n{{document}}\\n\\n请生成 3 到 5 个问答对，返回 JSON：\\n{\\"qa_pairs\\":[{\\"question\\":\\"问题\\",\\"answer\\":\\"答案\\"}]}"}]', '[{"name":"title","required":false,"description":"标题"},{"name":"document","required":true,"description":"用于生成问答的内容"}]', '{"type":"object","properties":{"qa_pairs":{"type":"array","items":{"type":"object","properties":{"question":{"type":"string"},"answer":{"type":"string"}},"required":["question","answer"]},"minItems":3,"maxItems":5}},"required":["qa_pairs"]}', 'Initial classics default QA prompt.', '2026-02-27 04:00:00.000'),
    (940104, 930104, 1, '[{"role":"system","content":"你是古籍知识发现的查询理解助手，负责把用户查询转换成稳定的检索意图。\\n\\n规则：\\n1. 保留用户原始语义，不擅自改变问题目标。\\n2. 识别可能的人物、地点、时代、书名、主题和器物名。\\n3. 同义词扩展应保守，优先使用中文常见表达。\\n4. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"用户查询：{{query}}\\n可用检索范围：{{availableScopes}}\\n\\n请返回 JSON：\\n{\\n  \\"normalizedQueryText\\": \\"规范化查询\\",\\n  \\"rewrittenQueryText\\": \\"适合检索的改写查询\\",\\n  \\"expandedSynonyms\\": [\\"同义词\\"],\\n  \\"recognizedEntities\\": [\\"实体\\"],\\n  \\"intentType\\": \\"KEYWORD_SEARCH\\"\\n}"}]', '[{"name":"query","required":true,"description":"用户原始查询"},{"name":"availableScopes","required":false,"description":"可用检索范围"}]', '{"type":"object","properties":{"normalizedQueryText":{"type":"string"},"rewrittenQueryText":{"type":"string"},"expandedSynonyms":{"type":"array","items":{"type":"string"}},"recognizedEntities":{"type":"array","items":{"type":"string"}},"intentType":{"type":"string"}},"required":["normalizedQueryText","rewrittenQueryText","expandedSynonyms","recognizedEntities","intentType"]}', 'Initial discovery default query understanding prompt.', '2026-02-27 04:00:00.000'),
    (940105, 930105, 1, '[{"role":"system","content":"你是古籍知识发现的回答助手，负责根据检索来源生成可信中文回答。\\n\\n规则：\\n1. 只依据 sources 回答，不得编造来源之外的信息。\\n2. 如果来源不足以回答，answerStatus 设为 INSUFFICIENT_SOURCE，并说明缺口。\\n3. 回答应简洁、清楚，必要时指出不同来源之间的差异。\\n4. 输出必须是 JSON，不输出 Markdown。"},{"role":"user","content":"用户问题：{{question}}\\n回答风格：{{answerStyle}}\\n\\n检索来源：\\n{{sources}}\\n\\n请返回 JSON：\\n{\\n  \\"answer\\": \\"回答正文\\",\\n  \\"answerStatus\\": \\"ANSWERED\\",\\n  \\"sourceSummaries\\": [\\"来源摘要\\"]\\n}"}]', '[{"name":"question","required":true,"description":"用户问题"},{"name":"sources","required":true,"description":"检索来源列表"},{"name":"answerStyle","required":false,"description":"回答风格约束"}]', '{"type":"object","properties":{"answer":{"type":"string"},"answerStatus":{"type":"string"},"sourceSummaries":{"type":"array","items":{"type":"string"}}},"required":["answer","answerStatus","sourceSummaries"]}', 'Initial discovery default answer generation prompt.', '2026-02-27 04:00:00.000'),
    (940106, 930106, 1, '[{"role":"system","content":"你是古籍整理助手，负责把古文或文言文翻译成现代中文。\\n\\n规则：\\n1. 逐句理解后翻译，保持原意，不擅自增删事实。\\n2. 专名、官名、地名、书名不确定时保留原词，避免误译。\\n3. 遇到残缺、 OCR 错误或无法判断的内容，保守处理，不编造。\\n4. 只输出译文正文，不输出注释、说明、标题或“译文：”前缀。"},{"role":"user","content":"内容类型：{{contentType}}\\n标题：{{title}}\\n上下文：{{contextPath}}\\n\\n原文：\\n{{sourceText}}\\n\\n请翻译为现代中文。"}]', '[{"name":"contentType","required":true,"description":"内容类型"},{"name":"title","required":false,"description":"标题"},{"name":"contextPath","required":false,"description":"卷册或上下文路径"},{"name":"sourceText","required":true,"description":"待翻译原文"}]', '{"type":"text"}', 'Initial classics default translate prompt.', '2026-02-27 04:00:00.000'),
    (940107, 930107, 1, '[{"role":"system","content":"你是古籍图像解读助手，负责描述和解释古籍插图、版刻图、器物图或人物图。\\n\\n规则：\\n1. 区分可见事实和推测，不把不确定内容说成事实。\\n2. 优先描述主体、构图、文字标注、器物形态、人物关系和可能用途。\\n3. 如果图像信息不足，说明无法判断的部分。\\n4. 输出 Markdown，可使用简短小标题。"},{"role":"user","content":"标题：{{title}}\\n\\n相关上下文：\\n{{contextText}}\\n\\n图像描述：\\n{{imageDescription}}\\n\\n请给出面向古籍整理场景的图像解读，包括可见内容、可能含义和不确定点。"}]', '[{"name":"title","required":false,"description":"图像或条目标题"},{"name":"contextText","required":false,"description":"相关文字上下文"},{"name":"imageDescription","required":false,"description":"模型或人工提供的图像描述"}]', '{"type":"text"}', 'Initial classics default image analysis prompt.', '2026-02-27 04:00:00.000'),
    (940108, 930108, 1, '[{"role":"system","content":"你是古籍视觉资产提示词编写助手，负责把古籍条目转换成适合文生图模型的中文提示词。\\n\\n规则：\\n1. 提示词应描述主体、场景、构图、材质、光线和风格。\\n2. 不添加输入中没有依据的具体人物身份、年代事件或敏感内容。\\n3. 默认风格应接近古籍插图、博物馆图录或克制的学术视觉，不使用夸张奇幻风。\\n4. 只输出最终图像生成提示词，不输出解释。"},{"role":"user","content":"标题：{{title}}\\n\\n条目内容：\\n{{sourceText}}\\n\\n视觉风格约束：\\n{{styleGuide}}\\n\\n请生成一段可直接用于文生图模型的中文提示词。"}]', '[{"name":"title","required":false,"description":"条目标题"},{"name":"sourceText","required":false,"description":"原文或说明"},{"name":"styleGuide","required":false,"description":"视觉风格约束"}]', '{"type":"text"}', 'Initial classics default image generation prompt.', '2026-02-27 04:00:00.000'),
    (940109, 930109, 1, '[{"role":"system","content":"你是古籍图文信息融合助手，负责把条目原文、译文和图片理解结果整合为一段可靠的视觉资料说明。\\n\\n规则：\\n1. 明确区分文字依据、图像依据和推测。\\n2. 不添加输入中没有依据的人物、年代、地点或用途。\\n3. 输出应适合后续视觉描述和资料展示。\\n4. 输出 Markdown，可使用简短小标题。"},{"role":"user","content":"标题：{{title}}\\n\\n原文：\\n{{sourceText}}\\n\\n译文：\\n{{translationText}}\\n\\n图片理解：\\n{{imageAnalysis}}\\n\\n请融合上述信息，形成面向古籍视觉资产的说明。"}]', '[{"name":"title","required":false,"description":"条目标题"},{"name":"sourceText","required":false,"description":"原文"},{"name":"translationText","required":false,"description":"译文"},{"name":"imageAnalysis","required":false,"description":"图片理解结果"}]', '{"type":"text"}', 'Initial classics default visual fusion prompt.', '2026-02-27 04:00:00.000'),
    (940110, 930110, 1, '[{"role":"system","content":"你是古籍视觉描述助手，负责把融合后的图文资料整理成可用于展示和后续生图的视觉描述。\\n\\n规则：\\n1. 描述主体、构图、器物、人物姿态、环境和风格。\\n2. 保持克制、准确，不使用夸张玄幻表达。\\n3. 不引入输入外事实。\\n4. 只输出视觉描述正文。"},{"role":"user","content":"标题：{{title}}\\n\\n融合说明：\\n{{fusionText}}\\n\\n风格约束：\\n{{styleGuide}}\\n\\n请生成一段视觉描述。"}]', '[{"name":"title","required":false,"description":"条目标题"},{"name":"fusionText","required":false,"description":"融合说明"},{"name":"styleGuide","required":false,"description":"视觉风格约束"}]', '{"type":"text"}', 'Initial classics default visual description prompt.', '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `message_templates_json` = VALUES(`message_templates_json`),
    `variables_snapshot_json` = VALUES(`variables_snapshot_json`),
    `output_schema_json` = VALUES(`output_schema_json`),
    `change_summary` = VALUES(`change_summary`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_prompt_variable` (
    `id`, `template_id`, `variable_name`, `required`, `description`, `priority`
) VALUES
    (960101, 930101, 'contentType', 1, '内容类型', 960101),
    (960102, 930101, 'title', 0, '标题', 960102),
    (960103, 930101, 'categoryPath', 0, '门类或卷册路径', 960103),
    (960104, 930101, 'originalText', 0, '原文', 960104),
    (960105, 930101, 'translationText', 0, '已有译文', 960105),
    (960106, 930101, 'bodyText', 0, '正文或补充内容', 960106),
    (960107, 930101, 'existingSummary', 0, '已有摘要', 960107),
    (960301, 930102, 'title', 0, '标题', 960301),
    (960302, 930102, 'categoryPath', 0, '分类路径', 960302),
    (960303, 930102, 'document', 1, '待抽取标签的内容', 960303),
    (960401, 930103, 'title', 0, '标题', 960401),
    (960402, 930103, 'document', 1, '用于生成问答的内容', 960402),
    (961001, 930104, 'query', 1, '用户原始查询', 961001),
    (961002, 930104, 'availableScopes', 0, '可用检索范围', 961002),
    (961101, 930105, 'question', 1, '用户问题', 961101),
    (961102, 930105, 'sources', 1, '检索来源列表', 961102),
    (961103, 930105, 'answerStyle', 0, '回答风格约束', 961103),
    (960201, 930106, 'contentType', 1, '内容类型', 960201),
    (960202, 930106, 'title', 0, '标题', 960202),
    (960203, 930106, 'contextPath', 0, '卷册或上下文路径', 960203),
    (960204, 930106, 'sourceText', 1, '待翻译原文', 960204),
    (960501, 930107, 'title', 0, '图像或条目标题', 960501),
    (960502, 930107, 'contextText', 0, '相关文字上下文', 960502),
    (960503, 930107, 'imageDescription', 0, '模型或人工提供的图像描述', 960503),
    (960601, 930108, 'title', 0, '条目标题', 960601),
    (960602, 930108, 'sourceText', 0, '原文或说明', 960602),
    (960603, 930108, 'styleGuide', 0, '视觉风格约束', 960603),
    (960701, 930109, 'title', 0, '条目标题', 960701),
    (960702, 930109, 'sourceText', 0, '原文', 960702),
    (960703, 930109, 'translationText', 0, '译文', 960703),
    (960704, 930109, 'imageAnalysis', 0, '图片理解结果', 960704),
    (960801, 930110, 'title', 0, '条目标题', 960801),
    (960802, 930110, 'fusionText', 0, '融合说明', 960802),
    (960803, 930110, 'styleGuide', 0, '视觉风格约束', 960803)
ON DUPLICATE KEY UPDATE
    `required` = VALUES(`required`),
    `description` = VALUES(`description`),
    `priority` = VALUES(`priority`);
