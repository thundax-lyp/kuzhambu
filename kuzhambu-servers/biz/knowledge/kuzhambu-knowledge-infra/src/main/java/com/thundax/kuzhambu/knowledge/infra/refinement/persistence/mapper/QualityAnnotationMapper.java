package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityAnnotationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QualityAnnotationMapper extends BaseMapper<QualityAnnotationDO> {}
