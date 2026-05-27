SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `storage_object` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `extend_name` varchar(64) DEFAULT NULL,
    `mime_type` varchar(128) DEFAULT NULL,
    `owner_id` varchar(64) DEFAULT NULL,
    `owner_type` varchar(64) DEFAULT NULL,
    `bucket_name` varchar(128) DEFAULT NULL,
    `object_key` varchar(512) NOT NULL,
    `size` bigint NOT NULL DEFAULT 0,
    `access_endpoint` varchar(1024) DEFAULT NULL,
    `object_status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `reference_status` varchar(16) NOT NULL DEFAULT 'UNREFERENCED',
    `priority` int NOT NULL DEFAULT 0,
    `remarks` varchar(512) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_storage_object_key` (`bucket_name`, `object_key`),
    KEY `idx_storage_object_status` (`object_status`, `reference_status`),
    KEY `idx_storage_object_mime_type` (`mime_type`),
    KEY `idx_storage_object_owner` (`owner_type`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储对象表';

CREATE TABLE IF NOT EXISTS `storage_object_reference` (
    `object_id` bigint NOT NULL,
    `reference_owner_type` varchar(64) NOT NULL,
    `reference_owner_id` varchar(64) NOT NULL,
    `business_params` text DEFAULT NULL,
    `reference_status` varchar(16) NOT NULL DEFAULT 'REFERENCED',
    PRIMARY KEY (`object_id`, `reference_owner_type`, `reference_owner_id`),
    KEY `idx_storage_object_reference_owner` (`reference_owner_type`, `reference_owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储对象引用关系表';

CREATE TABLE IF NOT EXISTS `storage_multipart_upload` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `upload_id` varchar(64) NOT NULL,
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
    `completed_date` datetime(3) DEFAULT NULL,
    `aborted_date` datetime(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_storage_multipart_upload_id` (`upload_id`),
    KEY `idx_storage_multipart_upload_owner` (`owner_type`, `owner_id`, `upload_status`),
    KEY `idx_storage_multipart_upload_object_key` (`bucket_name`, `object_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传会话表';

CREATE TABLE IF NOT EXISTS `storage_multipart_upload_part` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `upload_id` varchar(64) NOT NULL,
    `part_number` int NOT NULL,
    `etag` varchar(255) NOT NULL,
    `size` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_storage_multipart_upload_part` (`upload_id`, `part_number`),
    KEY `idx_storage_multipart_upload_part_upload` (`upload_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传分片记录表';
