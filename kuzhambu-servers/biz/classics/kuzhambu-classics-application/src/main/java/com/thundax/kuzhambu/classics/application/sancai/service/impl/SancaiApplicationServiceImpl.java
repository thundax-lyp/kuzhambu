package com.thundax.kuzhambu.classics.application.sancai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategorySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<SancaiVolume> listVolumes(SancaiCategoryId categoryId) {
        return repository.listVolumesByCategoryId(categoryId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortCategories(SancaiCategorySortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<SancaiCategoryId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<SancaiCategory> currentCategories = repository.listCategories(effectiveDirection);
        if (currentCategories == null || currentCategories.isEmpty() || currentCategories.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentCategories.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentCategories.size());
        List<SancaiCategoryId> currentOrderedIds = new ArrayList<>(currentCategories.size());
        for (int i = 0; i < currentCategories.size(); i++) {
            SancaiCategory category = currentCategories.get(i);
            if (category == null || category.getId() == null) {
                throw sortDbFailure();
            }
            long categoryId = category.getId().value();
            indexById.put(categoryId, i);
            priorityById.put(categoryId, category.getPriority());
            currentOrderedIds.add(category.getId());
        }

        validateOrderedIds(orderedIdList, indexById);
        int temporaryPriority = repository.maxCategoryPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            SancaiCategoryId targetId = orderedIdList.get(i);
            SancaiCategoryId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateCategoryPriorityOrThrow(targetId, temporaryPriority++);
            updateCategoryPriorityOrThrow(currentId, targetPriority);
            updateCategoryPriorityOrThrow(targetId, currentPriority);

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortVolumes(SancaiVolumeSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<SancaiVolumeId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<SancaiVolume> currentVolumes = repository.listVolumes(effectiveDirection);
        if (currentVolumes == null || currentVolumes.isEmpty() || currentVolumes.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentVolumes.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentVolumes.size());
        List<SancaiVolumeId> currentOrderedIds = new ArrayList<>(currentVolumes.size());
        for (int i = 0; i < currentVolumes.size(); i++) {
            SancaiVolume volume = currentVolumes.get(i);
            if (volume == null || volume.getId() == null) {
                throw sortDbFailure();
            }
            long volumeId = volume.getId().value();
            indexById.put(volumeId, i);
            priorityById.put(volumeId, volume.getPriority());
            currentOrderedIds.add(volume.getId());
        }

        validateOrderedIds(orderedIdList, indexById);
        int temporaryPriority = repository.maxVolumePriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            SancaiVolumeId targetId = orderedIdList.get(i);
            SancaiVolumeId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateVolumePriorityOrThrow(targetId, temporaryPriority++);
            updateVolumePriorityOrThrow(currentId, targetPriority);
            updateVolumePriorityOrThrow(targetId, currentPriority);

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortEntries(SancaiEntrySortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<SancaiEntryId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<SancaiEntry> currentEntries = repository.listEntries(effectiveDirection);
        if (currentEntries == null || currentEntries.isEmpty() || currentEntries.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentEntries.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentEntries.size());
        List<SancaiEntryId> currentOrderedIds = new ArrayList<>(currentEntries.size());
        for (int i = 0; i < currentEntries.size(); i++) {
            SancaiEntry entry = currentEntries.get(i);
            if (entry == null || entry.getId() == null) {
                throw sortDbFailure();
            }
            long entryId = entry.getId().value();
            indexById.put(entryId, i);
            priorityById.put(entryId, entry.getPriority());
            currentOrderedIds.add(entry.getId());
        }

        validateOrderedIds(orderedIdList, indexById);
        int temporaryPriority = repository.maxEntryPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            SancaiEntryId targetId = orderedIdList.get(i);
            SancaiEntryId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateEntryPriorityOrThrow(targetId, temporaryPriority++);
            updateEntryPriorityOrThrow(currentId, targetPriority);
            updateEntryPriorityOrThrow(targetId, currentPriority);

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    public SancaiEntry getEntry(SancaiEntryId id) {
        return id == null ? null : repository.getEntryById(id);
    }

    @Override
    public PageResult<SancaiEntry> pageEntries(SancaiEntryPageQuery query, PageQuery page) {
        IPage<SancaiEntry> dataPage = repository.pageEntries(
                query == null ? null : SancaiVolumeIdCodec.toDomain(query.getVolumeId()),
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
    public SancaiEntryId saveEntry(SancaiEntrySaveCommand command) {
        if (command == null) {
            return null;
        }
        SancaiEntry entry = toEntry(command);
        if (entry.getId() == null) {
            entry.setPriority(repository.maxEntryPriority() + 1);
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
        entry.setId(SancaiEntryIdCodec.toDomain(command.getId()));
        entry.setLifecycleStatus(command.getLifecycleStatus());
        repository.updateEntryStatus(entry);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeEntryVisibility(SancaiEntryId id, String visibility) {
        repository.updateEntryVisibility(id, visibility);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEntry(SancaiEntryId id) {
        repository.deleteEntryById(id);
    }

    private static void validateOrderedIds(List<? extends Object> orderedIds, Map<Long, Integer> indexById) {
        for (Object item : orderedIds) {
            Long idValue = null;
            if (item instanceof SancaiCategoryId) {
                idValue = ((SancaiCategoryId) item).value();
            } else if (item instanceof SancaiVolumeId) {
                idValue = ((SancaiVolumeId) item).value();
            } else if (item instanceof SancaiEntryId) {
                idValue = ((SancaiEntryId) item).value();
            }
            if (idValue == null || !indexById.containsKey(idValue)) {
                throw sortMissingId();
            }
        }
    }

    private void updateCategoryPriorityOrThrow(SancaiCategoryId id, int priority) {
        SancaiCategory category = new SancaiCategory();
        category.setId(id);
        category.setPriority(priority);
        if (repository.updateCategoryPriority(category) != 1) {
            throw sortDbFailure();
        }
    }

    private void updateVolumePriorityOrThrow(SancaiVolumeId id, int priority) {
        SancaiVolume volume = new SancaiVolume();
        volume.setId(id);
        volume.setPriority(priority);
        if (repository.updateVolumePriority(volume) != 1) {
            throw sortDbFailure();
        }
    }

    private void updateEntryPriorityOrThrow(SancaiEntryId id, int priority) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(id);
        entry.setPriority(priority);
        if (repository.updateEntryPriority(entry) != 1) {
            throw sortDbFailure();
        }
    }

    private static BizException sortEmptyInput() {
        return new BizException(
                ErrorCode.SORT_EMPTY_INPUT.getCode(),
                ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                ErrorCode.SORT_EMPTY_INPUT.getMessage());
    }

    private static BizException sortMissingId() {
        return new BizException(
                ErrorCode.SORT_MISSING_ID.getCode(),
                ErrorCode.SORT_MISSING_ID.getMessageKey(),
                ErrorCode.SORT_MISSING_ID.getMessage());
    }

    private static BizException sortDbFailure() {
        return new BizException(
                ErrorCode.SORT_DB_FAILURE.getCode(),
                ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                ErrorCode.SORT_DB_FAILURE.getMessage());
    }

    private static SancaiEntry toEntry(SancaiEntrySaveCommand command) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(command.getId()));
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(command.getVolumeId()));
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
        return entry;
    }
}
