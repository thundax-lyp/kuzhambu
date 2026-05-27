package com.thundax.kuzhambu.system.application.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.system.application.core.dao.DictDao;
import com.thundax.kuzhambu.system.application.core.service.DictService;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeDictInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateDictCommand;
import com.thundax.kuzhambu.system.application.core.service.command.DictSortCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DictQuery;
import com.thundax.kuzhambu.system.domain.model.entity.Dict;
import com.thundax.kuzhambu.system.domain.model.valueobject.DictId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class DictServiceImpl implements DictService {

    private static final int PRIORITY_STEP = 10;

    private final DictDao dao;

    public DictServiceImpl(DictDao dao) {
        this.dao = dao;
    }

    public Dict get(DictId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<String> listTypes(DictQuery query) {
        return dao.listTypes();
    }

    public List<String> listLabels(DictQuery query) {
        List<String> result = new ArrayList<String>();
        List<Dict> list = list(query);
        String s = "";
        for (Dict item : list) {
            s = item.getLabel();
            if (StringUtils.isNotEmpty(s)) {
                result.add(s);
            }
        }
        return result;
    }

    public List<Dict> list(DictQuery query) {
        return dao.list(
                query == null ? null : query.getType(),
                query == null ? null : query.getLabel(),
                query == null ? null : query.getRemarks());
    }

    public PageResult<Dict> page(DictQuery query, PageQuery page) {
        IPage<Dict> dataPage = dao.page(
                query == null ? null : query.getType(),
                query == null ? null : query.getLabel(),
                query == null ? null : query.getRemarks(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictId create(CreateDictCommand command) {
        Dict dict = toEntity(command);
        dict.setPriority(dao.maxPriority() + PRIORITY_STEP);
        dict.setId(dao.insert(dict));
        return dict.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(DictSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<DictId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<Dict> selectedDicts = dao.listByIds(toValues(orderedIdList));
        Map<Long, String> typeById = new HashMap<>();
        String dictType = null;
        if (selectedDicts == null || selectedDicts.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }
        for (Dict dict : selectedDicts) {
            if (dict == null || dict.getId() == null) {
                continue;
            }
            Long idValue = dict.getId().value();
            typeById.put(idValue, dict.getType());
            if (dictType == null) {
                dictType = dict.getType();
            }
        }

        for (DictId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
            String currentType = typeById.get(orderedId.value());
            if (StringUtils.isEmpty(dictType)) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
            if (!Objects.equals(dictType, currentType)) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        List<Dict> currentDicts = dao.listByType(dictType, effectiveDirection);
        if (currentDicts == null || currentDicts.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        if (currentDicts.size() != orderedIdList.size()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        Map<Long, Integer> indexById = new HashMap<>(currentDicts.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentDicts.size());
        List<DictId> currentOrderedIds = new ArrayList<>(currentDicts.size());

        for (int i = 0; i < currentDicts.size(); i++) {
            Dict dict = currentDicts.get(i);
            if (dict == null || dict.getId() == null) {
                throw new BizException(
                        ErrorCode.SORT_DB_FAILURE.getCode(),
                        ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                        ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long dictId = dict.getId().value();
            indexById.put(dictId, i);
            priorityById.put(dictId, dict.getPriority());
            currentOrderedIds.add(dict.getId());
        }

        for (DictId orderedId : orderedIdList) {
            if (!indexById.containsKey(orderedId.value())) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        int temporaryPriority = dao.maxPriority() + PRIORITY_STEP;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            DictId targetId = orderedIdList.get(i);
            DictId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updatePriorityOrThrow(targetId, temporaryPriority++, "暂态更新失败");
            updatePriorityOrThrow(currentId, targetPriority, "交换更新失败");
            updatePriorityOrThrow(targetId, currentPriority, "交换更新失败");

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
    public void changeInfo(ChangeDictInfoCommand command) {
        dao.update(toEntity(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(DictId id) {
        if (id != null) {
            dao.deleteById(id);
        }
    }

    private Dict toEntity(CreateDictCommand command) {
        Dict dict = new Dict();
        if (command == null) {
            return dict;
        }
        dict.setType(command.getType());
        dict.setLabel(command.getLabel());
        dict.setValue(command.getValue());
        dict.setRemarks(command.getRemarks());
        return dict;
    }

    private List<Long> toValues(List<DictId> ids) {
        List<Long> values = new ArrayList<>(ids.size());
        for (DictId id : ids) {
            values.add(id.value());
        }
        return values;
    }

    private void updatePriorityOrThrow(DictId id, int priority, String message) {
        Dict dict = new Dict();
        dict.setId(id);
        dict.setPriority(priority);

        int updated = dao.updatePriority(dict);
        if (updated != 1) {
            throw new BizException(
                    ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessageKey(), message);
        }
    }

    private Dict toEntity(ChangeDictInfoCommand command) {
        Dict dict = new Dict();
        if (command == null) {
            return dict;
        }
        dict.setId(command.getId());
        dict.setType(command.getType());
        dict.setLabel(command.getLabel());
        dict.setValue(command.getValue());
        dict.setRemarks(command.getRemarks());
        return dict;
    }
}
