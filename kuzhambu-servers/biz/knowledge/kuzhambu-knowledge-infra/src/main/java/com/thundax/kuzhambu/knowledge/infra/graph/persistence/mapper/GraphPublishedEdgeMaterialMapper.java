package com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeMaterialDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GraphPublishedEdgeMaterialMapper extends BaseMapper<GraphPublishedEdgeMaterialDO> {}
