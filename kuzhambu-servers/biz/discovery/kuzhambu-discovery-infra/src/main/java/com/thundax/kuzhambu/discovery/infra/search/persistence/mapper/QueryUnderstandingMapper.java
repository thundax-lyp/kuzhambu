package com.thundax.kuzhambu.discovery.infra.search.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.search.persistence.dataobject.QueryUnderstandingDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QueryUnderstandingMapper extends BaseMapper<QueryUnderstandingDO> {}
