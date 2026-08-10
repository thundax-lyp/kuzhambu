package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller;

import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationApplicationService;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.assembler.ClassicsPublicationInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationJobGetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationJobPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationJobResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块-发布任务", description = "发布与下线任务只读查询")
@SysLogger(module = {"古籍", "发布任务"})
@RequestMapping("/api/classics/publication-jobs")
@WrappedApiController
public class ClassicsPublicationAdminController {
    private final ClassicsPublicationApplicationService service;

    public ClassicsPublicationAdminController(ClassicsPublicationApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询发布任务", description = "classics:publication:view")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @HasPermission("classics:publication:view")
    @SysLogger(value = "分页查询")
    @PostMapping("page")
    public PageResponse<ClassicsPublicationJobResponse> page(
            @Valid @RequestBody ClassicsPublicationJobPageRequest request) {
        return PageResponseHelper.fromPageResult(
                service.page(
                        ClassicsPublicationInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                ClassicsPublicationInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查看发布任务", description = "classics:publication:view")
    @ApiImplicitParams(@ApiImplicitParam(name = AccessTokenNames.HEADER_TOKEN, paramType = "header"))
    @HasPermission("classics:publication:view")
    @SysLogger(value = "任务详情")
    @PostMapping("get")
    public ClassicsPublicationJobResponse get(@Valid @RequestBody ClassicsPublicationJobGetRequest request) {
        return ClassicsPublicationInterfaceAssembler.toResponse(
                service.get(ClassicsPublicationJobIdCodec.toDomain(request.getId())));
    }
}
