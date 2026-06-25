package com.thundax.kuzhambu.operations.infra.restore.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.operations.infra.restore.persistence.dataobject.RestoreDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RestoreMapper extends BaseMapper<RestoreDO> {}
