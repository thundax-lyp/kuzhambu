package com.thundax.kuzhambu.infra.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.auth.dataobject.PrincipalIdentityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrincipalIdentityMapper extends BaseMapper<PrincipalIdentityDO> {}
