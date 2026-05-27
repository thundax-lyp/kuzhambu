package com.thundax.kuzhambu.storage.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.storage.infra.dataobject.StoredObjectReferenceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoredObjectReferenceMapper extends BaseMapper<StoredObjectReferenceDO> {}
