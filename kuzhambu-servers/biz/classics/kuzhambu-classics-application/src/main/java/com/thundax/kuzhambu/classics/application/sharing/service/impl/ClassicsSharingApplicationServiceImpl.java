package com.thundax.kuzhambu.classics.application.sharing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.query.ShareAccessQuery;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
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
public class ClassicsSharingApplicationServiceImpl implements ClassicsSharingApplicationService {

    private final ClassicsSharingRepository repository;

    public ClassicsSharingApplicationServiceImpl(ClassicsSharingRepository repository) {
        this.repository = repository;
    }

    @Override
    public ClassicsShareLink getLink(Long id) {
        return id == null ? null : repository.getLinkById(id);
    }

    @Override
    public ClassicsShareLink getLinkByTokenHash(String tokenHash) {
        return repository.getLinkByTokenHash(tokenHash);
    }

    @Override
    public PageResult<ClassicsShareLink> pageLinks(String status, String visibility, PageQuery page) {
        IPage<ClassicsShareLink> dataPage = repository.pageLinks(status, visibility, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLink(ShareLinkCreateCommand command) {
        ClassicsShareLink link = command.toLink();
        if (link.getIssuedAt() == null) {
            link.setIssuedAt(LocalDateTime.now());
        }
        Long linkId = repository.insertLink(link);
        for (ClassicsShareTarget target : command.getTargets()) {
            target.setShareLinkId(linkId);
            repository.insertTarget(target);
        }
        return linkId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(ShareLinkStatusCommand command) {
        repository.updateLinkStatus(command.getId(), command.getStatus().value());
    }

    @Override
    public List<ClassicsShareTarget> listTargets(Long shareLinkId) {
        return repository.listTargetsByLinkId(shareLinkId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAccess(ClassicsShareAccessRecord record) {
        if (record.getAccessedAt() == null) {
            record.setAccessedAt(LocalDateTime.now());
        }
        repository.insertAccessRecord(record);
        repository.increaseAccessCount(record.getShareLinkId());
    }

    @Override
    public PageResult<ClassicsShareAccessRecord> pageAccessRecords(ShareAccessQuery query, PageQuery page) {
        IPage<ClassicsShareAccessRecord> dataPage = repository.pageAccessRecords(
                query == null ? null : query.getShareLinkId(),
                query == null ? null : query.getShareTargetId(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }
}
