package com.thundax.kuzhambu.infra.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.storage.dataobject.MultipartUploadPartDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MultipartUploadPartMapper extends BaseMapper<MultipartUploadPartDO> {}
