package com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler;

import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiCategoryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVolumeCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryQuery;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import com.thundax.kuzhambu.classics.interfaces.admin.content.assembler.ClassicsContentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiCategoryRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiVolumeRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryVersionResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiVolumeResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.web.response.DictResponse;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class SancaiInterfaceAssembler {
    private SancaiInterfaceAssembler() {}

    @NonNull
    public static ContentObjectQuery toContentObjectQuery(@NonNull String contentType, @NonNull Long contentId) {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(contentId, "contentId");
        return ClassicsContentInterfaceAssembler.toObjectQuery(contentType, contentId);
    }

    public static SancaiEntryQuery toQuery(SancaiEntryPageRequest request, Set<String> operatorPermissions) {
        return new SancaiEntryQuery(
                request.getCategoryId(),
                request.getVolumeId(),
                request.getKeyword(),
                fromLifecycle(request.getLifecycleStatus()),
                fromTranslation(request.getTranslationStatus()),
                fromImage(request.getImageStatus()),
                fromVisualAsset(request.getVisualAssetStatus()),
                fromRefinement(request.getRefinementStatus()),
                StringUtils.isBlank(request.getSortDirection())
                        ? SortDirection.ASC
                        : SortDirection.valueOf(
                                request.getSortDirection().trim().toUpperCase()),
                operatorPermissions);
    }

    public static SancaiEntryCommand toCommand(SancaiEntryRequest request) {
        return new SancaiEntryCommand(
                request.getId(),
                request.getVolumeId(),
                request.getTitle(),
                request.getOriginalText(),
                request.getTranslationText(),
                request.getSummary(),
                fromLifecycle(request.getLifecycleStatus()),
                fromTranslation(request.getTranslationStatus()),
                fromImage(request.getImageStatus()),
                fromVisualAsset(request.getVisualAssetStatus()),
                fromRefinement(request.getRefinementStatus()));
    }

    public static SancaiEntryStatusCommand toStatusCommand(
            SancaiEntryRequest request, Set<String> operatorPermissions) {
        return new SancaiEntryStatusCommand(
                request == null ? null : request.getId(),
                fromLifecycle(request == null ? null : request.getLifecycleStatus()),
                operatorPermissions);
    }

    public static SancaiCategoryCommand toCommand(SancaiCategoryRequest request) {
        return new SancaiCategoryCommand(
                request.getId(), request.getTitle(), fromCategoryType(request.getCategoryType()), null);
    }

    public static SancaiVolumeCommand toCommand(SancaiVolumeRequest request) {
        return new SancaiVolumeCommand(
                request.getId(),
                request.getCategoryId(),
                request.getTitle(),
                fromVolumeType(request.getVolumeType()),
                null);
    }

    public static List<DictResponse> toCategoryTypes() {
        return List.of(
                dict("SANCAI_CATEGORY_TYPE", SancaiCategoryType.FORMAL.value(), "正式门类"),
                dict("SANCAI_CATEGORY_TYPE", SancaiCategoryType.AUXILIARY.value(), "辅助内容"));
    }

    public static List<DictResponse> toVolumeTypes() {
        return List.of(
                dict("SANCAI_VOLUME_TYPE", SancaiVolumeType.MAIN.value(), "正式卷目"),
                dict("SANCAI_VOLUME_TYPE", SancaiVolumeType.AUXILIARY.value(), "辅助卷目"));
    }

    public static SancaiEntryResponse toResponse(SancaiEntry entity) {
        return toResponse(entity, List.of());
    }

    public static SancaiEntryResponse toResponse(SancaiEntry entity, List<ClassicsContentTag> tags) {
        if (entity == null) {
            return SancaiEntryResponse.builder().build();
        }
        List<ClassicsContentResponse> tagResponses = tags == null
                ? List.of()
                : tags.stream()
                        .map(ClassicsContentInterfaceAssembler::toTagResponse)
                        .toList();
        return SancaiEntryResponse.builder()
                .id(entity.getId() == null ? null : entity.getId().value())
                .volumeId(
                        entity.getVolumeId() == null
                                ? null
                                : entity.getVolumeId().value())
                .title(entity.getTitle())
                .originalText(entity.getOriginalText())
                .translationText(entity.getTranslationText())
                .summary(entity.getSummary())
                .lifecycleStatus(value(entity.getLifecycleStatus()))
                .transitionStatus(value(entity.getTransitionStatus()))
                .currentPublicationJobId(
                        entity.getCurrentPublicationJobId() == null
                                ? null
                                : entity.getCurrentPublicationJobId().value())
                .translationStatus(value(entity.getTranslationStatus()))
                .imageStatus(value(entity.getImageStatus()))
                .visualAssetStatus(value(entity.getVisualAssetStatus()))
                .refinementStatus(value(entity.getRefinementStatus()))
                .currentVersionId(ClassicsContentVersionIdCodec.toValue(entity.getCurrentVersionId()))
                .currentVersionNo(entity.getCurrentVersionNo())
                .currentVersionedAt(entity.getCurrentVersionedAt())
                .contentUpdatedAt(entity.getContentUpdatedAt())
                .versionDirty(versionDirty(entity))
                .tags(tagResponses)
                .build();
    }

    public static SancaiEntryVersionResponse toVersionResponse(ClassicsContentVersion version) {
        return version == null
                ? SancaiEntryVersionResponse.builder().build()
                : SancaiEntryVersionResponse.builder()
                        .id(ClassicsContentVersionIdCodec.toValue(version.getId()))
                        .contentType(
                                version.getContentType() == null
                                        ? null
                                        : version.getContentType().value())
                        .contentId(ClassicsContentIdCodec.toValue(version.getContentId()))
                        .versionNo(version.getVersionNo())
                        .versionedAt(version.getVersionedAt())
                        .snapshotJson(version.getSnapshotJson())
                        .changeType(
                                version.getChangeType() == null
                                        ? null
                                        : version.getChangeType().value())
                        .changeSummary(version.getChangeSummary())
                        .build();
    }

    public static SancaiCategoryResponse toResponse(SancaiCategory entity) {
        if (entity == null) {
            return SancaiCategoryResponse.builder().build();
        }
        return SancaiCategoryResponse.builder()
                .id(entity.getId() == null ? null : entity.getId().value())
                .title(entity.getTitle())
                .categoryType(value(entity.getCategoryType()))
                .build();
    }

    public static SancaiVolumeResponse toResponse(SancaiVolume entity) {
        if (entity == null) {
            return SancaiVolumeResponse.builder().build();
        }
        return SancaiVolumeResponse.builder()
                .id(entity.getId() == null ? null : entity.getId().value())
                .categoryId(
                        entity.getCategoryId() == null
                                ? null
                                : entity.getCategoryId().value())
                .title(entity.getTitle())
                .volumeType(value(entity.getVolumeType()))
                .build();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static boolean versionDirty(SancaiEntry entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getCurrentVersionId() == null || entity.getCurrentVersionedAt() == null) {
            return true;
        }
        return entity.getContentUpdatedAt() != null
                && entity.getContentUpdatedAt().isAfter(entity.getCurrentVersionedAt());
    }

    private static DictResponse dict(String type, String value, String label) {
        return DictResponse.builder().type(type).value(value).label(label).build();
    }

    private static SancaiCategoryType fromCategoryType(String value) {
        return StringUtils.isBlank(value) ? null : SancaiCategoryType.from(value);
    }

    private static SancaiVolumeType fromVolumeType(String value) {
        return StringUtils.isBlank(value) ? null : SancaiVolumeType.from(value);
    }

    private static SancaiEntryLifecycleStatus fromLifecycle(String value) {
        return StringUtils.isBlank(value) ? null : SancaiEntryLifecycleStatus.from(value);
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
