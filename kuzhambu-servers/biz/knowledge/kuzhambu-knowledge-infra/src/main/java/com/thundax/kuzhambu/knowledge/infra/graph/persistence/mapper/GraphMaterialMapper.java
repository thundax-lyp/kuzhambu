package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GraphMaterialMapper extends BaseMapper<GraphMaterialDO> {

    @Select(
            """
            <script>
            select *
            from knowledge_graph_material
            where (#{status} is null or status = #{status})
              <if test="keyword != null and keyword != ''">
                and content_title_snapshot like concat('%', #{keyword}, '%')
              </if>
            order by case when published_at is null then 1 else 0 end, published_at desc, id desc
            limit #{pageSize} offset #{offset}
            </script>
            """)
    List<GraphMaterialDO> pageMaterials(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    @Select(
            """
            <script>
            select count(*)
            from knowledge_graph_material
            where (#{status} is null or status = #{status})
              <if test="keyword != null and keyword != ''">
                and content_title_snapshot like concat('%', #{keyword}, '%')
              </if>
            </script>
            """)
    long countMaterials(@Param("keyword") String keyword, @Param("status") String status);

    @Update(
            """
            update knowledge_graph_material
            set content_title_snapshot = #{row.contentTitleSnapshot},
                status = #{row.status},
                published_at = #{row.publishedAt},
                failure_reason = #{row.failureReason},
                failed_operation = #{row.failedOperation},
                current_extraction_task_id = #{row.currentExtractionTaskId},
                lock_version = lock_version + 1
            where content_type = #{row.contentType}
              and content_ref_id = #{row.contentRefId}
              and lock_version = #{expectedLockVersion}
            """)
    int updateIfLockVersion(@Param("row") GraphMaterialDO row, @Param("expectedLockVersion") Long expectedLockVersion);
}
