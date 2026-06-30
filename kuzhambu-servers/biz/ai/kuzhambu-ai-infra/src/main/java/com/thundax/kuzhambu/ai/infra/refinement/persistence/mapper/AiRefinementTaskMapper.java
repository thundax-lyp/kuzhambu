package com.thundax.kuzhambu.ai.infra.refinement.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.refinement.persistence.dataobject.AiRefinementTaskDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiRefinementTaskMapper extends BaseMapper<AiRefinementTaskDO> {

    @Select(
            """
            select * from ai_refinement_task
            where (#{capability} is null or capability = #{capability})
              and (#{status} is null or status = #{status})
              and (#{contentType} is null or content_type = #{contentType})
              and (#{contentId} is null or content_id = #{contentId})
              and (#{requestedBy} is null or requested_by = #{requestedBy})
            order by requested_at desc
            limit #{pageSize} offset #{offset}
            """)
    List<AiRefinementTaskDO> selectTasks(
            String capability,
            String status,
            String contentType,
            Long contentId,
            Long requestedBy,
            Integer offset,
            Integer pageSize);

    @Select("select * from ai_refinement_task where task_id = #{taskId}")
    AiRefinementTaskDO selectTask(Long taskId);

    @Select(
            """
            select * from ai_refinement_task
            where status in ('PENDING', 'RUNNING')
              and requested_at < #{threshold}
            """)
    List<AiRefinementTaskDO> selectExpiredRunningTasks(java.time.Instant threshold);

    @Insert(
            """
            insert into ai_refinement_task
                (task_id, scope, capability, content_type, content_id, object_id, requested_by,
                 request_id, trace_id, status, service_role, model_id, model_name, prompt_version_id,
                 call_id, candidate_id, failure_stage, error_type, error_message, result_format,
                 result_preview, stream_enabled, requested_at, started_at, completed_at, cancelled_at)
            values
                (#{taskId}, #{scope}, #{capability}, #{contentType}, #{contentId}, #{objectId}, #{requestedBy},
                 #{requestId}, #{traceId}, #{status}, #{serviceRole}, #{modelId}, #{modelName}, #{promptVersionId},
                 #{callId}, #{candidateId}, #{failureStage}, #{errorType}, #{errorMessage}, #{resultFormat},
                 #{resultPreview}, #{streamEnabled}, #{requestedAt}, #{startedAt}, #{completedAt}, #{cancelledAt})
            """)
    int insertTask(AiRefinementTaskDO dataObject);
}
