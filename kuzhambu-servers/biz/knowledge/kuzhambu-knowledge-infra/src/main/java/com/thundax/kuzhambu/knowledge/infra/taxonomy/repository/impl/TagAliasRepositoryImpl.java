package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagAliasIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagAlias;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagAliasId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagAliasRepository;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.assembler.TaxonomyPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagAliasDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagAliasMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TagAliasRepositoryImpl implements TagAliasRepository {

    private final TagAliasMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public TagAliasRepositoryImpl(TagAliasMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public TagAlias getById(TagAliasId id) {
        QueryWrapper<TagAliasDO> wrapper = new QueryWrapper<>();
        wrapper.eq("alias_id", TagAliasIdCodec.toValue(id));
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public TagAlias getByName(String name) {
        QueryWrapper<TagAliasDO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).orderByDesc("id");
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public List<TagAlias> listByTagId(TagId tagId) {
        QueryWrapper<TagAliasDO> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_id", TagIdCodec.toValue(tagId)).orderByDesc("id");
        return TaxonomyPersistenceAssembler.toTagAliasDomainList(mapper.selectList(wrapper));
    }

    @Override
    public int countByName(String name, TagAliasId excludedId) {
        QueryWrapper<TagAliasDO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).ne(excludedId != null, "alias_id", TagAliasIdCodec.toValue(excludedId));
        return mapper.selectCount(wrapper).intValue();
    }

    @Override
    public TagAliasId insert(TagAlias entity) {
        TagAliasDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        if (dataObject.getAliasId() == null) {
            dataObject.setAliasId(idGenerator.nextId().value());
        }
        mapper.insert(dataObject);
        return TagAliasIdCodec.toDomain(dataObject.getAliasId());
    }

    @Override
    public int deleteById(TagAliasId id) {
        QueryWrapper<TagAliasDO> wrapper = new QueryWrapper<>();
        wrapper.eq("alias_id", TagAliasIdCodec.toValue(id));
        return mapper.delete(wrapper);
    }
}
