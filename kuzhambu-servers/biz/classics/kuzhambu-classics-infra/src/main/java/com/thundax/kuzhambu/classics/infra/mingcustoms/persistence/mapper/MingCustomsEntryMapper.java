package com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsEntryDO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MingCustomsEntryMapper extends BaseMapper<MingCustomsEntryDO> {
    @Select("select * from classics_ming_customs_entry where id = #{id} for update")
    MingCustomsEntryDO selectPublicationStateForUpdate(@Param("id") Long id);

    @Select(
            """
            <script>
            select
                tag.tag_id as tagId,
                tag.tag_name_snapshot as tagNameSnapshot,
                count(distinct tag.content_id) as count
            from classics_content_tag tag
            join classics_ming_customs_entry entry on entry.id = tag.content_id
            where tag.content_type = 'MING_CUSTOMS'
                and tag.status = 'ACTIVE'
                and tag.tag_name_snapshot is not null
                and tag.tag_name_snapshot != ''
                <if test="category != null and category != ''">
                    and entry.category = #{category}
                </if>
                <if test="keyword != null and keyword != ''">
                    and (
                        entry.title like concat('%', #{keyword}, '%')
                        or entry.summary like concat('%', #{keyword}, '%')
                        or entry.content like concat('%', #{keyword}, '%')
                        or entry.original_excerpts like concat('%', #{keyword}, '%')
                    )
                </if>
            group by tag.tag_id, tag.tag_name_snapshot
            order by count desc, tag.tag_name_snapshot asc
            </script>
            """)
    List<Map<String, Object>> selectTagCloud(@Param("category") String category, @Param("keyword") String keyword);

    @org.apache.ibatis.annotations.Update(
            """
            update classics_ming_customs_entry
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
