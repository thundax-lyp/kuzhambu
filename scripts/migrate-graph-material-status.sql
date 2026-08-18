ALTER TABLE knowledge_graph_material
    ADD COLUMN failure_reason varchar(1024) DEFAULT NULL AFTER published_at,
    ADD COLUMN failed_operation varchar(16) DEFAULT NULL AFTER failure_reason;

UPDATE knowledge_graph_material material
SET status = 'READY'
WHERE status = 'DRAFT'
  AND EXISTS (
      SELECT 1
      FROM knowledge_graph_material_node node
      WHERE node.material_id = material.id
  );
