package com.thundax.kuzhambu.classics.interfaces.portal.sharing.assembler;

import com.thundax.kuzhambu.classics.application.sharing.result.SharePortalResult;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsSharePortalListItem;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListItemResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalTargetResponse;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Collections;
import java.util.List;

public final class ClassicsSharingPortalInterfaceAssembler {
    private ClassicsSharingPortalInterfaceAssembler() {}

    public static ClassicsSharePortalResponse toResponse(SharePortalResult result) {
        if (result == null) {
            return null;
        }
        return ClassicsSharePortalResponse.builder()
                .title(result.getTitle())
                .visibility(value(result.getVisibility()))
                .status(value(result.getStatus()))
                .issuedAt(result.getIssuedAt())
                .expiresAt(result.getExpiresAt())
                .targets(toTargetResponses(result.getTargets()))
                .build();
    }

    public static ClassicsSharePortalListResponse toListResponse(PageResult<ClassicsSharePortalListItem> page) {
        if (page == null) {
            return null;
        }
        return ClassicsSharePortalListResponse.builder()
                .pageNo(page.getPageNo())
                .pageSize(page.getPageSize())
                .totalCount(page.getTotalCount())
                .totalPage(page.getTotalPage())
                .records(toListItemResponses(page.getRecords()))
                .build();
    }

    private static List<ClassicsSharePortalTargetResponse> toTargetResponses(List<ClassicsShareTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        return targets.stream()
                .map(ClassicsSharingPortalInterfaceAssembler::toTargetResponse)
                .toList();
    }

    private static ClassicsSharePortalTargetResponse toTargetResponse(ClassicsShareTarget target) {
        if (target == null) {
            return null;
        }
        return ClassicsSharePortalTargetResponse.builder()
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
                .titleSnapshot(target.getTitleSnapshot())
                .contentSnapshotJson(target.getContentSnapshotJson())
                .contentVisibilitySnapshot(value(target.getContentVisibilitySnapshot()))
                .targetStatus(value(target.getTargetStatus()))
                .priority(target.getPriority())
                .build();
    }

    private static List<ClassicsSharePortalListItemResponse> toListItemResponses(
            List<ClassicsSharePortalListItem> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .map(ClassicsSharingPortalInterfaceAssembler::toListItemResponse)
                .toList();
    }

    private static ClassicsSharePortalListItemResponse toListItemResponse(ClassicsSharePortalListItem item) {
        if (item == null) {
            return null;
        }
        return ClassicsSharePortalListItemResponse.builder()
                .shareLinkId(
                        item.getShareLinkId() == null
                                ? null
                                : item.getShareLinkId().value())
                .shareTitle(item.getShareTitle())
                .issuedAt(item.getIssuedAt())
                .expiresAt(item.getExpiresAt())
                .contentType(value(item.getContentType()))
                .contentId(
                        item.getContentId() == null ? null : item.getContentId().value())
                .contentVersionId(
                        item.getContentVersionId() == null
                                ? null
                                : item.getContentVersionId().value())
                .contentVersionNo(item.getContentVersionNo())
                .titleSnapshot(item.getTitleSnapshot())
                .contentVisibilitySnapshot(value(item.getContentVisibilitySnapshot()))
                .targetStatus(value(item.getTargetStatus()))
                .priority(item.getPriority())
                .build();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
