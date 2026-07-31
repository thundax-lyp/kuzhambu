package com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SancaiMapper extends BaseMapper<SancaiEntryDO> {
    @Select("select * from classics_sancai_entry where id = #{id} for update")
    SancaiEntryDO selectPublicationStateForUpdate(@Param("id") Long id);

    @Update(
            """
            update classics_sancai_entry
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
