SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `classics_sancai_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(64) NOT NULL COMMENT '门类名称',
    `category_type` varchar(16) NOT NULL DEFAULT 'FORMAL' COMMENT '门类类型',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_category_title` (`title`),
    UNIQUE KEY `uk_classics_sancai_category_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会门类表';

CREATE TABLE IF NOT EXISTS `classics_sancai_volume` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` bigint NOT NULL COMMENT '所属门类ID',
    `title` varchar(128) NOT NULL COMMENT '卷标题',
    `volume_type` varchar(16) NOT NULL DEFAULT 'MAIN' COMMENT '卷类型',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_volume_priority` (`priority`),
    KEY `idx_classics_sancai_volume_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会卷表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `volume_id` bigint NOT NULL COMMENT '所属卷ID',
    `title` varchar(255) NOT NULL COMMENT '条目标题',
    `original_text` longtext DEFAULT NULL COMMENT '原文',
    `translation_text` longtext DEFAULT NULL COMMENT '译文',
    `summary` text DEFAULT NULL COMMENT '摘要',
    `lifecycle_status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '生命周期状态',
    `transition_status` varchar(24) NOT NULL DEFAULT 'NONE' COMMENT '发布或下线过程状态',
    `current_publication_job_id` bigint DEFAULT NULL COMMENT '当前发布或下线任务ID',
    `translation_status` varchar(16) NOT NULL DEFAULT 'MISSING' COMMENT '翻译状态',
    `image_status` varchar(16) NOT NULL DEFAULT 'MISSING' COMMENT '配图状态',
    `visual_asset_status` varchar(16) NOT NULL DEFAULT 'MISSING' COMMENT '视觉资产状态',
    `refinement_status` varchar(16) NOT NULL DEFAULT 'RAW' COMMENT '完善状态',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    `current_version_id` bigint DEFAULT NULL COMMENT '当前正式内容版本ID',
    `current_version_no` int DEFAULT NULL COMMENT '当前正式内容版本号',
    `current_versioned_at` BIGINT DEFAULT NULL COMMENT '当前正式内容版本生成时间',
    `content_updated_at` BIGINT NOT NULL COMMENT '内容语义更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_priority` (`priority`),
    KEY `idx_classics_sancai_entry_current_version` (`current_version_id`),
    KEY `idx_classics_sancai_entry_volume` (`volume_id`),
    KEY `idx_classics_sancai_entry_lifecycle` (`lifecycle_status`),
    KEY `idx_classics_sancai_entry_transition` (`transition_status`),
    KEY `idx_classics_sancai_entry_publication_job` (`current_publication_job_id`),
    KEY `idx_classics_sancai_entry_status` (`translation_status`, `image_status`, `visual_asset_status`, `refinement_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_draft` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `entry_id` bigint NOT NULL COMMENT '条目ID',
    `autosaved_at` BIGINT NOT NULL COMMENT '自动保存时间',
    `draft_json` json NOT NULL COMMENT '草稿快照',
    PRIMARY KEY (`id`),
    KEY `idx_classics_sancai_entry_draft_entry` (`entry_id`, `autosaved_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目草稿表';

CREATE TABLE IF NOT EXISTS `classics_sancai_entry_image` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `entry_id` bigint NOT NULL COMMENT '条目ID',
    `storage_object_id` bigint NOT NULL COMMENT 'Storage对象ID',
    `image_type` varchar(16) NOT NULL COMMENT '图片类型',
    `title` varchar(512) DEFAULT NULL COMMENT '图片标题',
    `current_used` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否当前使用',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_entry_image_object` (`entry_id`, `storage_object_id`),
    UNIQUE KEY `uk_classics_sancai_entry_image_priority` (`priority`),
    KEY `idx_classics_sancai_entry_image_entry` (`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目图片引用表';

CREATE TABLE IF NOT EXISTS `classics_sancai_visual_asset` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `entry_id` bigint NOT NULL COMMENT '条目ID',
    `version_no` int NOT NULL COMMENT '视觉资产版本号',
    `status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    `source_image_storage_object_id` bigint DEFAULT NULL COMMENT '原图Storage对象ID',
    `generated_image_storage_object_id` bigint DEFAULT NULL COMMENT 'AI生成图Storage对象ID',
    `current_used` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否当前使用版本',
    `text_weight` int NOT NULL DEFAULT 50 COMMENT '文本权重',
    `image_weight` int NOT NULL DEFAULT 50 COMMENT '图片理解权重',
    `image_analysis_markdown` longtext DEFAULT NULL COMMENT '图片理解结果',
    `fusion_description` longtext DEFAULT NULL COMMENT '融合描述',
    `visual_description` longtext DEFAULT NULL COMMENT '视觉描述',
    `generation_params_json` json DEFAULT NULL COMMENT '生图参数快照',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_sancai_visual_asset_version` (`entry_id`, `version_no`),
    KEY `idx_classics_sancai_visual_asset_current` (`entry_id`, `current_used`),
    KEY `idx_classics_sancai_visual_asset_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会视觉资产表';

CREATE TABLE IF NOT EXISTS `classics_wangqi_document` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(255) NOT NULL COMMENT '标题',
    `summary` text DEFAULT NULL COMMENT '摘要',
    `content_format` varchar(16) NOT NULL DEFAULT 'MARKDOWN' COMMENT '正文格式',
    `content` longtext DEFAULT NULL COMMENT '正文',
    `document_time` BIGINT DEFAULT NULL COMMENT '文档时间',
    `storage_object_id` bigint DEFAULT NULL COMMENT '原始文档Storage对象ID',
    `lifecycle_status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '生命周期状态',
    `transition_status` varchar(24) NOT NULL DEFAULT 'NONE' COMMENT '发布或下线过程状态',
    `current_publication_job_id` bigint DEFAULT NULL COMMENT '当前发布或下线任务ID',
    `current_version_id` bigint DEFAULT NULL COMMENT '当前正式内容版本ID',
    `current_version_no` int DEFAULT NULL COMMENT '当前正式内容版本号',
    `current_versioned_at` BIGINT DEFAULT NULL COMMENT '当前正式内容版本生成时间',
    `content_updated_at` BIGINT NOT NULL COMMENT '内容语义更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_classics_wangqi_document_current_version` (`current_version_id`),
    KEY `idx_classics_wangqi_document_time` (`document_time`),
    KEY `idx_classics_wangqi_document_lifecycle` (`lifecycle_status`),
    KEY `idx_classics_wangqi_document_transition` (`transition_status`),
    KEY `idx_classics_wangqi_document_publication_job` (`current_publication_job_id`),
    KEY `idx_classics_wangqi_document_storage_object` (`storage_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档表';

CREATE TABLE IF NOT EXISTS `classics_wangqi_document_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` bigint NOT NULL COMMENT '王圻文档ID',
    `title` varchar(255) NOT NULL COMMENT '事件标题',
    `occurred_at` BIGINT DEFAULT NULL COMMENT '历史事件发生时间',
    `occurred_label` varchar(128) DEFAULT NULL COMMENT '历史事件发生时间展示文本',
    `summary` text DEFAULT NULL COMMENT '事件摘要',
    `priority` int NOT NULL COMMENT '全局事件排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_wangqi_event_priority` (`priority`),
    KEY `idx_classics_wangqi_event_time` (`occurred_at`),
    KEY `idx_classics_wangqi_event_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档历史事件表';

CREATE TABLE IF NOT EXISTS `classics_ming_customs_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(255) NOT NULL COMMENT '标题',
    `category` varchar(128) DEFAULT NULL COMMENT '分类',
    `chapter` varchar(128) DEFAULT NULL COMMENT '章节',
    `section` varchar(128) DEFAULT NULL COMMENT '节',
    `summary` text DEFAULT NULL COMMENT '概述或摘要',
    `content_format` varchar(16) NOT NULL DEFAULT 'MARKDOWN' COMMENT '正文格式',
    `content` longtext DEFAULT NULL COMMENT '正文',
    `original_excerpts` longtext DEFAULT NULL COMMENT '原文摘录',
    `lifecycle_status` varchar(16) NOT NULL DEFAULT 'DRAFT' COMMENT '生命周期状态',
    `transition_status` varchar(24) NOT NULL DEFAULT 'NONE' COMMENT '发布或下线过程状态',
    `current_publication_job_id` bigint DEFAULT NULL COMMENT '当前发布或下线任务ID',
    `current_version_id` bigint DEFAULT NULL COMMENT '当前正式内容版本ID',
    `current_version_no` int DEFAULT NULL COMMENT '当前正式内容版本号',
    `current_versioned_at` BIGINT DEFAULT NULL COMMENT '当前正式内容版本生成时间',
    `content_updated_at` BIGINT NOT NULL COMMENT '内容语义更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_classics_ming_customs_current_version` (`current_version_id`),
    KEY `idx_classics_ming_customs_category` (`category`, `lifecycle_status`),
    KEY `idx_classics_ming_customs_lifecycle` (`lifecycle_status`),
    KEY `idx_classics_ming_customs_transition` (`transition_status`),
    KEY `idx_classics_ming_customs_publication_job` (`current_publication_job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗条目表';

CREATE TABLE IF NOT EXISTS `classics_ming_customs_keyword` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `custom_id` bigint NOT NULL COMMENT '明代习俗条目ID',
    `keyword` varchar(128) NOT NULL COMMENT '关键词',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_ming_customs_keyword` (`custom_id`, `keyword`),
    UNIQUE KEY `uk_classics_ming_customs_keyword_priority` (`priority`),
    KEY `idx_classics_ming_customs_keyword_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗关键词表';

CREATE TABLE IF NOT EXISTS `classics_content_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `tag_id` bigint DEFAULT NULL COMMENT 'Knowledge标签ID',
    `tag_name_snapshot` varchar(128) NOT NULL COMMENT '标签名称快照',
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL' COMMENT '来源',
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_content_tag_name` (`content_type`, `content_id`, `tag_name_snapshot`),
    UNIQUE KEY `uk_classics_content_tag_priority` (`priority`),
    KEY `idx_classics_content_tag_content` (`content_type`, `content_id`),
    KEY `idx_classics_content_tag_tag` (`tag_id`, `content_type`),
    KEY `idx_classics_content_tag_name` (`tag_name_snapshot`, `content_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签引用表';

CREATE TABLE IF NOT EXISTS `classics_content_qa_pair` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `question` text NOT NULL COMMENT '问题',
    `answer` longtext NOT NULL COMMENT '答案',
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL' COMMENT '来源',
    `priority` int NOT NULL COMMENT '全局唯一排序',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_content_qa_priority` (`priority`),
    KEY `idx_classics_content_qa_content` (`content_type`, `content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容问答对表';

CREATE TABLE IF NOT EXISTS `classics_content_version` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `version_no` int NOT NULL COMMENT '版本号',
    `versioned_at` BIGINT NOT NULL COMMENT '版本时间',
    `snapshot_json` json NOT NULL COMMENT '快照内容',
    `change_type` varchar(32) NOT NULL COMMENT '变更类型',
    `change_summary` varchar(512) DEFAULT NULL COMMENT '变更摘要',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_content_version_no` (`content_type`, `content_id`, `version_no`),
    KEY `idx_classics_content_version_time` (`content_type`, `content_id`, `versioned_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容版本表';

CREATE TABLE IF NOT EXISTS `classics_publication_job` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `job_type` varchar(16) NOT NULL COMMENT '任务类型',
    `content_type` varchar(32) NOT NULL COMMENT '内容类型',
    `content_id` bigint NOT NULL COMMENT '内容ID',
    `content_title_snapshot` varchar(255) NOT NULL COMMENT '稿件标题快照',
    `content_deleted_at` BIGINT DEFAULT NULL COMMENT '稿件业务删除时间',
    `source_lifecycle_status` varchar(16) NOT NULL COMMENT '发起时生命周期状态',
    `target_lifecycle_status` varchar(16) NOT NULL COMMENT '目标生命周期状态',
    `content_version_id` bigint DEFAULT NULL COMMENT '发布绑定正式版本ID',
    `content_version_no` int DEFAULT NULL COMMENT '发布绑定正式版本号',
    `job_status` varchar(32) NOT NULL DEFAULT 'QUEUED' COMMENT '最后完成的状态机里程碑',
    `job_result_status` varchar(16) NOT NULL DEFAULT 'RUNNING' COMMENT '任务整体结果',
    `execution_token` varchar(64) DEFAULT NULL COMMENT '当前切片执行令牌',
    `expires_at` BIGINT DEFAULT NULL COMMENT '当前执行租约过期时间',
    `next_retry_at` BIGINT DEFAULT NULL COMMENT '下一次重试时间',
    `attempt_count` int NOT NULL DEFAULT 0 COMMENT '当前切片已尝试次数',
    `max_attempts` int NOT NULL DEFAULT 4 COMMENT '当前切片最大尝试次数',
    `es_document_id` varchar(256) DEFAULT NULL COMMENT 'ES文档ID',
    `fastgpt_collection_id` varchar(256) DEFAULT NULL COMMENT 'FastGPT稿件collection ID',
    `fastgpt_data_ids_json` json DEFAULT NULL COMMENT 'FastGPT碎片ID诊断快照',
    `es_cleanup_status` varchar(16) NOT NULL DEFAULT 'NONE' COMMENT 'ES残留清理状态',
    `es_cleanup_token` varchar(64) DEFAULT NULL COMMENT 'ES清理执行令牌',
    `es_cleanup_expires_at` BIGINT DEFAULT NULL COMMENT 'ES清理租约过期时间',
    `fastgpt_cleanup_status` varchar(16) NOT NULL DEFAULT 'NONE' COMMENT 'FastGPT残留清理状态',
    `fastgpt_cleanup_token` varchar(64) DEFAULT NULL COMMENT 'FastGPT清理执行令牌',
    `fastgpt_cleanup_expires_at` BIGINT DEFAULT NULL COMMENT 'FastGPT清理租约过期时间',
    `detail_json` json DEFAULT NULL COMMENT '端侧响应和清理诊断明细',
    `requested_at` BIGINT NOT NULL COMMENT '发起时间',
    `started_at` BIGINT DEFAULT NULL COMMENT '开始时间',
    `finished_at` BIGINT DEFAULT NULL COMMENT '完成时间',
    `failure_reason` varchar(1024) DEFAULT NULL COMMENT '失败原因',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_classics_publication_job_content` (`content_type`, `content_id`),
    KEY `idx_classics_publication_job_dispatch` (`job_result_status`, `job_status`, `expires_at`),
    KEY `idx_classics_publication_job_retry` (`job_result_status`, `job_status`, `next_retry_at`),
    KEY `idx_classics_publication_job_result` (`job_type`, `job_result_status`),
    KEY `idx_classics_publication_job_es_cleanup` (`es_cleanup_status`, `es_cleanup_expires_at`),
    KEY `idx_classics_publication_job_fastgpt_cleanup` (`fastgpt_cleanup_status`, `fastgpt_cleanup_expires_at`),
    KEY `idx_classics_publication_job_deleted` (`content_deleted_at`),
    KEY `idx_classics_publication_job_requested` (`requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容发布与下线任务表';

CREATE TABLE IF NOT EXISTS `classics_content_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `export_kind` varchar(32) NOT NULL COMMENT '导出类型',
    `content_type` varchar(32) NOT NULL COMMENT '内容范围类型',
    `export_format` varchar(16) NOT NULL COMMENT '导出格式',
    `scope_type` varchar(32) NOT NULL COMMENT '范围类型',
    `scope_json` json NOT NULL COMMENT '范围快照',
    `requested_at` BIGINT NOT NULL COMMENT '请求时间',
    `expires_at` BIGINT NOT NULL COMMENT '过期时间',
    `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `storage_object_id` bigint DEFAULT NULL COMMENT '导出产物Storage对象ID',
    `item_count` int NOT NULL DEFAULT 0 COMMENT '内容数量',
    `asset_count` int NOT NULL DEFAULT 0 COMMENT '资产数量',
    `lifecycle_risk_status` varchar(16) NOT NULL DEFAULT 'PUBLISHED_ONLY' COMMENT '非发布内容风险状态',
    `content_changed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '导出后内容是否可能已变更',
    PRIMARY KEY (`id`),
    KEY `idx_classics_content_export_status` (`status`, `expires_at`),
    KEY `idx_classics_content_export_type` (`content_type`, `export_kind`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容导出任务表';
