package com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagContentRefDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagContentRefMapper extends BaseMapper<TagContentRefDO> {}
