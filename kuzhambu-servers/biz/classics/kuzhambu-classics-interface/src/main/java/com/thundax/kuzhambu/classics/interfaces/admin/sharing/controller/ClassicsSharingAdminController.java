package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller;

import com.thundax.kuzhambu.classics.application.sharing.command.ClassicsShareTargetSortCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.query.ShareAccessQuery;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareLinkIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareTargetIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.assembler.ClassicsSharingInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsShareTargetSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsSharingRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingAccessRecordResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块-分享", description = "分享")
@SysLogger(module = {"古籍", "分享"})
@RequestMapping("/api/classics/shares")
@WrappedApiController
public class ClassicsSharingAdminController {
    private final ClassicsSharingApplicationService service;

    public ClassicsSharingAdminController(ClassicsSharingApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "创建古籍分享", description = "classics:sharing:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:edit")
    @SysLogger(value = "创建分享")
    @PostMapping("create")
    public ClassicsSharingResponse create(@Valid @RequestBody ClassicsSharingRequest request) {
        return ClassicsSharingInterfaceAssembler.toResponse(
                service.createLink(ClassicsSharingInterfaceAssembler.toCreateCommand(request)));
    }

    @Operation(summary = "分页查询古籍分享", description = "classics:sharing:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:view")
    @SysLogger(value = "分页查询")
    @PostMapping("page")
    public PageResponse<ClassicsSharingResponse> page(@Valid @RequestBody ClassicsSharingRequest request) {
        return PageResponseHelper.fromPageResult(
                service.pageLinks(
                        request == null ? null : request.getStatus(),
                        request == null ? null : request.getVisibility(),
                        PageInterfaceAssembler.toPageQuery(request)),
                ClassicsSharingInterfaceAssembler::toResponse);
    }

    @Operation(summary = "变更古籍分享状态", description = "classics:sharing:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:edit")
    @SysLogger(value = "变更状态")
    @PostMapping("status/update")
    public void updateStatus(@Valid @RequestBody ClassicsSharingRequest request) {
        service.changeStatus(new ShareLinkStatusCommand(
                ClassicsShareLinkIdCodec.toDomain(request.getId()), ClassicsShareLinkStatus.from(request.getStatus())));
    }

    @Operation(summary = "排序古籍分享目标", description = "classics:sharing:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:edit")
    @SysLogger(value = "目标排序")
    @PostMapping("targets/sort")
    public Boolean sortTargets(@Valid @RequestBody ClassicsShareTargetSortRequest request) {
        service.sortTargets(new ClassicsShareTargetSortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        ClassicsShareTargetIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "查看古籍分享", description = "classics:sharing:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:view")
    @SysLogger(value = "详情")
    @GetMapping("{id}")
    public ClassicsSharingResponse get(@PathVariable("id") Long id) {
        ClassicsShareLinkId linkId = ClassicsShareLinkIdCodec.toDomain(id);
        return ClassicsSharingInterfaceAssembler.toResponse(service.getLink(linkId), service.listTargets(linkId));
    }

    @Operation(summary = "分页查询古籍分享访问记录", description = "classics:sharing:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:view")
    @SysLogger(value = "访问记录分页")
    @PostMapping("access-records/page")
    public PageResponse<ClassicsSharingAccessRecordResponse> pageAccessRecords(
            @Valid @RequestBody ClassicsSharingRequest request) {
        ShareAccessQuery query = new ShareAccessQuery(
                ClassicsShareLinkIdCodec.toDomain(
                        requireParameter(request == null ? null : request.getShareLinkId(), "shareLinkId")),
                ClassicsShareTargetIdCodec.toDomain(request == null ? null : request.getShareTargetId()));
        return PageResponseHelper.fromPageResult(
                service.pageAccessRecords(query, PageInterfaceAssembler.toPageQuery(request)),
                ClassicsSharingInterfaceAssembler::toAccessRecordResponse);
    }

    private static Long requireParameter(Long value, String name) {
        if (value == null) {
            throw AdminResponseExceptions.invalidParameter(name);
        }
        return value;
    }
}
