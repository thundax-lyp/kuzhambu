-- Generated from db/schema/*.sql and db/data/*.sql.


-- db/schema/sys.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` char(26) NOT NULL,
    `name` varchar(128) NOT NULL,
    `email` varchar(512) DEFAULT NULL,
    `mobile` varchar(512) DEFAULT NULL,
    `tel` varchar(64) DEFAULT NULL,
    `avatar_object_id` char(26) DEFAULT NULL,
    `rank` int NOT NULL DEFAULT 0,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `remarks` varchar(512) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_user_id` (`user_id`),
    KEY `idx_sys_user_status` (`status`, `id`),
    KEY `idx_sys_user_privilege` (`privilege`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台用户主体表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `role_id` char(26) NOT NULL,
    `name` varchar(128) NOT NULL,
    `privilege` varchar(16) NOT NULL DEFAULT 'NORMAL',
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_role_id` (`role_id`),
    KEY `idx_sys_role_status` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `menu_id` char(26) NOT NULL,
    `parent_menu_id` char(26) DEFAULT NULL,
    `name` varchar(128) NOT NULL,
    `permission_codes` varchar(512) DEFAULT NULL,
    `rank` int NOT NULL DEFAULT 0,
    `visibility` varchar(16) NOT NULL DEFAULT 'VISIBLE',
    `display_params` text DEFAULT NULL,
    `path` varchar(512) DEFAULT NULL,
    `target` varchar(64) DEFAULT NULL,
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_menu_menu_id` (`menu_id`),
    KEY `idx_sys_menu_parent` (`parent_menu_id`, `priority`),
    KEY `idx_sys_menu_visibility` (`visibility`, `rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台菜单和权限资源表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id` char(26) NOT NULL,
    `role_id` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id` char(26) NOT NULL,
    `menu_id` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`role_id`, `menu_id`),
    KEY `idx_sys_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关系表';


-- db/schema/auth.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `auth_principal_identity` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `identity_id` char(26) NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` char(26) NOT NULL,
    `identity_type` varchar(32) NOT NULL,
    `identity_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ENABLED',
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_identity_id` (`identity_id`),
    UNIQUE KEY `uk_auth_principal_identity_type_value` (`identity_type`, `identity_value`),
    KEY `idx_auth_principal_identity_principal` (`principal_type`, `principal_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证主体登录标识表';

CREATE TABLE IF NOT EXISTS `auth_principal_credential` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `credential_id` char(26) NOT NULL,
    `principal_type` varchar(32) NOT NULL,
    `principal_id` char(26) NOT NULL,
    `identity_id` char(26) NOT NULL,
    `credential_type` varchar(32) NOT NULL,
    `credential_value` varchar(255) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `need_change_password` tinyint(1) NOT NULL DEFAULT 0,
    `failed_count` int NOT NULL DEFAULT 0,
    `failed_limit` int NOT NULL DEFAULT 5,
    `locked_until` datetime(3) DEFAULT NULL,
    `expires_at` datetime(3) DEFAULT NULL,
    `last_verified_at` datetime(3) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_credential_id` (`credential_id`),
    UNIQUE KEY `uk_auth_principal_credential_identity_type` (`identity_id`, `credential_type`),
    KEY `idx_auth_principal_credential_principal` (`principal_type`, `principal_id`, `status`),
    KEY `idx_auth_principal_credential_locked` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证主体凭据表';

CREATE TABLE IF NOT EXISTS `auth_principal_login_event` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `event_id` char(26) NOT NULL,
    `principal_type` varchar(32) DEFAULT NULL,
    `principal_id` char(26) DEFAULT NULL,
    `client_id` varchar(64) NOT NULL,
    `event_type` varchar(32) NOT NULL,
    `authentication_method` varchar(32) NOT NULL,
    `identity_type` varchar(32) DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    `ip` varchar(64) DEFAULT NULL,
    `user_agent` varchar(512) DEFAULT NULL,
    `reason` varchar(128) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_principal_login_event_id` (`event_id`),
    KEY `idx_auth_principal_login_event_principal_time` (`principal_type`, `principal_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_client_time` (`client_id`, `occurred_at`),
    KEY `idx_auth_principal_login_event_type_time` (`event_type`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证事件表';


-- db/schema/audit.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_meta` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `meta_id` char(26) NOT NULL,
    `object_type` varchar(128) NOT NULL,
    `object_id` varchar(64) NOT NULL,
    `version` bigint NOT NULL,
    `last_log_id` char(26) NOT NULL,
    `last_action` varchar(32) NOT NULL,
    `last_operator_type` varchar(32) NOT NULL,
    `last_operator_id` varchar(64) DEFAULT NULL,
    `last_operator_name` varchar(128) DEFAULT NULL,
    `last_operated_at` datetime(3) NOT NULL,
    `created_log_id` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_audit_meta_meta_id` (`meta_id`),
    UNIQUE KEY `uk_audit_meta_object` (`object_type`, `object_id`),
    KEY `idx_audit_meta_last_operated` (`last_operated_at`),
    KEY `idx_audit_meta_last_operator` (`last_operator_type`, `last_operator_id`, `last_operated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务对象审计元数据表';

CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `log_id` char(26) NOT NULL,
    `meta_id` char(26) NOT NULL,
    `object_type` varchar(128) NOT NULL,
    `object_id` varchar(64) NOT NULL,
    `version` bigint NOT NULL,
    `previous_version` bigint NOT NULL,
    `action` varchar(32) NOT NULL,
    `idempotency_key` varchar(128) NOT NULL,
    `operator_type` varchar(32) NOT NULL,
    `operator_id` varchar(64) DEFAULT NULL,
    `operator_name` varchar(128) DEFAULT NULL,
    `source` varchar(64) NOT NULL,
    `request_id` varchar(128) DEFAULT NULL,
    `trace_id` varchar(128) DEFAULT NULL,
    `remote_addr` varchar(64) DEFAULT NULL,
    `summary` varchar(512) DEFAULT NULL,
    `snapshot_schema_version` int NOT NULL DEFAULT 1,
    `before_snapshot` longtext DEFAULT NULL,
    `after_snapshot` longtext DEFAULT NULL,
    `changed_fields` longtext DEFAULT NULL,
    `occurred_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_audit_log_log_id` (`log_id`),
    UNIQUE KEY `uk_audit_log_idempotency` (`idempotency_key`),
    KEY `idx_audit_log_object` (`object_type`, `object_id`, `occurred_at`),
    KEY `idx_audit_log_meta_version` (`meta_id`, `version`),
    KEY `idx_audit_log_operator` (`operator_type`, `operator_id`, `occurred_at`),
    KEY `idx_audit_log_action` (`action`, `occurred_at`),
    KEY `idx_audit_log_request` (`request_id`),
    KEY `idx_audit_log_trace` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务对象审计日志表';


-- db/schema/storage.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `storage_object` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `object_id` char(26) NOT NULL,
    `name` varchar(255) NOT NULL,
    `extend_name` varchar(64) DEFAULT NULL,
    `mime_type` varchar(128) DEFAULT NULL,
    `owner_id` varchar(64) DEFAULT NULL,
    `owner_type` varchar(64) DEFAULT NULL,
    `bucket_name` varchar(128) DEFAULT NULL,
    `object_key` varchar(512) NOT NULL,
    `original_filename` varchar(255) DEFAULT NULL,
    `content_type` varchar(128) DEFAULT NULL,
    `size` bigint NOT NULL DEFAULT 0,
    `access_endpoint` varchar(1024) DEFAULT NULL,
    `object_status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `reference_status` varchar(16) NOT NULL DEFAULT 'UNREFERENCED',
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_storage_object_id` (`object_id`),
    UNIQUE KEY `uk_storage_object_key` (`bucket_name`, `object_key`),
    KEY `idx_storage_object_status` (`object_status`, `reference_status`),
    KEY `idx_storage_object_mime_type` (`mime_type`),
    KEY `idx_storage_object_owner` (`owner_type`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储对象表';

CREATE TABLE IF NOT EXISTS `storage_object_reference` (
    `object_id` char(26) NOT NULL,
    `reference_owner_type` varchar(64) NOT NULL,
    `reference_owner_id` varchar(64) NOT NULL,
    `owner_params` text DEFAULT NULL,
    `reference_status` varchar(16) NOT NULL DEFAULT 'REFERENCED',
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`object_id`, `reference_owner_type`, `reference_owner_id`),
    KEY `idx_storage_object_reference_owner` (`reference_owner_type`, `reference_owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储对象引用关系表';

CREATE TABLE IF NOT EXISTS `storage_multipart_upload` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `upload_id` char(26) NOT NULL,
    `owner_id` varchar(64) NOT NULL,
    `owner_type` varchar(64) NOT NULL,
    `business_type` varchar(64) DEFAULT NULL,
    `original_filename` varchar(255) NOT NULL,
    `mime_type` varchar(128) NOT NULL,
    `bucket_name` varchar(128) DEFAULT NULL,
    `object_key` varchar(512) NOT NULL,
    `provider_upload_id` varchar(255) DEFAULT NULL,
    `total_size` bigint NOT NULL,
    `part_size` bigint NOT NULL,
    `uploaded_part_count` int NOT NULL DEFAULT 0,
    `upload_status` varchar(16) NOT NULL DEFAULT 'INITIATED',
    `completed_at` datetime(3) DEFAULT NULL,
    `aborted_at` datetime(3) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_storage_multipart_upload_id` (`upload_id`),
    KEY `idx_storage_multipart_upload_owner` (`owner_type`, `owner_id`, `upload_status`),
    KEY `idx_storage_multipart_upload_object_key` (`bucket_name`, `object_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传会话表';

CREATE TABLE IF NOT EXISTS `storage_multipart_upload_part` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `upload_id` char(26) NOT NULL,
    `part_number` int NOT NULL,
    `etag` varchar(255) NOT NULL,
    `size` bigint NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_storage_multipart_upload_part` (`upload_id`, `part_number`),
    KEY `idx_storage_multipart_upload_part_upload` (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传分片记录表';


-- db/schema/taxonomy.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `taxonomy_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `category_id` char(26) NOT NULL,
    `name` varchar(128) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `sort_order` int NOT NULL DEFAULT 0,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_category_id` (`category_id`),
    UNIQUE KEY `uk_taxonomy_category_name` (`name`),
    KEY `idx_taxonomy_category_sort` (`enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签分类表';

CREATE TABLE IF NOT EXISTS `taxonomy_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `tag_id` char(26) NOT NULL,
    `tag_name` varchar(128) NOT NULL,
    `category_id` char(26) DEFAULT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    `source` varchar(32) NOT NULL DEFAULT 'AI_EXTRACTED',
    `merge_target_tag_id` char(26) DEFAULT NULL,
    `created_by` char(26) DEFAULT NULL,
    `reviewed_by` char(26) DEFAULT NULL,
    `reviewed_at` datetime(3) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_tag_id` (`tag_id`),
    UNIQUE KEY `uk_taxonomy_tag_name` (`tag_name`),
    KEY `idx_taxonomy_tag_category` (`category_id`, `status`),
    KEY `idx_taxonomy_tag_status` (`status`, `source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一标签表';

CREATE TABLE IF NOT EXISTS `taxonomy_tag_alias` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `alias_id` char(26) NOT NULL,
    `tag_id` char(26) NOT NULL,
    `alias_name` varchar(128) NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_tag_alias_id` (`alias_id`),
    UNIQUE KEY `uk_taxonomy_tag_alias_name` (`alias_name`),
    KEY `idx_taxonomy_tag_alias_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签别名表';

CREATE TABLE IF NOT EXISTS `taxonomy_content_tag_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `relation_id` char(26) NOT NULL,
    `content_type` varchar(32) NOT NULL,
    `content_id` char(26) NOT NULL,
    `tag_id` char(26) NOT NULL,
    `source` varchar(32) NOT NULL DEFAULT 'MANUAL',
    `created_by` char(26) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_content_tag_relation_id` (`relation_id`),
    UNIQUE KEY `uk_taxonomy_content_tag_relation` (`content_type`, `content_id`, `tag_id`),
    KEY `idx_taxonomy_content_tag_content` (`content_type`, `content_id`),
    KEY `idx_taxonomy_content_tag_tag` (`tag_id`, `content_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签关联表';

CREATE TABLE IF NOT EXISTS `taxonomy_synonym` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `synonym_id` char(26) NOT NULL,
    `term` varchar(128) NOT NULL,
    `synonym` varchar(128) NOT NULL,
    `enabled` tinyint(1) NOT NULL DEFAULT 1,
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_synonym_id` (`synonym_id`),
    UNIQUE KEY `uk_taxonomy_synonym_pair` (`term`, `synonym`),
    KEY `idx_taxonomy_synonym_term` (`term`, `enabled`),
    KEY `idx_taxonomy_synonym_reverse` (`synonym`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同义词表';

CREATE TABLE IF NOT EXISTS `taxonomy_operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `operation_id` char(26) NOT NULL,
    `operation_type` varchar(32) NOT NULL,
    `tag_id` char(26) NOT NULL,
    `target_tag_id` char(26) DEFAULT NULL,
    `detail_json` text DEFAULT NULL,
    `operator_user_id` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_taxonomy_operation_log_id` (`operation_id`),
    KEY `idx_taxonomy_operation_log_tag` (`tag_id`, `created_at`),
    KEY `idx_taxonomy_operation_log_operator` (`operator_user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签治理操作日志表';


-- db/schema/sancai.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sancai_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `category_code` varchar(16) NOT NULL,
    `name` varchar(64) NOT NULL,
    `formal` tinyint(1) NOT NULL DEFAULT 1,
    `sort_order` int NOT NULL DEFAULT 0,
    `description` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_category_code` (`category_code`),
    KEY `idx_sancai_category_sort` (`formal`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会门类表';

CREATE TABLE IF NOT EXISTS `sancai_volume` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `volume_id` char(26) NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `volume_no` int DEFAULT NULL,
    `title` varchar(128) NOT NULL,
    `auxiliary` tinyint(1) NOT NULL DEFAULT 0,
    `sort_order` int NOT NULL DEFAULT 0,
    `entry_count` int NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_volume_id` (`volume_id`),
    UNIQUE KEY `uk_sancai_volume_category_no` (`category_code`, `volume_no`),
    KEY `idx_sancai_volume_category_sort` (`category_code`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会卷表';

CREATE TABLE IF NOT EXISTS `sancai_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `entry_id` char(26) NOT NULL,
    `category_code` varchar(16) NOT NULL,
    `volume_id` char(26) NOT NULL,
    `entry_no` int DEFAULT NULL,
    `title` varchar(255) NOT NULL,
    `original_text` longtext DEFAULT NULL,
    `translation_text` longtext DEFAULT NULL,
    `summary` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `lifecycle_status` varchar(16) NOT NULL DEFAULT 'DRAFT',
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` char(26) NOT NULL,
    `translation_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `image_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `visual_asset_status` varchar(16) NOT NULL DEFAULT 'MISSING',
    `refinement_status` varchar(16) NOT NULL DEFAULT 'RAW',
    `current_version` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_id` (`entry_id`),
    KEY `idx_sancai_entry_volume` (`volume_id`, `entry_no`),
    KEY `idx_sancai_entry_category_status` (`category_code`, `lifecycle_status`, `visibility`),
    KEY `idx_sancai_entry_updated` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目表';

CREATE TABLE IF NOT EXISTS `sancai_entry_image` (
    `entry_id` char(26) NOT NULL,
    `object_id` char(26) NOT NULL,
    `image_role` varchar(16) NOT NULL,
    `current_used` tinyint(1) NOT NULL DEFAULT 0,
    `sort_order` int NOT NULL DEFAULT 0,
    `caption` varchar(512) DEFAULT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`entry_id`, `object_id`),
    KEY `idx_sancai_entry_image_object` (`object_id`),
    KEY `idx_sancai_entry_image_sort` (`entry_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目图片引用表';

CREATE TABLE IF NOT EXISTS `sancai_entry_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `qa_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_qa_id` (`qa_id`),
    KEY `idx_sancai_entry_qa_entry_sort` (`entry_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目问答对表';

CREATE TABLE IF NOT EXISTS `sancai_entry_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_version_id` (`version_id`),
    UNIQUE KEY `uk_sancai_entry_version_no` (`entry_id`, `version_no`),
    KEY `idx_sancai_entry_version_entry_time` (`entry_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目版本表';

CREATE TABLE IF NOT EXISTS `sancai_entry_draft` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `draft_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `user_id` char(26) NOT NULL,
    `draft_json` longtext NOT NULL,
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_entry_draft_id` (`draft_id`),
    UNIQUE KEY `uk_sancai_entry_draft_user_entry` (`entry_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会条目草稿表';

CREATE TABLE IF NOT EXISTS `sancai_visual_asset` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `asset_id` char(26) NOT NULL,
    `entry_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `source_object_id` char(26) DEFAULT NULL,
    `generated_object_id` char(26) DEFAULT NULL,
    `image_analysis` longtext DEFAULT NULL,
    `fusion_description` longtext DEFAULT NULL,
    `visual_description` longtext DEFAULT NULL,
    `text_weight` int NOT NULL DEFAULT 50,
    `image_weight` int NOT NULL DEFAULT 50,
    `generation_params` text DEFAULT NULL,
    `current_used` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_visual_asset_id` (`asset_id`),
    UNIQUE KEY `uk_sancai_visual_asset_version` (`entry_id`, `version_no`),
    KEY `idx_sancai_visual_asset_entry_current` (`entry_id`, `current_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会视觉资产表';

CREATE TABLE IF NOT EXISTS `sancai_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` char(26) NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `entry_count` int NOT NULL DEFAULT 0,
    `asset_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_export_job_id` (`export_id`),
    KEY `idx_sancai_export_job_creator` (`created_by`, `created_at`),
    KEY `idx_sancai_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会导出产物表';

CREATE TABLE IF NOT EXISTS `sancai_showcase_page` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `showcase_id` char(26) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) NOT NULL,
    `entry_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sancai_showcase_page_id` (`showcase_id`),
    KEY `idx_sancai_showcase_page_creator` (`created_by`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三才图会静态展示页面表';


-- db/schema/wangqi.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `wangqi_document` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `document_id` char(26) NOT NULL,
    `title` varchar(255) NOT NULL,
    `content` longtext DEFAULT NULL,
    `content_format` varchar(16) NOT NULL DEFAULT 'MARKDOWN',
    `summary` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `file_object_id` char(26) DEFAULT NULL,
    `word_count` int NOT NULL DEFAULT 0,
    `document_time` datetime(3) DEFAULT NULL,
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` char(26) NOT NULL,
    `current_version` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_document_id` (`document_id`),
    KEY `idx_wangqi_document_visibility` (`visibility`, `deleted_at`),
    KEY `idx_wangqi_document_time` (`document_time`, `created_at`),
    KEY `idx_wangqi_document_updated` (`updated_at`),
    KEY `idx_wangqi_document_file` (`file_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档表';

CREATE TABLE IF NOT EXISTS `wangqi_document_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `qa_id` char(26) NOT NULL,
    `document_id` char(26) NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_document_qa_id` (`qa_id`),
    KEY `idx_wangqi_document_qa_document_sort` (`document_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档问答对表';

CREATE TABLE IF NOT EXISTS `wangqi_document_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` char(26) NOT NULL,
    `document_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_document_version_id` (`version_id`),
    UNIQUE KEY `uk_wangqi_document_version_no` (`document_id`, `version_no`),
    KEY `idx_wangqi_document_version_time` (`document_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻文档版本表';

CREATE TABLE IF NOT EXISTS `wangqi_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` char(26) NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `document_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wangqi_export_job_id` (`export_id`),
    KEY `idx_wangqi_export_job_creator` (`created_by`, `created_at`),
    KEY `idx_wangqi_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='王圻导出产物表';


-- db/schema/mingcustoms.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ming_customs_entry` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `custom_id` char(26) NOT NULL,
    `title` varchar(255) NOT NULL,
    `summary` text DEFAULT NULL,
    `content` longtext DEFAULT NULL,
    `category` varchar(128) DEFAULT NULL,
    `chapter` varchar(128) DEFAULT NULL,
    `section` varchar(128) DEFAULT NULL,
    `keywords_snapshot` text DEFAULT NULL,
    `tags_snapshot` text DEFAULT NULL,
    `original_excerpts` longtext DEFAULT NULL,
    `word_count` int NOT NULL DEFAULT 0,
    `visibility` varchar(16) NOT NULL DEFAULT 'PUBLIC',
    `owner_user_id` char(26) NOT NULL,
    `current_version` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    `deleted_at` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_entry_id` (`custom_id`),
    KEY `idx_ming_customs_entry_category` (`category`, `visibility`, `deleted_at`),
    KEY `idx_ming_customs_entry_updated` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗条目表';

CREATE TABLE IF NOT EXISTS `ming_customs_qa` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `qa_id` char(26) NOT NULL,
    `custom_id` char(26) NOT NULL,
    `question` text NOT NULL,
    `answer` longtext NOT NULL,
    `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
    `sort_order` int NOT NULL DEFAULT 0,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_qa_id` (`qa_id`),
    KEY `idx_ming_customs_qa_custom_sort` (`custom_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗问答对表';

CREATE TABLE IF NOT EXISTS `ming_customs_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version_id` char(26) NOT NULL,
    `custom_id` char(26) NOT NULL,
    `version_no` int NOT NULL,
    `snapshot_json` longtext NOT NULL,
    `change_type` varchar(32) NOT NULL,
    `change_summary` varchar(512) DEFAULT NULL,
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_version_id` (`version_id`),
    UNIQUE KEY `uk_ming_customs_version_no` (`custom_id`, `version_no`),
    KEY `idx_ming_customs_version_time` (`custom_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗版本表';

CREATE TABLE IF NOT EXISTS `ming_customs_export_job` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `export_id` char(26) NOT NULL,
    `export_type` varchar(32) NOT NULL,
    `scope_type` varchar(32) NOT NULL,
    `scope_json` text NOT NULL,
    `object_id` char(26) DEFAULT NULL,
    `custom_count` int NOT NULL DEFAULT 0,
    `contains_private` tinyint(1) NOT NULL DEFAULT 0,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `created_by` char(26) NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `expires_at` datetime(3) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ming_customs_export_job_id` (`export_id`),
    KEY `idx_ming_customs_export_job_creator` (`created_by`, `created_at`),
    KEY `idx_ming_customs_export_job_status` (`status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明代习俗导出产物表';


-- db/data/sys.sql
SET NAMES utf8mb4;

INSERT INTO `sys_user` (
    `user_id`, `name`, `email`, `mobile`, `tel`, `avatar_object_id`, `rank`,
    `privilege`, `status`, `remarks`
) VALUES (
    '01KUZHAMBU00000000000001',
    '系统管理员',
    NULL,
    NULL,
    NULL,
    NULL,
    9,
    'SUPER',
    'ENABLED',
    '系统初始化管理员'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `rank` = VALUES(`rank`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `sys_role` (
    `role_id`, `name`, `privilege`, `status`, `priority`, `remarks`
) VALUES (
    '01KUZHAMBU00000000000002',
    '超级管理员',
    'SUPER',
    'ENABLED',
    1,
    '拥有全部后台管理权限'
) ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `privilege` = VALUES(`privilege`),
    `status` = VALUES(`status`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);

INSERT INTO `sys_menu` (
    `menu_id`, `parent_menu_id`, `name`, `permission_codes`, `rank`,
    `visibility`, `display_params`, `path`, `target`, `priority`, `remarks`
) VALUES
    (
        '01KUZHAMBU00000000000003', NULL, '系统管理', 'sys:*', 9,
        'VISIBLE', '{"icon":"settings"}', '/system', '_self', 10, '系统管理根菜单'
    ),
    (
        '01KUZHAMBU00000000000004', '01KUZHAMBU00000000000003', '用户管理',
        'sys:user:view,sys:user:create,sys:user:update,sys:user:disable,sys:user:delete',
        9, 'VISIBLE', '{"icon":"user"}', '/system/users', '_self', 20, '后台用户管理'
    ),
    (
        '01KUZHAMBU00000000000005', '01KUZHAMBU00000000000003', '角色管理',
        'sys:role:view,sys:role:create,sys:role:update,sys:role:delete,sys:role:authorize',
        9, 'VISIBLE', '{"icon":"shield"}', '/system/roles', '_self', 30, '后台角色管理'
    ),
    (
        '01KUZHAMBU00000000000006', '01KUZHAMBU00000000000003', '菜单管理',
        'sys:menu:view,sys:menu:create,sys:menu:update,sys:menu:delete',
        9, 'VISIBLE', '{"icon":"menu"}', '/system/menus', '_self', 40, '后台菜单管理'
    )
ON DUPLICATE KEY UPDATE
    `parent_menu_id` = VALUES(`parent_menu_id`),
    `name` = VALUES(`name`),
    `permission_codes` = VALUES(`permission_codes`),
    `rank` = VALUES(`rank`),
    `visibility` = VALUES(`visibility`),
    `display_params` = VALUES(`display_params`),
    `path` = VALUES(`path`),
    `target` = VALUES(`target`),
    `priority` = VALUES(`priority`),
    `remarks` = VALUES(`remarks`);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES
    ('01KUZHAMBU00000000000001', '01KUZHAMBU00000000000002');

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000003'),
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000004'),
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000005'),
    ('01KUZHAMBU00000000000002', '01KUZHAMBU00000000000006');


-- db/data/auth.sql
SET NAMES utf8mb4;

-- Initial admin account:
--   login name: admin
--   password credential value is a placeholder and must be rotated before production use.

INSERT INTO `auth_principal_identity` (
    `identity_id`, `principal_type`, `principal_id`, `identity_type`,
    `identity_value`, `status`
) VALUES (
    '01KUZHAMBU00000000000007',
    'USER',
    '01KUZHAMBU00000000000001',
    'USER_ACCOUNT',
    'admin',
    'ENABLED'
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `auth_principal_credential` (
    `credential_id`, `principal_type`, `principal_id`, `identity_id`,
    `credential_type`, `credential_value`, `status`, `need_change_password`,
    `failed_count`, `failed_limit`
) VALUES (
    '01KUZHAMBU00000000000008',
    'USER',
    '01KUZHAMBU00000000000001',
    '01KUZHAMBU00000000000007',
    'USER_PASSWORD',
    '{noop}admin',
    'ACTIVE',
    1,
    0,
    5
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`);


-- db/data/audit.sql
SET NAMES utf8mb4;

-- Audit has no required seed data.


-- db/data/storage.sql
SET NAMES utf8mb4;

-- Storage has no required seed data.


-- db/data/taxonomy.sql
SET NAMES utf8mb4;

-- Taxonomy has no required seed data.


-- db/data/sancai.sql
SET NAMES utf8mb4;

INSERT INTO `sancai_category` (
    `category_code`, `name`, `formal`, `sort_order`, `description`
) VALUES
    ('js', '卷首', 0, 0, '序、凡例等卷首辅助内容'),
    ('tw', '天文', 1, 10, '三才图会正式门类'),
    ('dl', '地理', 1, 20, '三才图会正式门类'),
    ('rw', '人物', 1, 30, '三才图会正式门类'),
    ('sl', '时令', 1, 40, '三才图会正式门类'),
    ('gs', '宫室', 1, 50, '三才图会正式门类'),
    ('qy', '器用', 1, 60, '三才图会正式门类'),
    ('st', '身体', 1, 70, '三才图会正式门类'),
    ('yf', '衣服', 1, 80, '三才图会正式门类'),
    ('rs', '人事', 1, 90, '三才图会正式门类'),
    ('yz', '仪制', 1, 100, '三才图会正式门类'),
    ('zb', '珍宝', 1, 110, '三才图会正式门类'),
    ('ws', '文史', 1, 120, '三才图会正式门类'),
    ('ns', '鸟兽', 1, 130, '三才图会正式门类'),
    ('cm', '草木', 1, 140, '三才图会正式门类')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `formal` = VALUES(`formal`),
    `sort_order` = VALUES(`sort_order`),
    `description` = VALUES(`description`);

INSERT INTO `sancai_volume` (
    `volume_id`, `category_code`, `volume_no`, `title`, `auxiliary`, `sort_order`, `entry_count`
) VALUES (
    '01KUZHAMBU00000000010001',
    'js',
    NULL,
    '卷首',
    1,
    0,
    0
) ON DUPLICATE KEY UPDATE
    `category_code` = VALUES(`category_code`),
    `volume_no` = VALUES(`volume_no`),
    `title` = VALUES(`title`),
    `auxiliary` = VALUES(`auxiliary`),
    `sort_order` = VALUES(`sort_order`),
    `entry_count` = VALUES(`entry_count`);


-- db/data/wangqi.sql
SET NAMES utf8mb4;

-- Wangqi has no required seed data.


-- db/data/mingcustoms.sql
SET NAMES utf8mb4;

-- Ming Customs has no required seed data.

