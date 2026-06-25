package com.thundax.kuzhambu.operations.infra.health.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.operations.infra.health.persistence.dataobject.HealthCheckDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthCheckMapper extends BaseMapper<HealthCheckDO> {}
