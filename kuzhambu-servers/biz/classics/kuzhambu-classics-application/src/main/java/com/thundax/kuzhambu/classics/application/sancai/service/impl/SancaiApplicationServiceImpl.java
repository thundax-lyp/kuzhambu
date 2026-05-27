package com.thundax.kuzhambu.classics.application.sancai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
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
public class SancaiApplicationServiceImpl implements SancaiApplicationService {

    private final SancaiRepository repository;

    public SancaiApplicationServiceImpl(SancaiRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SancaiCategory> listCategories() {
        return repository.listCategories(SortDirection.ASC);
    }

    @Override
    public List<SancaiVolume> listVolumes(Long categoryId) {
        return repository.listVolumesByCategoryId(categoryId, SortDirection.ASC);
    }

    @Override
    public SancaiEntry getEntry(Long id) {
        return id == null ? null : repository.getEntryById(id);
    }

    @Override
    public PageResult<SancaiEntry> pageEntries(SancaiEntryPageQuery query, PageQuery page) {
        IPage<SancaiEntry> dataPage = repository.pageEntries(
                query == null ? null : query.getVolumeId(),
                query == null ? null : query.getKeyword(),
                query == null || query.getLifecycleStatus() == null
                        ? null
                        : query.getLifecycleStatus().value(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null || query.getTranslationStatus() == null
                        ? null
                        : query.getTranslationStatus().value(),
                query == null || query.getImageStatus() == null
                        ? null
                        : query.getImageStatus().value(),
                query == null || query.getVisualAssetStatus() == null
                        ? null
                        : query.getVisualAssetStatus().value(),
                query == null || query.getRefinementStatus() == null
                        ? null
                        : query.getRefinementStatus().value(),
                query == null ? SortDirection.ASC : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveEntry(SancaiEntrySaveCommand command) {
        if (command == null) {
            return null;
        }
        SancaiEntry entry = toEntry(command);
        if (entry.getId() == null) {
            return repository.insertEntry(entry);
        }
        repository.updateEntry(entry);
        return entry.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeEntryStatus(SancaiEntryStatusCommand command) {
        if (command == null || command.getId() == null) {
            return;
        }
        SancaiEntry entry = new SancaiEntry();
        entry.setId(command.getId());
        entry.setLifecycleStatus(command.getLifecycleStatus());
        repository.updateEntryStatus(entry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeEntryVisibility(Long id, String visibility) {
        repository.updateEntryVisibility(id, visibility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEntry(Long id) {
        repository.deleteEntryById(id);
    }

    private static SancaiEntry toEntry(SancaiEntrySaveCommand command) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(command.getId());
        entry.setVolumeId(command.getVolumeId());
        entry.setTitle(command.getTitle());
        entry.setOriginalText(command.getOriginalText());
        entry.setTranslationText(command.getTranslationText());
        entry.setSummary(command.getSummary());
        entry.setLifecycleStatus(command.getLifecycleStatus());
        entry.setVisibility(command.getVisibility());
        entry.setTranslationStatus(command.getTranslationStatus());
        entry.setImageStatus(command.getImageStatus());
        entry.setVisualAssetStatus(command.getVisualAssetStatus());
        entry.setRefinementStatus(command.getRefinementStatus());
        entry.setPriority(command.getPriority());
        return entry;
    }
}
