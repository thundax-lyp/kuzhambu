package com.thundax.kuzhambu.ai.infra.batch.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.AiBatchJobDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.EntrySplitCandidateDO;
import com.thundax.kuzhambu.ai.infra.batch.persistence.dataobject.ImageUnderstandingResultDO;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiBatchJobMapper extends BaseMapper<AiBatchJobDO> {

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
}
