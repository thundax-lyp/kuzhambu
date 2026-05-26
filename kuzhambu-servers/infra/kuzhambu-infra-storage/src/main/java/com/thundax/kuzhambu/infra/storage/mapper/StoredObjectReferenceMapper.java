package com.thundax.kuzhambu.infra.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.storage.dataobject.StoredObjectReferenceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoredObjectReferenceMapper extends BaseMapper<StoredObjectReferenceDO> {}
