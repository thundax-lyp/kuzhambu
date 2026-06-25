package com.thundax.kuzhambu.operations.infra.task.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.operations.infra.task.persistence.dataobject.LongTaskSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LongTaskSnapshotMapper extends BaseMapper<LongTaskSnapshotDO> {}
