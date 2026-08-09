package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseEnsureRequest;
import com.thundax.kuzhambu.common.knowledge.model.base.KnowledgeBaseResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemDeleteRequest;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemResult;
import com.thundax.kuzhambu.common.knowledge.model.item.KnowledgeItemUpsertRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncRequest;
import com.thundax.kuzhambu.common.knowledge.model.sync.KnowledgeSyncResult;
import com.thundax.kuzhambu.discovery.application.qa.command.SyncKnowledgeContentCommand;
import com.thundax.kuzhambu.discovery.application.qa.query.KnowledgeSyncItemQuery;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeHealthResult;
import com.thundax.kuzhambu.discovery.application.qa.result.KnowledgeSyncItemResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeSyncApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeDocument;
import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeDocumentAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeItemTextRenderer;
import com.thundax.kuzhambu.discovery.application.qa.support.KnowledgeRevisionCalculator;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncBatch;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncItem;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.KnowledgeSourceId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaKnowledgeSyncStatus;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncBatchRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncItemRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class KnowledgeSyncApplicationServiceImpl implements KnowledgeSyncApplicationService {

    private static final String KNOWLEDGE_BASE_NAME = "kuzhambu-qa";
    private static final String DEFAULT_PROVIDER = "unknown";

    private static final String SYNC_STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String SYNC_STATUS_FAILED = "FAILED";
    private static final String SYNC_STATUS_DELETED = "DELETED";
    private static final String SYNC_STATUS_SYNCING = "SYNCING";
    private static final String SYNC_STATUS_PENDING = "PENDING";

    private static final String TRIGGER_FULL_REBUILD = "FULL_REBUILD";
    private static final String TRIGGER_MANUAL = "MANUAL";

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final KnowledgeBaseClient knowledgeBaseClient;
    private final ClassicsFacade classicsFacade;
    private final QaKnowledgeSyncItemRepository qaKnowledgeSyncItemRepository;
    private final QaKnowledgeSyncBatchRepository qaKnowledgeSyncBatchRepository;
    private final KnowledgeDocumentAssembler knowledgeDocumentAssembler;
    private final KnowledgeItemTextRenderer knowledgeItemTextRenderer;
    private final KnowledgeRevisionCalculator knowledgeRevisionCalculator;

    public KnowledgeSyncApplicationServiceImpl(
            KnowledgeBaseClient knowledgeBaseClient,
            ClassicsFacade classicsFacade,
            QaKnowledgeSyncItemRepository qaKnowledgeSyncItemRepository,
            QaKnowledgeSyncBatchRepository qaKnowledgeSyncBatchRepository,
            KnowledgeDocumentAssembler knowledgeDocumentAssembler,
            KnowledgeItemTextRenderer knowledgeItemTextRenderer,
            KnowledgeRevisionCalculator knowledgeRevisionCalculator) {
        this.knowledgeBaseClient = knowledgeBaseClient;
        this.classicsFacade = classicsFacade;
        this.qaKnowledgeSyncItemRepository = qaKnowledgeSyncItemRepository;
        this.qaKnowledgeSyncBatchRepository = qaKnowledgeSyncBatchRepository;
        this.knowledgeDocumentAssembler = knowledgeDocumentAssembler;
        this.knowledgeItemTextRenderer = knowledgeItemTextRenderer;
        this.knowledgeRevisionCalculator = knowledgeRevisionCalculator;
    }

    @Override
    public KnowledgeHealthResult health() {
        com.thundax.kuzhambu.common.knowledge.model.health.KnowledgeHealthResult clientResult =
                knowledgeBaseClient.health();
        if (clientResult == null) {
            return new KnowledgeHealthResult(
                    false, DEFAULT_PROVIDER, "Health check returned empty result", Collections.emptyMap());
        }
        return new KnowledgeHealthResult(
                clientResult.available(),
                StringUtils.defaultIfBlank(clientResult.provider(), DEFAULT_PROVIDER),
                clientResult.message(),
                clientResult.raw() == null ? Collections.emptyMap() : clientResult.raw());
    }

    @Override
    public Long rebuild() {
        ensureKnowledgeBase();

        Instant startedAt = Instant.now();
        String provider = resolveProvider();
        List<ClassicsQaKnowledgeFacadeRequest> syncSources = listSyncSources();

        QaKnowledgeSyncBatch batch =
                new QaKnowledgeSyncBatch(null, TRIGGER_FULL_REBUILD, provider, 0, 0, 0, startedAt, null);
        Long batchId = qaKnowledgeSyncBatchRepository.save(batch);
        batch.setId(batchId);

        int successCount = 0;
        int failureCount = 0;
        for (ClassicsQaKnowledgeFacadeRequest source : syncSources) {
            SyncKnowledgeContentCommand command = new SyncKnowledgeContentCommand(
                    source.getContentType(), parseContentId(source.getContentId()), 0, null, null);
            KnowledgeSyncItemResult syncResult = syncContent(
                    command, qaKnowledgeSyncItemRepository.getBySourceId(sourceId(command)), TRIGGER_FULL_REBUILD);
            if (SYNC_STATUS_SUCCEEDED.equalsIgnoreCase(syncResult.getSyncStatus())) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        qaKnowledgeSyncBatchRepository.update(new QaKnowledgeSyncBatch(
                batchId,
                TRIGGER_FULL_REBUILD,
                provider,
                syncSources.size(),
                successCount,
                failureCount,
                startedAt,
                Instant.now()));
        return batchId;
    }

    @Override
    public KnowledgeSyncItemResult syncContent(SyncKnowledgeContentCommand command) {
        validateCommand(command);
        QaKnowledgeSyncItem existingItem = qaKnowledgeSyncItemRepository.getBySourceId(sourceId(command));
        return syncContent(command, existingItem, TRIGGER_MANUAL);
    }

    @Override
    public KnowledgeSyncItemResult deleteContent(SyncKnowledgeContentCommand command) {
        validateCommand(command);
        QaKnowledgeSyncItem syncItem = qaKnowledgeSyncItemRepository.getBySourceId(sourceId(command));
        if (syncItem == null) {
            return toResult(new QaKnowledgeSyncItem(
                    null,
                    sourceIdValue(sourceId(command)),
                    command.getContentType(),
                    command.getContentId(),
                    KNOWLEDGE_BASE_NAME,
                    command.getCurrentVersionNo(),
                    null,
                    resolveProvider(),
                    null,
                    null,
                    SYNC_STATUS_FAILED,
                    "Sync item does not exist",
                    null,
                    null,
                    null));
        }

        Instant now = Instant.now();
        syncItem.setUpdatedAt(now);
        if (StringUtils.isBlank(syncItem.getExternalKnowledgeItemId())) {
            syncItem.setSyncStatus(SYNC_STATUS_FAILED);
            syncItem.setFailureReason("Sync item does not have external knowledge item id");
            qaKnowledgeSyncItemRepository.update(syncItem);
            return toResult(syncItem);
        }

        try {
            KnowledgeSyncResult deleteResult = knowledgeBaseClient.deleteKnowledgeItem(new KnowledgeItemDeleteRequest(
                    knowledgeItemBase(syncItem),
                    syncItem.getExternalKnowledgeItemId(),
                    sourceIdValue(syncItem.getSourceId()),
                    Map.of("operation", "deleteContent")));
            syncItem.setSyncStatus(isSuccessStatus(deleteResult) ? SYNC_STATUS_DELETED : SYNC_STATUS_FAILED);
            syncItem.setFailureReason(syncResultFailureReason(deleteResult, "deleteKnowledgeItem failed"));
            if (SYNC_STATUS_DELETED.equals(syncStatusValue(syncItem))) {
                syncItem.setSyncedAt(now);
            }
            qaKnowledgeSyncItemRepository.update(syncItem);
            return toResult(syncItem);
        } catch (Exception ex) {
            syncItem.setSyncStatus(SYNC_STATUS_FAILED);
            syncItem.setFailureReason(errorMessage(ex));
            qaKnowledgeSyncItemRepository.update(syncItem);
            return toResult(syncItem);
        }
    }

    @Override
    public PageResult<KnowledgeSyncItemResult> pageSyncItems(KnowledgeSyncItemQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        int pageNo = normalizePageNo(effectivePage.getPageNo());
        int pageSize = normalizePageSize(effectivePage.getPageSize());
        int start = (pageNo - 1) * pageSize;

        List<QaKnowledgeSyncItem> allItems = listItemsForPageQuery(query);
        List<QaKnowledgeSyncItem> pageItems = allItems.stream()
                .sorted(Comparator.comparing(
                        QaKnowledgeSyncItem::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .skip(start)
                .limit(pageSize)
                .toList();

        return PageResult.of(
                pageNo,
                pageSize,
                allItems.size(),
                pageItems.stream().map(this::toResultWithTitle).toList());
    }

    private KnowledgeSyncItemResult syncContent(
            SyncKnowledgeContentCommand command, QaKnowledgeSyncItem existingItem, String triggerType) {
        KnowledgeSourceId sourceId = sourceId(command);
        Instant now = Instant.now();
        String externalKnowledgeBaseId = existingItem == null ? null : existingItem.getExternalKnowledgeBaseId();
        try {
            ClassicsQaKnowledgeFacadeResponse sourceResponse =
                    classicsFacade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                            .contentType(command.getContentType())
                            .contentId(String.valueOf(command.getContentId()))
                            .build());
            if (sourceResponse == null || sourceResponse.getKnowledge() == null) {
                return disableContentIfNeeded(command, existingItem, sourceId, now);
            }

            KnowledgeDocument document = knowledgeDocumentAssembler.toKnowledgeDocument(sourceResponse);
            if (document == null) {
                throw new BizException(
                        "DISCOVERY-30012", "discovery.qa.sync.document-missing", "QA knowledge document is missing");
            }

            if (shouldDisableContentFromKnowledge(document)) {
                return disableContentIfNeeded(command, existingItem, sourceId, now);
            }

            String title =
                    document.knowledge() == null ? null : document.knowledge().title();
            String renderedKnowledge = knowledgeItemTextRenderer.render(document.knowledge());
            String knowledgeRevision = knowledgeRevisionCalculator.calculate(document.knowledge());
            Integer currentVersionNo = chooseVersionNo(command, document);
            String knowledgeBaseName = resolveKnowledgeBaseName(document);
            KnowledgeBaseResult knowledgeBase = ensureKnowledgeBase(knowledgeBaseName);
            externalKnowledgeBaseId = knowledgeBase.knowledgeBaseId();

            KnowledgeItemResult itemResult = knowledgeBaseClient.upsertKnowledgeItem(new KnowledgeItemUpsertRequest(
                    knowledgeBaseName,
                    sourceIdValue(sourceId),
                    title,
                    renderedKnowledge,
                    buildItemMetadata(command, currentVersionNo, document, knowledgeRevision),
                    buildSyncOptions(command, triggerType)));
            if (itemResult == null || StringUtils.isBlank(itemResult.knowledgeItemId())) {
                throw new BizException(
                        "DISCOVERY-30013", "discovery.qa.sync.upsert-failed", "Knowledge item upsert failed");
            }

            KnowledgeSyncResult syncResult = knowledgeBaseClient.syncKnowledgeItem(new KnowledgeSyncRequest(
                    knowledgeBaseName, itemResult.knowledgeItemId(), Map.of("trigger", triggerType)));

            QaKnowledgeSyncItem syncItem = existingItem == null ? new QaKnowledgeSyncItem() : existingItem;
            syncItem.setSourceId(sourceId);
            syncItem.setContentType(command.getContentType());
            syncItem.setContentId(command.getContentId());
            syncItem.setKnowledgeBaseName(knowledgeBaseName);
            syncItem.setCurrentVersionNo(currentVersionNo);
            syncItem.setKnowledgeRevision(knowledgeRevision);
            syncItem.setProvider(resolveProvider());
            syncItem.setExternalKnowledgeBaseId(itemResult.knowledgeBaseId());
            syncItem.setExternalKnowledgeItemId(itemResult.knowledgeItemId());
            syncItem.setSyncStatus(isSuccessStatus(syncResult) ? SYNC_STATUS_SUCCEEDED : SYNC_STATUS_FAILED);
            syncItem.setFailureReason(syncResultFailureReason(syncResult, "Knowledge item sync failed"));
            syncItem.setCreatedAt(syncItem.getCreatedAt() == null ? now : syncItem.getCreatedAt());
            syncItem.setUpdatedAt(now);
            syncItem.setSyncedAt(isSuccessStatus(syncResult) ? now : null);

            if (syncItem.getId() == null) {
                qaKnowledgeSyncItemRepository.save(syncItem);
            } else {
                qaKnowledgeSyncItemRepository.update(syncItem);
            }
            return toResult(syncItem, title);
        } catch (Exception ex) {
            QaKnowledgeSyncItem failedItem = existingItem == null ? new QaKnowledgeSyncItem() : existingItem;
            failedItem.setSourceId(sourceId);
            failedItem.setContentType(command.getContentType());
            failedItem.setContentId(command.getContentId());
            failedItem.setKnowledgeBaseName(KNOWLEDGE_BASE_NAME);
            failedItem.setCurrentVersionNo(command.getCurrentVersionNo());
            failedItem.setKnowledgeRevision(null);
            failedItem.setProvider(resolveProvider());
            failedItem.setExternalKnowledgeBaseId(externalKnowledgeBaseId);
            failedItem.setExternalKnowledgeItemId(null);
            failedItem.setSyncStatus(SYNC_STATUS_FAILED);
            failedItem.setFailureReason(errorMessage(ex));
            failedItem.setSyncedAt(null);
            failedItem.setCreatedAt(failedItem.getCreatedAt() == null ? now : failedItem.getCreatedAt());
            failedItem.setUpdatedAt(now);
            if (failedItem.getId() == null) {
                qaKnowledgeSyncItemRepository.save(failedItem);
            } else {
                qaKnowledgeSyncItemRepository.update(failedItem);
            }
            return toResult(failedItem);
        }
    }

    private KnowledgeSyncItemResult disableContentIfNeeded(
            SyncKnowledgeContentCommand command,
            QaKnowledgeSyncItem existingItem,
            KnowledgeSourceId sourceId,
            Instant now) {
        if (!isWangqiOrMingCustoms(command.getContentType())) {
            throw new BizException(
                    "DISCOVERY-30011", "discovery.qa.sync.source-missing", "QA knowledge source is not available");
        }

        if (existingItem == null) {
            return syncDeletedContent(command, sourceId, now, "Sync item does not exist", null);
        }

        return deleteSyncItem(existingItem, sourceId, now, command.getRequestId(), command.getTraceId());
    }

    private List<QaKnowledgeSyncItem> listItemsForPageQuery(KnowledgeSyncItemQuery query) {
        String syncStatus = normalizeString(query == null ? null : query.syncStatus());
        String contentType = normalizeString(query == null ? null : query.contentType());

        List<QaKnowledgeSyncItem> items = StringUtils.isBlank(syncStatus)
                ? listAllSyncItems()
                : new ArrayList<>(qaKnowledgeSyncItemRepository.listBySyncStatus(
                        QaStringValueCodec.toKnowledgeSyncStatus(syncStatus), Integer.MAX_VALUE));

        if (StringUtils.isBlank(contentType)) {
            return items;
        }

        return items.stream()
                .filter(item -> contentType.equals(item.getContentType()))
                .collect(Collectors.toList());
    }

    private List<ClassicsQaKnowledgeFacadeRequest> listSyncSources() {
        ClassicsPublicContentsFacadeResponse response = classicsFacade.listPublicContents();
        if (response == null || response.getContents() == null) {
            return Collections.emptyList();
        }
        return response.getContents().stream()
                .map(content -> {
                    if (StringUtils.isBlank(content.getContentType()) || StringUtils.isBlank(content.getContentId())) {
                        return null;
                    }
                    return ClassicsQaKnowledgeFacadeRequest.builder()
                            .contentType(content.getContentType())
                            .contentId(content.getContentId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<QaKnowledgeSyncItem> listAllSyncItems() {
        List<String> syncStatuses = List.of(
                SYNC_STATUS_SUCCEEDED,
                SYNC_STATUS_FAILED,
                SYNC_STATUS_DELETED,
                SYNC_STATUS_SYNCING,
                SYNC_STATUS_PENDING);
        Map<String, QaKnowledgeSyncItem> unique = new LinkedHashMap<>();
        for (String status : syncStatuses) {
            QaKnowledgeSyncStatus syncStatus = QaStringValueCodec.toKnowledgeSyncStatus(status);
            for (QaKnowledgeSyncItem item :
                    qaKnowledgeSyncItemRepository.listBySyncStatus(syncStatus, Integer.MAX_VALUE)) {
                String itemSourceId = sourceIdValue(item == null ? null : item.getSourceId());
                if (StringUtils.isNotBlank(itemSourceId)) {
                    unique.put(itemSourceId, item);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private void ensureKnowledgeBase() {
        ensureKnowledgeBase(KNOWLEDGE_BASE_NAME);
    }

    private KnowledgeBaseResult ensureKnowledgeBase(String knowledgeBaseName) {
        return knowledgeBaseClient.ensureKnowledgeBase(new KnowledgeBaseEnsureRequest(
                knowledgeBaseName, "Discovery QA Knowledge Base", Collections.emptyMap()));
    }

    private Map<String, Object> buildSyncOptions(SyncKnowledgeContentCommand command, String triggerType) {
        Map<String, Object> options = new HashMap<>();
        options.put("requestId", StringUtils.defaultString(command.getRequestId()));
        options.put("traceId", StringUtils.defaultString(command.getTraceId()));
        options.put("triggerType", StringUtils.defaultString(triggerType));
        return options;
    }

    private String resolveProvider() {
        try {
            com.thundax.kuzhambu.common.knowledge.model.health.KnowledgeHealthResult result =
                    knowledgeBaseClient.health();
            return result == null || StringUtils.isBlank(result.provider()) ? DEFAULT_PROVIDER : result.provider();
        } catch (Exception ex) {
            return DEFAULT_PROVIDER;
        }
    }

    private String resolveKnowledgeBaseName(KnowledgeDocument document) {
        if (document.metadata() == null
                || StringUtils.isBlank(document.metadata().knowledgeBase())) {
            return KNOWLEDGE_BASE_NAME;
        }
        return document.metadata().knowledgeBase();
    }

    private int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo <= 0 ? DEFAULT_PAGE_NO : pageNo;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    private Integer chooseVersionNo(SyncKnowledgeContentCommand command, KnowledgeDocument document) {
        Integer currentVersionNo = command.getCurrentVersionNo();
        if (currentVersionNo != null) {
            return currentVersionNo;
        }
        if (document.metadata() != null && document.metadata().currentVersionNo() != null) {
            return document.metadata().currentVersionNo();
        }
        return 0;
    }

    private Map<String, Object> buildItemMetadata(
            SyncKnowledgeContentCommand command,
            Integer currentVersionNo,
            KnowledgeDocument document,
            String knowledgeRevision) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceId", sourceIdValue(sourceId(command)));
        metadata.put("contentType", command.getContentType());
        metadata.put(
                "contentId",
                command.getContentId() == null ? null : command.getContentId().toString());
        metadata.put("knowledgeBase", KNOWLEDGE_BASE_NAME);
        if (currentVersionNo != null) {
            metadata.put("currentVersionNo", currentVersionNo);
        }
        if (knowledgeRevision != null) {
            metadata.put("knowledgeRevision", knowledgeRevision);
        }
        if (document.knowledge() != null) {
            metadata.put("title", document.knowledge().title());
        }
        if (document.metadata() != null) {
            metadata.put("sourcePath", document.metadata().sourcePath());
        }
        return metadata;
    }

    private boolean isSuccessStatus(KnowledgeSyncResult syncResult) {
        return syncResult != null
                && ("SUCCEEDED".equalsIgnoreCase(syncResult.status())
                        || "DONE".equalsIgnoreCase(syncResult.status())
                        || SYNC_STATUS_DELETED.equalsIgnoreCase(syncResult.status()));
    }

    private boolean shouldDisableContentFromKnowledge(KnowledgeDocument document) {
        if (document == null || document.metadata() == null) {
            return false;
        }
        String contentType = document.metadata().contentType();
        String visibility = document.metadata().visibility();
        return isWangqiOrMingCustoms(contentType) && !"PUBLIC".equalsIgnoreCase(visibility);
    }

    private boolean isWangqiOrMingCustoms(String contentType) {
        return "WANGQI_DOCUMENT".equals(contentType) || "MING_CUSTOMS".equals(contentType);
    }

    private KnowledgeSyncItemResult syncDeletedContent(
            SyncKnowledgeContentCommand command,
            KnowledgeSourceId sourceId,
            Instant now,
            String failureReasonIfMissing,
            QaKnowledgeSyncItem existingItem) {
        QaKnowledgeSyncItem failedItem = existingItem == null ? new QaKnowledgeSyncItem() : existingItem;
        failedItem.setSourceId(sourceId);
        failedItem.setContentType(command.getContentType());
        failedItem.setContentId(command.getContentId());
        failedItem.setKnowledgeBaseName(KNOWLEDGE_BASE_NAME);
        failedItem.setCurrentVersionNo(command.getCurrentVersionNo());
        failedItem.setKnowledgeRevision(null);
        failedItem.setProvider(resolveProvider());
        failedItem.setExternalKnowledgeBaseId(null);
        failedItem.setExternalKnowledgeItemId(null);
        failedItem.setSyncStatus(SYNC_STATUS_FAILED);
        failedItem.setFailureReason(failureReasonIfMissing);
        failedItem.setSyncedAt(null);
        failedItem.setCreatedAt(failedItem.getCreatedAt() == null ? now : failedItem.getCreatedAt());
        failedItem.setUpdatedAt(now);
        if (failedItem.getId() == null) {
            qaKnowledgeSyncItemRepository.save(failedItem);
        } else {
            qaKnowledgeSyncItemRepository.update(failedItem);
        }
        return toResult(failedItem);
    }

    private KnowledgeSyncItemResult deleteSyncItem(
            QaKnowledgeSyncItem existingItem,
            KnowledgeSourceId sourceId,
            Instant now,
            String requestId,
            String traceId) {
        if (StringUtils.isBlank(existingItem.getExternalKnowledgeItemId())) {
            return syncDeletedContent(
                    new SyncKnowledgeContentCommand(
                            existingItem.getContentType(),
                            existingItem.getContentId(),
                            existingItem.getCurrentVersionNo(),
                            requestId,
                            traceId),
                    sourceId,
                    now,
                    "Sync item does not have external knowledge item id",
                    existingItem);
        }

        try {
            KnowledgeSyncResult deleteResult = knowledgeBaseClient.deleteKnowledgeItem(new KnowledgeItemDeleteRequest(
                    knowledgeItemBase(existingItem),
                    existingItem.getExternalKnowledgeItemId(),
                    sourceIdValue(sourceId),
                    Map.of("operation", "deleteContent")));
            existingItem.setSyncStatus(isSuccessStatus(deleteResult) ? SYNC_STATUS_DELETED : SYNC_STATUS_FAILED);
            existingItem.setFailureReason(syncResultFailureReason(deleteResult, "deleteKnowledgeItem failed"));
            if (SYNC_STATUS_DELETED.equals(syncStatusValue(existingItem))) {
                existingItem.setSyncedAt(now);
            }
            existingItem.setUpdatedAt(now);
            qaKnowledgeSyncItemRepository.update(existingItem);
            return toResult(existingItem);
        } catch (Exception ex) {
            existingItem.setSyncStatus(SYNC_STATUS_FAILED);
            existingItem.setFailureReason(errorMessage(ex));
            existingItem.setSyncedAt(null);
            existingItem.setUpdatedAt(now);
            qaKnowledgeSyncItemRepository.update(existingItem);
            return toResult(existingItem);
        }
    }

    private String syncResultFailureReason(KnowledgeSyncResult syncResult, String fallback) {
        if (syncResult == null || StringUtils.isBlank(syncResult.status())) {
            return fallback;
        }
        return syncResult.status();
    }

    private String knowledgeItemBase(QaKnowledgeSyncItem item) {
        if (item == null) {
            return KNOWLEDGE_BASE_NAME;
        }
        return StringUtils.defaultIfBlank(item.getKnowledgeBaseName(), KNOWLEDGE_BASE_NAME);
    }

    private void validateCommand(SyncKnowledgeContentCommand command) {
        if (command == null || StringUtils.isBlank(command.getContentType()) || command.getContentId() == null) {
            throw new BizException("DISCOVERY-30001", "discovery.qa.sync.command.invalid", "Sync command is invalid");
        }
    }

    private KnowledgeSyncItemResult toResult(QaKnowledgeSyncItem item) {
        return toResult(item, null);
    }

    private KnowledgeSyncItemResult toResultWithTitle(QaKnowledgeSyncItem item) {
        return toResult(item, resolveTitle(item));
    }

    private String resolveTitle(QaKnowledgeSyncItem item) {
        if (item == null || StringUtils.isBlank(item.getContentType()) || item.getContentId() == null) {
            return null;
        }
        try {
            ClassicsQaKnowledgeFacadeResponse response =
                    classicsFacade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                            .contentType(item.getContentType())
                            .contentId(String.valueOf(item.getContentId()))
                            .build());
            return response == null || response.getKnowledge() == null
                    ? null
                    : response.getKnowledge().getTitle();
        } catch (Exception ex) {
            return null;
        }
    }

    private KnowledgeSyncItemResult toResult(QaKnowledgeSyncItem item, String title) {
        return new KnowledgeSyncItemResult(
                sourceIdValue(item == null ? null : item.getSourceId()),
                item == null ? null : item.getContentType(),
                item == null ? null : item.getContentId(),
                title,
                item == null ? null : item.getKnowledgeBaseName(),
                item == null ? null : item.getCurrentVersionNo(),
                item == null ? null : item.getKnowledgeRevision(),
                item == null ? null : item.getProvider(),
                item == null ? null : item.getExternalKnowledgeBaseId(),
                item == null ? null : item.getExternalKnowledgeItemId(),
                syncStatusValue(item),
                item == null ? null : item.getFailureReason(),
                item == null || item.getSyncedAt() == null
                        ? null
                        : item.getSyncedAt().toEpochMilli(),
                item == null || item.getCreatedAt() == null
                        ? null
                        : item.getCreatedAt().toEpochMilli(),
                item == null || item.getUpdatedAt() == null
                        ? null
                        : item.getUpdatedAt().toEpochMilli());
    }

    private KnowledgeSourceId sourceId(SyncKnowledgeContentCommand command) {
        return QaStringValueCodec.toKnowledgeSourceId(command.getContentType() + ":" + command.getContentId());
    }

    private String sourceIdValue(KnowledgeSourceId sourceId) {
        return QaStringValueCodec.toValue(sourceId);
    }

    private String syncStatusValue(QaKnowledgeSyncItem item) {
        return item == null ? null : QaStringValueCodec.toValue(item.getSyncStatus());
    }

    private String normalizeString(String value) {
        return StringUtils.isBlank(value) ? null : value;
    }

    private Long parseContentId(String contentId) {
        try {
            return Long.valueOf(contentId);
        } catch (NumberFormatException ex) {
            throw new BizException("DISCOVERY-30002", "discovery.qa.sync.content-id.invalid", "Content id is invalid");
        }
    }

    private String errorMessage(Throwable ex) {
        return ex == null ? "Sync failed" : ex.getMessage();
    }
}
