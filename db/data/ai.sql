SET NAMES utf8mb4;

INSERT INTO `ai_model` (
    `id`, `api_source`, `base_url`, `encrypted_api_key`, `model_name`, `display_name`, `capabilities_json`,
    `default_params_json`, `description`, `enabled`, `registered_at`
) VALUES
    (900101, 'OPENAI', '', NULL, 'CTYUN-CX-Qwen3.5-397B-A17B', '天翼千问 3.5 397B', '["TEXT2TEXT","IMAGE2TEXT"]', '{"temperature":0.2,"max_tokens":4096}', 'Default OpenAI-compatible vision-capable model for classics AI.', 1, '2026-02-27 04:00:00.000'),
    (900102, 'OPENAI', '', NULL, 'CTYUN-bot-DeepSeek-V3.2-pro', '天翼 DeepSeek V3.2 Pro', '["TEXT2TEXT"]', '{"temperature":0.2,"max_tokens":4096}', 'Default OpenAI-compatible LLM from local server configuration.', 1, '2026-02-27 04:00:00.000'),
    (900201, 'BYTEDANCE', '', NULL, 'doubao-seedream-5-0-pro-260628', '豆包 Seedream 5.0 Pro', '["TEXT2IMAGE"]', '{"response_format":"url","size":"2K","stream":false,"watermark":true}', 'Default ByteDance text-to-image model.', 1, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `api_source` = VALUES(`api_source`),
    `base_url` = COALESCE(NULLIF(VALUES(`base_url`), ''), `base_url`),
    `encrypted_api_key` = COALESCE(VALUES(`encrypted_api_key`), `encrypted_api_key`),
    `display_name` = VALUES(`display_name`),
    `capabilities_json` = VALUES(`capabilities_json`),
    `default_params_json` = VALUES(`default_params_json`),
    `description` = VALUES(`description`),
    `enabled` = VALUES(`enabled`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_prompt_template` (
    `id`, `capability`, `name`, `description`, `enabled`,
    `current_version_no`, `registered_at`
) VALUES
    (930101, 'classics_summary', '古籍摘要提示词', '古籍内容默认摘要提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930102, 'classics_tags', '古籍标签提取提示词', '古籍内容默认标签抽取提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930103, 'classics_qa', '古籍问答生成提示词', '古籍内容默认问答生成提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930104, 'discovery_query_understanding', '知识发现查询理解提示词', '知识发现默认查询理解提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930105, 'discovery_answer_generation', '知识发现回答生成提示词', '知识发现默认来源融合回答提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930106, 'classics_translate', '古籍翻译提示词', '古籍文言文默认现代中文翻译提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930107, 'classics_image_describe', '古籍图片理解提示词', '古籍图像默认解读提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930108, 'classics_image_generate', '古籍图片生成提示词', '古籍视觉资产默认文生图提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930109, 'classics_image_prompt_fusion', '古籍图文融合提示词', '古籍图文信息融合默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930110, 'classics_visual_describe', '古籍视觉描述提示词', '古籍视觉描述默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930111, 'classics_translate_batch_item', '古籍批量翻译项提示词', '古籍批量翻译单条任务默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930112, 'classics_split', '古籍条目拆分提示词', '古籍长条目拆分默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930113, 'knowledge_relation_extract', '实体关系抽取提示词', '知识实体关系候选抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930114, 'knowledge_graph_extract', '知识图谱抽取提示词', '知识图谱节点和边候选抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930115, 'knowledge_lineage_extract', '世系图抽取提示词', '世系图候选抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930116, 'knowledge_tags', '知识标签提取提示词', '知识标签候选抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930117, 'prompt_suggestion', '提示词优化建议提示词', '提示词优化建议默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930118, 'platform_version_summary', '版本摘要提示词', '版本变更摘要默认提示词。', 1, 1, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `capability` = VALUES(`capability`),
    `name` = VALUES(`name`),
    `description` = VALUES(`description`),
    `enabled` = VALUES(`enabled`),
    `current_version_no` = VALUES(`current_version_no`),
    `registered_at` = VALUES(`registered_at`);

INSERT INTO `ai_business_config` (
    `id`, `capability`, `prompt_template_id`, `model_id`, `default_params_json`, `enabled`, `priority`, `configured_at`
) VALUES
    (910101, 'classics_summary', 930101, 900102, NULL, 1, 1, '2026-02-27 04:00:00.000'),
    (910102, 'classics_tags', 930102, 900102, NULL, 1, 2, '2026-02-27 04:00:00.000'),
    (910103, 'classics_qa', 930103, 900102, NULL, 1, 3, '2026-02-27 04:00:00.000'),
    (910104, 'discovery_query_understanding', 930104, 900102, NULL, 1, 4, '2026-02-27 04:00:00.000'),
    (910105, 'discovery_answer_generation', 930105, 900102, NULL, 1, 5, '2026-02-27 04:00:00.000'),
    (910106, 'classics_translate', 930106, 900102, NULL, 1, 6, '2026-02-27 04:00:00.000'),
    (910107, 'classics_image_describe', 930107, 900101, NULL, 1, 7, '2026-02-27 04:00:00.000'),
    (910108, 'classics_image_generate', 930108, 900201, NULL, 1, 8, '2026-02-27 04:00:00.000'),
    (910109, 'classics_image_prompt_fusion', 930109, 900102, NULL, 1, 9, '2026-02-27 04:00:00.000'),
    (910110, 'classics_visual_describe', 930110, 900102, NULL, 1, 10, '2026-02-27 04:00:00.000'),
    (910111, 'classics_translate_batch_item', 930111, 900102, NULL, 1, 11, '2026-02-27 04:00:00.000'),
    (910112, 'classics_split', 930112, 900102, NULL, 1, 12, '2026-02-27 04:00:00.000'),
    (910113, 'knowledge_relation_extract', 930113, 900102, NULL, 1, 13, '2026-02-27 04:00:00.000'),
    (910114, 'knowledge_graph_extract', 930114, 900102, NULL, 1, 14, '2026-02-27 04:00:00.000'),
    (910115, 'knowledge_lineage_extract', 930115, 900102, NULL, 1, 15, '2026-02-27 04:00:00.000'),
    (910116, 'knowledge_tags', 930116, 900102, NULL, 1, 16, '2026-02-27 04:00:00.000'),
    (910117, 'prompt_suggestion', 930117, 900102, NULL, 1, 17, '2026-02-27 04:00:00.000'),
    (910118, 'platform_version_summary', 930118, 900102, NULL, 1, 18, '2026-02-27 04:00:00.000')
ON DUPLICATE KEY UPDATE
    `prompt_template_id` = VALUES(`prompt_template_id`),
    `model_id` = COALESCE(`model_id`, VALUES(`model_id`)),
    `default_params_json` = VALUES(`default_params_json`),
    `enabled` = VALUES(`enabled`),
    `priority` = VALUES(`priority`),
    `configured_at` = VALUES(`configured_at`);

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
    (940110, 930110, 1, '[{"role":"system","content":"你是古籍视觉描述助手，负责把融合后的图文资料整理成可用于展示和后续生图的视觉描述。\\n\\n规则：\\n1. 描述主体、构图、器物、人物姿态、环境和风格。\\n2. 保持克制、准确，不使用夸张玄幻表达。\\n3. 不引入输入外事实。\\n4. 只输出视觉描述正文。"},{"role":"user","content":"标题：{{title}}\\n\\n融合说明：\\n{{fusionText}}\\n\\n风格约束：\\n{{styleGuide}}\\n\\n请生成一段视觉描述。"}]', '[{"name":"title","required":false,"description":"条目标题"},{"name":"fusionText","required":false,"description":"融合说明"},{"name":"styleGuide","required":false,"description":"视觉风格约束"}]', '{"type":"text"}', 'Initial classics default visual description prompt.', '2026-02-27 04:00:00.000'),
    (940111, 930111, 1, '[{"role":"system","content":"你是古籍批量整理助手，负责在批量任务中把单条古文或文言文翻译成现代中文。\\n\\n规则：\\n1. 与同批次上下文保持术语、称谓和格式一致。\\n2. 只翻译当前条目，不合并其他条目内容。\\n3. 专名、官名、地名、书名不确定时保留原词，避免误译。\\n4. 遇到残缺、OCR 错误或无法判断的内容，保守处理，不编造。\\n5. 只输出译文正文，不输出注释、说明、标题或“译文：”前缀。"},{"role":"user","content":"内容类型：{{contentType}}\\n标题：{{title}}\\n批量上下文：\\n{{batchContext}}\\n\\n原文：\\n{{sourceText}}\\n\\n请翻译为现代中文，并保持与批量上下文一致。"}]', '[{"name":"contentType","required":true,"description":"内容类型"},{"name":"title","required":false,"description":"条目标题"},{"name":"batchContext","required":false,"description":"批量任务上下文"},{"name":"sourceText","required":true,"description":"待翻译原文"}]', '{"type":"text"}', 'Initial classics default batch translate item prompt.', '2026-02-27 04:00:00.000'),
    (940112, 930112, 1, '[{"role":"system","content":"你是古籍条目整理助手，负责把过长或混合多个主题的古籍内容拆分成稳定候选条目。\\n\\n规则：\\n1. 只依据输入内容拆分，不新增输入之外的事实。\\n2. 拆分边界应围绕对象、主题、人物、地点、器物或事件变化。\\n3. 每个候选条目必须保留可追溯的原文片段或内容片段。\\n4. 不确定是否应拆分时，保守减少拆分数量。\\n5. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"当前标题：{{title}}\\n分类路径：{{categoryPath}}\\n拆分提示：{{splitHint}}\\n\\n内容：\\n{{sourceText}}\\n\\n请返回 JSON：\\n{\\n  \\"items\\": [\\n    {\\n      \\"title\\": \\"候选条目标题\\",\\n      \\"content\\": \\"候选条目内容\\",\\n      \\"reason\\": \\"拆分理由\\"\\n    }\\n  ]\\n}"}]', '[{"name":"title","required":false,"description":"当前条目标题"},{"name":"categoryPath","required":false,"description":"门类、卷册或目标归属"},{"name":"sourceText","required":true,"description":"待拆分原文或译文"},{"name":"splitHint","required":false,"description":"拆分粒度、数量或归属提示"}]', '{"type":"object","properties":{"items":{"type":"array","items":{"type":"object","properties":{"title":{"type":"string"},"content":{"type":"string"},"reason":{"type":"string"}},"required":["title","content"]}}},"required":["items"]}', 'Initial classics default entry split prompt.', '2026-02-27 04:00:00.000'),
    (940113, 930113, 1, '[{"role":"system","content":"你是知识图谱整理助手，负责从文本中抽取实体关系候选。\\n\\n规则：\\n1. 关系必须有输入文本证据，不从常识补全。\\n2. 实体名称保持原文可识别表达，必要时保留别称。\\n3. 谓词应短小稳定，优先使用“属于”“位于”“记载”“使用”“相关”等可复用关系。\\n4. 只输出 JSON，不输出解释性文字。"},{"role":"user","content":"上下文：\\n{{context}}\\n\\n内容：\\n{{document}}\\n\\n请返回 JSON：\\n{\\n  \\"relations\\": [\\n    {\\n      \\"subject\\": \\"主体实体\\",\\n      \\"predicate\\": \\"关系\\",\\n      \\"object\\": \\"客体实体\\",\\n      \\"evidence\\": \\"文本证据\\"\\n    }\\n  ]\\n}"}]', '[{"name":"document","required":true,"description":"待抽取关系的内容"},{"name":"context","required":false,"description":"人工精修上下文或已有实体关系"}]', '{"type":"object","properties":{"relations":{"type":"array","items":{"type":"object","properties":{"subject":{"type":"string"},"predicate":{"type":"string"},"object":{"type":"string"},"evidence":{"type":"string"}},"required":["subject","predicate","object","evidence"]}}},"required":["relations"]}', 'Initial knowledge default relation extraction prompt.', '2026-02-27 04:00:00.000'),
    (940114, 930114, 1, '[{"role":"system","content":"你是知识图谱构建助手，负责从多段知识内容中抽取节点和边候选。\\n\\n规则：\\n1. 节点和边必须能追溯到输入片段。\\n2. 不合并无法确认相同的实体，不发明缺失关系。\\n3. 节点类型应稳定，例如 PERSON、PLACE、OBJECT、TEXT、CONCEPT、EVENT。\\n4. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"抽取范围：{{scope}}\\n\\n内容片段：\\n{{documents}}\\n\\n请返回 JSON：\\n{\\n  \\"nodes\\": [\\n    {\\n      \\"name\\": \\"实体名称\\",\\n      \\"type\\": \\"实体类型\\",\\n      \\"evidence\\": \\"文本证据\\"\\n    }\\n  ],\\n  \\"edges\\": [\\n    {\\n      \\"source\\": \\"源实体\\",\\n      \\"relation\\": \\"关系\\",\\n      \\"target\\": \\"目标实体\\",\\n      \\"evidence\\": \\"文本证据\\"\\n    }\\n  ]\\n}"}]', '[{"name":"scope","required":false,"description":"知识库、门类或卷册范围"},{"name":"documents","required":true,"description":"待抽取图谱的内容片段"}]', '{"type":"object","properties":{"nodes":{"type":"array","items":{"type":"object"}},"edges":{"type":"array","items":{"type":"object"}}},"required":["nodes","edges"]}', 'Initial knowledge default graph extraction prompt.', '2026-02-27 04:00:00.000'),
    (940115, 930115, 1, '[{"role":"system","content":"你是世系资料整理助手，负责从文本中抽取人物节点和亲属、传承、继承关系候选。\\n\\n规则：\\n1. 只抽取文本明确支持的关系，不根据历史常识补全。\\n2. 关系类型应稳定，例如 父子、母子、夫妻、兄弟、继承、师承。\\n3. 遇到不确定称谓时保留原文并标记低置信度。\\n4. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"约束：\\n{{constraints}}\\n\\n内容：\\n{{document}}\\n\\n请返回 JSON：\\n{\\n  \\"nodes\\": [\\n    {\\n      \\"name\\": \\"人物名称\\",\\n      \\"evidence\\": \\"文本证据\\"\\n    }\\n  ],\\n  \\"relations\\": [\\n    {\\n      \\"source\\": \\"人物A\\",\\n      \\"relation\\": \\"关系\\",\\n      \\"target\\": \\"人物B\\",\\n      \\"evidence\\": \\"文本证据\\",\\n      \\"confidence\\": \\"HIGH\\"\\n    }\\n  ]\\n}"}]', '[{"name":"document","required":true,"description":"世系相关原文或说明"},{"name":"constraints","required":false,"description":"已知人物、关系或时代约束"}]', '{"type":"object","properties":{"nodes":{"type":"array","items":{"type":"object"}},"relations":{"type":"array","items":{"type":"object"}}},"required":["nodes","relations"]}', 'Initial knowledge default lineage extraction prompt.', '2026-02-27 04:00:00.000'),
    (940116, 930116, 1, '[{"role":"system","content":"你是知识标签治理助手，负责从跨库内容中抽取可进入标签治理流程的候选标签。\\n\\n规则：\\n1. 标签应短小稳定，避免长句和一次性描述。\\n2. 优先复用已有标签体系中的同义或上位标签。\\n3. 不输出没有文本依据的标签。\\n4. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"标签体系上下文：\\n{{taxonomyContext}}\\n\\n内容：\\n{{document}}\\n\\n请返回 JSON：\\n{\\n  \\"tags\\": [\\n    {\\n      \\"name\\": \\"标签名\\",\\n      \\"reason\\": \\"推荐理由\\"\\n    }\\n  ]\\n}"}]', '[{"name":"document","required":true,"description":"待抽取标签的内容片段"},{"name":"taxonomyContext","required":false,"description":"已有标签体系、分类或合并约束"}]', '{"type":"object","properties":{"tags":{"type":"array","items":{"type":"object","properties":{"name":{"type":"string"},"reason":{"type":"string"}},"required":["name"]}}},"required":["tags"]}', 'Initial knowledge default tag extraction prompt.', '2026-02-27 04:00:00.000'),
    (940117, 930117, 1, '[{"role":"system","content":"你是 AI 提示词治理助手，负责给出可审核、可回滚的提示词优化建议。\\n\\n规则：\\n1. 不直接改写用户数据，只输出建议。\\n2. 每条建议必须说明问题、修改方向和预期影响。\\n3. 不改变目标业务能力的输出格式约束，除非明确说明兼容影响。\\n4. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"目标能力：{{targetCapability}}\\n\\n当前提示词：\\n{{currentPrompt}}\\n\\n变量定义：\\n{{variables}}\\n\\n失败样例或质量问题：\\n{{failureSamples}}\\n\\n请返回 JSON：\\n{\\n  \\"suggestions\\": [\\n    {\\n      \\"problem\\": \\"发现的问题\\",\\n      \\"recommendation\\": \\"建议修改\\",\\n      \\"impact\\": \\"预期影响\\"\\n    }\\n  ]\\n}"}]', '[{"name":"targetCapability","required":true,"description":"目标业务能力"},{"name":"currentPrompt","required":true,"description":"当前提示词内容"},{"name":"variables","required":false,"description":"变量定义"},{"name":"failureSamples","required":false,"description":"失败样例或质量问题"}]', '{"type":"object","properties":{"suggestions":{"type":"array","items":{"type":"object"}}},"required":["suggestions"]}', 'Initial platform default prompt suggestion prompt.', '2026-02-27 04:00:00.000'),
    (940118, 930118, 1, '[{"role":"system","content":"你是版本记录助手，负责根据变更前后内容生成面向后台审计和人工复核的中文摘要。\\n\\n规则：\\n1. 摘要只描述可从输入看出的变化。\\n2. 优先说明业务对象、关键字段、语义影响和需要人工注意的风险。\\n3. 不输出无法确认的原因、责任人或上线结论。\\n4. 输出纯文本，不使用 Markdown 列表。"},{"role":"user","content":"业务域：{{domain}}\\n操作上下文：{{operationContext}}\\n\\n变更前：\\n{{beforeContent}}\\n\\n变更后：\\n{{afterContent}}\\n\\n请生成一段简洁的中文版本摘要。"}]', '[{"name":"domain","required":false,"description":"业务域或模块"},{"name":"operationContext","required":false,"description":"操作上下文"},{"name":"beforeContent","required":false,"description":"变更前内容"},{"name":"afterContent","required":true,"description":"变更后内容"}]', '{"type":"text"}', 'Initial platform default version summary prompt.', '2026-02-27 04:00:00.000')
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
    (960803, 930110, 'styleGuide', 0, '视觉风格约束', 960803),
    (960901, 930111, 'contentType', 1, '内容类型', 960901),
    (960902, 930111, 'title', 0, '条目标题', 960902),
    (960903, 930111, 'batchContext', 0, '批量任务上下文', 960903),
    (960904, 930111, 'sourceText', 1, '待翻译原文', 960904),
    (961201, 930112, 'title', 0, '当前条目标题', 961201),
    (961202, 930112, 'categoryPath', 0, '门类、卷册或目标归属', 961202),
    (961203, 930112, 'sourceText', 1, '待拆分原文或译文', 961203),
    (961204, 930112, 'splitHint', 0, '拆分粒度、数量或归属提示', 961204),
    (962101, 930113, 'document', 1, '待抽取关系的内容', 962101),
    (962102, 930113, 'context', 0, '人工精修上下文或已有实体关系', 962102),
    (962201, 930114, 'scope', 0, '知识库、门类或卷册范围', 962201),
    (962202, 930114, 'documents', 1, '待抽取图谱的内容片段', 962202),
    (962301, 930115, 'document', 1, '世系相关原文或说明', 962301),
    (962302, 930115, 'constraints', 0, '已知人物、关系或时代约束', 962302),
    (962401, 930116, 'document', 1, '待抽取标签的内容片段', 962401),
    (962402, 930116, 'taxonomyContext', 0, '已有标签体系、分类或合并约束', 962402),
    (963101, 930117, 'targetCapability', 1, '目标业务能力', 963101),
    (963102, 930117, 'currentPrompt', 1, '当前提示词内容', 963102),
    (963103, 930117, 'variables', 0, '变量定义', 963103),
    (963104, 930117, 'failureSamples', 0, '失败样例或质量问题', 963104),
    (963201, 930118, 'domain', 0, '业务域或模块', 963201),
    (963202, 930118, 'operationContext', 0, '操作上下文', 963202),
    (963203, 930118, 'beforeContent', 0, '变更前内容', 963203),
    (963204, 930118, 'afterContent', 1, '变更后内容', 963204)
ON DUPLICATE KEY UPDATE
    `required` = VALUES(`required`),
    `description` = VALUES(`description`),
    `priority` = VALUES(`priority`);
