package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialVersionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GraphMaterialVersionMapper extends BaseMapper<GraphMaterialVersionDO> {

    @Select(
            "select coalesce(max(version_no), 0) from knowledge_graph_material_version where material_id = #{materialId}")
    long maxVersionNo(@Param("materialId") Long materialId);
}
