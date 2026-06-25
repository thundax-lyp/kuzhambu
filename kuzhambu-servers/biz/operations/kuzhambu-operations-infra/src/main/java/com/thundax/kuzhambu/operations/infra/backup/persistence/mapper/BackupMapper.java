package com.thundax.kuzhambu.operations.infra.backup.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.operations.infra.backup.persistence.dataobject.BackupDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BackupMapper extends BaseMapper<BackupDO> {}
