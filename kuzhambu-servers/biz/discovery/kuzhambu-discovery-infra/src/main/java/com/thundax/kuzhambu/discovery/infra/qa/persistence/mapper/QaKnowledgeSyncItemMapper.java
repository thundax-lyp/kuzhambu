package com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaKnowledgeSyncItemDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaKnowledgeSyncItemMapper extends BaseMapper<QaKnowledgeSyncItemDO> {}
