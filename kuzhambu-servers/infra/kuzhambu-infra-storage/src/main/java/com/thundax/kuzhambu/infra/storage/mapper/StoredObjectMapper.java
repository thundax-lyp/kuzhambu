package com.thundax.kuzhambu.infra.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.storage.dataobject.StoredObjectDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoredObjectMapper extends BaseMapper<StoredObjectDO> {}
