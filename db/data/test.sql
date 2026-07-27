SET NAMES utf8mb4;

-- Dev/test-only data. Do not import this file into production environments.
-- Fixed ID range: 990000000000-990000000999.
-- The delete block keeps this file idempotent for repeated local/dev imports.

DELETE FROM `ai_refinement_task`
WHERE `task_id` BETWEEN 990000000001 AND 990000000099
    OR `call_id` BETWEEN 990000000001 AND 990000000099
    OR `candidate_id` BETWEEN 990000000001 AND 990000000099;

DELETE FROM `ai_candidate`
WHERE `id` BETWEEN 990000000001 AND 990000000099
    OR `call_id` BETWEEN 990000000001 AND 990000000099;

DELETE FROM `ai_invocation_log`
WHERE `call_id` BETWEEN 990000000001 AND 990000000099;

DELETE FROM `classics_content_tag`
WHERE `content_type` = 'WANGQI_DOCUMENT'
    AND `content_id` BETWEEN 990000000001 AND 990000000099;

DELETE FROM `classics_content_qa_pair`
WHERE `content_type` = 'WANGQI_DOCUMENT'
    AND `content_id` BETWEEN 990000000001 AND 990000000099;

DELETE FROM `classics_content_version`
WHERE `content_type` = 'WANGQI_DOCUMENT'
    AND `content_id` BETWEEN 990000000001 AND 990000000099;

DELETE FROM `classics_wangqi_document_event`
WHERE `document_id` BETWEEN 990000000001 AND 990000000099;

DELETE FROM `classics_wangqi_document`
WHERE `id` BETWEEN 990000000001 AND 990000000099;

INSERT INTO `classics_wangqi_document` (
    `id`, `title`, `summary`, `content_format`, `content`, `document_time`, `storage_object_id`,
    `visibility`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`
) VALUES
    (
        990000000001,
        '测试：王圻 AI 精修样例',
        '王圻归里后整理文献，并以梅花源为地方水利与文献整理的观察对象。',
        'MARKDOWN',
        '## 王圻 AI 精修样例\n\n王圻归里后继续整理文献，关注地方水利、乡里治理与文献编纂。梅花源引水绕村，既关乎农田灌溉，也成为其晚年生活与著述的空间线索。',
        '1586-01-01 00:00:00.000',
        NULL,
        'PUBLIC',
        990000000001,
        1,
        '2026-07-20 12:00:00.000',
        '2026-07-20 12:00:00.000'
    )
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `summary` = VALUES(`summary`),
    `content_format` = VALUES(`content_format`),
    `content` = VALUES(`content`),
    `document_time` = VALUES(`document_time`),
    `storage_object_id` = VALUES(`storage_object_id`),
    `visibility` = VALUES(`visibility`),
    `current_version_id` = VALUES(`current_version_id`),
    `current_version_no` = VALUES(`current_version_no`),
    `current_versioned_at` = VALUES(`current_versioned_at`),
    `content_updated_at` = VALUES(`content_updated_at`);

INSERT INTO `classics_wangqi_document_event` (
    `id`, `document_id`, `title`, `occurred_at`, `occurred_label`, `summary`, `priority`
) VALUES
    (
        990000000001,
        990000000001,
        '测试事件：王圻归里整理文献',
        '1586-01-01 00:00:00.000',
        '万历十四年',
        '王圻归里后继续整理文献，并关注乡里水利与地方治理。',
        990000001
    )
ON DUPLICATE KEY UPDATE
    `document_id` = VALUES(`document_id`),
    `title` = VALUES(`title`),
    `occurred_at` = VALUES(`occurred_at`),
    `occurred_label` = VALUES(`occurred_label`),
    `summary` = VALUES(`summary`),
    `priority` = VALUES(`priority`);

INSERT INTO `classics_content_version` (
    `id`, `content_type`, `content_id`, `version_no`, `versioned_at`, `snapshot_json`,
    `change_type`, `change_summary`
) VALUES
    (
        990000000001,
        'WANGQI_DOCUMENT',
        990000000001,
        1,
        '2026-07-20 12:00:00.000',
        '{"title":"测试：王圻 AI 精修样例","summary":"王圻归里后整理文献，并以梅花源为地方水利与文献整理的观察对象。","contentFormat":"MARKDOWN","visibility":"PUBLIC"}',
        'TEST_SEED',
        '测试数据：王圻 AI 精修样例初始版本'
    )
ON DUPLICATE KEY UPDATE
    `version_no` = VALUES(`version_no`),
    `versioned_at` = VALUES(`versioned_at`),
    `snapshot_json` = VALUES(`snapshot_json`),
    `change_type` = VALUES(`change_type`),
    `change_summary` = VALUES(`change_summary`);

INSERT INTO `classics_content_tag` (
    `id`, `content_type`, `content_id`, `tag_id`, `tag_name_snapshot`, `source`, `status`, `priority`
) VALUES
    (990000000001, 'WANGQI_DOCUMENT', 990000000001, NULL, '水利', 'MANUAL', 'ACTIVE', 990000011),
    (990000000002, 'WANGQI_DOCUMENT', 990000000001, NULL, '文献整理', 'MANUAL', 'ACTIVE', 990000012)
ON DUPLICATE KEY UPDATE
    `tag_id` = VALUES(`tag_id`),
    `tag_name_snapshot` = VALUES(`tag_name_snapshot`),
    `source` = VALUES(`source`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`);

