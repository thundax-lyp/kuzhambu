package com.thundax.kuzhambu.classics.infra.content.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentExportJobIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.infra.content.persistence.assembler.ClassicsContentPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentExportJobDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentQaPairDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentTagDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentVersionDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentQaPairMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentTagMapper;
import com.thundax.kuzhambu.classics.infra.content.persistence.mapper.ClassicsContentVersionMapper;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.assembler.MingCustomsPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsEntryDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper.MingCustomsEntryMapper;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler.SancaiPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper.SancaiMapper;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler.WangqiDocumentPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentDO;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper.WangqiDocumentMapper;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Date;
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
    private final SancaiMapper sancaiMapper;
    private final WangqiDocumentMapper wangqiDocumentMapper;
    private final MingCustomsEntryMapper mingCustomsEntryMapper;

    public ClassicsContentRepositoryImpl(
            ClassicsContentTagMapper tagMapper,
            ClassicsContentQaPairMapper qaPairMapper,
            ClassicsContentVersionMapper versionMapper,
            ClassicsContentMapper exportMapper,
            SancaiMapper sancaiMapper,
            WangqiDocumentMapper wangqiDocumentMapper,
            MingCustomsEntryMapper mingCustomsEntryMapper) {
        this.tagMapper = tagMapper;
        this.qaPairMapper = qaPairMapper;
        this.versionMapper = versionMapper;
        this.exportMapper = exportMapper;
        this.sancaiMapper = sancaiMapper;
        this.wangqiDocumentMapper = wangqiDocumentMapper;
        this.mingCustomsEntryMapper = mingCustomsEntryMapper;
    }

    public List<ClassicsContentTag> listTags(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
        return ClassicsContentPersistenceAssembler.toTagDomainList(tagMapper.selectList(new LambdaQueryWrapper<
                        ClassicsContentTagDO>()
                .eq(StringUtils.isNotBlank(contentType), ClassicsContentTagDO::getContentType, contentType)
                .eq(contentId != null, ClassicsContentTagDO::getContentId, ClassicsContentIdCodec.toValue(contentId))
                .orderBy(true, sortDirection != SortDirection.DESC, ClassicsContentTagDO::getPriority)));
    }

    @Override
    public int maxTagPriority(String contentType, ClassicsContentId contentId) {
        return maxPriority(tagMapper.selectObjs(new QueryWrapper<ClassicsContentTagDO>()
                .select("max(priority)")
                .eq(StringUtils.isNotBlank(contentType), "content_type", contentType)
                .eq(contentId != null, "content_id", ClassicsContentIdCodec.toValue(contentId))));
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

    public int deleteTagById(String contentType, ClassicsContentId contentId, ClassicsContentTagId id) {
        return tagMapper.delete(new LambdaUpdateWrapper<ClassicsContentTagDO>()
                .eq(ClassicsContentTagDO::getId, ClassicsContentTagIdCodec.toValue(id))
                .eq(StringUtils.isNotBlank(contentType), ClassicsContentTagDO::getContentType, contentType)
                .eq(contentId != null, ClassicsContentTagDO::getContentId, ClassicsContentIdCodec.toValue(contentId)));
    }

    public List<ClassicsContentQaPair> listQaPairs(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
        return ClassicsContentPersistenceAssembler.toQaDomainList(qaPairMapper.selectList(new LambdaQueryWrapper<
                        ClassicsContentQaPairDO>()
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
        return maxPriority(
                qaPairMapper.selectObjs(new QueryWrapper<ClassicsContentQaPairDO>().select("max(priority)")));
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

    @Override
    public SancaiEntry getSancaiEntryForAiApply(ClassicsContentId contentId) {
        return SancaiPersistenceAssembler.toEntryDomain(
                sancaiMapper.selectById(ClassicsContentIdCodec.toValue(contentId)));
    }

    @Override
    public int updateSancaiEntryAiFields(SancaiEntry entry) {
        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        return sancaiMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, dataObject.getId())
                        .set(SancaiEntryDO::getSummary, dataObject.getSummary())
                        .set(SancaiEntryDO::getTranslationText, dataObject.getTranslationText())
                        .set(SancaiEntryDO::getTranslationStatus, dataObject.getTranslationStatus())
                        .set(SancaiEntryDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int updateSancaiEntryVersionMarkers(SancaiEntry entry) {
        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        return sancaiMapper.update(
                null,
                new LambdaUpdateWrapper<SancaiEntryDO>()
                        .eq(SancaiEntryDO::getId, dataObject.getId())
                        .set(SancaiEntryDO::getCurrentVersionId, dataObject.getCurrentVersionId())
                        .set(SancaiEntryDO::getCurrentVersionNo, dataObject.getCurrentVersionNo())
                        .set(SancaiEntryDO::getCurrentVersionedAt, dataObject.getCurrentVersionedAt())
                        .set(SancaiEntryDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public WangqiDocument getWangqiDocumentForAiApply(ClassicsContentId contentId) {
        return WangqiDocumentPersistenceAssembler.toDomain(
                wangqiDocumentMapper.selectById(ClassicsContentIdCodec.toValue(contentId)));
    }

    @Override
    public int updateWangqiDocumentAiFields(WangqiDocument document) {
        WangqiDocumentDO dataObject = WangqiDocumentPersistenceAssembler.toObject(document);
        return wangqiDocumentMapper.update(
                null,
                new LambdaUpdateWrapper<WangqiDocumentDO>()
                        .eq(WangqiDocumentDO::getId, dataObject.getId())
                        .set(WangqiDocumentDO::getSummary, dataObject.getSummary())
                        .set(WangqiDocumentDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int updateWangqiDocumentVersionMarkers(WangqiDocument document) {
        WangqiDocumentDO dataObject = WangqiDocumentPersistenceAssembler.toObject(document);
        return wangqiDocumentMapper.update(
                null,
                new LambdaUpdateWrapper<WangqiDocumentDO>()
                        .eq(WangqiDocumentDO::getId, dataObject.getId())
                        .set(WangqiDocumentDO::getCurrentVersionId, dataObject.getCurrentVersionId())
                        .set(WangqiDocumentDO::getCurrentVersionNo, dataObject.getCurrentVersionNo())
                        .set(WangqiDocumentDO::getCurrentVersionedAt, dataObject.getCurrentVersionedAt())
                        .set(WangqiDocumentDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public MingCustomsEntry getMingCustomsEntryForAiApply(ClassicsContentId contentId) {
        return MingCustomsPersistenceAssembler.toEntryDomain(
                mingCustomsEntryMapper.selectById(ClassicsContentIdCodec.toValue(contentId)));
    }

    @Override
    public int updateMingCustomsEntryAiFields(MingCustomsEntry entry) {
        MingCustomsEntryDO dataObject = MingCustomsPersistenceAssembler.toEntryObject(entry);
        return mingCustomsEntryMapper.update(
                null,
                new LambdaUpdateWrapper<MingCustomsEntryDO>()
                        .eq(MingCustomsEntryDO::getId, dataObject.getId())
                        .set(MingCustomsEntryDO::getSummary, dataObject.getSummary())
                        .set(MingCustomsEntryDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int updateMingCustomsEntryVersionMarkers(MingCustomsEntry entry) {
        MingCustomsEntryDO dataObject = MingCustomsPersistenceAssembler.toEntryObject(entry);
        return mingCustomsEntryMapper.update(
                null,
                new LambdaUpdateWrapper<MingCustomsEntryDO>()
                        .eq(MingCustomsEntryDO::getId, dataObject.getId())
                        .set(MingCustomsEntryDO::getCurrentVersionId, dataObject.getCurrentVersionId())
                        .set(MingCustomsEntryDO::getCurrentVersionNo, dataObject.getCurrentVersionNo())
                        .set(MingCustomsEntryDO::getCurrentVersionedAt, dataObject.getCurrentVersionedAt())
                        .set(MingCustomsEntryDO::getContentUpdatedAt, dataObject.getContentUpdatedAt()));
    }

    @Override
    public int deleteAiTags(String contentType, ClassicsContentId contentId) {
        return tagMapper.delete(new LambdaQueryWrapper<ClassicsContentTagDO>()
                .eq(StringUtils.isNotBlank(contentType), ClassicsContentTagDO::getContentType, contentType)
                .eq(
                        ClassicsContentIdCodec.toValue(contentId) != null,
                        ClassicsContentTagDO::getContentId,
                        ClassicsContentIdCodec.toValue(contentId))
                .eq(ClassicsContentTagDO::getSource, ClassicsContentSource.AI.value()));
    }

    @Override
    public int deleteAiQaPairs(String contentType, ClassicsContentId contentId) {
        return qaPairMapper.delete(new LambdaQueryWrapper<ClassicsContentQaPairDO>()
                .eq(StringUtils.isNotBlank(contentType), ClassicsContentQaPairDO::getContentType, contentType)
                .eq(
                        ClassicsContentIdCodec.toValue(contentId) != null,
                        ClassicsContentQaPairDO::getContentId,
                        ClassicsContentIdCodec.toValue(contentId))
                .eq(ClassicsContentQaPairDO::getSource, ClassicsContentSource.AI.value()));
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

    public int deleteVersions(String contentType, ClassicsContentId contentId) {
        return versionMapper.delete(new LambdaQueryWrapper<ClassicsContentVersionDO>()
                .eq(ClassicsContentVersionDO::getContentType, contentType)
                .eq(ClassicsContentVersionDO::getContentId, ClassicsContentIdCodec.toValue(contentId)));
    }

    public ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob) {
        ClassicsContentExportJobDO dataObject = ClassicsContentPersistenceAssembler.toExportObject(exportJob);
        exportMapper.insert(dataObject);
        return ClassicsContentExportJobIdCodec.toDomain(dataObject.getId());
    }

    public ClassicsContentExportJob getExportJobById(ClassicsContentExportJobId id) {
        return ClassicsContentPersistenceAssembler.toExportDomain(
                exportMapper.selectById(ClassicsContentExportJobIdCodec.toValue(id)));
    }

    public int updateExportJob(ClassicsContentExportJob exportJob) {
        return exportMapper.updateById(ClassicsContentPersistenceAssembler.toExportObject(exportJob));
    }

    @Override
    public int markExportJobCompleted(
            ClassicsContentExportJobId id,
            StorageObjectId storageObjectId,
            Date expiresAt,
            int itemCount,
            int assetCount) {
        return exportMapper.markExportJobCompleted(
                ClassicsContentExportJobIdCodec.toValue(id),
                ClassicsExportStatus.COMPLETED.value(),
                StorageObjectIdCodec.toValue(storageObjectId),
                expiresAt,
                itemCount,
                assetCount);
    }

    @Override
    public int markExportJobFailed(ClassicsContentExportJobId id) {
        return exportMapper.markExportJobStatus(
                ClassicsContentExportJobIdCodec.toValue(id), ClassicsExportStatus.FAILED.value());
    }

    @Override
    public int markExportJobExpired(ClassicsContentExportJobId id) {
        return exportMapper.markExportJobStatus(
                ClassicsContentExportJobIdCodec.toValue(id), ClassicsExportStatus.EXPIRED.value());
    }

    public PageResult<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, int pageNo, int pageSize) {
        LambdaQueryWrapper<ClassicsContentExportJobDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(contentType), ClassicsContentExportJobDO::getContentType, contentType)
                .eq(StringUtils.isNotBlank(exportKind), ClassicsContentExportJobDO::getExportKind, exportKind)
                .eq(StringUtils.isNotBlank(status), ClassicsContentExportJobDO::getStatus, status)
                .orderByDesc(ClassicsContentExportJobDO::getRequestedAt);
        Page<ClassicsContentExportJobDO> dataPage = exportMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataPage.getCurrent(),
                (int) dataPage.getSize(),
                dataPage.getTotal(),
                ClassicsContentPersistenceAssembler.toExportDomainList(dataPage.getRecords()));
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
