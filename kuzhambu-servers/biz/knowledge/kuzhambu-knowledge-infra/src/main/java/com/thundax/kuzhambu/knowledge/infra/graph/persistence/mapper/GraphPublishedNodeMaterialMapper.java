package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeMaterialDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GraphPublishedNodeMaterialMapper extends BaseMapper<GraphPublishedNodeMaterialDO> {

    @Select(
            """
            select count(*)
            from (
                select distinct content_type, content_ref_id
                from knowledge_graph_published_node_material
            ) distinct_material
            """)
    long countDistinctMaterials();
}