INSERT INTO `classics_content_qa_pair` (
    `id`, `content_type`, `content_id`, `question`, `answer`, `source`, `priority`
) VALUES
    (
        990000000001,
        'WANGQI_DOCUMENT',
        990000000001,
        '王圻归里后主要关注哪些事项？',
        '王圻归里后继续整理文献，并关注地方水利、乡里治理与文献编纂。',
        'MANUAL',
        990000021
    )
ON DUPLICATE KEY UPDATE
    `question` = VALUES(`question`),
    `answer` = VALUES(`answer`),
    `source` = VALUES(`source`),
    `priority` = VALUES(`priority`);

INSERT INTO `ai_invocation_log` (
    `id`, `call_id`, `batch_id`, `scope`, `capability`, `content_type`, `content_id`, `object_id`,
    `service_id`, `service_role`, `model_id`, `model_name`, `prompt_version_id`, `request_id`, `trace_id`,
    `status`, `stream_used`, `stream_completed`, `fallback_used`, `latency_ms`, `input_tokens`, `output_tokens`,
    `cost_amount`, `failure_stage`, `result_format`, `result_payload`, `artifact_reference_json`,
    `error_type`, `error_message`, `warnings_json`, `requested_at`, `completed_at`
) VALUES
    (
        990000000001, 990000000001, NULL, 'classics', 'summary', 'WANGQI_DOCUMENT', 990000000001, NULL,
        NULL, 'PRIMARY', 900102, 'CTYUN-bot-DeepSeek-V3.2-pro', 940101, 'test-wangqi-summary-request', 'test-wangqi-summary-trace',
        'SUCCEEDED', 0, 0, 0, 860, 620, 128,
        0.000000, NULL, 'TEXT', '王圻归里后整理文献，关注梅花源水利、乡里治理和文献编纂之间的关联。', NULL,
        NULL, NULL, NULL, '2026-07-20 12:01:00.000', '2026-07-20 12:01:01.000'
    ),
    (
        990000000002, 990000000002, NULL, 'classics', 'tags', 'WANGQI_DOCUMENT', 990000000001, NULL,
        NULL, 'PRIMARY', 900102, 'CTYUN-bot-DeepSeek-V3.2-pro', 940102, 'test-wangqi-tags-request', 'test-wangqi-tags-trace',
        'SUCCEEDED', 0, 0, 0, 920, 680, 96,
        0.000000, NULL, 'STRUCTURED', '{"tags":["地方水利","梅花源","文献编纂"]}', NULL,
        NULL, NULL, NULL, '2026-07-20 12:02:00.000', '2026-07-20 12:02:01.000'
    ),
    (
        990000000003, 990000000003, NULL, 'classics', 'qa', 'WANGQI_DOCUMENT', 990000000001, NULL,
        NULL, 'PRIMARY', 900102, 'CTYUN-bot-DeepSeek-V3.2-pro', 940103, 'test-wangqi-qa-request', 'test-wangqi-qa-trace',
        'SUCCEEDED', 0, 0, 0, 980, 720, 156,
        0.000000, NULL, 'STRUCTURED', '{"qaPairs":[{"question":"梅花源在样例文档中有什么意义？","answer":"梅花源既关乎农田灌溉，也作为王圻晚年生活与著述的空间线索。"}]}', NULL,
        NULL, NULL, NULL, '2026-07-20 12:03:00.000', '2026-07-20 12:03:01.000'
    )
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `latency_ms` = VALUES(`latency_ms`),
    `input_tokens` = VALUES(`input_tokens`),
    `output_tokens` = VALUES(`output_tokens`),
    `result_format` = VALUES(`result_format`),
    `result_payload` = VALUES(`result_payload`),
    `completed_at` = VALUES(`completed_at`);

