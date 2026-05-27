package com.thundax.kuzhambu.system.infra.auth.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.system.infra.auth.persistence.dataobject.PrincipalCredentialDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrincipalCredentialMapper extends BaseMapper<PrincipalCredentialDO> {}
