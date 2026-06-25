package com.thundax.kuzhambu.operations.infra.report.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.operations.infra.report.persistence.dataobject.ReportDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper extends BaseMapper<ReportDO> {}