INSERT INTO `ai_candidate` (
    `id`, `call_id`, `batch_id`, `capability`, `content_type`, `content_id`, `object_id`,
    `result_format`, `result_payload`, `status`, `artifact_reference_json`, `failure_stage`,
    `prompt_version_id`, `model_name`, `error_type`, `error_message`, `requested_at`, `applied_at`, `rejected_at`
) VALUES
    (
        990000000001, 990000000001, NULL, 'summary', 'WANGQI_DOCUMENT', 990000000001, NULL,
        'TEXT', '王圻归里后整理文献，关注梅花源水利、乡里治理和文献编纂之间的关联。', 'PENDING', NULL, NULL,
        940101, 'CTYUN-bot-DeepSeek-V3.2-pro', NULL, NULL, '2026-07-20 12:01:01.000', NULL, NULL
    ),
    (
        990000000002, 990000000002, NULL, 'tags', 'WANGQI_DOCUMENT', 990000000001, NULL,
        'STRUCTURED', '{"tags":["地方水利","梅花源","文献编纂"]}', 'PENDING', NULL, NULL,
        940102, 'CTYUN-bot-DeepSeek-V3.2-pro', NULL, NULL, '2026-07-20 12:02:01.000', NULL, NULL
    ),
    (
        990000000003, 990000000003, NULL, 'qa', 'WANGQI_DOCUMENT', 990000000001, NULL,
        'STRUCTURED', '{"qaPairs":[{"question":"梅花源在样例文档中有什么意义？","answer":"梅花源既关乎农田灌溉，也作为王圻晚年生活与著述的空间线索。"}]}', 'PENDING', NULL, NULL,
        940103, 'CTYUN-bot-DeepSeek-V3.2-pro', NULL, NULL, '2026-07-20 12:03:01.000', NULL, NULL
    )
ON DUPLICATE KEY UPDATE
    `call_id` = VALUES(`call_id`),
    `result_format` = VALUES(`result_format`),
    `result_payload` = VALUES(`result_payload`),
    `status` = VALUES(`status`),
    `prompt_version_id` = VALUES(`prompt_version_id`),
    `model_name` = VALUES(`model_name`),
    `requested_at` = VALUES(`requested_at`),
    `applied_at` = VALUES(`applied_at`),
    `rejected_at` = VALUES(`rejected_at`);

INSERT INTO `ai_refinement_task` (
    `id`, `task_id`, `scope`, `capability`, `content_type`, `content_id`, `object_id`, `requested_by`,
    `request_id`, `trace_id`, `status`, `service_role`, `model_id`, `model_name`, `prompt_version_id`,
    `call_id`, `candidate_id`, `result_format`, `result_preview`, `failure_stage`, `error_type`,
    `error_message`, `stream_enabled`, `requested_at`, `started_at`, `completed_at`, `cancelled_at`
) VALUES
    (
        990000000001, 990000000001, 'classics', 'summary', 'WANGQI_DOCUMENT', 990000000001, NULL, 2,
        'test-wangqi-summary-request', 'test-wangqi-summary-trace', 'SUCCEEDED', 'PRIMARY', 900102, 'CTYUN-bot-DeepSeek-V3.2-pro', 940101,
        990000000001, 990000000001, 'TEXT', '王圻归里后整理文献，关注梅花源水利、乡里治理和文献编纂之间的关联。', NULL, NULL,
        NULL, 0, '2026-07-20 12:01:00.000', '2026-07-20 12:01:00.000', '2026-07-20 12:01:01.000', NULL
    ),
    (
        990000000002, 990000000002, 'classics', 'tags', 'WANGQI_DOCUMENT', 990000000001, NULL, 2,
        'test-wangqi-tags-request', 'test-wangqi-tags-trace', 'SUCCEEDED', 'PRIMARY', 900102, 'CTYUN-bot-DeepSeek-V3.2-pro', 940102,
        990000000002, 990000000002, 'STRUCTURED', '地方水利、梅花源、文献编纂', NULL, NULL,
        NULL, 0, '2026-07-20 12:02:00.000', '2026-07-20 12:02:00.000', '2026-07-20 12:02:01.000', NULL
    ),
    (
        990000000003, 990000000003, 'classics', 'qa', 'WANGQI_DOCUMENT', 990000000001, NULL, 2,
        'test-wangqi-qa-request', 'test-wangqi-qa-trace', 'SUCCEEDED', 'PRIMARY', 900102, 'CTYUN-bot-DeepSeek-V3.2-pro', 940103,
        990000000003, 990000000003, 'STRUCTURED', '梅花源既关乎农田灌溉，也作为王圻晚年生活与著述的空间线索。', NULL, NULL,
        NULL, 0, '2026-07-20 12:03:00.000', '2026-07-20 12:03:00.000', '2026-07-20 12:03:01.000', NULL
    )
ON DUPLICATE KEY UPDATE
    `status` = VALUES(`status`),
    `call_id` = VALUES(`call_id`),
    `candidate_id` = VALUES(`candidate_id`),
    `result_format` = VALUES(`result_format`),
    `result_preview` = VALUES(`result_preview`),
    `completed_at` = VALUES(`completed_at`);

ALTER TABLE `classics_wangqi_document` AUTO_INCREMENT = 990000000100;
ALTER TABLE `classics_wangqi_document_event` AUTO_INCREMENT = 990000000100;
ALTER TABLE `classics_content_version` AUTO_INCREMENT = 990000000100;
ALTER TABLE `classics_content_tag` AUTO_INCREMENT = 990000000100;
ALTER TABLE `classics_content_qa_pair` AUTO_INCREMENT = 990000000100;
ALTER TABLE `ai_invocation_log` AUTO_INCREMENT = 990000000100;
ALTER TABLE `ai_refinement_task` AUTO_INCREMENT = 990000000100;
