package com.thundax.kuzhambu.classics.interfaces.portal.sancai.assembler;

import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryQuery;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiImageContentQuery;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiVisualAssetContentQuery;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.KnowledgeTagId;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVisualAssetIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.request.SancaiPortalEntrySearchRequest;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalCategoryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalEntryResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sancai.controller.response.SancaiPortalVolumeResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class SancaiPortalInterfaceAssembler {
    private SancaiPortalInterfaceAssembler() {}

    @NonNull
    public static ContentObjectQuery toContentObjectQuery(@NonNull String contentType, @NonNull Long contentId) {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(contentId, "contentId");
        return new ContentObjectQuery(contentType, ClassicsContentIdCodec.toDomain(contentId));
    }

    public static SancaiEntryQuery toPublicQuery(SancaiPortalEntrySearchRequest request) {
        SancaiPortalEntrySearchRequest effectiveRequest =
                request == null ? new SancaiPortalEntrySearchRequest() : request;
        return new SancaiEntryQuery(
                effectiveRequest.getCategoryId(),
                effectiveRequest.getVolumeId(),
                effectiveRequest.getKeyword(),
                null,
                null,
                null,
                null,
                null,
                SortDirection.ASC);
    }

    @NonNull
    public static PageQuery toPageQuery(@NonNull SancaiPortalEntrySearchRequest request) {
        Objects.requireNonNull(request, "request");
        return new PageQuery(pageNo(request.getPageNo()), pageSize(request.getPageSize()));
    }

    @NonNull
    public static SancaiImageContentQuery toImageContentQuery(@NonNull Long entryId, @NonNull Long imageId) {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(imageId, "imageId");
        return new SancaiImageContentQuery(
                SancaiEntryIdCodec.toDomain(entryId), SancaiEntryImageIdCodec.toDomain(imageId));
    }

    @NonNull
    public static SancaiVisualAssetContentQuery toVisualAssetContentQuery(
            @NonNull Long entryId, @NonNull Long visualAssetId) {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(visualAssetId, "visualAssetId");
        return new SancaiVisualAssetContentQuery(
                SancaiEntryIdCodec.toDomain(entryId), SancaiVisualAssetIdCodec.toDomain(visualAssetId));
    }

    public static SancaiPortalCategoryResponse toResponse(SancaiCategory category) {
        return toResponse(category, null);
    }

    public static SancaiPortalCategoryResponse toResponse(SancaiCategory category, SancaiCategoryOverview overview) {
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
                .publicEntryCount(overview == null ? 0L : overview.getPublicEntryCount())
                .illustratedEntryCount(overview == null ? 0L : overview.getIllustratedEntryCount())
                .thumbnailUrl(
                        overview == null
                                ? null
                                : imageContentUrl(
                                        value(overview.getRepresentativeEntryId()),
                                        value(overview.getRepresentativeImageId())))
                .thumbnailTitle(overview == null ? null : overview.getRepresentativeImageTitle())
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
                .build();
    }

    public static SancaiPortalEntryResponse toResponse(SancaiEntry entry) {
        return toResponse(entry, List.of(), List.of(), List.of());
    }

    public static SancaiPortalEntryResponse toResponse(
            SancaiEntry entry,
            List<ClassicsContentTag> tags,
            List<SancaiEntryImage> images,
            List<SancaiVisualAsset> visualAssets) {
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
                .contentUpdatedAt(entry.getContentUpdatedAt())
                .tags(toTagResponses(tags))
                .images(toImageResponses(images))
                .currentVisualAsset(toCurrentVisualAssetResponse(visualAssets))
                .build();
    }

    public static List<SancaiPortalEntryResponse.TagResponse> toTagResponses(List<ClassicsContentTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .filter(tag -> tag.getStatus() == null || tag.getStatus() == ClassicsContentTagStatus.ACTIVE)
                .map(SancaiPortalInterfaceAssembler::toTagResponse)
                .toList();
    }

    public static List<SancaiPortalEntryResponse.ImageResponse> toImageResponses(List<SancaiEntryImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .map(SancaiPortalInterfaceAssembler::toImageResponse)
                .toList();
    }

    public static SancaiPortalEntryResponse.TagResponse toTagResponse(ClassicsContentTag tag) {
        return tag == null
                ? SancaiPortalEntryResponse.TagResponse.builder().build()
                : SancaiPortalEntryResponse.TagResponse.builder()
                        .id(value(tag.getId()))
                        .tagId(value(tag.getTagId()))
                        .tagName(tag.getTagNameSnapshot())
                        .source(tag.getSource() == null ? null : tag.getSource().value())
                        .build();
    }

    public static SancaiPortalEntryResponse.ImageResponse toImageResponse(SancaiEntryImage image) {
        Long entryId = image == null ? null : value(image.getEntryId());
        Long imageId = image == null ? null : value(image.getId());
        String contentUrl = imageContentUrl(entryId, imageId);
        return image == null
                ? SancaiPortalEntryResponse.ImageResponse.builder().build()
                : SancaiPortalEntryResponse.ImageResponse.builder()
                        .id(imageId)
                        .title(imageTitle(image))
                        .imageType(
                                image.getImageType() == null
                                        ? null
                                        : image.getImageType().value())
                        .currentUsed(image.isCurrentUsed())
                        .previewUrl(contentUrl)
                        .downloadUrl(contentUrl == null ? null : contentUrl + "?download=true")
                        .build();
    }

    public static SancaiPortalEntryResponse.VisualAssetResponse toCurrentVisualAssetResponse(
            List<SancaiVisualAsset> visualAssets) {
        if (visualAssets == null || visualAssets.isEmpty()) {
            return null;
        }
        return visualAssets.stream()
                .filter(SancaiVisualAsset::isCurrentUsed)
                .findFirst()
                .map(SancaiPortalInterfaceAssembler::toVisualAssetResponse)
                .orElse(null);
    }

    public static SancaiPortalEntryResponse.VisualAssetResponse toVisualAssetResponse(SancaiVisualAsset visualAsset) {
        Long entryId = visualAsset == null ? null : value(visualAsset.getEntryId());
        Long visualAssetId = visualAsset == null ? null : value(visualAsset.getId());
        return visualAsset == null
                ? SancaiPortalEntryResponse.VisualAssetResponse.builder().build()
                : SancaiPortalEntryResponse.VisualAssetResponse.builder()
                        .visualAssetId(visualAssetId)
                        .versionNo(visualAsset.getVersionNo())
                        .status(
                                visualAsset.getStatus() == null
                                        ? null
                                        : visualAsset.getStatus().value())
                        .imageAnalysisMarkdown(visualAsset.getImageAnalysisMarkdown())
                        .fusionDescription(visualAsset.getFusionDescription())
                        .visualDescription(visualAsset.getVisualDescription())
                        .sourcePreviewUrl(visualAssetContentUrl(
                                entryId,
                                visualAssetId,
                                visualAsset.getSourceImageStorageObjectId() == null
                                        ? null
                                        : visualAsset
                                                .getSourceImageStorageObjectId()
                                                .value(),
                                "source-content"))
                        .generatedPreviewUrl(visualAssetContentUrl(
                                entryId,
                                visualAssetId,
                                visualAsset.getGeneratedImageStorageObjectId() == null
                                        ? null
                                        : visualAsset
                                                .getGeneratedImageStorageObjectId()
                                                .value(),
                                "generated-content"))
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

    private static Long value(SancaiEntryImageId id) {
        return id == null ? null : id.value();
    }

    private static Long value(SancaiVisualAssetId id) {
        return id == null ? null : id.value();
    }

    private static Long value(ClassicsContentTagId id) {
        return id == null ? null : id.value();
    }

    private static Long value(KnowledgeTagId id) {
        return id == null ? null : id.value();
    }

    private static String imageTitle(SancaiEntryImage image) {
        if (StringUtils.isNotBlank(image.getTitle())) {
            return image.getTitle();
        }
        return image.getId() == null ? "图片" : "图片 " + image.getId().value();
    }

    private static String imageContentUrl(Long entryId, Long imageId) {
        return entryId == null || imageId == null
                ? null
                : "/api/portal/classics/sancai/images/" + entryId + "/" + imageId + "/content";
    }

    private static String visualAssetContentUrl(
            Long entryId, Long visualAssetId, Long storageObjectId, String contentPath) {
        return entryId == null || visualAssetId == null || storageObjectId == null
                ? null
                : "/api/portal/classics/sancai/visual-assets/" + entryId + "/" + visualAssetId + "/" + contentPath;
    }
}
