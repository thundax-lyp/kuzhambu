package com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiInvocationMapper extends BaseMapper<AiInvocationMapper.AiCallRecordDO> {

    @Select("select * from ai_candidate where candidate_id = #{candidateId}")
    AiCandidateDO selectCandidate(Long candidateId);

    @Select(
            """
            select * from ai_candidate
            where (#{contentType} is null or content_type = #{contentType})
              and (#{contentId} is null or content_id = #{contentId})
              and (#{capability} is null or capability = #{capability})
              and (#{status} is null or status = #{status})
            order by requested_at desc
            """)
    List<AiCandidateDO> selectCandidates(String contentType, Long contentId, String capability, String status);

    @Insert(
            """
            insert into ai_candidate
                (candidate_id, call_id, batch_id, capability, content_type, content_id, object_id,
                 result_format, result_payload, status, prompt_version_id, model_name,
                 error_type, error_message, requested_at, applied_at)
            values
                (#{candidateId}, #{callId}, #{batchId}, #{capability}, #{contentType}, #{contentId}, #{objectId},
                 #{resultFormat}, #{resultPayload}, #{status}, #{promptVersionId}, #{modelName},
                 #{errorType}, #{errorMessage}, #{requestedAt}, #{appliedAt})
            """)
    int insertCandidate(AiCandidateDO dataObject);

    @Update(
            """
            update ai_candidate
            set result_format = #{resultFormat},
                result_payload = #{resultPayload},
                status = #{status},
                error_type = #{errorType},
                error_message = #{errorMessage},
                applied_at = #{appliedAt}
            where candidate_id = #{candidateId}
            """)
    int updateCandidate(AiCandidateDO dataObject);

    @Data
    @TableName("ai_call_record")
    class AiCallRecordDO {

        @TableId(type = IdType.AUTO)
        private Long id;

        private Long callId;
        private Long batchId;
        private String scope;
        private String capability;
        private String contentType;
        private Long contentId;
        private Long objectId;
        private Long serviceId;
        private String serviceRole;
        private Long modelId;
        private String modelName;
        private Long promptVersionId;
        private String requestId;
        private String traceId;
        private String status;
        private Boolean streamUsed;
        private Boolean streamCompleted;
        private Boolean fallbackUsed;
        private Integer latencyMs;
        private Integer inputTokens;
        private Integer outputTokens;
        private BigDecimal costAmount;
        private String errorType;
        private String errorMessage;
        private String warningsJson;
        private Instant requestedAt;
        private Instant completedAt;
    }

    @Data
    @TableName("ai_candidate")
    class AiCandidateDO {

        @TableId(type = IdType.AUTO)
        private Long id;

        private Long candidateId;
        private Long callId;
        private Long batchId;
        private String capability;
        private String contentType;
        private Long contentId;
        private Long objectId;
        private String resultFormat;
        private String resultPayload;
        private String status;
        private Long promptVersionId;
        private String modelName;
        private String errorType;
        private String errorMessage;
        private Instant requestedAt;
        private Instant appliedAt;
    }
}
