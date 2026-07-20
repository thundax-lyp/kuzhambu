package com.thundax.kuzhambu.classics.interfaces.admin.sharing.assembler;

import com.thundax.kuzhambu.classics.application.sharing.command.BatchShareCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareTargetCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsBatchShareCreateRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsShareTargetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.request.ClassicsSharingRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingAccessRecordResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sharing.controller.response.ClassicsSharingResponse.Target;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public final class ClassicsSharingInterfaceAssembler {
    private ClassicsSharingInterfaceAssembler() {}

    public static ShareLinkCreateCommand toCreateCommand(ClassicsSharingRequest request) {
        return new ShareLinkCreateCommand(
                request.getTitle(),
                ClassicsShareVisibility.from(request.getVisibility()),
                ClassicsShareLinkStatus.ACTIVE,
                StringUtils.isBlank(request.getVisibilityRiskStatus())
                        ? null
                        : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()),
                null,
                request.getExpiresAt(),
                toTargetCommands(request.getTargets()));
    }

    public static BatchShareCreateCommand toBatchCreateCommand(ClassicsBatchShareCreateRequest request) {
        return new BatchShareCreateCommand(
                request.getTitlePrefix(),
                ClassicsShareVisibility.from(request.getVisibility()),
                StringUtils.isBlank(request.getStatus())
                        ? ClassicsShareLinkStatus.ACTIVE
                        : ClassicsShareLinkStatus.from(request.getStatus()),
                StringUtils.isBlank(request.getVisibilityRiskStatus())
                        ? null
                        : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()),
                request.getExpiresAt(),
                request.isPrivateContentConfirmed(),
                toTargetCommands(request.getTargets()));
    }

    public static ClassicsSharingResponse toResponse(ShareLinkCreateResult result) {
        return result == null
                ? ClassicsSharingResponse.builder().build()
                : ClassicsSharingResponse.builder()
                        .id(result.getId() == null ? null : result.getId().value())
                        .shareToken(result.getShareToken())
                        .shareUrl(result.getShareUrl())
                        .title(result.getTitle())
                        .visibility(value(result.getVisibility()))
                        .status(value(result.getStatus()))
                        .expiresAt(result.getExpiresAt())
                        .targets(toTargetResponses(result.getTargets()))
                        .build();
    }

    public static ClassicsSharingResponse toResponse(ClassicsShareLink link) {
        return toResponse(link, List.of());
    }

    public static ClassicsSharingResponse toResponse(ClassicsShareLink link, List<ClassicsShareTarget> targets) {
        return link == null
                ? ClassicsSharingResponse.builder().build()
                : ClassicsSharingResponse.builder()
                        .id(link.getId() == null ? null : link.getId().value())
                        .title(link.getTitle())
                        .visibility(value(link.getVisibility()))
                        .status(value(link.getStatus()))
                        .issuedAt(link.getIssuedAt())
                        .expiresAt(link.getExpiresAt())
                        .accessCount(link.getAccessCount())
                        .targets(toTargetResponses(targets))
                        .build();
    }

    public static ClassicsSharingAccessRecordResponse toAccessRecordResponse(ClassicsShareAccessRecord record) {
        return record == null
                ? ClassicsSharingAccessRecordResponse.builder().build()
                : ClassicsSharingAccessRecordResponse.builder()
                        .id(record.getId() == null ? null : record.getId().value())
                        .shareLinkId(
                                record.getShareLinkId() == null
                                        ? null
                                        : record.getShareLinkId().value())
                        .shareTargetId(
                                record.getShareTargetId() == null
                                        ? null
                                        : record.getShareTargetId().value())
                        .accessedAt(record.getAccessedAt())
                        .accessResult(value(record.getAccessResult()))
                        .clientSnapshot(record.getClientSnapshot())
                        .build();
    }

    private static List<ShareTargetCreateCommand> toTargetCommands(List<ClassicsShareTargetRequest> targets) {
        return targets == null
                ? List.of()
                : targets.stream()
                        .map(target -> new ShareTargetCreateCommand(
                                ClassicsContentType.from(target.getContentType()),
                                ClassicsContentId.of(target.getContentId())))
                        .toList();
    }

    private static List<Target> toTargetResponses(List<ClassicsShareTarget> targets) {
        return targets == null
                ? List.of()
                : targets.stream()
                        .map(ClassicsSharingInterfaceAssembler::toTargetResponse)
                        .toList();
    }

    private static Target toTargetResponse(ClassicsShareTarget target) {
        return Target.builder()
                .id(target.getId() == null ? null : target.getId().value())
                .contentType(value(target.getContentType()))
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
                .contentVisibilitySnapshot(value(target.getContentVisibilitySnapshot()))
                .targetStatus(value(target.getTargetStatus()))
                .build();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
