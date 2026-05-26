package com.thundax.kuzhambu.infra.audit.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.audit.persistence.dataobject.AuditMetaDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditMetaMapper extends BaseMapper<AuditMetaDO> {}
