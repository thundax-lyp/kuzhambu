package com.thundax.kuzhambu.operations.infra.cleanup.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.operations.infra.cleanup.persistence.dataobject.CleanupItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CleanupItemMapper extends BaseMapper<CleanupItemDO> {}
