package com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WangqiDocumentMapper extends BaseMapper<WangqiDocumentDO> {
    @Select("select * from classics_wangqi_document where id = #{id} for update")
    WangqiDocumentDO selectPublicationStateForUpdate(@Param("id") Long id);

    @Update(
            """
            update classics_wangqi_document
            set lifecycle_status = #{targetLifecycleStatus},
                transition_status = #{targetTransitionStatus},
                current_publication_job_id = #{targetJobId}
            where id = #{id}
              and lifecycle_status = #{expectedLifecycleStatus}
              and transition_status = #{expectedTransitionStatus}
              and (
                (#{expectedJobId} is null and current_publication_job_id is null)
                or current_publication_job_id = #{expectedJobId}
              )
            """)
    int updatePublicationState(
            @Param("id") Long id,
            @Param("expectedLifecycleStatus") String expectedLifecycleStatus,
            @Param("expectedTransitionStatus") String expectedTransitionStatus,
            @Param("expectedJobId") Long expectedJobId,
            @Param("targetLifecycleStatus") String targetLifecycleStatus,
            @Param("targetTransitionStatus") String targetTransitionStatus,
            @Param("targetJobId") Long targetJobId);
}
