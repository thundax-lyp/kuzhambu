package com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SancaiMapper extends BaseMapper<SancaiEntryDO> {}
