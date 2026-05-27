package com.thundax.kuzhambu.storage.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.storage.infra.dataobject.StoredObjectDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoredObjectMapper extends BaseMapper<StoredObjectDO> {}
