package com.thundax.kuzhambu.ai.infra.config.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiModelDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiModelMapper extends BaseMapper<AiModelDO> {}
