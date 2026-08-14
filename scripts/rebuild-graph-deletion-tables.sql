DELIMITER //

CREATE PROCEDURE kuzhambu_rebuild_graph_deletion_tables()
BEGIN
    DECLARE deletion_change_count BIGINT DEFAULT 0;
    DECLARE deletion_task_count BIGINT DEFAULT 0;

    SELECT COUNT(*) INTO deletion_change_count FROM knowledge_graph_material_deletion_change;
    SELECT COUNT(*) INTO deletion_task_count FROM knowledge_graph_material_deletion_task;

    IF deletion_change_count <> 0 OR deletion_task_count <> 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'knowledge graph deletion tables are not empty; stop rebuild';
    END IF;

    DROP TABLE IF EXISTS knowledge_graph_material_deletion_task;
    DROP TABLE IF EXISTS knowledge_graph_material_deletion_change;

    CREATE TABLE IF NOT EXISTS `knowledge_graph_material_deletion_change` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `material_id` bigint NOT NULL,
        `content_type` varchar(32) NOT NULL,
        `content_ref_id` bigint NOT NULL,
        `material_snapshot_json` json NOT NULL,
        `decision` varchar(32) DEFAULT NULL,
        `status` varchar(32) NOT NULL,
        `lock_version` bigint NOT NULL DEFAULT 0,
        `result_summary_json` json DEFAULT NULL,
        `requested_at` BIGINT NOT NULL,
        `completed_at` BIGINT DEFAULT NULL,
        PRIMARY KEY (`id`),
        KEY `idx_knowledge_graph_deletion_change_material_status` (`material_id`, `status`, `requested_at`),
        KEY `idx_knowledge_graph_deletion_change_content` (`content_type`, `content_ref_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除变更快照';

    CREATE TABLE IF NOT EXISTS `knowledge_graph_material_deletion_task` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `deletion_change_id` bigint NOT NULL,
        `idempotency_key` varchar(128) NOT NULL,
        `status` varchar(32) NOT NULL,
        `lock_version` bigint NOT NULL DEFAULT 0,
        `progress` int NOT NULL DEFAULT 0,
        `failure_reason` varchar(1024) DEFAULT NULL,
        `result_summary_json` json DEFAULT NULL,
        `requested_at` BIGINT NOT NULL,
        `completed_at` BIGINT DEFAULT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `uk_knowledge_graph_deletion_task_idempotency` (`idempotency_key`),
        KEY `idx_knowledge_graph_deletion_task_status_requested` (`status`, `requested_at`),
        KEY `idx_knowledge_graph_deletion_task_change` (`deletion_change_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除后台任务';
END//

DELIMITER ;

CALL kuzhambu_rebuild_graph_deletion_tables();
DROP PROCEDURE kuzhambu_rebuild_graph_deletion_tables;
