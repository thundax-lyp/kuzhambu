package com.thundax.kuzhambu.infra.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.infra.core.dataobject.MenuRoleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuRoleMapper extends BaseMapper<MenuRoleDO> {}
