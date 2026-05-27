package com.thundax.kuzhambu.system.interfaces.admin.core.controller;

import com.thundax.kuzhambu.system.application.core.entity.Dict;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.DictId;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.DictIdCodec;
import com.thundax.kuzhambu.system.application.core.service.DictService;
import com.thundax.kuzhambu.system.application.core.service.command.DictSortCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DictQuery;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.system.interfaces.admin.core.aop.annotation.SysLogger;
import com.thundax.kuzhambu.system.interfaces.admin.core.assembler.DictInterfaceAssembler;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictSortRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.DictResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "系统/字典")
@SysLogger(module = {"系统", "字典"})
@RequestMapping(value = "/api/sys/dict")
@WrappedApiController
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @Operation(summary = "获取对象", description = "sys:dict:view")
    @HasPermission(value = "sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "读取")
    @PostMapping(value = "get")
    public DictResponse get(@Valid @RequestBody DictIdRequest request) {
        return DictInterfaceAssembler.toResponse(dictService.get(DictInterfaceAssembler.toId(request)));
    }

    @Operation(summary = "获取列表", description = "sys:dict:view")
    @HasPermission(value = "sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "列表")
    @PostMapping(value = "list")
    public List<DictResponse> list(@Valid @RequestBody DictQueryRequest request) {
        DictQuery query = DictInterfaceAssembler.toQuery(request);
        return dictService.list(query).stream()
                .map(dict -> DictInterfaceAssembler.toResponse(dict))
                .collect(Collectors.toList());
    }

    @Operation(summary = "获取分页列表", description = "sys:dict:view")
    @HasPermission(value = "sys:dict:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "分页")
    @PostMapping(value = "page")
    public PageResponse<DictResponse> page(@Valid @RequestBody DictPageRequest request) {
        DictQuery query = DictInterfaceAssembler.toQuery(request);
        return PageResponseHelper.fromPageResult(
                dictService.page(query, PageInterfaceAssembler.toPageQuery(request)),
                DictInterfaceAssembler::toResponse);
    }

    @Operation(summary = "添加", description = "sys:dict:edit")
    @HasPermission(value = "sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "添加")
    @PostMapping(value = "create")
    public DictResponse add(@Valid @RequestBody DictSaveRequest request) {
        DictId id = dictService.create(DictInterfaceAssembler.toCreateCommand(request));
        return DictInterfaceAssembler.toResponse(dictService.get(id));
    }

    @Operation(summary = "更新", description = "sys:dict:edit")
    @HasPermission(value = "sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "更新")
    @PostMapping(value = "update")
    public DictResponse update(@Valid @RequestBody DictSaveRequest request) {
        DictId id = DictIdCodec.toDomain(request.getId());
        Dict dict = dictService.get(id);
        if (dict == null) {
            throw AdminResponseExceptions.objectNotFound();
        }
        dictService.changeInfo(DictInterfaceAssembler.toChangeInfoCommand(request));
        return DictInterfaceAssembler.toResponse(dictService.get(id));
    }

    @Operation(summary = "删除", description = "sys:dict:edit")
    @HasPermission(value = "sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger(value = "删除")
    @PostMapping(value = "delete")
    public Boolean delete(@Valid @RequestBody List<DictIdRequest> list) {
        List<DictId> idList = new ArrayList<>();
        for (DictIdRequest request : RequestListHelper.present(list)) {
            Dict bean = dictService.get(DictInterfaceAssembler.toId(request));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            idList.add(bean.getId());
        }
        if (idList.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("list");
        }
        idList.forEach(dictService::remove);
        return true;
    }

    @Operation(summary = "排序", description = "sys:dict:edit")
    @HasPermission(value = "sys:dict:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @PostMapping(value = "sort")
    public Boolean sort(@Valid @RequestBody DictSortRequest request) {
        dictService.sort(new DictSortCommand(
                RequestListHelper.map(
                        readOrderedIds(request == null ? null : request.getOrderedIds()), DictIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    private List<Long> readOrderedIds(List<String> sourceList) {
        List<String> orderedIdValues = RequestListHelper.present(sourceList);
        if (sourceList == null || orderedIdValues.size() != sourceList.size() || orderedIdValues.isEmpty()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        List<Long> orderedIds = orderedIdValues.stream()
                .map(value -> Long.valueOf(value.trim()))
                .collect(Collectors.toList());
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw AdminResponseExceptions.invalidParameter("orderedIds");
        }
        return orderedIds;
    }
}
