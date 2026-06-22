package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagCategoryIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagRepository;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.assembler.TaxonomyPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class TagRepositoryImpl implements TagRepository {

    private final TagMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public TagRepositoryImpl(TagMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Tag getById(TagId id) {
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectById(TagIdCodec.toValue(id)));
    }

    @Override
    public Tag getByTagId(TagId tagId) {
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectOne(
                buildQueryWrapper(null, null, null, null, null).eq("tag_id", TagIdCodec.toValue(tagId))));
    }

    @Override
    public Tag getByName(String name) {
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectOne(buildQueryWrapper(name, null, null, null, null)));
    }

    @Override
    public PageResult<Tag> page(
            String name,
            TagCategoryId categoryId,
            TagStatus status,
            TagSource source,
            TagReviewStatus reviewStatus,
            int pageNo,
            int pageSize) {
        IPage<TagDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize), buildQueryWrapper(name, categoryId, status, source, reviewStatus));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                TaxonomyPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public PageResult<Tag> pagePending(int pageNo, int pageSize) {
        IPage<TagDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize),
                buildQueryWrapper(null, null, null, TagSource.AI_EXTRACTED, TagReviewStatus.PENDING)
                        .orderByDesc("created_at", "id"));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                TaxonomyPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public int countByName(String name, TagId excludedId) {
        QueryWrapper<TagDO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).ne(excludedId != null, "id", TagIdCodec.toValue(excludedId));
        return mapper.selectCount(wrapper).intValue();
    }

    @Override
    public TagId insert(Tag entity) {
        TagDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        mapper.insert(dataObject);
        return TagIdCodec.toDomain(dataObject.getTagId());
    }

    @Override
    public int update(Tag entity) {
        TagDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(TagDO::getTagId, dataObject.getTagId())
                        .set(TagDO::getName, dataObject.getName())
                        .set(TagDO::getCategoryId, dataObject.getCategoryId())
                        .set(TagDO::getDescription, dataObject.getDescription())
                        .set(TagDO::getStatus, dataObject.getStatus())
                        .set(TagDO::getSource, dataObject.getSource())
                        .set(TagDO::getReviewStatus, dataObject.getReviewStatus())
                        .set(TagDO::getReviewNote, dataObject.getReviewNote()));
    }

    @Override
    public int updateStatus(Tag entity) {
        TagDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        return mapper.update(null, buildIdUpdateWrapper(dataObject).set(TagDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateReviewStatus(Tag entity) {
        TagDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(TagDO::getReviewStatus, dataObject.getReviewStatus())
                        .set(TagDO::getReviewedAt, dataObject.getReviewedAt())
                        .set(TagDO::getReviewNote, dataObject.getReviewNote()));
    }

    private QueryWrapper<TagDO> buildQueryWrapper(
            String name, TagCategoryId categoryId, TagStatus status, TagSource source, TagReviewStatus reviewStatus) {
        QueryWrapper<TagDO> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(name), "name", name)
                .eq(categoryId != null, "category_id", TagCategoryIdCodec.toValue(categoryId))
                .eq(status != null, "status", status.value())
                .eq(source != null, "source", source.value())
                .eq(reviewStatus != null, "review_status", reviewStatus.value())
                .orderByDesc("created_at")
                .orderByAsc("id");
        return wrapper;
    }

    private LambdaUpdateWrapper<TagDO> buildIdUpdateWrapper(TagDO dataObject) {
        LambdaUpdateWrapper<TagDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TagDO::getId, dataObject.getId());
        return wrapper;
    }
}
