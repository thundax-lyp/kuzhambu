package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeMaterialDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GraphPublishedEdgeMaterialMapper extends BaseMapper<GraphPublishedEdgeMaterialDO> {

    @Select(
            """
            <script>
            select *
            from knowledge_graph_published_edge_material
            where
            <foreach collection="materialRefs" item="ref" open="(" separator=" or " close=")">
              (content_type = #{ref.contentType} and content_ref_id = #{ref.contentId})
            </foreach>
            </script>
            """)
    List<GraphPublishedEdgeMaterialDO> listByMaterials(@Param("materialRefs") List<ContentRef> materialRefs);

    @Select(
            """
            select count(*)
            from (
                select distinct content_type, content_ref_id
                from knowledge_graph_published_edge_material
            ) distinct_material
            """)
    long countDistinctMaterials();
}
