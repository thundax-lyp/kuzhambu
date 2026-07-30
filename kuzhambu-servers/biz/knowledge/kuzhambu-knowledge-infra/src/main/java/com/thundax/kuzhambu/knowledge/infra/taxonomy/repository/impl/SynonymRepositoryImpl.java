package com.thundax.kuzhambu.knowledge.infra.taxonomy.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.SynonymIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Synonym;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.SynonymStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.SynonymId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.SynonymRepository;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.assembler.TaxonomyPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.dataobject.SynonymDO;
import com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper.SynonymMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class SynonymRepositoryImpl implements SynonymRepository {

    private final SynonymMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public SynonymRepositoryImpl(SynonymMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Synonym getById(SynonymId id) {
        return TaxonomyPersistenceAssembler.toDomain(mapper.selectOne(buildSynonymIdQueryWrapper(id)));
    }

    @Override
    public PageResult<Synonym> page(String term, String synonym, SynonymStatus status, int pageNo, int pageSize) {
        IPage<SynonymDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildQueryWrapper(term, synonym, status));
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                TaxonomyPersistenceAssembler.toSynonymDomainList(dataObjectPage.getRecords()));
    }

    @Override
    public int countByPair(String term, String synonym, SynonymId excludedId) {
        QueryWrapper<SynonymDO> wrapper = new QueryWrapper<>();
        wrapper.eq("term", term)
                .eq("synonym", synonym)
                .ne(excludedId != null, "synonym_id", SynonymIdCodec.toValue(excludedId));
        return mapper.selectCount(wrapper).intValue();
    }

    @Override
    public SynonymId insert(Synonym entity) {
        SynonymDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId().value());
        }
        if (dataObject.getSynonymId() == null) {
            dataObject.setSynonymId(idGenerator.nextId().value());
        }
        mapper.insert(dataObject);
        return SynonymIdCodec.toDomain(dataObject.getSynonymId());
    }

    @Override
    public int update(Synonym entity) {
        SynonymDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        return mapper.update(
                null,
                buildSynonymIdUpdateWrapper(dataObject)
                        .set(SynonymDO::getTerm, dataObject.getTerm())
                        .set(SynonymDO::getSynonym, dataObject.getSynonym())
                        .set(SynonymDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int updateStatus(Synonym entity) {
        SynonymDO dataObject = TaxonomyPersistenceAssembler.toObject(entity);
        return mapper.update(
                null, buildSynonymIdUpdateWrapper(dataObject).set(SynonymDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int deleteById(SynonymId id) {
        return mapper.delete(buildSynonymIdQueryWrapper(id));
    }

    private QueryWrapper<SynonymDO> buildQueryWrapper(String term, String synonym, SynonymStatus status) {
        QueryWrapper<SynonymDO> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(term), "term", term)
                .eq(StringUtils.isNotBlank(synonym), "synonym", synonym)
                .eq(status != null, "status", status.value())
                .orderByDesc("id");
        return wrapper;
    }

    private LambdaUpdateWrapper<SynonymDO> buildSynonymIdUpdateWrapper(SynonymDO dataObject) {
        LambdaUpdateWrapper<SynonymDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SynonymDO::getSynonymId, dataObject.getSynonymId());
        return wrapper;
    }

    private QueryWrapper<SynonymDO> buildSynonymIdQueryWrapper(SynonymId id) {
        QueryWrapper<SynonymDO> wrapper = new QueryWrapper<>();
        wrapper.eq("synonym_id", SynonymIdCodec.toValue(id));
        return wrapper;
    }
}
