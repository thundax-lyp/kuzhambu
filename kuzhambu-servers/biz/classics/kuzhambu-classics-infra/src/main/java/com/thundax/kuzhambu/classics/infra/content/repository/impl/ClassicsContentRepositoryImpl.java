package com.thundax.kuzhambu.classics.infra.content.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
                        .eq(ClassicsContentTagDO::getContentType, contentType)
                        .eq(ClassicsContentTagDO::getContentId, ClassicsContentIdCodec.toValue(contentId))
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsContentTagDO::getPriority)));
    }

    public ClassicsContentTagId insertTag(ClassicsContentTag tag) {
        ClassicsContentTagDO dataObject = ClassicsContentPersistenceAssembler.toTagObject(tag);
        tagMapper.insert(dataObject);
        return ClassicsContentTagIdCodec.toDomain(dataObject.getId());
    }

    public int updateTag(ClassicsContentTag tag) {
        return tagMapper.updateById(ClassicsContentPersistenceAssembler.toTagObject(tag));
    }

    public int deleteTagById(ClassicsContentTagId id) {
        return tagMapper.deleteById(ClassicsContentTagIdCodec.toValue(id));
    }

    public List<ClassicsContentQaPair> listQaPairs(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
        return ClassicsContentPersistenceAssembler.toQaDomainList(
                qaPairMapper.selectList(new LambdaQueryWrapper<ClassicsContentQaPairDO>()
                        .eq(ClassicsContentQaPairDO::getContentType, contentType)
                        .eq(ClassicsContentQaPairDO::getContentId, ClassicsContentIdCodec.toValue(contentId))
                        .orderBy(true, sortDirection != SortDirection.DESC, ClassicsContentQaPairDO::getPriority)));
    }

    public ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair) {
        ClassicsContentQaPairDO dataObject = ClassicsContentPersistenceAssembler.toQaObject(qaPair);
        qaPairMapper.insert(dataObject);
        return ClassicsContentQaPairIdCodec.toDomain(dataObject.getId());
    }

    public int updateQaPair(ClassicsContentQaPair qaPair) {
        return qaPairMapper.updateById(ClassicsContentPersistenceAssembler.toQaObject(qaPair));
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
}
