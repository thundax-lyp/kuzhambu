package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller;

import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareLinkIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsSharingRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
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
        ClassicsShareLinkId id = service.createLink(new ShareLinkCreateCommand(
                request.getTokenHash(),
                request.getTitle(),
                ClassicsShareVisibility.from(request.getVisibility()),
                ClassicsShareLinkStatus.ACTIVE,
                StringUtils.isBlank(request.getVisibilityRiskStatus())
                        ? null
                        : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()),
                null,
                request.getExpiresAt(),
                request.getTargets()));
        return ClassicsSharingResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "变更古籍分享状态", description = "classics:sharing:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:edit")
    @SysLogger(value = "变更状态")
    @PostMapping("status")
    public void status(@Valid @RequestBody ClassicsSharingRequest request) {
        service.changeStatus(new ShareLinkStatusCommand(
                ClassicsShareLinkIdCodec.toDomain(request.getId()), ClassicsShareLinkStatus.from(request.getStatus())));
    }

    @Operation(summary = "查看古籍分享", description = "classics:sharing:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sharing:view")
    @SysLogger(value = "详情")
    @GetMapping("{id}")
    public ClassicsSharingResponse get(@PathVariable Long id) {
        return toResponse(service.getLink(ClassicsShareLinkIdCodec.toDomain(id)));
    }

    private static ClassicsSharingResponse toResponse(ClassicsShareLink link) {
        return link == null
                ? ClassicsSharingResponse.builder().build()
                : ClassicsSharingResponse.builder()
                        .id(link.getId() == null ? null : link.getId().value())
                        .title(link.getTitle())
                        .visibility(
                                link.getVisibility() == null
                                        ? null
                                        : link.getVisibility().value())
                        .status(
                                link.getStatus() == null
                                        ? null
                                        : link.getStatus().value())
                        .issuedAt(link.getIssuedAt())
                        .expiresAt(link.getExpiresAt())
                        .accessCount(link.getAccessCount())
                        .build();
    }
}
