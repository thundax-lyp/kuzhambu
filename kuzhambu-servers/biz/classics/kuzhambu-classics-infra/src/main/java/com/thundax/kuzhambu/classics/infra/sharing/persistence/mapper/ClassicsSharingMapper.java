package com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareAccessRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClassicsSharingMapper extends BaseMapper<ClassicsShareAccessRecordDO> {}
