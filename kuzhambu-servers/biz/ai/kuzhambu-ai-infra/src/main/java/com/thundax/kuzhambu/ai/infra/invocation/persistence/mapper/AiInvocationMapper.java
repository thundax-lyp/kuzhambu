package com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCandidateDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiInvocationLogDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiInvocationMapper extends BaseMapper<AiInvocationLogDO> {

    @Select(
            """
            select * from ai_invocation_log
            where (#{requestedAtStart} is null or requested_at >= #{requestedAtStart})
              and (#{requestedAtEnd} is null or requested_at <= #{requestedAtEnd})
            order by requested_at desc
            """)
    List<AiInvocationLogDO> selectInvocationLogs(
            @Param("requestedAtStart") java.time.Instant requestedAtStart,
            @Param("requestedAtEnd") java.time.Instant requestedAtEnd);

    @Select(
            """
            select * from ai_invocation_log
            where batch_id = #{batchId}
            order by requested_at desc
            """)
    List<AiInvocationLogDO> selectInvocationLogsByBatch(@Param("batchId") Long batchId);

    @Select(
            """
            <script>
            select * from ai_invocation_log
            where batch_id in
            <foreach collection="batchIds" item="batchId" open="(" separator="," close=")">
                #{batchId}
            </foreach>
            order by batch_id asc, requested_at desc
            </script>
            """)
    List<AiInvocationLogDO> selectInvocationLogsByBatches(@Param("batchIds") List<Long> batchIds);

    @Select(
            """
            <script>
            select * from ai_invocation_log
            where batch_id in
            <foreach collection="batchIds" item="batchId" open="(" separator="," close=")">
                #{batchId}
            </foreach>
              and (#{contentType} is null or content_type = #{contentType})
              and (#{contentId} is null or content_id = #{contentId})
            order by batch_id asc, requested_at desc
            </script>
            """)
    List<AiInvocationLogDO> selectInvocationLogsByBatchesAndContent(
            @Param("batchIds") List<Long> batchIds,
            @Param("contentType") String contentType,
            @Param("contentId") Long contentId);

    @Select(
            """
            select * from ai_invocation_log
            where (#{scope} is null or scope = #{scope})
              and (#{capability} is null or capability = #{capability})
              and (#{contentType} is null or content_type = #{contentType})
              and (#{contentId} is null or content_id = #{contentId})
              and (#{status} is null or status = #{status})
              and (#{serviceRole} is null or service_role = #{serviceRole})
              and (#{modelName} is null or model_name like concat('%', #{modelName}, '%'))
              and (#{fallbackUsed} is null or fallback_used = #{fallbackUsed})
              and (#{requestedAtStart} is null or requested_at >= #{requestedAtStart})
              and (#{requestedAtEnd} is null or requested_at <= #{requestedAtEnd})
            order by requested_at desc
            limit #{pageSize} offset #{offset}
            """)
    List<AiInvocationLogDO> selectInvocationLogsPage(
            @Param("scope") String scope,
            @Param("capability") String capability,
            @Param("contentType") String contentType,
            @Param("contentId") Long contentId,
            @Param("status") String status,
            @Param("serviceRole") String serviceRole,
            @Param("modelName") String modelName,
            @Param("fallbackUsed") Boolean fallbackUsed,
            @Param("requestedAtStart") java.time.Instant requestedAtStart,
            @Param("requestedAtEnd") java.time.Instant requestedAtEnd,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    @Select(
            """
            select count(1) from ai_invocation_log
            where (#{scope} is null or scope = #{scope})
              and (#{capability} is null or capability = #{capability})
              and (#{contentType} is null or content_type = #{contentType})
              and (#{contentId} is null or content_id = #{contentId})
              and (#{status} is null or status = #{status})
              and (#{serviceRole} is null or service_role = #{serviceRole})
              and (#{modelName} is null or model_name like concat('%', #{modelName}, '%'))
              and (#{fallbackUsed} is null or fallback_used = #{fallbackUsed})
              and (#{requestedAtStart} is null or requested_at >= #{requestedAtStart})
              and (#{requestedAtEnd} is null or requested_at <= #{requestedAtEnd})
            """)
    long countInvocationLogs(
            @Param("scope") String scope,
            @Param("capability") String capability,
            @Param("contentType") String contentType,
            @Param("contentId") Long contentId,
            @Param("status") String status,
            @Param("serviceRole") String serviceRole,
            @Param("modelName") String modelName,
            @Param("fallbackUsed") Boolean fallbackUsed,
            @Param("requestedAtStart") java.time.Instant requestedAtStart,
            @Param("requestedAtEnd") java.time.Instant requestedAtEnd);

    @Select(
            """
            select * from ai_invocation_log
            where (#{scope} is null or scope = #{scope})
              and (#{capability} is null or capability = #{capability})
              and (#{serviceRole} is null or service_role = #{serviceRole})
              and (#{requestedAtStart} is null or requested_at >= #{requestedAtStart})
              and (#{requestedAtEnd} is null or requested_at <= #{requestedAtEnd})
            order by requested_at desc
            """)
    List<AiInvocationLogDO> selectInvocationLogsForSummary(
            @Param("scope") String scope,
            @Param("capability") String capability,
            @Param("serviceRole") String serviceRole,
            @Param("requestedAtStart") java.time.Instant requestedAtStart,
            @Param("requestedAtEnd") java.time.Instant requestedAtEnd);

    @Select("select * from ai_candidate where id = #{candidateId}")
    AiCandidateDO selectCandidate(Long candidateId);

    @Select(
            """
            select * from ai_candidate
            where (#{contentType} is null or content_type = #{contentType})
              and (#{contentId} is null or content_id = #{contentId})
              and (#{objectId} is null or object_id = #{objectId})
              and (#{capability} is null or capability = #{capability})
              and (#{status} is null or status = #{status})
            order by requested_at desc
            """)
    List<AiCandidateDO> selectCandidates(
            @Param("contentType") String contentType,
            @Param("contentId") Long contentId,
            @Param("objectId") Long objectId,
            @Param("capability") String capability,
            @Param("status") String status);

    @Select(
            """
            select * from ai_candidate
            where batch_id = #{batchId}
            order by requested_at desc
            """)
    List<AiCandidateDO> selectCandidatesByBatch(@Param("batchId") Long batchId);

    @Select(
            """
            <script>
            select * from ai_candidate
            where batch_id in
            <foreach collection="batchIds" item="batchId" open="(" separator="," close=")">
                #{batchId}
            </foreach>
            order by batch_id asc, requested_at desc
            </script>
            """)
    List<AiCandidateDO> selectCandidatesByBatches(@Param("batchIds") List<Long> batchIds);

    @Select(
            """
            <script>
            select * from ai_candidate
            where batch_id in
            <foreach collection="batchIds" item="batchId" open="(" separator="," close=")">
                #{batchId}
            </foreach>
              and (#{contentType} is null or content_type = #{contentType})
              and (#{contentId} is null or content_id = #{contentId})
            order by batch_id asc, requested_at desc
            </script>
            """)
    List<AiCandidateDO> selectCandidatesByBatchesAndContent(
            @Param("batchIds") List<Long> batchIds,
            @Param("contentType") String contentType,
            @Param("contentId") Long contentId);

    @Insert(
            """
            insert into ai_candidate
                (call_id, batch_id, capability, content_type, content_id, object_id,
                 artifact_reference_json, result_format, result_payload, status, prompt_version_id, model_name,
                 failure_stage, error_type, error_message, requested_at, applied_at, rejected_at)
            values
                (#{callId}, #{batchId}, #{capability}, #{contentType}, #{contentId}, #{objectId},
                 #{artifactReferenceJson}, #{resultFormat}, #{resultPayload}, #{status}, #{promptVersionId}, #{modelName},
                 #{failureStage}, #{errorType}, #{errorMessage}, #{requestedAt}, #{appliedAt}, #{rejectedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCandidate(AiCandidateDO dataObject);

    @Update(
            """
            update ai_candidate
            set result_format = #{resultFormat},
                result_payload = #{resultPayload},
                artifact_reference_json = #{artifactReferenceJson},
                status = #{status},
                failure_stage = #{failureStage},
                error_type = #{errorType},
                error_message = #{errorMessage},
                applied_at = #{appliedAt},
                rejected_at = #{rejectedAt}
            where id = #{id}
              and status = 'PENDING'
            """)
    int updateCandidate(AiCandidateDO dataObject);
}
