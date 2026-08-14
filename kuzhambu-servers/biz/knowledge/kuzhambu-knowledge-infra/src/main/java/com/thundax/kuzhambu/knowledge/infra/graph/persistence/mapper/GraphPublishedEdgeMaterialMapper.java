package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeMaterialDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GraphPublishedEdgeMaterialMapper extends BaseMapper<GraphPublishedEdgeMaterialDO> {

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
