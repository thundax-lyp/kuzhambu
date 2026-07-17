SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `classics_wangqi_document_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` bigint NOT NULL COMMENT '王圻文档ID',
    `title` varchar(255) NOT NULL COMMENT '事件标题',
    `occurred_at` datetime(3) DEFAULT NULL COMMENT '历史事件发生时间',
    `occurred_label` varchar(128) DEFAULT NULL COMMENT '历史事件发生时间展示文本',
    `summary` text DEFAULT NULL COMMENT '事件摘要',
    `priority` int NOT NULL COMMENT '文档内事件排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_wangqi_event_priority` (`document_id`, `priority`),
    KEY `idx_classics_wangqi_event_time` (`occurred_at`),
    KEY `idx_classics_wangqi_event_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档历史事件表';
