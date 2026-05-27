package com.thundax.kuzhambu.storage.infra.object.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.MultipartUploadPartDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MultipartUploadPartMapper extends BaseMapper<MultipartUploadPartDO> {}
