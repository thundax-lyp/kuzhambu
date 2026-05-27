package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller;

import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsSharingRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@SysLogger(module = {"古籍", "分享"})
@RequestMapping("/api/classics/shares")
@WrappedApiController
public class ClassicsSharingAdminController {
    private final ClassicsSharingApplicationService service;
    public ClassicsSharingAdminController(ClassicsSharingApplicationService service) { this.service = service; }
    @HasPermission("classics:sharing:edit") @PostMapping("create") public Long create(@Valid @RequestBody ClassicsSharingRequest request) { return service.createLink(new ShareLinkCreateCommand(request.getTokenHash(), request.getTitle(), ClassicsShareVisibility.from(request.getVisibility()), ClassicsShareLinkStatus.ACTIVE, StringUtils.isBlank(request.getVisibilityRiskStatus()) ? null : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()), null, request.getExpiresAt(), request.getTargets())); }
    @HasPermission("classics:sharing:edit") @PostMapping("status") public void status(@Valid @RequestBody ClassicsSharingRequest request) { service.changeStatus(new ShareLinkStatusCommand(request.getId(), ClassicsShareLinkStatus.from(request.getStatus()))); }
    @HasPermission("classics:sharing:view") @GetMapping("{id}") public ClassicsSharingResponse get(@PathVariable Long id) { return toResponse(service.getLink(id)); }
    private static ClassicsSharingResponse toResponse(ClassicsShareLink link) { return link == null ? ClassicsSharingResponse.builder().build() : ClassicsSharingResponse.builder().id(link.getId()).title(link.getTitle()).visibility(link.getVisibility() == null ? null : link.getVisibility().value()).status(link.getStatus() == null ? null : link.getStatus().value()).issuedAt(link.getIssuedAt()).expiresAt(link.getExpiresAt()).accessCount(link.getAccessCount()).build(); }
}
