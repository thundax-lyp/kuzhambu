package com.thundax.kuzhambu.classics.interfaces.portal.sancai.assembler;

import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request.SancaiPortalEntrySearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalVolumeResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import org.apache.commons.lang3.StringUtils;

public final class SancaiPortalInterfaceAssembler {
    private SancaiPortalInterfaceAssembler() {}

    public static SancaiEntryPageQuery toPublicQuery(SancaiPortalEntrySearchRequest request) {
        SancaiPortalEntrySearchRequest effectiveRequest =
                request == null ? new SancaiPortalEntrySearchRequest() : request;
        SancaiEntryPageQuery query = new SancaiEntryPageQuery();
        query.setCategoryId(effectiveRequest.getCategoryId());
        query.setVolumeId(effectiveRequest.getVolumeId());
        query.setKeyword(effectiveRequest.getKeyword());
        query.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        query.setVisibility(SancaiEntryVisibility.PUBLIC);
        query.setSortDirection(SortDirection.ASC);
        return query;
    }

    public static SancaiPortalCategoryResponse toResponse(SancaiCategory category) {
        if (category == null) {
            return SancaiPortalCategoryResponse.builder().build();
        }
        return SancaiPortalCategoryResponse.builder()
                .id(value(category.getId()))
                .title(category.getTitle())
                .categoryType(
                        category.getCategoryType() == null
                                ? null
                                : category.getCategoryType().value())
                .priority(category.getPriority())
                .build();
    }

    public static SancaiPortalVolumeResponse toResponse(SancaiVolume volume) {
        if (volume == null) {
            return SancaiPortalVolumeResponse.builder().build();
        }
        return SancaiPortalVolumeResponse.builder()
                .id(value(volume.getId()))
                .categoryId(value(volume.getCategoryId()))
                .title(volume.getTitle())
                .volumeType(
                        volume.getVolumeType() == null
                                ? null
                                : volume.getVolumeType().value())
                .priority(volume.getPriority())
                .build();
    }

    public static SancaiPortalEntryResponse toResponse(SancaiEntry entry) {
        if (entry == null) {
            return SancaiPortalEntryResponse.builder().build();
        }
        return SancaiPortalEntryResponse.builder()
                .id(value(entry.getId()))
                .volumeId(value(entry.getVolumeId()))
                .title(entry.getTitle())
                .originalText(entry.getOriginalText())
                .translationText(entry.getTranslationText())
                .summary(entry.getSummary())
                .lifecycleStatus(
                        entry.getLifecycleStatus() == null
                                ? null
                                : entry.getLifecycleStatus().value())
                .visibility(
                        entry.getVisibility() == null
                                ? null
                                : entry.getVisibility().value())
                .translationStatus(
                        entry.getTranslationStatus() == null
                                ? null
                                : entry.getTranslationStatus().value())
                .imageStatus(
                        entry.getImageStatus() == null
                                ? null
                                : entry.getImageStatus().value())
                .visualAssetStatus(
                        entry.getVisualAssetStatus() == null
                                ? null
                                : entry.getVisualAssetStatus().value())
                .refinementStatus(
                        entry.getRefinementStatus() == null
                                ? null
                                : entry.getRefinementStatus().value())
                .priority(entry.getPriority())
                .contentUpdatedAt(entry.getContentUpdatedAt())
                .build();
    }

    public static int pageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    public static int pageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }

    public static boolean isPublicPublished(SancaiEntry entry) {
        return entry != null
                && entry.getLifecycleStatus() == SancaiEntryLifecycleStatus.PUBLISHED
                && entry.getVisibility() == SancaiEntryVisibility.PUBLIC;
    }

    public static String normalizeKeyword(String keyword) {
        return StringUtils.isBlank(keyword) ? null : keyword.trim();
    }

    private static Long value(SancaiCategoryId id) {
        return id == null ? null : id.value();
    }

    private static Long value(SancaiVolumeId id) {
        return id == null ? null : id.value();
    }

    private static Long value(SancaiEntryId id) {
        return id == null ? null : id.value();
    }
}
