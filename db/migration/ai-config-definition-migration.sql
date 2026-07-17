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

ALTER TABLE `ai_prompt_template`
    ADD COLUMN IF NOT EXISTS `enabled` tinyint(1) NOT NULL DEFAULT 1;

UPDATE `ai_prompt_template`
SET
    `id` = `template_id`,
    `enabled` = CASE WHEN `status` = 'ACTIVE' THEN 1 ELSE 0 END
WHERE `template_id` IS NOT NULL
    AND `id` <> `template_id`;

UPDATE `ai_prompt_version`
SET `id` = `prompt_version_id`
WHERE `prompt_version_id` IS NOT NULL
    AND `id` <> `prompt_version_id`;

UPDATE `ai_prompt_variable`
SET `id` = `variable_id`
WHERE `variable_id` IS NOT NULL
    AND `id` <> `variable_id`;
