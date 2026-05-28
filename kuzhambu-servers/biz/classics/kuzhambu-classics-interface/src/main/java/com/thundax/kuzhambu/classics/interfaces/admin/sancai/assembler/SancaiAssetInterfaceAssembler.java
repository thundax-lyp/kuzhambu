package com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftSaveCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiAssetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiAssetResponse;
import org.apache.commons.lang3.StringUtils;

public final class SancaiAssetInterfaceAssembler {
    private SancaiAssetInterfaceAssembler() {}

    public static SancaiDraftSaveCommand toDraftCommand(SancaiAssetRequest request) {
        return new SancaiDraftSaveCommand(request.getEntryId(), null, request.getDraftJson());
    }

    public static SancaiImageCommand toImageCommand(SancaiAssetRequest request) {
        return new SancaiImageCommand(
                request.getId(),
                request.getEntryId(),
                StorageObjectIdCodec.toDomain(request.getStorageObjectId()),
                StringUtils.isBlank(request.getImageType()) ? null : SancaiEntryImageType.from(request.getImageType()),
                request.getTitle(),
                request.isCurrentUsed(),
                request.getPriority());
    }

    public static SancaiShowcaseCommand toShowcaseCommand(SancaiAssetRequest request) {
        return new SancaiShowcaseCommand(
                null,
                StringUtils.isBlank(request.getStatus())
                        ? SancaiShowcaseStatus.REQUESTED
                        : SancaiShowcaseStatus.from(request.getStatus()),
                request.getScopeJson(),
                StorageObjectIdCodec.toDomain(request.getStorageObjectId()),
                request.getEntryCount(),
                StringUtils.isBlank(request.getVisibilityRiskStatus())
                        ? null
                        : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()));
    }

    public static SancaiAssetResponse toImageResponse(SancaiEntryImage image) {
        return image == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(image.getId() == null ? null : image.getId().value())
                        .entryId(
                                image.getEntryId() == null
                                        ? null
                                        : image.getEntryId().value())
                        .storageObjectId(
                                image.getStorageObjectId() == null
                                        ? null
                                        : image.getStorageObjectId().value())
                        .imageType(
                                image.getImageType() == null
                                        ? null
                                        : image.getImageType().value())
                        .title(image.getTitle())
                        .currentUsed(image.isCurrentUsed())
                        .priority(image.getPriority())
                        .build();
    }

    public static SancaiAssetResponse toDraftResponse(SancaiEntryDraft draft) {
        return draft == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(draft.getId() == null ? null : draft.getId().value())
                        .entryId(
                                draft.getEntryId() == null
                                        ? null
                                        : draft.getEntryId().value())
                        .draftJson(draft.getDraftJson())
                        .build();
    }

    public static SancaiAssetResponse toShowcaseResponse(SancaiShowcase showcase) {
        return showcase == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(showcase.getId() == null ? null : showcase.getId().value())
                        .storageObjectId(
                                showcase.getStorageObjectId() == null
                                        ? null
                                        : showcase.getStorageObjectId().value())
                        .status(
                                showcase.getStatus() == null
                                        ? null
                                        : showcase.getStatus().value())
                        .scopeJson(showcase.getScopeJson())
                        .build();
    }
}
