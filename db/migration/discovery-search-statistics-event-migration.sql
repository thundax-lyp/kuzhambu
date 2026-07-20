SET NAMES utf8mb4;

RENAME TABLE `discovery_search_query_log` TO `discovery_search_query_event`;
ALTER TABLE `discovery_search_query_event`
    RENAME INDEX `uk_discovery_search_query_log_id` TO `uk_discovery_search_query_event_id`,
    RENAME INDEX `idx_discovery_search_query_log_user` TO `idx_discovery_search_query_event_user`,
    RENAME INDEX `idx_discovery_search_query_log_intent` TO `idx_discovery_search_query_event_intent`;
ALTER TABLE `discovery_search_query_event` COMMENT = '搜索查询事件表';

RENAME TABLE `discovery_search_log` TO `discovery_search_event`;
ALTER TABLE `discovery_search_event`
    CHANGE COLUMN `search_log_id` `search_event_id` varchar(64) NOT NULL,
    RENAME INDEX `uk_discovery_search_log_id` TO `uk_discovery_search_event_id`,
    RENAME INDEX `idx_discovery_search_log_status` TO `idx_discovery_search_event_status`,
    RENAME INDEX `idx_discovery_search_log_operator` TO `idx_discovery_search_event_operator`,
    RENAME INDEX `idx_discovery_search_log_intent` TO `idx_discovery_search_event_intent`;
ALTER TABLE `discovery_search_event` COMMENT = '检索统计事件表';

RENAME TABLE `discovery_search_click` TO `discovery_search_click_event`;
ALTER TABLE `discovery_search_click_event`
    CHANGE COLUMN `search_click_id` `search_click_event_id` varchar(64) NOT NULL,
    CHANGE COLUMN `search_log_id` `search_event_id` varchar(64) NOT NULL,
    RENAME INDEX `uk_discovery_search_click_id` TO `uk_discovery_search_click_event_id`,
    RENAME INDEX `idx_discovery_search_click_log` TO `idx_discovery_search_click_event_event`,
    RENAME INDEX `idx_discovery_search_click_content` TO `idx_discovery_search_click_event_content`,
    RENAME INDEX `idx_discovery_search_click_operator` TO `idx_discovery_search_click_event_operator`;
ALTER TABLE `discovery_search_click_event` COMMENT = '检索点击事件表';

ALTER TABLE `discovery_query_understanding`
    CHANGE COLUMN `search_log_id` `search_event_id` varchar(64) DEFAULT NULL,
    RENAME INDEX `idx_discovery_query_understanding_log` TO `idx_discovery_query_understanding_event`;
