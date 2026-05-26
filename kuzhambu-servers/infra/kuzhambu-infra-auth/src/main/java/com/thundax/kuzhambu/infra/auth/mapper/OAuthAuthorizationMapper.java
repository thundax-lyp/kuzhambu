package com.thundax.kuzhambu.infra.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.auth.dataobject.OAuthAuthorizationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuthAuthorizationMapper extends BaseMapper<OAuthAuthorizationDO> {}
