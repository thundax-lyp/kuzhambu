package com.thundax.kuzhambu.infra.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.auth.dataobject.OAuthClientDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuthClientMapper extends BaseMapper<OAuthClientDO> {}
