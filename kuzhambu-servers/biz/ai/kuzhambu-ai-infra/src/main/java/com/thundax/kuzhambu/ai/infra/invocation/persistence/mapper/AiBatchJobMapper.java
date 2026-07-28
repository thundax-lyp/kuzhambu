package com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.invocation.persistence.dataobject.AiBatchJobDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiBatchJobMapper extends BaseMapper<AiBatchJobDO> {

    @Select(
            """
            <script>
            select distinct job.*
            from ai_batch_job job
            left join ai_invocation_log invocation on invocation.batch_id = job.id
            where (#{scope} is null or job.scope = #{scope})
              and (#{capability} is null or job.capability = #{capability})
              <if test="capabilities != null and capabilities.size() > 0">
                and job.capability in
                <foreach collection="capabilities" item="item" open="(" separator="," close=")">
                  #{item}
                </foreach>
              </if>
              and (#{status} is null or job.status = #{status})
              and (#{contentType} is null or job.content_type = #{contentType})
              and (job.content_id = #{contentId} or (job.content_id is null and invocation.content_id = #{contentId}))
            order by job.requested_at desc
            limit #{pageSize} offset #{offset}
            </script>
            """)
    List<AiBatchJobDO> selectJobsByInvocationContent(
            @Param("scope") String scope,
            @Param("capability") String capability,
            @Param("capabilities") List<String> capabilities,
            @Param("status") String status,
            @Param("contentType") String contentType,
            @Param("contentId") Long contentId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    @Select(
            """
            <script>
            select count(distinct job.id)
            from ai_batch_job job
            left join ai_invocation_log invocation on invocation.batch_id = job.id
            where (#{scope} is null or job.scope = #{scope})
              and (#{capability} is null or job.capability = #{capability})
              <if test="capabilities != null and capabilities.size() > 0">
                and job.capability in
                <foreach collection="capabilities" item="item" open="(" separator="," close=")">
                  #{item}
                </foreach>
              </if>
              and (#{status} is null or job.status = #{status})
              and (#{contentType} is null or job.content_type = #{contentType})
              and (job.content_id = #{contentId} or (job.content_id is null and invocation.content_id = #{contentId}))
            </script>
            """)
    long countJobsByInvocationContent(
            @Param("scope") String scope,
            @Param("capability") String capability,
            @Param("capabilities") List<String> capabilities,
            @Param("status") String status,
            @Param("contentType") String contentType,
            @Param("contentId") Long contentId);
}
