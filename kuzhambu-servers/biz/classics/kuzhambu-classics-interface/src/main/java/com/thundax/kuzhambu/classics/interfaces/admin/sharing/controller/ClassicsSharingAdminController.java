package com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller;

import com.thundax.kuzhambu.classics.application.sharing.command.ClassicsShareTargetSortCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareTargetCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareLinkIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareTargetIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsShareTargetSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsSharingRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse.Target;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
        ShareLinkCreateResult result = service.createLink(new ShareLinkCreateCommand(
                request.getTitle(),
                ClassicsShareVisibility.from(request.getVisibility()),
                ClassicsShareLinkStatus.ACTIVE,
                StringUtils.isBlank(request.getVisibilityRiskStatus())
                        ? null
                        : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()),
                null,
                request.getExpiresAt(),
                toTargetCommands(request.getTargets())));
        return ClassicsSharingResponse.builder()
                .id(result.getId() == null ? null : result.getId().value())
                .shareToken(result.getShareToken())
                .title(result.getTitle())
                .visibility(
                        result.getVisibility() == null
                                ? null
                                : result.getVisibility().value())
                .status(result.getStatus() == null ? null : result.getStatus().value())
                .expiresAt(result.getExpiresAt())
                .targets(toTargetResponses(result.getTargets()))
                .build();
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
        return toResponse(service.getLink(linkId), service.listTargets(linkId));
    }

    private static ClassicsSharingResponse toResponse(ClassicsShareLink link, List<ClassicsShareTarget> targets) {
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
                        .targets(toTargetResponses(targets))
                        .build();
    }

    private static List<Target> toTargetResponses(List<ClassicsShareTarget> targets) {
        return targets == null
                ? List.of()
                : targets.stream()
                        .map(ClassicsSharingAdminController::toTargetResponse)
                        .toList();
    }

    private static List<ShareTargetCreateCommand> toTargetCommands(List<ClassicsShareTarget> targets) {
        return targets == null
                ? List.of()
                : targets.stream()
                        .map(target -> new ShareTargetCreateCommand(target.getContentType(), target.getContentId()))
                        .toList();
    }

    private static Target toTargetResponse(ClassicsShareTarget target) {
        return Target.builder()
                .id(target.getId() == null ? null : target.getId().value())
                .contentType(
                        target.getContentType() == null
                                ? null
                                : target.getContentType().value())
                .contentId(
                        target.getContentId() == null
                                ? null
                                : target.getContentId().value())
                .contentVersionId(
                        target.getContentVersionId() == null
                                ? null
                                : target.getContentVersionId().value())
                .contentVersionNo(target.getContentVersionNo())
                .currentContentVersionId(
                        target.getCurrentContentVersionId() == null
                                ? null
                                : target.getCurrentContentVersionId().value())
                .currentContentVersionNo(target.getCurrentContentVersionNo())
                .contentChangedAfterShare(target.getContentChangedAfterShare())
                .titleSnapshot(target.getTitleSnapshot())
                .contentVisibilitySnapshot(
                        target.getContentVisibilitySnapshot() == null
                                ? null
                                : target.getContentVisibilitySnapshot().value())
                .targetStatus(
                        target.getTargetStatus() == null
                                ? null
                                : target.getTargetStatus().value())
                .priority(target.getPriority())
                .build();
    }
}
