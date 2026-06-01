package com.thundax.kuzhambu.ai.infra.batch.persistence.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiBatchJobMapper extends BaseMapper<AiBatchJobMapper.AiBatchJobDO> {

    @Insert(
            """
            insert into ai_image_understanding
                (understanding_id, storage_object_id, content_hash, analysis_markdown,
                 call_id, prompt_version_id, model_name, requested_at)
            values
                (#{understandingId}, #{storageObjectId}, #{contentHash}, #{analysisMarkdown},
                 #{callId}, #{promptVersionId}, #{modelName}, #{requestedAt})
            """)
    int insertImageUnderstanding(ImageUnderstandingResultDO dataObject);

    @Select(
            """
            select * from ai_image_understanding
            where storage_object_id = #{storageObjectId} and content_hash = #{contentHash}
            """)
    ImageUnderstandingResultDO selectImageUnderstanding(Long storageObjectId, String contentHash);

    @Insert(
            """
            insert into ai_entry_split_candidate
                (split_candidate_id, candidate_id, parent_content_type, parent_content_id,
                 title, original_text, translation_text, target_volume_id, priority)
            values
                (#{splitCandidateId}, #{candidateId}, #{parentContentType}, #{parentContentId},
                 #{title}, #{originalText}, #{translationText}, #{targetVolumeId}, #{priority})
            """)
    int insertEntrySplitCandidate(EntrySplitCandidateDO dataObject);

    @Select("select * from ai_entry_split_candidate where candidate_id = #{candidateId} order by priority asc")
    List<EntrySplitCandidateDO> selectEntrySplitCandidates(Long candidateId);

    @Data
    @TableName("ai_batch_job")
    class AiBatchJobDO {

        @TableId(type = IdType.AUTO)
        private Long id;

        private Long batchId;
        private String scope;
        private String capability;
        private String contentType;
        private String status;
        private Integer totalCount;
        private Integer successCount;
        private Integer failedCount;
        private Integer cancelledCount;
        private String failureSummaryJson;
        private Instant requestedAt;
        private Instant cancelledAt;
        private Instant completedAt;
    }

    @Data
    @TableName("ai_image_understanding")
    class ImageUnderstandingResultDO {

        @TableId(type = IdType.AUTO)
        private Long id;

        private Long understandingId;
        private Long storageObjectId;
        private String contentHash;
        private String analysisMarkdown;
        private Long callId;
        private Long promptVersionId;
        private String modelName;
        private Instant requestedAt;
    }

    @Data
    @TableName("ai_entry_split_candidate")
    class EntrySplitCandidateDO {

        @TableId(type = IdType.AUTO)
        private Long id;

        private Long splitCandidateId;
        private Long candidateId;
        private String parentContentType;
        private Long parentContentId;
        private String title;
        private String originalText;
        private String translationText;
        private Long targetVolumeId;
        private Integer priority;
    }
}
