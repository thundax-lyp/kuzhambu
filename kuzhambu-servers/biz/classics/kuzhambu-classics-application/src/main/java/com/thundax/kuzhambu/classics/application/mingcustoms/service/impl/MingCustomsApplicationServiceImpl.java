package com.thundax.kuzhambu.classics.application.mingcustoms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsSaveCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class MingCustomsApplicationServiceImpl implements MingCustomsApplicationService {

    private final MingCustomsRepository repository;

    public MingCustomsApplicationServiceImpl(MingCustomsRepository repository) {
        this.repository = repository;
    }

    @Override
    public MingCustomsEntry get(Long id) {
        return id == null ? null : repository.getById(id);
    }

    @Override
    public PageResult<MingCustomsEntry> page(MingCustomsPageQuery query, PageQuery page) {
        IPage<MingCustomsEntry> dataPage = repository.page(
                query == null ? null : query.getCategory(),
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getTagName(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? SortDirection.ASC : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(MingCustomsSaveCommand command) {
        MingCustomsEntry entry = toEntry(command);
        if (entry.getId() == null) {
            return repository.insert(entry);
        }
        repository.update(entry);
        return entry.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeVisibility(Long id, String visibility) {
        repository.updateVisibility(id, visibility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<MingCustomsKeyword> listKeywords(Long customId) {
        return repository.listKeywordsByCustomId(customId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addKeyword(MingCustomsKeywordCommand command) {
        MingCustomsKeyword keyword = new MingCustomsKeyword();
        keyword.setCustomId(command.getCustomId());
        keyword.setKeyword(command.getKeyword());
        keyword.setPriority(command.getPriority());
        return repository.insertKeyword(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKeyword(Long id) {
        repository.deleteKeywordById(id);
    }

    @Override
    public List<String> listKeywordCloud(String visibility) {
        return repository.listKeywordCloud(visibility);
    }

    private static MingCustomsEntry toEntry(MingCustomsSaveCommand command) {
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(command.getId());
        entry.setTitle(command.getTitle());
        entry.setCategory(command.getCategory());
        entry.setChapter(command.getChapter());
        entry.setSection(command.getSection());
        entry.setSummary(command.getSummary());
        entry.setContentFormat(command.getContentFormat());
        entry.setContent(command.getContent());
        entry.setOriginalExcerpts(command.getOriginalExcerpts());
        entry.setVisibility(command.getVisibility());
        return entry;
    }
}
