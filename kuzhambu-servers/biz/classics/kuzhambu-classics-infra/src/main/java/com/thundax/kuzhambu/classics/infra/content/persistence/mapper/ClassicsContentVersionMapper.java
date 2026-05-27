package com.thundax.kuzhambu.classics.infra.content.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentVersionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClassicsContentVersionMapper extends BaseMapper<ClassicsContentVersionDO> {}
