package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphExtractionTaskMapper extends BaseMapper<GraphExtractionTaskDO> {

    @Select(
            "select * from knowledge_graph_extraction_task where idempotency_key = #{idempotencyKey} order by id asc limit 1")
    GraphExtractionTaskDO selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    @Select(
            """
            select * from knowledge_graph_extraction_task
            where material_id = #{materialId}
            order by requested_at desc, id desc
            """)
    List<GraphExtractionTaskDO> selectByMaterialId(@Param("materialId") Long materialId);

    @Select(
            """
            select * from knowledge_graph_extraction_task
            where batch_id = #{batchId}
            order by requested_at asc, id asc
            """)
    List<GraphExtractionTaskDO> selectByBatchId(@Param("batchId") String batchId);

    @Select(
            """
            select * from knowledge_graph_extraction_task
            where purge_after is not null and purge_after <= #{deadline}
            order by purge_after asc, id asc
            limit #{limit}
            """)
    List<GraphExtractionTaskDO> selectPurgeableBefore(@Param("deadline") Instant deadline, @Param("limit") int limit);

    @Update(
            """
            update knowledge_graph_extraction_task
            set model_snapshot_json = #{row.modelSnapshotJson},
                prompt_snapshot_json = #{row.promptSnapshotJson},
                output_schema_json = #{row.outputSchemaJson},
                execution_status = #{row.executionStatus},
                disposition = #{row.disposition},
                attempt_no = #{row.attemptNo},
                batch_id = #{row.batchId},
                candidate_id = #{row.candidateId},
                current_stage = #{row.currentStage},
                progress = #{row.progress},
                regenerated_from_task_id = #{row.regeneratedFromTaskId},
                superseded_by_task_id = #{row.supersededByTaskId},
                triggered_by_task_id = #{row.triggeredByTaskId},
                completed_at = #{row.completedAt},
                disposed_at = #{row.disposedAt},
                purge_after = #{row.purgeAfter},
                lock_version = lock_version + 1
            where id = #{row.id}
              and lock_version = #{expectedLockVersion}
            """)
    int updateIfLockVersion(
            @Param("row") GraphExtractionTaskDO row, @Param("expectedLockVersion") Long expectedLockVersion);
}
