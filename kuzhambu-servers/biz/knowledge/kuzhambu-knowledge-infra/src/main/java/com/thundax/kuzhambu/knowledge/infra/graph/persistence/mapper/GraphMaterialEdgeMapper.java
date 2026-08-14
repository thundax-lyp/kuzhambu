package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialEdgeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GraphMaterialEdgeMapper extends BaseMapper<GraphMaterialEdgeDO> {}
