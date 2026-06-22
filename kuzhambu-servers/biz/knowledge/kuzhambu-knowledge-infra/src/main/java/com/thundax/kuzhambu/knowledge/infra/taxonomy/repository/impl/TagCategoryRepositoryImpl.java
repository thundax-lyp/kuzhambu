package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagCategoryIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.TagCategory;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagCategoryId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagCategoryRepository;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.assembler.TaxonomyPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.TagCategoryDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.TagCategoryMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class TagCategoryRepositoryImpl implements TagCategoryRepository {

    private final TagCategoryMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public TagCategoryRepositoryImpl(TagCategoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public TagCategory getById(TagCategoryId id) {
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectById(TagCategoryIdCodec.toValue(id)));
    }

    @Override
    public TagCategory getByCategoryId(TagCategoryId categoryId) {
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectOne(
                buildQueryWrapper(null, null).eq("category_id", TagCategoryIdCodec.toValue(categoryId))));
    }

    @Override
    public PageResult<TagCategory> page(String name, TagCategoryStatus status, int pageNo, int pageSize) {
        IPage<TagCategoryDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildQueryWrapper(name, status));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                TaxonomyPersistenceAssembler.toTagCategoryDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public TagCategoryId insert(TagCategory entity) {
        TagCategoryDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        mapper.insert(dataObject);
        return TagCategoryIdCodec.toDomain(dataObject.getCategoryId());
    }

    @Override
    public int update(TagCategory entity) {
        TagCategoryDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(TagCategoryDO::getName, dataObject.getName())
                        .set(TagCategoryDO::getDescription, dataObject.getDescription())
                        .set(TagCategoryDO::getCategoryId, dataObject.getCategoryId())
                        .set(TagCategoryDO::getPriority, dataObject.getPriority())
                        .set(TagCategoryDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateStatus(TagCategory entity) {
        TagCategoryDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(TagCategoryDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int countByName(String name, TagCategoryId excludedId) {
        QueryWrapper<TagCategoryDO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.ne(excludedId != null, "id", TagCategoryIdCodec.toValue(excludedId));
        return mapper.selectCount(wrapper).intValue();
    }

    @Override
    public int countEnabledByCategoryId(TagCategoryId categoryId) {
        QueryWrapper<TagCategoryDO> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", TagCategoryIdCodec.toValue(categoryId))
                .eq("status", TagCategoryStatus.ENABLED.value());
        Long count = mapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    private QueryWrapper<TagCategoryDO> buildQueryWrapper(String name, TagCategoryStatus status) {
        QueryWrapper<TagCategoryDO> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(name), "name", name)
                .eq(status != null, "status", status.value())
                .orderByAsc("category_id");
        return wrapper;
    }

    private LambdaUpdateWrapper<TagCategoryDO> buildIdUpdateWrapper(TagCategoryDO dataObject) {
        LambdaUpdateWrapper<TagCategoryDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TagCategoryDO::getId, dataObject.getId());
        return wrapper;
    }
}
