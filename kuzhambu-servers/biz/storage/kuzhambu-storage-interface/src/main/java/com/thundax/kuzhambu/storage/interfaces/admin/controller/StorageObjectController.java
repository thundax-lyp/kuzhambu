package com.thundax.kuzhambu.storage.interfaces.admin.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.storage.application.service.StorageService;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.interfaces.admin.assembler.StorageInterfaceAssembler;
import com.thundax.kuzhambu.storage.interfaces.admin.controller.request.StoragePageRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.controller.response.StorageObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "存储模块")
@RequestMapping(value = "/api/storage/object")
@WrappedApiController
public class StorageObjectController {

    private final StorageService storageService;

    public StorageObjectController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "获取存储对象分页列表", description = "storage:object:view")
    @HasPermission(value = "storage:object:view")
    @Parameter(name = AccessTokenNames.HEADER_TOKEN, description = "令牌")
    @PostMapping(value = "page")
    public PageResponse<StorageObjectResponse> page(@Valid @RequestBody StoragePageRequest request) {
        StorageQuery query = StorageInterfaceAssembler.toQuery(request);
        return PageResponseHelper.fromPageResult(
                storageService.page(query, PageInterfaceAssembler.toPageQuery(request)),
                StorageInterfaceAssembler::toResponse);
    }
}
