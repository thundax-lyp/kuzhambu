SET NAMES utf8mb4;

-- Search has no required seed data.
INSERT INTO `discovery_search_query_log` (
    `query_id`, `user_id`, `raw_query`, `normalized_query`, `intent`, `rewritten_query`,
    `filters_json`, `expanded_terms_json`, `linked_entities_json`, `result_count`, `searched_at`
) VALUES (
    1001,
    2,
    '世系图',
    '世系图',
    'REWRITE',
    '上古帝王及世系图',
    '{"contentType":"SANCAI_ENTRY"}',
    '["世系","世系图谱"]',
    '["上古帝王","世系图"]',
    1,
    '2026-02-27 04:10:00.000'
) ON DUPLICATE KEY UPDATE
    `user_id` = VALUES(`user_id`),
    `raw_query` = VALUES(`raw_query`),
    `normalized_query` = VALUES(`normalized_query`),
    `intent` = VALUES(`intent`),
    `rewritten_query` = VALUES(`rewritten_query`),
    `filters_json` = VALUES(`filters_json`),
    `expanded_terms_json` = VALUES(`expanded_terms_json`),
    `linked_entities_json` = VALUES(`linked_entities_json`),
    `result_count` = VALUES(`result_count`),
    `searched_at` = VALUES(`searched_at`);

INSERT INTO `discovery_search_click_log` (
    `click_id`, `query_id`, `user_id`, `content_type`, `content_id`, `result_rank`, `clicked_at`
) VALUES (
    1101,
    1001,
    2,
    'SANCAI_ENTRY',
    300000000604,
    1,
    '2026-02-27 04:10:30.000'
) ON DUPLICATE KEY UPDATE
    `query_id` = VALUES(`query_id`),
    `user_id` = VALUES(`user_id`),
    `content_type` = VALUES(`content_type`),
    `content_id` = VALUES(`content_id`),
    `result_rank` = VALUES(`result_rank`),
    `clicked_at` = VALUES(`clicked_at`);

INSERT INTO `discovery_qa_session` (
    `session_id`, `owner_type`, `owner_id`, `title`, `scope`, `context_mode`,
    `context_content_type`, `context_content_id`, `status`, `knowledge_base_name`,
    `opened_at`, `last_message_at`, `removed_at`
) VALUES (
    2001,
    'USER',
    '2',
    '上古帝王及世系图问答',
    'portal',
    'QA',
    'SANCAI_ENTRY',
    300000000604,
    'OPEN',
    'kuzhambu-qa',
    '2026-02-27 04:11:00.000',
    '2026-02-27 04:12:00.000',
    NULL
) ON DUPLICATE KEY UPDATE
    `owner_type` = VALUES(`owner_type`),
    `owner_id` = VALUES(`owner_id`),
    `title` = VALUES(`title`),
    `scope` = VALUES(`scope`),
    `context_mode` = VALUES(`context_mode`),
    `knowledge_base_name` = VALUES(`knowledge_base_name`),
    `context_content_type` = VALUES(`context_content_type`),
    `context_content_id` = VALUES(`context_content_id`),
    `status` = VALUES(`status`),
    `opened_at` = VALUES(`opened_at`),
    `last_message_at` = VALUES(`last_message_at`),
    `removed_at` = VALUES(`removed_at`);

INSERT INTO `discovery_qa_message` (
    `message_id`, `session_id`, `role`, `content`, `answer_status`, `model`,
    `context_turn_count`, `failure_reason`, `provider_chat_id`, `finish_reason`, `sent_at`, `answered_at`
) VALUES
    (
        4001,
        2001,
        'USER',
        '上古帝王及世系图讲了什么？',
        'SENT',
        'kuzhambu-qa',
        3,
        NULL,
        NULL,
        NULL,
        '2026-02-27 04:11:00.000',
        NULL
    ),
    (
        4002,
        2001,
        'ASSISTANT',
        '主要围绕上古帝王、世系关系与相关图像展开。',
        'SUCCEEDED',
        'kuzhambu-qa',
        3,
        NULL,
        'chat_qa_4002',
        'stop',
        '2026-02-27 04:11:30.000',
        '2026-02-27 04:11:30.000'
    )
ON DUPLICATE KEY UPDATE
    `session_id` = VALUES(`session_id`),
    `role` = VALUES(`role`),
    `content` = VALUES(`content`),
    `answer_status` = VALUES(`answer_status`),
    `model` = VALUES(`model`),
    `context_turn_count` = VALUES(`context_turn_count`),
    `failure_reason` = VALUES(`failure_reason`),
    `provider_chat_id` = VALUES(`provider_chat_id`),
    `finish_reason` = VALUES(`finish_reason`),
    `sent_at` = VALUES(`sent_at`),
    `answered_at` = VALUES(`answered_at`);

