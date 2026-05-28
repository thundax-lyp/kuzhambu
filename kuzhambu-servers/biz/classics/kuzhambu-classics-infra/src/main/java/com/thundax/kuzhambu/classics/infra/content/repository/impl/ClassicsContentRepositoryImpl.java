package com.thundax.kuzhambu.classics.infra.content.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentExportJobIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.infra.content.persistence.assembler.ClassicsContentPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentExportJobDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentQaPairDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentTagDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentVersionDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentQaPairMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentTagMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentVersionMapper;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ClassicsContentRepositoryImpl implements ClassicsContentRepository {
    private final ClassicsContentTagMapper tagMapper;
    private final ClassicsContentQaPairMapper qaPairMapper;
    private final ClassicsContentVersionMapper versionMapper;
    private final ClassicsContentMapper exportMapper;

    public ClassicsContentRepositoryImpl(
            ClassicsContentTagMapper tagMapper,
            ClassicsContentQaPairMapper qaPairMapper,
            ClassicsContentVersionMapper versionMapper,
            ClassicsContentMapper exportMapper) {
        this.tagMapper = tagMapper;
        this.qaPairMapper = qaPairMapper;
        this.versionMapper = versionMapper;
        this.exportMapper = exportMapper;
    }

    public List<ClassicsContentTag> listTags(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
        return ClassicsContentPersistenceAssembler.toTagDomainList(
                tagMapper.selectList(new LambdaQueryWrapper<ClassicsContentTagDO>()
                        .eq(StringUtils.isNotBlank(contentType), ClassicsContentTagDO::getContentType, contentType)
                        .eq(contentId != null, ClassicsContentTagDO::getContentId, ClassicsContentIdCodec.toValue(contentId))
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsContentTagDO::getPriority)));
    }

    @Override
    public List<ClassicsContentTag> listTags(SortDirection sortDirection) {
        return ClassicsContentPersistenceAssembler.toTagDomainList(
                tagMapper.selectList(new LambdaQueryWrapper<ClassicsContentTagDO>()
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsContentTagDO::getPriority)));
    }

    @Override
    public int maxTagPriority() {
        return maxPriority(tagMapper.selectObjs(new QueryWrapper<ClassicsContentTagDO>().select("max(priority)")));
    }

    public ClassicsContentTagId insertTag(ClassicsContentTag tag) {
        ClassicsContentTagDO dataObject = ClassicsContentPersistenceAssembler.toTagObject(tag);
        tagMapper.insert(dataObject);
        return ClassicsContentTagIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public ClassicsContentTag getTagById(ClassicsContentTagId id) {
        return ClassicsContentPersistenceAssembler.toTagDomain(
                tagMapper.selectById(ClassicsContentTagIdCodec.toValue(id)));
    }

    @Override
    public int updateTagPriority(ClassicsContentTag tag) {
        ClassicsContentTagDO dataObject = ClassicsContentPersistenceAssembler.toTagObject(tag);
        return tagMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsContentTagDO>()
                        .eq(ClassicsContentTagDO::getId, dataObject.getId())
                        .set(ClassicsContentTagDO::getPriority, dataObject.getPriority()));
    }

    public int updateTag(ClassicsContentTag tag) {
        ClassicsContentTagDO dataObject = ClassicsContentPersistenceAssembler.toTagObject(tag);
        return tagMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsContentTagDO>()
                        .eq(ClassicsContentTagDO::getId, dataObject.getId())
                        .set(ClassicsContentTagDO::getContentType, dataObject.getContentType())
                        .set(ClassicsContentTagDO::getContentId, dataObject.getContentId())
                        .set(ClassicsContentTagDO::getTagId, dataObject.getTagId())
                        .set(ClassicsContentTagDO::getTagNameSnapshot, dataObject.getTagNameSnapshot())
                        .set(ClassicsContentTagDO::getSource, dataObject.getSource())
                        .set(ClassicsContentTagDO::getStatus, dataObject.getStatus()));
    }

    public int deleteTagById(ClassicsContentTagId id) {
        return tagMapper.deleteById(ClassicsContentTagIdCodec.toValue(id));
    }

    public List<ClassicsContentQaPair> listQaPairs(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
        return ClassicsContentPersistenceAssembler.toQaDomainList(
                qaPairMapper.selectList(new LambdaQueryWrapper<ClassicsContentQaPairDO>()
                        .eq(StringUtils.isNotBlank(contentType), ClassicsContentQaPairDO::getContentType, contentType)
                        .eq(contentId != null, ClassicsContentQaPairDO::getContentId, ClassicsContentIdCodec.toValue(contentId))
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsContentQaPairDO::getPriority)));
    }

    @Override
    public List<ClassicsContentQaPair> listQaPairs(SortDirection sortDirection) {
        return ClassicsContentPersistenceAssembler.toQaDomainList(
                qaPairMapper.selectList(new LambdaQueryWrapper<ClassicsContentQaPairDO>()
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsContentQaPairDO::getPriority)));
    }

    @Override
    public int maxQaPairPriority() {
        return maxPriority(qaPairMapper.selectObjs(new QueryWrapper<ClassicsContentQaPairDO>().select("max(priority)")));
    }

    public ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair) {
        ClassicsContentQaPairDO dataObject = ClassicsContentPersistenceAssembler.toQaObject(qaPair);
        qaPairMapper.insert(dataObject);
        return ClassicsContentQaPairIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public ClassicsContentQaPair getQaPairById(ClassicsContentQaPairId id) {
        return ClassicsContentPersistenceAssembler.toQaDomain(
                qaPairMapper.selectById(ClassicsContentQaPairIdCodec.toValue(id)));
    }

    @Override
    public int updateQaPairPriority(ClassicsContentQaPair qaPair) {
        ClassicsContentQaPairDO dataObject = ClassicsContentPersistenceAssembler.toQaObject(qaPair);
        return qaPairMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsContentQaPairDO>()
                        .eq(ClassicsContentQaPairDO::getId, dataObject.getId())
                        .set(ClassicsContentQaPairDO::getPriority, dataObject.getPriority()));
    }

    public int updateQaPair(ClassicsContentQaPair qaPair) {
        ClassicsContentQaPairDO dataObject = ClassicsContentPersistenceAssembler.toQaObject(qaPair);
        return qaPairMapper.update(
                null,
                new LambdaUpdateWrapper<ClassicsContentQaPairDO>()
                        .eq(ClassicsContentQaPairDO::getId, dataObject.getId())
                        .set(ClassicsContentQaPairDO::getContentType, dataObject.getContentType())
                        .set(ClassicsContentQaPairDO::getContentId, dataObject.getContentId())
                        .set(ClassicsContentQaPairDO::getQuestion, dataObject.getQuestion())
                        .set(ClassicsContentQaPairDO::getAnswer, dataObject.getAnswer())
                        .set(ClassicsContentQaPairDO::getSource, dataObject.getSource()));
    }

    public int deleteQaPairById(ClassicsContentQaPairId id) {
        return qaPairMapper.deleteById(ClassicsContentQaPairIdCodec.toValue(id));
    }

    public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
        return ClassicsContentPersistenceAssembler.toVersionDomainList(
                versionMapper.selectList(new LambdaQueryWrapper<ClassicsContentVersionDO>()
                        .eq(ClassicsContentVersionDO::getContentType, contentType)
                        .eq(ClassicsContentVersionDO::getContentId, ClassicsContentIdCodec.toValue(contentId))
                        .orderByDesc(ClassicsContentVersionDO::getVersionNo)));
    }

    public ClassicsContentVersionId insertVersion(ClassicsContentVersion version) {
        ClassicsContentVersionDO dataObject = ClassicsContentPersistenceAssembler.toVersionObject(version);
        versionMapper.insert(dataObject);
        return ClassicsContentVersionIdCodec.toDomain(dataObject.getId());
    }

    public ClassicsContentVersion getVersionById(ClassicsContentVersionId id) {
        return ClassicsContentPersistenceAssembler.toVersionDomain(
                versionMapper.selectById(ClassicsContentVersionIdCodec.toValue(id)));
    }

    public ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob) {
        ClassicsContentExportJobDO dataObject = ClassicsContentPersistenceAssembler.toExportObject(exportJob);
        exportMapper.insert(dataObject);
        return ClassicsContentExportJobIdCodec.toDomain(dataObject.getId());
    }

    public int updateExportJob(ClassicsContentExportJob exportJob) {
        return exportMapper.updateById(ClassicsContentPersistenceAssembler.toExportObject(exportJob));
    }

    public Page<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<ClassicsContentExportJobDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(contentType), ClassicsContentExportJobDO::getContentType, contentType)
                .eq(StringUtils.isNotBlank(exportKind), ClassicsContentExportJobDO::getExportKind, exportKind)
                .eq(StringUtils.isNotBlank(status), ClassicsContentExportJobDO::getStatus, status)
                .orderByDesc(ClassicsContentExportJobDO::getRequestedAt);
        Page<ClassicsContentExportJobDO> dataPage = exportMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<ClassicsContentExportJob> entityPage = new Page<>(dataPage.getCurrent(), dataPage.getSize());
        entityPage.setTotal(dataPage.getTotal());
        entityPage.setRecords(ClassicsContentPersistenceAssembler.toExportDomainList(dataPage.getRecords()));
        return entityPage;
    }

    private static int maxPriority(List<Object> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        Object max = values.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (max == null) {
            return 0;
        }
        if (max instanceof Number) {
            return ((Number) max).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(max));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
