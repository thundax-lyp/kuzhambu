package com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCallRecordDO;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiCandidateDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiInvocationMapper extends BaseMapper<AiCallRecordDO> {

    @Select(
            """
            select * from ai_call_record
            where (#{requestedAtStart} is null or requested_at >= #{requestedAtStart})
              and (#{requestedAtEnd} is null or requested_at <= #{requestedAtEnd})
            order by requested_at desc
            """)
    List<AiCallRecordDO> selectCallRecords(java.time.Instant requestedAtStart, java.time.Instant requestedAtEnd);

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
}
