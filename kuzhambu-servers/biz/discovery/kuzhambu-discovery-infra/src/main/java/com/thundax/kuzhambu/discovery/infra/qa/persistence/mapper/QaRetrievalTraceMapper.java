package com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaRetrievalTraceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaRetrievalTraceMapper extends BaseMapper<QaRetrievalTraceDO> {}
