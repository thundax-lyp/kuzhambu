package com.thundax.kuzhambu.system.application.core.service.impl;

import com.thundax.kuzhambu.common.audit.annotation.AuditLog;
import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.core.sort.SortablePrioritySwapSupport;
import com.thundax.kuzhambu.system.application.core.command.ChangeDictInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateDictCommand;
import com.thundax.kuzhambu.system.application.core.command.DictSortCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveDictCommand;
import com.thundax.kuzhambu.system.application.core.query.DictQuery;
import com.thundax.kuzhambu.system.application.core.query.GetDictQuery;
import com.thundax.kuzhambu.system.application.core.service.DictionaryManagementApplicationService;
import com.thundax.kuzhambu.system.domain.core.model.entity.Dict;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DictId;
import com.thundax.kuzhambu.system.domain.core.repository.DictRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class DictionaryManagementApplicationServiceImpl implements DictionaryManagementApplicationService {

    private static final int PRIORITY_STEP = 1;

    private final DictRepository dao;

    public DictionaryManagementApplicationServiceImpl(DictRepository dao) {
        this.dao = dao;
    }

    public Dict get(GetDictQuery query) {
        DictId id = query == null ? null : query.id();
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
                query == null ? null : query.type(),
                query == null ? null : query.label(),
                query == null ? null : query.remarks());
    }

    public PageResult<Dict> page(DictQuery query, PageQuery page) {
        return dao.page(
                query == null ? null : query.type(),
                query == null ? null : query.label(),
                query == null ? null : query.remarks(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Dict", id = "", action = AuditAction.CREATE, summary = "创建字典")
    public DictId create(CreateDictCommand command) {
        Dict dict = toDomain(command);
        dict.setPriority(dao.maxPriority() + PRIORITY_STEP);
        dict.setId(dao.insert(dict));
        return dict.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(DictSortCommand command) {
        List<DictId> orderedIdList =
                command == null || command.orderedIds() == null ? Collections.emptyList() : command.orderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<Dict> currentDicts = dao.list(SortDirection.ASC);
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                currentDicts,
                Dict::getId,
                DictId::value,
                Dict::getPriority,
                dao::maxPriority,
                (id, priority) -> updatePriorityOrThrow(id, priority, "排序更新失败"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(type = "Dict", id = "#command.id().value()", action = AuditAction.UPDATE, summary = "更新字典")
    public void changeInfo(ChangeDictInfoCommand command) {
        dao.update(toDomain(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @AuditLog(
            type = "Dict",
            id = "#command.id() == null ? null : #command.id().value()",
            action = AuditAction.DELETE,
            summary = "删除字典")
    public void remove(RemoveDictCommand command) {
        DictId id = command == null ? null : command.id();
        if (id != null) {
            dao.deleteById(id);
        }
    }

    private Dict toDomain(CreateDictCommand command) {
        Dict dict = new Dict();
        if (command == null) {
            return dict;
        }
        dict.setType(command.type());
        dict.setLabel(command.label());
        dict.setValue(command.value());
        dict.setRemarks(command.remarks());
        return dict;
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

    private Dict toDomain(ChangeDictInfoCommand command) {
        Dict dict = new Dict();
        if (command == null) {
            return dict;
        }
        dict.setId(command.id());
        dict.setType(command.type());
        dict.setLabel(command.label());
        dict.setValue(command.value());
        dict.setRemarks(command.remarks());
        return dict;
    }
}
