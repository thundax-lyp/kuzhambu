package com.thundax.kuzhambu.classics.interfaces.portal.sharing.assembler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.sharing.result.SharePortalResult;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsSharePortalListItem;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListItemResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalListResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalResponse;
import com.thundax.kuzhambu.classics.interfaces.portal.sharing.controller.response.ClassicsSharePortalTargetResponse;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.util.Collections;
import java.util.List;

public final class ClassicsSharingPortalInterfaceAssembler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PORTAL_RESOURCE_PATH_PREFIX = "/api/portal/classics/shares/";
    private static final String PRIVATE_PORTAL_RESOURCE_PATH_PREFIX = "/api/portal/classics/private-shares/";
    private static final String PORTAL_RESOURCE_PATH_MIDDLE = "/resources/";
    private static final String PORTAL_RESOURCE_PATH_SUFFIX = "/content";
    private static final String DOWNLOAD_QUERY = "?download=true";

    private ClassicsSharingPortalInterfaceAssembler() {}

    public static ClassicsSharePortalResponse toResponse(SharePortalResult result) {
        return toResponse(result, null);
    }

    public static ClassicsSharePortalResponse toResponse(SharePortalResult result, String shareToken) {
        return toResponse(result, shareToken, PORTAL_RESOURCE_PATH_PREFIX);
    }

    public static ClassicsSharePortalResponse toPrivateResponse(SharePortalResult result, String shareToken) {
        return toResponse(result, shareToken, PRIVATE_PORTAL_RESOURCE_PATH_PREFIX);
    }

    public static ClassicsSharePortalResponse privateAuthRequiredResponse() {
        return ClassicsSharePortalResponse.builder()
                .visibility("PRIVATE")
                .loginRequired(Boolean.TRUE)
                .targets(Collections.emptyList())
                .build();
    }

    private static ClassicsSharePortalResponse toResponse(
            SharePortalResult result, String shareToken, String resourcePathPrefix) {
        if (result == null) {
            return null;
        }
        return ClassicsSharePortalResponse.builder()
                .title(result.getTitle())
                .visibility(value(result.getVisibility()))
                .status(value(result.getStatus()))
                .issuedAt(result.getIssuedAt())
                .expiresAt(result.getExpiresAt())
                .loginRequired(Boolean.FALSE)
                .targets(toTargetResponses(result.getTargets(), shareToken, resourcePathPrefix))
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

    private static List<ClassicsSharePortalTargetResponse> toTargetResponses(
            List<ClassicsShareTarget> targets, String shareToken, String resourcePathPrefix) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }
        return targets.stream()
                .map(target -> toTargetResponse(target, shareToken, resourcePathPrefix))
                .toList();
    }

    private static ClassicsSharePortalTargetResponse toTargetResponse(
            ClassicsShareTarget target, String shareToken, String resourcePathPrefix) {
        if (target == null) {
            return null;
        }
        boolean deleted = isDeletedTarget(target);
        JsonNode snapshot = deleted ? null : readSnapshot(target.getContentSnapshotJson());
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
                .contentSnapshotJson(deleted ? null : target.getContentSnapshotJson())
                .storageObject(deleted ? null : toStorageObject(target, snapshot, shareToken, resourcePathPrefix))
                .images(deleted ? null : toImageResponses(target, snapshot, shareToken, resourcePathPrefix))
                .contentVisibilitySnapshot(value(target.getContentVisibilitySnapshot()))
                .targetStatus(value(target.getTargetStatus()))
                .build();
    }

    private static boolean isDeletedTarget(ClassicsShareTarget target) {
        return target != null && target.getTargetStatus() == ClassicsShareTargetStatus.CONTENT_DELETED;
    }

    private static ClassicsSharePortalTargetResponse.ResourceResponse toStorageObject(
            ClassicsShareTarget target, JsonNode snapshot, String shareToken, String resourcePathPrefix) {
        if (target == null || target.getContentType() != ClassicsContentType.WANGQI_DOCUMENT || snapshot == null) {
            return null;
        }
        return toResourceResponse(
                longValue(snapshot.get("storageObjectId")),
                textValue(snapshot.get("originalFilename")),
                textValue(snapshot.get("contentType")),
                longValue(snapshot.get("size")),
                shareToken,
                resourcePathPrefix);
    }

    private static List<ClassicsSharePortalTargetResponse.ImageResponse> toImageResponses(
            ClassicsShareTarget target, JsonNode snapshot, String shareToken, String resourcePathPrefix) {
        if (target == null || target.getContentType() != ClassicsContentType.SANCAI_ENTRY || snapshot == null) {
            return null;
        }
        JsonNode images = snapshot.get("images");
        if (images == null || !images.isArray()) {
            return Collections.emptyList();
        }
        return java.util.stream.StreamSupport.stream(images.spliterator(), false)
                .map(image -> toImageResponse(image, shareToken, resourcePathPrefix))
                .toList();
    }

    private static ClassicsSharePortalTargetResponse.ImageResponse toImageResponse(
            JsonNode image, String shareToken, String resourcePathPrefix) {
        Long storageObjectId = longValue(image.get("storageObjectId"));
        return ClassicsSharePortalTargetResponse.ImageResponse.builder()
                .imageId(longValue(image.get("imageId")))
                .storageObjectId(storageObjectId)
                .originalFilename(textValue(image.get("originalFilename")))
                .contentType(textValue(image.get("contentType")))
                .size(longValue(image.get("size")))
                .imageType(textValue(image.get("imageType")))
                .title(textValue(image.get("title")))
                .currentUsed(booleanValue(image.get("currentUsed")))
                .storageObject(toResourceResponse(
                        storageObjectId,
                        textValue(image.get("originalFilename")),
                        textValue(image.get("contentType")),
                        longValue(image.get("size")),
                        shareToken,
                        resourcePathPrefix))
                .build();
    }

    private static ClassicsSharePortalTargetResponse.ResourceResponse toResourceResponse(
            Long storageObjectId,
            String originalFilename,
            String contentType,
            Long size,
            String shareToken,
            String resourcePathPrefix) {
        if (storageObjectId == null || shareToken == null) {
            return null;
        }
        String contentUrl = resourcePathPrefix
                + shareToken
                + PORTAL_RESOURCE_PATH_MIDDLE
                + storageObjectId
                + PORTAL_RESOURCE_PATH_SUFFIX;
        return ClassicsSharePortalTargetResponse.ResourceResponse.builder()
                .storageObjectId(storageObjectId)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .size(size)
                .previewUrl(contentUrl)
                .downloadUrl(contentUrl + DOWNLOAD_QUERY)
                .build();
    }

    private static JsonNode readSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(snapshotJson);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            return null;
        }
    }

    private static Long longValue(JsonNode node) {
        return node == null || node.isNull() ? null : node.asLong();
    }

    private static Boolean booleanValue(JsonNode node) {
        return node == null || node.isNull() ? null : node.asBoolean();
    }

    private static String textValue(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
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
                .shareToken(item.getShareToken())
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
                .build();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
