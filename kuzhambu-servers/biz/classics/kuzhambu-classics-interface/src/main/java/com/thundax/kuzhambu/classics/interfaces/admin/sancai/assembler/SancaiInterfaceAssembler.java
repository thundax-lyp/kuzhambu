package com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntrySaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntrySaveRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import org.apache.commons.lang3.StringUtils;

public final class SancaiInterfaceAssembler {
    private SancaiInterfaceAssembler() {}

    public static SancaiEntryPageQuery toQuery(SancaiEntryPageRequest request) {
        SancaiEntryPageQuery query = new SancaiEntryPageQuery();
        query.setVolumeId(request.getVolumeId());
        query.setKeyword(request.getKeyword());
        query.setLifecycleStatus(fromLifecycle(request.getLifecycleStatus()));
        query.setVisibility(fromVisibility(request.getVisibility()));
        query.setTranslationStatus(fromTranslation(request.getTranslationStatus()));
        query.setImageStatus(fromImage(request.getImageStatus()));
        query.setVisualAssetStatus(fromVisualAsset(request.getVisualAssetStatus()));
        query.setRefinementStatus(fromRefinement(request.getRefinementStatus()));
        query.setSortDirection(
                StringUtils.isBlank(request.getSortDirection())
                        ? SortDirection.ASC
                        : SortDirection.valueOf(
                                request.getSortDirection().trim().toUpperCase()));
        return query;
    }

    public static SancaiEntrySaveCommand toCommand(SancaiEntrySaveRequest request) {
        return new SancaiEntrySaveCommand(
                request.getId(),
                request.getVolumeId(),
                request.getTitle(),
                request.getOriginalText(),
                request.getTranslationText(),
                request.getSummary(),
                fromLifecycle(request.getLifecycleStatus()),
                fromVisibility(request.getVisibility()),
                fromTranslation(request.getTranslationStatus()),
                fromImage(request.getImageStatus()),
                fromVisualAsset(request.getVisualAssetStatus()),
                fromRefinement(request.getRefinementStatus()),
                request.getPriority());
    }

    public static SancaiEntryResponse toResponse(SancaiEntry entity) {
        if (entity == null) {
            return SancaiEntryResponse.builder().build();
        }
        return SancaiEntryResponse.builder()
                .id(entity.getId())
                .volumeId(entity.getVolumeId())
                .title(entity.getTitle())
                .originalText(entity.getOriginalText())
                .translationText(entity.getTranslationText())
                .summary(entity.getSummary())
                .lifecycleStatus(value(entity.getLifecycleStatus()))
                .visibility(value(entity.getVisibility()))
                .translationStatus(value(entity.getTranslationStatus()))
                .imageStatus(value(entity.getImageStatus()))
                .visualAssetStatus(value(entity.getVisualAssetStatus()))
                .refinementStatus(value(entity.getRefinementStatus()))
                .priority(entity.getPriority())
                .build();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static SancaiEntryLifecycleStatus fromLifecycle(String value) {
        return StringUtils.isBlank(value) ? null : SancaiEntryLifecycleStatus.from(value);
    }

    private static SancaiEntryVisibility fromVisibility(String value) {
        return StringUtils.isBlank(value) ? null : SancaiEntryVisibility.from(value);
    }

    private static SancaiEntryTranslationStatus fromTranslation(String value) {
        return StringUtils.isBlank(value) ? null : SancaiEntryTranslationStatus.from(value);
    }

    private static SancaiEntryImageStatus fromImage(String value) {
        return StringUtils.isBlank(value) ? null : SancaiEntryImageStatus.from(value);
    }

    private static SancaiEntryVisualAssetStatus fromVisualAsset(String value) {
        return StringUtils.isBlank(value) ? null : SancaiEntryVisualAssetStatus.from(value);
    }

    private static SancaiEntryRefinementStatus fromRefinement(String value) {
        return StringUtils.isBlank(value) ? null : SancaiEntryRefinementStatus.from(value);
    }
}
