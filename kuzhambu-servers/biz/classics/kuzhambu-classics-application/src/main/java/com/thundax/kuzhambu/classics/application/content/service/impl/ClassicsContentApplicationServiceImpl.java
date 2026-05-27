package com.thundax.kuzhambu.classics.application.content.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class ClassicsContentApplicationServiceImpl implements ClassicsContentApplicationService {

    private final ClassicsContentRepository repository;

    public ClassicsContentApplicationServiceImpl(ClassicsContentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ClassicsContentTag> listTags(String contentType, Long contentId) {
        return repository.listTags(contentType, contentId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTag(ContentTagCommand command) {
        ClassicsContentTag tag = command.toEntity();
        if (tag.getId() == null) {
            return repository.insertTag(tag);
        }
        repository.updateTag(tag);
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        repository.deleteTagById(id);
    }

    @Override
    public List<ClassicsContentQaPair> listQaPairs(String contentType, Long contentId) {
        return repository.listQaPairs(contentType, contentId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        if (qaPair.getId() == null) {
            return repository.insertQaPair(qaPair);
        }
        repository.updateQaPair(qaPair);
        return qaPair.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQaPair(Long id) {
        repository.deleteQaPairById(id);
    }

    @Override
    public List<ClassicsContentVersion> listVersions(String contentType, Long contentId) {
        return repository.listVersions(contentType, contentId);
    }

    @Override
    public ClassicsContentVersion getVersion(Long id) {
        return repository.getVersionById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createExportJob(ContentExportCommand command) {
        ClassicsContentExportJob job = command.toEntity();
        if (job.getRequestedAt() == null) {
            job.setRequestedAt(LocalDateTime.now());
        }
        return repository.insertExportJob(job);
    }

    @Override
    public PageResult<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, PageQuery page) {
        IPage<ClassicsContentExportJob> dataPage =
                repository.pageExportJobs(contentType, exportKind, status, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }
}
