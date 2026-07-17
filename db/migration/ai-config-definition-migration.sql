SET NAMES utf8mb4;

ALTER TABLE `ai_model`
    ADD COLUMN IF NOT EXISTS `api_source` varchar(32) NOT NULL DEFAULT 'OPENAI',
    ADD COLUMN IF NOT EXISTS `base_url` varchar(512) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS `encrypted_api_key` varchar(2048) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS `capabilities_json` json DEFAULT NULL;

UPDATE `ai_model`
SET `id` = `model_id`
WHERE `model_id` IS NOT NULL
    AND `id` <> `model_id`;

UPDATE `ai_model` model
LEFT JOIN `ai_service_config` service_config
    ON model.`service_id` = service_config.`service_id`
SET
    model.`api_source` = CASE
        WHEN service_config.`api_source` = 'BYTEDANCE' THEN 'BYTEDANCE'
        ELSE 'OPENAI'
    END,
    model.`base_url` = COALESCE(NULLIF(model.`base_url`, ''), service_config.`base_url`, ''),
    model.`encrypted_api_key` = COALESCE(model.`encrypted_api_key`, service_config.`encrypted_api_key`),
    model.`capabilities_json` = CASE
        WHEN model.`capability_tags_json` IS NULL THEN model.`capabilities_json`
        WHEN JSON_CONTAINS(model.`capability_tags_json`, JSON_QUOTE('image_gen')) THEN JSON_ARRAY('TEXT2IMAGE')
        WHEN JSON_CONTAINS(model.`capability_tags_json`, JSON_QUOTE('vision')) THEN JSON_ARRAY('TEXT2TEXT', 'IMAGE2TEXT')
        ELSE JSON_ARRAY('TEXT2TEXT')
    END
WHERE service_config.`service_id` IS NOT NULL
    OR model.`capabilities_json` IS NULL;

INSERT INTO `ai_business_config` (
    `id`, `capability`, `prompt_template_id`, `model_id`, `default_params_json`, `enabled`, `priority`, `configured_at`
)
SELECT
    mapping.`mapping_id`,
    CASE
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'summary' THEN 'classics_summary'
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'tags' THEN 'classics_tags'
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'qa' THEN 'classics_qa'
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'image_analysis' THEN 'classics_image_describe'
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'translate' THEN 'classics_translate'
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'image_gen' THEN 'classics_image_generate'
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'fusion' THEN 'classics_image_prompt_fusion'
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'visual' THEN 'classics_visual_describe'
        WHEN mapping.`scope` = 'discovery' AND mapping.`capability` = 'query_understanding' THEN 'discovery_query_understanding'
        WHEN mapping.`scope` = 'discovery' AND mapping.`capability` = 'answer_generation' THEN 'discovery_answer_generation'
        ELSE CONCAT(mapping.`scope`, '_', mapping.`capability`)
    END AS `capability`,
    CASE
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'summary' THEN 930101
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'tags' THEN 930102
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'qa' THEN 930103
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'image_analysis' THEN 930107
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'translate' THEN 930106
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'image_gen' THEN 930108
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'fusion' THEN 930109
        WHEN mapping.`scope` = 'classics' AND mapping.`capability` = 'visual' THEN 930110
        WHEN mapping.`scope` = 'discovery' AND mapping.`capability` = 'query_understanding' THEN 930104
        WHEN mapping.`scope` = 'discovery' AND mapping.`capability` = 'answer_generation' THEN 930105
        ELSE NULL
    END AS `prompt_template_id`,
    mapping.`model_id`,
    NULL,
    mapping.`enabled`,
    mapping.`mapping_id`,
    mapping.`configured_at`
FROM `ai_capability_mapping` mapping
HAVING `prompt_template_id` IS NOT NULL
ON DUPLICATE KEY UPDATE
    `model_id` = VALUES(`model_id`),
    `enabled` = VALUES(`enabled`),
    `configured_at` = VALUES(`configured_at`);

ALTER TABLE `ai_model`
    DROP COLUMN IF EXISTS `model_id`,
    DROP COLUMN IF EXISTS `service_id`,
    DROP COLUMN IF EXISTS `capability_tags_json`;

ALTER TABLE `ai_prompt_template`
    ADD COLUMN IF NOT EXISTS `enabled` tinyint(1) NOT NULL DEFAULT 1;

UPDATE `ai_prompt_template`
SET
    `id` = `template_id`,
    `enabled` = CASE WHEN `status` = 'ACTIVE' THEN 1 ELSE 0 END
WHERE `template_id` IS NOT NULL
    AND `id` <> `template_id`;

ALTER TABLE `ai_prompt_template`
    DROP COLUMN IF EXISTS `template_id`,
    DROP COLUMN IF EXISTS `scope`,
    DROP COLUMN IF EXISTS `status`;

UPDATE `ai_prompt_version`
SET `id` = `prompt_version_id`
WHERE `prompt_version_id` IS NOT NULL
    AND `id` <> `prompt_version_id`;

ALTER TABLE `ai_prompt_version`
    DROP COLUMN IF EXISTS `prompt_version_id`,
    DROP COLUMN IF EXISTS `current_key`;

UPDATE `ai_prompt_variable`
SET `id` = `variable_id`
WHERE `variable_id` IS NOT NULL
    AND `id` <> `variable_id`;

ALTER TABLE `ai_prompt_variable`
    DROP COLUMN IF EXISTS `variable_id`;
