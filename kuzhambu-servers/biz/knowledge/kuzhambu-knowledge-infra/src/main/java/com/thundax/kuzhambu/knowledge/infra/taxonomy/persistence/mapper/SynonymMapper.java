package com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.SynonymDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SynonymMapper extends BaseMapper<SynonymDO> {}