INSERT INTO `discovery_qa_message_source` (
    `source_id`, `source_business_id`, `message_id`, `content_type`, `content_id`, `knowledge_base`,
    `title_snapshot`, `location_label`, `snippet`, `source_path`, `source_rank`, `score`,
    `source_status`, `referenced_at`
) VALUES (
    5001,
    'SANCAI_ENTRY:300000000604',
    4002,
    'SANCAI_ENTRY',
    300000000604,
    'classics',
    '上古帝王及世系图',
    '卷一',
    '上古帝王及世系图展示了三皇五帝及世系关系。',
    '/classics/sancai/300000000604',
    1,
    0.912345,
    'ACTIVE',
    '2026-02-27 04:11:30.000'
) ON DUPLICATE KEY UPDATE
    `message_id` = VALUES(`message_id`),
    `source_business_id` = VALUES(`source_business_id`),
    `content_type` = VALUES(`content_type`),
    `content_id` = VALUES(`content_id`),
    `knowledge_base` = VALUES(`knowledge_base`),
    `title_snapshot` = VALUES(`title_snapshot`),
    `location_label` = VALUES(`location_label`),
    `snippet` = VALUES(`snippet`),
    `source_path` = VALUES(`source_path`),
    `source_rank` = VALUES(`source_rank`),
    `score` = VALUES(`score`),
    `source_status` = VALUES(`source_status`),
    `referenced_at` = VALUES(`referenced_at`);

INSERT INTO `discovery_qa_retrieval_trace` (
    `trace_id`, `message_id`, `raw_question`, `provider`, `external_knowledge_base_id`,
    `external_knowledge_item_ids`, `external_chat_id`, `provider_request_id`, `latency_ms`,
    `failure_reason`, `raw`, `retrieved_at`
) VALUES (
    9001,
    4002,
    '上古帝王及世系图讲了什么？',
    'fastgpt',
    'kb_kuzhambu_qa',
    '["item_4002_1", "item_4002_2"]',
    'chat_qa_4002',
    'prf_4002',
    320,
    NULL,
    '{"message_id":4002}',
    '2026-02-27 04:11:30.000'
) ON DUPLICATE KEY UPDATE
    `message_id` = VALUES(`message_id`),
    `raw_question` = VALUES(`raw_question`),
    `provider` = VALUES(`provider`),
    `external_knowledge_base_id` = VALUES(`external_knowledge_base_id`),
    `external_knowledge_item_ids` = VALUES(`external_knowledge_item_ids`),
    `external_chat_id` = VALUES(`external_chat_id`),
    `provider_request_id` = VALUES(`provider_request_id`),
    `latency_ms` = VALUES(`latency_ms`),
    `failure_reason` = VALUES(`failure_reason`),
    `raw` = VALUES(`raw`),
    `retrieved_at` = VALUES(`retrieved_at`);

INSERT INTO `discovery_qa_knowledge_sync_batch` (
    `batch_id`, `trigger_type`, `provider`, `total_count`, `success_count`, `failure_count`, `started_at`, `finished_at`
) VALUES (
    7001,
    'MANUAL',
    'fastgpt',
    1,
    1,
    0,
    '2026-02-27 04:13:00.000',
    '2026-02-27 04:13:00.000'
) ON DUPLICATE KEY UPDATE
    `trigger_type` = VALUES(`trigger_type`),
    `provider` = VALUES(`provider`),
    `total_count` = VALUES(`total_count`),
    `success_count` = VALUES(`success_count`),
    `failure_count` = VALUES(`failure_count`),
    `started_at` = VALUES(`started_at`),
    `finished_at` = VALUES(`finished_at`);

INSERT INTO `discovery_qa_knowledge_sync_item` (
    `source_id`, `content_type`, `content_id`, `knowledge_base_name`, `current_version_no`,
    `knowledge_revision`, `provider`, `external_knowledge_base_id`, `external_knowledge_item_id`,
    `sync_status`, `failure_reason`, `synced_at`, `created_at`, `updated_at`
) VALUES (
    'SANCAI_ENTRY:300000000604',
    'SANCAI_ENTRY',
    300000000604,
    'kuzhambu-qa',
    1,
    'rev-2026-02-27-01',
    'fastgpt',
    'kb_kuzhambu_qa',
    'item_4002_1',
    'SUCCEEDED',
    NULL,
    '2026-02-27 04:13:00.000',
    '2026-02-27 04:13:00.000',
    '2026-02-27 04:13:00.000'
) ON DUPLICATE KEY UPDATE
    `content_type` = VALUES(`content_type`),
    `content_id` = VALUES(`content_id`),
    `knowledge_base_name` = VALUES(`knowledge_base_name`),
    `current_version_no` = VALUES(`current_version_no`),
    `knowledge_revision` = VALUES(`knowledge_revision`),
    `provider` = VALUES(`provider`),
    `external_knowledge_base_id` = VALUES(`external_knowledge_base_id`),
    `external_knowledge_item_id` = VALUES(`external_knowledge_item_id`),
    `sync_status` = VALUES(`sync_status`),
    `failure_reason` = VALUES(`failure_reason`),
    `synced_at` = VALUES(`synced_at`),
    `updated_at` = VALUES(`updated_at`);
