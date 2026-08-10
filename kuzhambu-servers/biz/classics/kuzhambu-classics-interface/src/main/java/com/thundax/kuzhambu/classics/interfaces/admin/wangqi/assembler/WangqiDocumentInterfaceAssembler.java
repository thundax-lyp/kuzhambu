package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.assembler;

import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentEventIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocumentEvent;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentEventResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentSourceFileResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentVersionResponse;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

public final class WangqiDocumentInterfaceAssembler {
    private WangqiDocumentInterfaceAssembler() {}

    public static WangqiDocumentQuery toQuery(WangqiDocumentRequest request) {
        return toQuery(request, Set.of());
    }

    @NonNull
    public static WangqiDocumentQuery toQuery(
            @NonNull WangqiDocumentRequest request, @NonNull Set<String> operatorPermissions) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(operatorPermissions, "operatorPermissions");
        return new WangqiDocumentQuery(
                request.getKeyword(),
                StringUtils.isBlank(request.getSortDirection())
                        ? SortDirection.ASC
                        : SortDirection.valueOf(
                                request.getSortDirection().trim().toUpperCase()),
                operatorPermissions);
    }

    public static WangqiDocumentCommand toCommand(WangqiDocumentRequest request) {
        return new WangqiDocumentCommand(
                request.getId(),
                request.getTitle(),
                request.getSummary(),
                StringUtils.isBlank(request.getContentFormat())
                        ? null
                        : WangqiContentFormat.from(request.getContentFormat()),
                request.getContent(),
                request.getDocumentTime(),
                request.getStorageObjectId());
    }

    @NonNull
    public static WangqiDocumentSourceFileCommand toSourceFileCommand(@NonNull Long id, @NonNull MultipartFile file)
            throws IOException {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(file, "file");
        return new WangqiDocumentSourceFileCommand(
                id, file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    public static WangqiDocumentResponse toResponse(WangqiDocument entity) {
        return entity == null
                ? WangqiDocumentResponse.builder().build()
                : WangqiDocumentResponse.builder()
                        .id(entity.getId() == null ? null : entity.getId().value())
                        .title(entity.getTitle())
                        .summary(entity.getSummary())
                        .contentFormat(
                                entity.getContentFormat() == null
                                        ? null
                                        : entity.getContentFormat().value())
                        .content(entity.getContent())
                        .documentTime(entity.getDocumentTime())
                        .storageObjectId(StorageObjectIdCodec.toValue(entity.getStorageObjectId()))
                        .lifecycleStatus(
                                entity.getLifecycleStatus() == null
                                        ? null
                                        : entity.getLifecycleStatus().name())
                        .transitionStatus(
                                entity.getTransitionStatus() == null
                                        ? null
                                        : entity.getTransitionStatus().name())
                        .currentPublicationJobId(
                                entity.getCurrentPublicationJobId() == null
                                        ? null
                                        : entity.getCurrentPublicationJobId().value())
                        .events(toEventResponses(entity.getEvents()))
                        .build();
    }

    public static WangqiDocumentSourceFileResponse toSourceFileResponse(WangqiDocumentSourceFile file) {
        return file == null
                ? WangqiDocumentSourceFileResponse.builder().build()
                : WangqiDocumentSourceFileResponse.builder()
                        .documentId(file.getDocumentId())
                        .storageObjectId(file.getStorageObjectId())
                        .originalFilename(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .contentUrl(sourceFileContentUrl(file.getDocumentId()))
                        .build();
    }

    public static WangqiDocumentVersionResponse toVersionResponse(ClassicsContentVersion version) {
        return version == null
                ? WangqiDocumentVersionResponse.builder().build()
                : WangqiDocumentVersionResponse.builder()
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

    private static java.util.List<WangqiDocumentEventResponse> toEventResponses(
            java.util.List<WangqiDocumentEvent> events) {
        if (events == null || events.isEmpty()) {
            return java.util.List.of();
        }
        return events.stream()
                .map(WangqiDocumentInterfaceAssembler::toEventResponse)
                .toList();
    }

    private static WangqiDocumentEventResponse toEventResponse(WangqiDocumentEvent event) {
        return event == null
                ? WangqiDocumentEventResponse.builder().build()
                : WangqiDocumentEventResponse.builder()
                        .id(WangqiDocumentEventIdCodec.toValue(event.getId()))
                        .documentId(
                                event.getDocumentId() == null
                                        ? null
                                        : event.getDocumentId().value())
                        .title(event.getTitle())
                        .occurredAt(event.getOccurredAt())
                        .occurredLabel(event.getOccurredLabel())
                        .summary(event.getSummary())
                        .build();
    }

    private static String sourceFileContentUrl(Long documentId) {
        return documentId == null ? null : "/api/classics/wangqi/documents/" + documentId + "/source-file/content";
    }
}
