DELIMITER //

CREATE PROCEDURE kuzhambu_rebuild_graph_governance_audit_tables()
BEGIN
    DECLARE governance_operation_count BIGINT DEFAULT 0;
    DECLARE manual_source_count BIGINT DEFAULT 0;

    SELECT COUNT(*) INTO governance_operation_count FROM knowledge_graph_governance_operation;
    SELECT COUNT(*) INTO manual_source_count FROM knowledge_graph_manual_source;

    IF governance_operation_count <> 0 OR manual_source_count <> 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'knowledge graph governance audit tables are not empty; stop rebuild';
    END IF;

    DROP TABLE IF EXISTS knowledge_graph_manual_source;
    DROP TABLE IF EXISTS knowledge_graph_governance_operation;

    CREATE TABLE IF NOT EXISTS `knowledge_graph_governance_operation` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `operation_type` varchar(32) NOT NULL,
        `target_type` varchar(32) NOT NULL,
        `target_id` bigint NOT NULL,
        `before_snapshot_json` json DEFAULT NULL,
        `after_snapshot_json` json DEFAULT NULL,
        `reason` varchar(1024) NOT NULL,
        `audit_log_id` bigint NOT NULL,
        `operated_at` BIGINT NOT NULL,
        PRIMARY KEY (`id`),
        KEY `idx_knowledge_graph_governance_operation_target` (`target_type`, `target_id`, `operated_at`),
        KEY `idx_knowledge_graph_governance_operation_type_time` (`operation_type`, `operated_at`),
        KEY `idx_knowledge_graph_governance_operation_audit` (`audit_log_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布空间治理操作记录';

    CREATE TABLE IF NOT EXISTS `knowledge_graph_manual_source` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `target_type` varchar(32) NOT NULL,
        `target_id` bigint NOT NULL,
        `reason` varchar(1024) NOT NULL,
        `audit_log_id` bigint NOT NULL,
        `recorded_at` BIGINT NOT NULL,
        PRIMARY KEY (`id`),
        KEY `idx_knowledge_graph_manual_source_target` (`target_type`, `target_id`),
        KEY `idx_knowledge_graph_manual_source_audit` (`audit_log_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布空间人工来源';
END//

DELIMITER ;

CALL kuzhambu_rebuild_graph_governance_audit_tables();
DROP PROCEDURE kuzhambu_rebuild_graph_governance_audit_tables;
