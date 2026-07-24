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
    (930111, 'classics_split', '古籍条目拆分提示词', '古籍条目拆分默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930112, 'knowledge_relation_extract', '实体关系抽取提示词', '实体关系抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930113, 'knowledge_graph_extract', '知识图谱抽取提示词', '知识图谱抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930114, 'knowledge_lineage_extract', '世系图抽取提示词', '世系图抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930115, 'knowledge_tags', '知识标签抽取提示词', '知识标签抽取默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930116, 'prompt_suggestion', '提示词优化建议提示词', '提示词优化建议默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930117, 'platform_version_summary', '提示词版本摘要提示词', '提示词版本摘要默认提示词。', 1, 1, '2026-02-27 04:00:00.000'),
    (930118, 'classics_translate_batch_item', '古籍批量翻译项提示词', '古籍批量翻译项默认提示词。', 1, 1, '2026-02-27 04:00:00.000')
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
    (910111, 'classics_split', 930111, 900102, NULL, 1, 11, '2026-02-27 04:00:00.000'),
    (910112, 'knowledge_relation_extract', 930112, 900102, NULL, 1, 12, '2026-02-27 04:00:00.000'),
    (910113, 'knowledge_graph_extract', 930113, 900102, NULL, 1, 13, '2026-02-27 04:00:00.000'),
    (910114, 'knowledge_lineage_extract', 930114, 900102, NULL, 1, 14, '2026-02-27 04:00:00.000'),
    (910115, 'knowledge_tags', 930115, 900102, NULL, 1, 15, '2026-02-27 04:00:00.000'),
    (910116, 'prompt_suggestion', 930116, 900102, NULL, 1, 16, '2026-02-27 04:00:00.000'),
    (910117, 'platform_version_summary', 930117, 900102, NULL, 1, 17, '2026-02-27 04:00:00.000'),
    (910118, 'classics_translate_batch_item', 930118, 900102, NULL, 1, 18, '2026-02-27 04:00:00.000')
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
    (940101, 930101, 1, '[{"role":"system","content":"你是古籍整理助手，负责为古籍条目生成可直接展示的中文摘要。\\n\\n规则：\\n1. 只依据输入内容总结，不得编造未出现的人名、地名、年代、关系或结论。\\n2. 摘要应简洁、准确、可读，优先保留条目的核心对象、事件、属性和用途。\\n3. 如果输入信息不足，只概括已知信息，不补充背景常识。\\n4. 不输出“摘要：”“以下是”等解释性前缀。\\n5. 输出纯文本，不使用 Markdown 列表。"},{"role":"user","content":"内容类型：{{contentType}}\\n标题：{{title}}\\n分类路径：{{categoryPath}}\\n\\n原文：\\n{{originalText}}\\n\\n已有译文：\\n{{translationText}}\\n\\n正文：\\n{{bodyText}}\\n\\n已有摘要：\\n{{existingSummary}}\\n\\n请生成一段中文摘要。若已有摘要质量较好，可以在保留事实的基础上压缩和润色；若内容为空或不足，请直接说明“暂无足够内容可摘要”。"}]', '[{"variableName":"contentType","required":true,"description":"内容类型"},{"variableName":"title","required":false,"description":"标题"},{"variableName":"categoryPath","required":false,"description":"门类或卷册路径"},{"variableName":"originalText","required":false,"description":"原文"},{"variableName":"translationText","required":false,"description":"已有译文"},{"variableName":"bodyText","required":false,"description":"正文或补充内容"},{"variableName":"existingSummary","required":false,"description":"已有摘要"}]', '{"type":"text"}', 'Initial classics default summary prompt.', '2026-02-27 04:00:00.000'),
    (940102, 930102, 1, '[{"role":"system","content":"你是古籍知识组织助手，负责从古籍条目中抽取稳定、可复用的中文标签。\\n\\n规则：\\n1. 标签应短小明确，优先选择人物、地点、时代、主题、器物、制度、文献类型。\\n2. 不输出过长短语，不使用解释句。\\n3. 不发明输入中没有依据的标签。\\n4. 输出必须是 JSON，字段为 tags。"},{"role":"user","content":"标题：{{title}}\\n分类路径：{{categoryPath}}\\n\\n内容：\\n{{document}}\\n\\n请抽取 3 到 8 个中文标签，返回 JSON：\\n{\\"tags\\":[\\"标签1\\",\\"标签2\\",\\"标签3\\"]}"}]', '[{"variableName":"title","required":false,"description":"标题"},{"variableName":"categoryPath","required":false,"description":"分类路径"},{"variableName":"document","required":true,"description":"待抽取标签的内容"}]', '{"type":"object","properties":{"tags":{"type":"array","items":{"type":"string"},"minItems":3,"maxItems":8}},"required":["tags"]}', 'Initial classics default tags prompt.', '2026-02-27 04:00:00.000'),
    (940103, 930103, 1, '[{"role":"system","content":"你是古籍问答生成助手，负责根据给定内容生成可用于知识问答的中文问答对。\\n\\n规则：\\n1. 问题必须能从输入内容中找到答案。\\n2. 答案应简洁、准确，不扩展输入之外的知识。\\n3. 问题应覆盖对象、关系、特征、用途或上下文，不要重复同一种问法。\\n4. 输出必须是 JSON，字段为 qa_pairs。"},{"role":"user","content":"标题：{{title}}\\n\\n内容：\\n{{document}}\\n\\n请生成 3 到 5 个问答对，返回 JSON：\\n{\\"qa_pairs\\":[{\\"question\\":\\"问题\\",\\"answer\\":\\"答案\\"}]}"}]', '[{"variableName":"title","required":false,"description":"标题"},{"variableName":"document","required":true,"description":"用于生成问答的内容"}]', '{"type":"object","properties":{"qa_pairs":{"type":"array","items":{"type":"object","properties":{"question":{"type":"string"},"answer":{"type":"string"}},"required":["question","answer"]},"minItems":3,"maxItems":5}},"required":["qa_pairs"]}', 'Initial classics default QA prompt.', '2026-02-27 04:00:00.000'),
    (940104, 930104, 1, '[{"role":"system","content":"你是古籍知识发现的查询理解助手，负责把用户查询转换成稳定的检索意图。\\n\\n规则：\\n1. 保留用户原始语义，不擅自改变问题目标。\\n2. 识别可能的人物、地点、时代、书名、主题和器物名。\\n3. 同义词扩展应保守，优先使用中文常见表达。\\n4. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"用户查询：{{query}}\\n可用检索范围：{{availableScopes}}\\n\\n请返回 JSON：\\n{\\n  \\"normalizedQueryText\\": \\"规范化查询\\",\\n  \\"rewrittenQueryText\\": \\"适合检索的改写查询\\",\\n  \\"expandedSynonyms\\": [\\"同义词\\"],\\n  \\"recognizedEntities\\": [\\"实体\\"],\\n  \\"intentType\\": \\"KEYWORD_SEARCH\\"\\n}"}]', '[{"variableName":"query","required":true,"description":"用户原始查询"},{"variableName":"availableScopes","required":false,"description":"可用检索范围"}]', '{"type":"object","properties":{"normalizedQueryText":{"type":"string"},"rewrittenQueryText":{"type":"string"},"expandedSynonyms":{"type":"array","items":{"type":"string"}},"recognizedEntities":{"type":"array","items":{"type":"string"}},"intentType":{"type":"string"}},"required":["normalizedQueryText","rewrittenQueryText","expandedSynonyms","recognizedEntities","intentType"]}', 'Initial discovery default query understanding prompt.', '2026-02-27 04:00:00.000'),
    (940105, 930105, 1, '[{"role":"system","content":"你是古籍知识发现的回答助手，负责根据检索来源生成可信中文回答。\\n\\n规则：\\n1. 只依据 sources 回答，不得编造来源之外的信息。\\n2. 如果来源不足以回答，answerStatus 设为 INSUFFICIENT_SOURCE，并说明缺口。\\n3. 回答应简洁、清楚，必要时指出不同来源之间的差异。\\n4. 输出必须是 JSON，不输出 Markdown。"},{"role":"user","content":"用户问题：{{question}}\\n回答风格：{{answerStyle}}\\n\\n检索来源：\\n{{sources}}\\n\\n请返回 JSON：\\n{\\n  \\"answer\\": \\"回答正文\\",\\n  \\"answerStatus\\": \\"ANSWERED\\",\\n  \\"sourceSummaries\\": [\\"来源摘要\\"]\\n}"}]', '[{"variableName":"question","required":true,"description":"用户问题"},{"variableName":"sources","required":true,"description":"检索来源列表"},{"variableName":"answerStyle","required":false,"description":"回答风格约束"}]', '{"type":"object","properties":{"answer":{"type":"string"},"answerStatus":{"type":"string"},"sourceSummaries":{"type":"array","items":{"type":"string"}}},"required":["answer","answerStatus","sourceSummaries"]}', 'Initial discovery default answer generation prompt.', '2026-02-27 04:00:00.000'),
    (940106, 930106, 1, '[{"role":"system","content":"你是古籍整理助手，负责把古文或文言文翻译成现代中文。\\n\\n规则：\\n1. 逐句理解后翻译，保持原意，不擅自增删事实。\\n2. 专名、官名、地名、书名不确定时保留原词，避免误译。\\n3. 遇到残缺、 OCR 错误或无法判断的内容，保守处理，不编造。\\n4. 只输出译文正文，不输出注释、说明、标题或“译文：”前缀。"},{"role":"user","content":"内容类型：{{contentType}}\\n标题：{{title}}\\n上下文：{{contextPath}}\\n\\n原文：\\n{{sourceText}}\\n\\n请翻译为现代中文。"}]', '[{"variableName":"contentType","required":true,"description":"内容类型"},{"variableName":"title","required":false,"description":"标题"},{"variableName":"contextPath","required":false,"description":"卷册或上下文路径"},{"variableName":"sourceText","required":true,"description":"待翻译原文"}]', '{"type":"text"}', 'Initial classics default translate prompt.', '2026-02-27 04:00:00.000'),
    (940107, 930107, 1, '[{"role":"system","content":"你是古籍图像解读助手，负责描述和解释古籍插图、版刻图、器物图或人物图。\\n\\n规则：\\n1. 区分可见事实和推测，不把不确定内容说成事实。\\n2. 优先描述主体、构图、文字标注、器物形态、人物关系和可能用途。\\n3. 如果图像信息不足，说明无法判断的部分。\\n4. 输出 Markdown，可使用简短小标题。"},{"role":"user","content":"标题：{{title}}\\n\\n相关上下文：\\n{{contextText}}\\n\\n图像描述：\\n{{imageDescription}}\\n\\n请给出面向古籍整理场景的图像解读，包括可见内容、可能含义和不确定点。"}]', '[{"variableName":"title","required":false,"description":"图像或条目标题"},{"variableName":"contextText","required":false,"description":"相关文字上下文"},{"variableName":"imageDescription","required":false,"description":"模型或人工提供的图像描述"}]', '{"type":"text"}', 'Initial classics default image analysis prompt.', '2026-02-27 04:00:00.000'),
    (940108, 930108, 1, '[{"role":"system","content":"你是古籍视觉资产提示词编写助手，负责把古籍条目转换成适合文生图模型的中文提示词。\\n\\n规则：\\n1. 提示词应描述主体、场景、构图、材质、光线和风格。\\n2. 不添加输入中没有依据的具体人物身份、年代事件或敏感内容。\\n3. 默认风格应接近古籍插图、博物馆图录或克制的学术视觉，不使用夸张奇幻风。\\n4. 只输出最终图像生成提示词，不输出解释。"},{"role":"user","content":"标题：{{title}}\\n\\n条目内容：\\n{{sourceText}}\\n\\n视觉风格约束：\\n{{styleGuide}}\\n\\n请生成一段可直接用于文生图模型的中文提示词。"}]', '[{"variableName":"title","required":false,"description":"条目标题"},{"variableName":"sourceText","required":false,"description":"原文或说明"},{"variableName":"styleGuide","required":false,"description":"视觉风格约束"}]', '{"type":"text"}', 'Initial classics default image generation prompt.', '2026-02-27 04:00:00.000'),
    (940109, 930109, 1, '[{"role":"system","content":"你是古籍图文信息融合助手，负责把条目原文、译文和图片理解结果整合为一段可靠的视觉资料说明。\\n\\n规则：\\n1. 明确区分文字依据、图像依据和推测。\\n2. 不添加输入中没有依据的人物、年代、地点或用途。\\n3. 输出应适合后续视觉描述和资料展示。\\n4. 输出 Markdown，可使用简短小标题。"},{"role":"user","content":"标题：{{title}}\\n\\n原文：\\n{{sourceText}}\\n\\n译文：\\n{{translationText}}\\n\\n图片理解：\\n{{imageAnalysis}}\\n\\n请融合上述信息，形成面向古籍视觉资产的说明。"}]', '[{"variableName":"title","required":false,"description":"条目标题"},{"variableName":"sourceText","required":false,"description":"原文"},{"variableName":"translationText","required":false,"description":"译文"},{"variableName":"imageAnalysis","required":false,"description":"图片理解结果"}]', '{"type":"text"}', 'Initial classics default visual fusion prompt.', '2026-02-27 04:00:00.000'),
    (940110, 930110, 1, '[{"role":"system","content":"你是古籍视觉描述助手，负责把融合后的图文资料整理成可用于展示和后续生图的视觉描述。\\n\\n规则：\\n1. 描述主体、构图、器物、人物姿态、环境和风格。\\n2. 保持克制、准确，不使用夸张玄幻表达。\\n3. 不引入输入外事实。\\n4. 只输出视觉描述正文。"},{"role":"user","content":"标题：{{title}}\\n\\n融合说明：\\n{{fusionText}}\\n\\n风格约束：\\n{{styleGuide}}\\n\\n请生成一段视觉描述。"}]', '[{"variableName":"title","required":false,"description":"条目标题"},{"variableName":"fusionText","required":false,"description":"融合说明"},{"variableName":"styleGuide","required":false,"description":"视觉风格约束"}]', '{"type":"text"}', 'Initial classics default visual description prompt.', '2026-02-27 04:00:00.000'),
    (940111, 930111, 1, '[{"role":"system","content":"你是古籍整理助手，负责把较长古籍内容拆分成可独立整理的条目候选。\\n\\n规则：\\n1. 只依据输入内容拆分，不补写原文没有的信息。\\n2. 每个候选条目必须有简短标题和对应原文。\\n3. 如果已有译文能对应到条目，可保守拆出译文；无法对应时留空。\\n4. 不改变原文顺序，不合并相隔较远的内容。\\n5. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"父条目标题：{{title}}\\n拆分提示：{{splitHint}}\\n\\n原文：\\n{{sourceText}}\\n\\n已有译文：\\n{{translationText}}\\n\\n请拆分为条目候选，返回 JSON：\\n{\\n  \\"entries\\": [\\n    {\\n      \\"title\\": \\"条目标题\\",\\n      \\"originalText\\": \\"对应原文\\",\\n      \\"translationText\\": \\"对应译文\\"\\n    }\\n  ]\\n}"}]', '[{"variableName":"title","required":false,"description":"父条目标题"},{"variableName":"sourceText","required":true,"description":"待拆分的原文或正文"},{"variableName":"translationText","required":false,"description":"已有译文"},{"variableName":"splitHint","required":false,"description":"拆分粒度或人工提示"}]', '{"type":"object","properties":{"entries":{"type":"array","items":{"type":"object","properties":{"title":{"type":"string"},"originalText":{"type":"string"},"translationText":{"type":"string"}},"required":["title","originalText"]}}},"required":["entries"]}', 'Initial classics default split prompt.', '2026-02-27 04:00:00.000'),
    (940112, 930112, 1, '[{"role":"system","content":"你是知识组织助手，负责从古籍或研究资料中抽取实体和实体关系候选。\\n\\n规则：\\n1. 只抽取文本中有明确依据的实体和关系。\\n2. 关系必须包含来源实体、目标实体、关系类型和证据片段。\\n3. 不生成正式知识表主键，不判断候选是否应正式入库。\\n4. 不确定内容放入 warnings。\\n5. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"来源标题：{{sourceTitle}}\\n已知实体：\\n{{knownEntities}}\\n\\n来源文本：\\n{{sourceText}}\\n\\n请抽取实体和关系候选，返回 JSON：\\n{\\n  \\"entities\\": [\\n    { \\"name\\": \\"实体名\\", \\"entityType\\": \\"实体类型\\", \\"description\\": \\"说明\\" }\\n  ],\\n  \\"relations\\": [\\n    { \\"sourceName\\": \\"来源实体\\", \\"targetName\\": \\"目标实体\\", \\"relationType\\": \\"关系类型\\", \\"evidence\\": \\"证据\\" }\\n  ],\\n  \\"sourceSnippets\\": [\\n    { \\"snippet\\": \\"证据片段\\", \\"sourceRef\\": \\"来源引用\\" }\\n  ],\\n  \\"warnings\\": []\\n}"}]', '[{"variableName":"sourceTitle","required":false,"description":"来源标题"},{"variableName":"sourceText","required":true,"description":"待抽取关系的文本"},{"variableName":"knownEntities","required":false,"description":"已知实体列表"}]', '{"type":"object","properties":{"entities":{"type":"array","items":{"type":"object"}},"relations":{"type":"array","items":{"type":"object"}},"sourceSnippets":{"type":"array","items":{"type":"object"}},"warnings":{"type":"array","items":{"type":"string"}}},"required":["entities","relations"]}', 'Initial knowledge default relation extraction prompt.', '2026-02-27 04:00:00.000'),
    (940113, 930113, 1, '[{"role":"system","content":"你是知识图谱抽取助手，负责把资料文本整理为实体、关系和条目引用候选。\\n\\n规则：\\n1. 只依据输入文本抽取图谱候选。\\n2. 实体和关系应便于人工确认，不写正式数据库主键。\\n3. 保留与条目引用相关的 contentType、contentId 和 title。\\n4. 信息不足或存在歧义时写入 warnings。\\n5. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"来源标题：{{sourceTitle}}\\n相关条目：\\n{{entryRefs}}\\n\\n来源文本：\\n{{sourceText}}\\n\\n请生成知识图谱候选，返回 JSON：\\n{\\n  \\"entities\\": [\\n    { \\"name\\": \\"实体名\\", \\"entityType\\": \\"实体类型\\", \\"description\\": \\"说明\\" }\\n  ],\\n  \\"relations\\": [\\n    { \\"sourceName\\": \\"来源实体\\", \\"targetName\\": \\"目标实体\\", \\"relationType\\": \\"关系类型\\", \\"evidence\\": \\"证据\\" }\\n  ],\\n  \\"entryRefs\\": [\\n    { \\"contentType\\": \\"内容类型\\", \\"contentId\\": \\"内容ID\\", \\"title\\": \\"标题\\" }\\n  ],\\n  \\"warnings\\": []\\n}"}]', '[{"variableName":"sourceTitle","required":false,"description":"来源标题"},{"variableName":"sourceText","required":true,"description":"待抽取图谱的文本"},{"variableName":"entryRefs","required":false,"description":"相关条目引用"}]', '{"type":"object","properties":{"entities":{"type":"array","items":{"type":"object"}},"relations":{"type":"array","items":{"type":"object"}},"entryRefs":{"type":"array","items":{"type":"object"}},"warnings":{"type":"array","items":{"type":"string"}}},"required":["entities","relations"]}', 'Initial knowledge default graph extraction prompt.', '2026-02-27 04:00:00.000'),
    (940114, 930114, 1, '[{"role":"system","content":"你是世系图抽取助手，负责从文本中抽取人物节点和亲属、传承或谱系关系候选。\\n\\n规则：\\n1. 只抽取文本中有明确依据的节点和关系。\\n2. 节点不得包含正式表主键，只返回名称、类型、代际和可识别属性。\\n3. 关系必须保留证据，不能根据常识补全缺失关系。\\n4. 不确定内容放入 warnings。\\n5. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"来源标题：{{sourceTitle}}\\n世系提示：{{lineageHint}}\\n\\n来源文本：\\n{{sourceText}}\\n\\n请抽取世系图候选，返回 JSON：\\n{\\n  \\"nodes\\": [\\n    { \\"name\\": \\"人物名\\", \\"nodeType\\": \\"PERSON\\", \\"generation\\": 1, \\"gender\\": \\"未知\\" }\\n  ],\\n  \\"relations\\": [\\n    { \\"sourceName\\": \\"来源人物\\", \\"targetName\\": \\"目标人物\\", \\"relationType\\": \\"关系类型\\", \\"evidence\\": \\"证据\\" }\\n  ],\\n  \\"sourceSnippets\\": [\\n    { \\"snippet\\": \\"证据片段\\", \\"sourceRef\\": \\"来源引用\\" }\\n  ],\\n  \\"warnings\\": []\\n}"}]', '[{"variableName":"sourceTitle","required":false,"description":"来源标题"},{"variableName":"sourceText","required":true,"description":"待抽取世系的文本"},{"variableName":"lineageHint","required":false,"description":"世系范围或人工提示"}]', '{"type":"object","properties":{"nodes":{"type":"array","items":{"type":"object"}},"relations":{"type":"array","items":{"type":"object"}},"sourceSnippets":{"type":"array","items":{"type":"object"}},"warnings":{"type":"array","items":{"type":"string"}}},"required":["nodes","relations"]}', 'Initial knowledge default lineage extraction prompt.', '2026-02-27 04:00:00.000'),
    (940115, 930115, 1, '[{"role":"system","content":"你是知识标签抽取助手，负责从资料文本中抽取稳定、可复用的中文标签候选。\\n\\n规则：\\n1. 标签应短小明确，优先选择人物、地点、时代、主题、制度、器物或文献类型。\\n2. 不输出过长短语或解释句。\\n3. 不发明输入中没有依据的标签。\\n4. 信息不足时返回较少标签，并把原因写入 warnings。\\n5. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"来源标题：{{sourceTitle}}\\n最大标签数量：{{maxTags}}\\n\\n来源文本：\\n{{sourceText}}\\n\\n请抽取知识标签候选，返回 JSON：\\n{\\n  \\"tags\\": [\\"标签1\\", \\"标签2\\", \\"标签3\\"],\\n  \\"warnings\\": []\\n}"}]', '[{"variableName":"sourceTitle","required":false,"description":"来源标题"},{"variableName":"sourceText","required":true,"description":"待抽取标签的文本"},{"variableName":"maxTags","required":false,"description":"最大标签数量"}]', '{"type":"object","properties":{"tags":{"type":"array","items":{"type":"string"}},"warnings":{"type":"array","items":{"type":"string"}}},"required":["tags"]}', 'Initial knowledge default tags prompt.', '2026-02-27 04:00:00.000'),
    (940116, 930116, 1, '[{"role":"system","content":"你是 AI 提示词治理助手，负责给出可审查、可回滚的提示词优化建议。\\n\\n规则：\\n1. 保留原提示词的业务边界和安全约束。\\n2. 只围绕输入的优化目标和已知问题提出修改。\\n3. 不移除明确的输出格式要求。\\n4. 风险和行为变化必须写入 risks。\\n5. 输出必须是 JSON，不输出解释性文字。"},{"role":"user","content":"当前提示词模板：\\n{{template}}\\n\\n优化目标：\\n{{changeGoal}}\\n\\n已知问题：\\n{{knownIssues}}\\n\\n请给出优化建议，返回 JSON：\\n{\\n  \\"suggestedTemplate\\": \\"优化后的提示词模板\\",\\n  \\"changeSummary\\": \\"变更摘要\\",\\n  \\"risks\\": [\\"风险或注意事项\\"]\\n}"}]', '[{"variableName":"template","required":true,"description":"当前提示词模板"},{"variableName":"changeGoal","required":false,"description":"优化目标"},{"variableName":"knownIssues","required":false,"description":"已知问题"}]', '{"type":"object","properties":{"suggestedTemplate":{"type":"string"},"changeSummary":{"type":"string"},"risks":{"type":"array","items":{"type":"string"}}},"required":["suggestedTemplate","changeSummary"]}', 'Initial platform default prompt suggestion prompt.', '2026-02-27 04:00:00.000'),
    (940117, 930117, 1, '[{"role":"system","content":"你是 AI 提示词版本管理助手，负责总结提示词版本之间的关键变化。\\n\\n规则：\\n1. 只比较输入中的提示词内容和人工变更摘要。\\n2. 摘要应说明行为变化、输出格式变化和风险点。\\n3. 不评价未给出的业务背景。\\n4. 输出纯文本，不使用 Markdown 表格。"},{"role":"user","content":"上一版本提示词：\\n{{previousTemplate}}\\n\\n当前版本提示词：\\n{{currentTemplate}}\\n\\n人工变更摘要：\\n{{changeSummary}}\\n\\n请生成一段版本摘要，说明主要变化和需要关注的兼容风险。"}]', '[{"variableName":"previousTemplate","required":false,"description":"上一版本提示词"},{"variableName":"currentTemplate","required":true,"description":"当前版本提示词"},{"variableName":"changeSummary","required":false,"description":"人工变更摘要"}]', '{"type":"text"}', 'Initial platform default version summary prompt.', '2026-02-27 04:00:00.000'),
    (940118, 930118, 1, '[{"role":"system","content":"你是古籍批量翻译助手，负责把单个批量项中的古文或文言文翻译成现代中文。\\n\\n规则：\\n1. 只处理当前批量项，不引用其他未提供的条目内容。\\n2. 保持原意，不擅自增删事实。\\n3. 专名、官名、地名、书名不确定时保留原词。\\n4. 遇到残缺、OCR 错误或无法判断的内容，保守处理。\\n5. 只输出译文正文，不输出批量处理说明。"},{"role":"user","content":"内容类型：{{contentType}}\\n标题：{{title}}\\n上下文：{{contextPath}}\\n\\n原文：\\n{{sourceText}}\\n\\n请翻译为现代中文。"}]', '[{"variableName":"contentType","required":true,"description":"内容类型"},{"variableName":"title","required":false,"description":"条目标题"},{"variableName":"contextPath","required":false,"description":"卷册或上下文路径"},{"variableName":"sourceText","required":true,"description":"待翻译原文"}]', '{"type":"text"}', 'Initial classics default batch translate item prompt.', '2026-02-27 04:00:00.000')
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
    (961201, 930111, 'title', 0, '父条目标题', 961201),
    (961202, 930111, 'sourceText', 1, '待拆分的原文或正文', 961202),
    (961203, 930111, 'translationText', 0, '已有译文', 961203),
    (961204, 930111, 'splitHint', 0, '拆分粒度或人工提示', 961204),
    (961301, 930112, 'sourceTitle', 0, '来源标题', 961301),
    (961302, 930112, 'sourceText', 1, '待抽取关系的文本', 961302),
    (961303, 930112, 'knownEntities', 0, '已知实体列表', 961303),
    (961401, 930113, 'sourceTitle', 0, '来源标题', 961401),
    (961402, 930113, 'sourceText', 1, '待抽取图谱的文本', 961402),
    (961403, 930113, 'entryRefs', 0, '相关条目引用', 961403),
    (961501, 930114, 'sourceTitle', 0, '来源标题', 961501),
    (961502, 930114, 'sourceText', 1, '待抽取世系的文本', 961502),
    (961503, 930114, 'lineageHint', 0, '世系范围或人工提示', 961503),
    (961601, 930115, 'sourceTitle', 0, '来源标题', 961601),
    (961602, 930115, 'sourceText', 1, '待抽取标签的文本', 961602),
    (961603, 930115, 'maxTags', 0, '最大标签数量', 961603),
    (961701, 930116, 'template', 1, '当前提示词模板', 961701),
    (961702, 930116, 'changeGoal', 0, '优化目标', 961702),
    (961703, 930116, 'knownIssues', 0, '已知问题', 961703),
    (961801, 930117, 'previousTemplate', 0, '上一版本提示词', 961801),
    (961802, 930117, 'currentTemplate', 1, '当前版本提示词', 961802),
    (961803, 930117, 'changeSummary', 0, '人工变更摘要', 961803),
    (961901, 930118, 'contentType', 1, '内容类型', 961901),
    (961902, 930118, 'title', 0, '条目标题', 961902),
    (961903, 930118, 'contextPath', 0, '卷册或上下文路径', 961903),
    (961904, 930118, 'sourceText', 1, '待翻译原文', 961904)
ON DUPLICATE KEY UPDATE
    `required` = VALUES(`required`),
    `description` = VALUES(`description`),
    `priority` = VALUES(`priority`);
