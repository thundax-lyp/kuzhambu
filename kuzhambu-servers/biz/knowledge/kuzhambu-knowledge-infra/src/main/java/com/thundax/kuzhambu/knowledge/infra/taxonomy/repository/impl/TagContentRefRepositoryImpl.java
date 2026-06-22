package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagContentRefIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagContentRef;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagContentRefRepository;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.assembler.TaxonomyPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagContentRefDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagContentRefMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TagContentRefRepositoryImpl implements TagContentRefRepository {

    private final TagContentRefMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public TagContentRefRepositoryImpl(TagContentRefMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<TagContentRef> listByTagId(TagId tagId) {
        QueryWrapper<TagContentRefDO> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_id", TagIdCodec.toValue(tagId)).orderByDesc("id");
        return TaxonomyPersistenceAssembler.toTagContentRefDomainList(mapper.selectList(wrapper));
    }

    @Override
    public int countByTagId(TagId tagId) {
        QueryWrapper<TagContentRefDO> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_id", TagIdCodec.toValue(tagId));
        return mapper.selectCount(wrapper).intValue();
    }

    @Override
    public int countByTagAndContentTypeAndContentId(
            TagId tagId, ContentType contentType, Long contentId, TagContentRefId excludedId) {
        QueryWrapper<TagContentRefDO> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_id", TagIdCodec.toValue(tagId))
                .eq("content_type", contentType.value())
                .eq("content_id", contentId)
                .ne(excludedId != null, "id", TagContentRefIdCodec.toValue(excludedId));
        return mapper.selectCount(wrapper).intValue();
    }

    @Override
    public TagContentRefId insert(TagContentRef entity) {
        TagContentRefDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        mapper.insert(dataObject);
        return TagContentRefIdCodec.toDomain(dataObject.getRefId());
    }

    @Override
    public int deleteById(TagContentRefId id) {
        return mapper.deleteById(TagContentRefIdCodec.toValue(id));
    }
}
