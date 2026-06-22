package com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagAliasDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagAliasMapper extends BaseMapper<TagAliasDO> {}
