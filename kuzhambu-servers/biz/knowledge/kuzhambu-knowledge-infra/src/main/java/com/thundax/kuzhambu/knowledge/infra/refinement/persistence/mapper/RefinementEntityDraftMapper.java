package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementEntityDraftDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefinementEntityDraftMapper extends BaseMapper<RefinementEntityDraftDO> {}
