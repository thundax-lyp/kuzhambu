package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.projection.GraphExtractionTaskWithMaterialProjection;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphExtractionTaskMapper extends BaseMapper<GraphExtractionTaskDO> {

    @Delete("delete from knowledge_graph_extraction_task where id = #{id} and lock_version = #{lockVersion}")
    int deleteByIdAndLockVersion(@Param("id") Long id, @Param("lockVersion") Long lockVersion);

    @Select(
            "select * from knowledge_graph_extraction_task where idempotency_scope = #{idempotencyScope} and requested_by = #{requestedBy} and idempotency_key = #{idempotencyKey} order by id asc limit 1")
    GraphExtractionTaskDO selectByIdempotencyScopeAndRequestedByAndKey(
            @Param("idempotencyScope") String idempotencyScope,
            @Param("requestedBy") Long requestedBy,
            @Param("idempotencyKey") String idempotencyKey);

    @Select(
            """
            select * from knowledge_graph_extraction_task
            where material_id = #{materialId}
            order by requested_at desc, id desc
            """)
    List<GraphExtractionTaskDO> selectByMaterialId(@Param("materialId") Long materialId);

    @Select(
            """
            <script>
            select t.*
            from knowledge_graph_extraction_task t
            join (
                select material_id, max(requested_at) as requested_at
                from knowledge_graph_extraction_task
                where material_id in
                <foreach collection="materialIds" item="materialId" open="(" separator="," close=")">
                  #{materialId}
                </foreach>
                group by material_id
            ) latest
              on latest.material_id = t.material_id
             and latest.requested_at = t.requested_at
            join (
                select material_id, requested_at, max(id) as id
                from knowledge_graph_extraction_task
                where material_id in
                <foreach collection="materialIds" item="materialId" open="(" separator="," close=")">
                  #{materialId}
                </foreach>
                group by material_id, requested_at
            ) tie_breaker
              on tie_breaker.material_id = t.material_id
             and tie_breaker.requested_at = t.requested_at
             and tie_breaker.id = t.id
            order by t.material_id asc
            </script>
            """)
    List<GraphExtractionTaskDO> selectLatestByMaterialIds(@Param("materialIds") List<Long> materialIds);

    @Select(
            """
            select * from knowledge_graph_extraction_task
            where batch_id = #{batchId}
            order by requested_at asc, id asc
            """)
    List<GraphExtractionTaskDO> selectByBatchId(@Param("batchId") String batchId);

    @Select(
            """
            <script>
            select content_type, content_ref_id
            from knowledge_graph_extraction_task
            where (#{executionStatus} is null or execution_status = #{executionStatus})
              and (#{disposition} is null or disposition = #{disposition})
            group by content_type, content_ref_id
            order by max(requested_at) desc, max(id) desc
            </script>
            """)
    List<GraphExtractionTaskDO> selectContentRefsByTaskState(
            @Param("executionStatus") String executionStatus, @Param("disposition") String disposition);

    @Select(
            """
            <script>
            select count(*)
            from knowledge_graph_extraction_task
            where (#{batchId} is null or batch_id = #{batchId})
              and (#{executionStatus} is null or execution_status = #{executionStatus})
              and (#{disposition} is null or disposition = #{disposition})
              <if test="refs != null and refs.size() > 0">
                and (content_type, content_ref_id) in
                <foreach collection="refs" item="ref" open="(" separator="," close=")">
                  (#{ref.contentType}, #{ref.contentRefId})
                </foreach>
              </if>
            </script>
            """)
    long countTasks(
            @Param("refs") List<GraphExtractionTaskDO> refs,
            @Param("batchId") String batchId,
            @Param("executionStatus") String executionStatus,
            @Param("disposition") String disposition);

    @Select(
            """
            <script>
            select *
            from knowledge_graph_extraction_task
            where (#{batchId} is null or batch_id = #{batchId})
              and (#{executionStatus} is null or execution_status = #{executionStatus})
              and (#{disposition} is null or disposition = #{disposition})
              <if test="refs != null and refs.size() > 0">
                and (content_type, content_ref_id) in
                <foreach collection="refs" item="ref" open="(" separator="," close=")">
                  (#{ref.contentType}, #{ref.contentRefId})
                </foreach>
              </if>
            order by requested_at desc, id desc
            limit #{pageSize} offset #{offset}
            </script>
            """)
    List<GraphExtractionTaskDO> pageTasks(
            @Param("refs") List<GraphExtractionTaskDO> refs,
            @Param("batchId") String batchId,
            @Param("executionStatus") String executionStatus,
            @Param("disposition") String disposition,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    @Select(
            """
            <script>
            select t.*, coalesce(material_by_id.content_title_snapshot, material_by_content.content_title_snapshot)
                as material_title
            from knowledge_graph_extraction_task t
            left join knowledge_graph_material material_by_id on material_by_id.id = t.material_id
            left join knowledge_graph_material material_by_content
                on material_by_content.content_type = t.content_type
                and material_by_content.content_ref_id = t.content_ref_id
            where (#{batchId} is null or t.batch_id = #{batchId})
              and (#{executionStatus} is null or t.execution_status = #{executionStatus})
              and (#{disposition} is null or t.disposition = #{disposition})
              <if test="refs != null and refs.size() > 0">
                and (t.content_type, t.content_ref_id) in
                <foreach collection="refs" item="ref" open="(" separator="," close=")">
                  (#{ref.contentType}, #{ref.contentRefId})
                </foreach>
              </if>
            order by t.requested_at desc, t.id desc
            limit #{pageSize} offset #{offset}
            </script>
            """)
    List<GraphExtractionTaskWithMaterialProjection> pageTasksWithMaterialTitle(
            @Param("refs") List<GraphExtractionTaskDO> refs,
            @Param("batchId") String batchId,
            @Param("executionStatus") String executionStatus,
            @Param("disposition") String disposition,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    @Select(
            """
            select * from knowledge_graph_extraction_task
            where purge_after is not null and purge_after <= #{deadline}
              and execution_status in ('SUCCEEDED', 'CANCELLED')
              and disposition in ('ADOPTED_MERGE', 'ADOPTED_REPLACE', 'DISCARDED', 'SUPERSEDED')
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
                ai_batch_id = #{row.aiBatchId},
                candidate_id = #{row.candidateId},
                current_stage = #{row.currentStage},
                progress = #{row.progress},
                failure_reason = #{row.failureReason},
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
