package com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler;

import com.thundax.kuzhambu.discovery.domain.qa.codec.QaMessageIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaMessageDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaRetrievalTraceDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionExportDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSourceDO;
import java.util.ArrayList;
import java.util.List;

public final class QaPersistenceAssembler {

    private QaPersistenceAssembler() {}

    public static QaSessionDO toObject(QaSession entity) {
        if (entity == null) {
            return null;
        }
        QaSessionDO dataObject = new QaSessionDO();
        dataObject.setId(QaSessionIdCodec.toValue(entity.getId()));
        dataObject.setOwnerType(entity.getOwnerType());
        dataObject.setOwnerId(entity.getOwnerId());
        dataObject.setKnowledgeBaseName(entity.getKnowledgeBaseName());
        dataObject.setTitle(entity.getTitle());
        dataObject.setScope(entity.getScope());
        dataObject.setContextMode(entity.getContextMode());
        dataObject.setContextContentType(entity.getContextContentType());
        dataObject.setContextContentId(entity.getContextContentId());
        dataObject.setStatus(QaStringValueCodec.toValue(entity.getStatus()));
        dataObject.setOpenedAt(entity.getOpenedAt());
        dataObject.setLastMessageAt(entity.getLastMessageAt());
        dataObject.setRemovedAt(entity.getRemovedAt());
        return dataObject;
    }

    public static QaSession toSessionDomain(QaSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaSession entity = new QaSession();
        entity.setId(QaSessionIdCodec.toDomain(dataObject.getId()));
        entity.setOwnerType(dataObject.getOwnerType());
        entity.setOwnerId(dataObject.getOwnerId());
        entity.setKnowledgeBaseName(dataObject.getKnowledgeBaseName());
        entity.setTitle(dataObject.getTitle());
        entity.setScope(dataObject.getScope());
        entity.setContextMode(dataObject.getContextMode());
        entity.setContextContentType(dataObject.getContextContentType());
        entity.setContextContentId(dataObject.getContextContentId());
        entity.setStatus(QaStringValueCodec.toSessionStatus(dataObject.getStatus()));
        entity.setOpenedAt(dataObject.getOpenedAt());
        entity.setLastMessageAt(dataObject.getLastMessageAt());
        entity.setRemovedAt(dataObject.getRemovedAt());
        return entity;
    }

    public static QaSessionExportDO toObject(QaSessionExport entity) {
        if (entity == null) {
            return null;
        }
        QaSessionExportDO dataObject = new QaSessionExportDO();
        dataObject.setId(entity.getId());
        dataObject.setSessionId(entity.getSessionId());
        dataObject.setFormat(entity.getFormat());
        dataObject.setStorageObjectId(entity.getStorageObjectId());
        dataObject.setExportStatus(entity.getExportStatus());
        dataObject.setFailureReason(entity.getFailureReason());
        dataObject.setRequesterUserId(entity.getRequesterUserId());
        dataObject.setRequestedAt(entity.getRequestedAt());
        dataObject.setCompletedAt(entity.getCompletedAt());
        return dataObject;
    }

    public static QaSessionExport toSessionExportDomain(QaSessionExportDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaSessionExport entity = new QaSessionExport();
        entity.setId(dataObject.getId());
        entity.setSessionId(dataObject.getSessionId());
        entity.setFormat(dataObject.getFormat());
        entity.setStorageObjectId(dataObject.getStorageObjectId());
        entity.setExportStatus(dataObject.getExportStatus());
        entity.setFailureReason(dataObject.getFailureReason());
        entity.setRequesterUserId(dataObject.getRequesterUserId());
        entity.setRequestedAt(dataObject.getRequestedAt());
        entity.setCompletedAt(dataObject.getCompletedAt());
        return entity;
    }

    public static QaMessageDO toObject(QaMessage entity) {
        if (entity == null) {
            return null;
        }
        QaMessageDO dataObject = new QaMessageDO();
        dataObject.setId(QaMessageIdCodec.toValue(entity.getId()));
        dataObject.setSessionId(QaSessionIdCodec.toValue(entity.getSessionId()));
        dataObject.setRole(QaStringValueCodec.toValue(entity.getRole()));
        dataObject.setContent(entity.getContent());
        dataObject.setAnswerStatus(entity.getAnswerStatus());
        dataObject.setModel(entity.getModel());
        dataObject.setContextTurnCount(entity.getContextTurnCount());
        dataObject.setFailureReason(entity.getFailureReason());
        dataObject.setProviderChatId(entity.getProviderChatId());
        dataObject.setFinishReason(entity.getFinishReason());
        dataObject.setSentAt(entity.getSentAt());
        dataObject.setAnsweredAt(entity.getAnsweredAt());
        return dataObject;
    }

    public static QaMessage toMessageDomain(QaMessageDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaMessage entity = new QaMessage();
        entity.setId(QaMessageIdCodec.toDomain(dataObject.getId()));
        entity.setSessionId(QaSessionIdCodec.toDomain(dataObject.getSessionId()));
        entity.setRole(QaStringValueCodec.toMessageRole(dataObject.getRole()));
        entity.setContent(dataObject.getContent());
        entity.setAnswerStatus(dataObject.getAnswerStatus());
        entity.setModel(dataObject.getModel());
        entity.setContextTurnCount(dataObject.getContextTurnCount());
        entity.setFailureReason(dataObject.getFailureReason());
        entity.setProviderChatId(dataObject.getProviderChatId());
        entity.setFinishReason(dataObject.getFinishReason());
        entity.setSentAt(dataObject.getSentAt());
        entity.setAnsweredAt(dataObject.getAnsweredAt());
        return entity;
    }

    public static QaSourceDO toObject(QaSource entity) {
        if (entity == null) {
            return null;
        }
        QaSourceDO dataObject = new QaSourceDO();
        dataObject.setId(entity.getId());
        dataObject.setSourceBusinessId(entity.getSourceBusinessId());
        dataObject.setMessageId(entity.getMessageId());
        dataObject.setContentType(entity.getContentType());
        dataObject.setContentId(entity.getContentId());
        dataObject.setKnowledgeBase(entity.getKnowledgeBase());
        dataObject.setTitleSnapshot(entity.getTitleSnapshot());
        dataObject.setLocationLabel(entity.getLocationLabel());
        dataObject.setSnippet(entity.getSnippet());
        dataObject.setSourcePath(entity.getSourcePath());
        dataObject.setSourceRank(entity.getSourceRank());
        dataObject.setScore(entity.getScore());
        dataObject.setSourceStatus(entity.getSourceStatus());
        dataObject.setReferencedAt(entity.getReferencedAt());
        return dataObject;
    }

    public static QaSource toSourceDomain(QaSourceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaSource entity = new QaSource();
        entity.setId(dataObject.getId());
        entity.setSourceBusinessId(dataObject.getSourceBusinessId());
        entity.setMessageId(dataObject.getMessageId());
        entity.setContentType(dataObject.getContentType());
        entity.setContentId(dataObject.getContentId());
        entity.setKnowledgeBase(dataObject.getKnowledgeBase());
        entity.setTitleSnapshot(dataObject.getTitleSnapshot());
        entity.setLocationLabel(dataObject.getLocationLabel());
        entity.setSnippet(dataObject.getSnippet());
        entity.setSourcePath(dataObject.getSourcePath());
        entity.setSourceRank(dataObject.getSourceRank());
        entity.setScore(dataObject.getScore());
        entity.setSourceStatus(dataObject.getSourceStatus());
        entity.setReferencedAt(dataObject.getReferencedAt());
        return entity;
    }

    public static QaRetrievalTraceDO toObject(QaRetrievalTrace entity) {
        if (entity == null) {
            return null;
        }
        QaRetrievalTraceDO dataObject = new QaRetrievalTraceDO();
        dataObject.setId(entity.getId());
        dataObject.setMessageId(entity.getMessageId());
        dataObject.setRawQuestion(entity.getRawQuestion());
        dataObject.setProvider(entity.getProvider());
        dataObject.setExternalKnowledgeBaseId(entity.getExternalKnowledgeBaseId());
        dataObject.setExternalKnowledgeItemIds(entity.getExternalKnowledgeItemIds());
        dataObject.setExternalChatId(entity.getExternalChatId());
        dataObject.setProviderRequestId(entity.getProviderRequestId());
        dataObject.setLatencyMs(entity.getLatencyMs());
        dataObject.setFailureReason(entity.getFailureReason());
        dataObject.setRaw(entity.getRaw());
        dataObject.setAiCallId(entity.getAiCallId());
        dataObject.setAiStatus(entity.getAiStatus());
        dataObject.setAiErrorType(entity.getAiErrorType());
        dataObject.setAiErrorMessage(entity.getAiErrorMessage());
        dataObject.setRetrievedAt(entity.getRetrievedAt());
        return dataObject;
    }

    public static QaRetrievalTrace toTraceDomain(QaRetrievalTraceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaRetrievalTrace entity = new QaRetrievalTrace();
        entity.setId(dataObject.getId());
        entity.setMessageId(dataObject.getMessageId());
        entity.setRawQuestion(dataObject.getRawQuestion());
        entity.setProvider(dataObject.getProvider());
        entity.setExternalKnowledgeBaseId(dataObject.getExternalKnowledgeBaseId());
        entity.setExternalKnowledgeItemIds(dataObject.getExternalKnowledgeItemIds());
        entity.setExternalChatId(dataObject.getExternalChatId());
        entity.setProviderRequestId(dataObject.getProviderRequestId());
        entity.setLatencyMs(dataObject.getLatencyMs());
        entity.setFailureReason(dataObject.getFailureReason());
        entity.setRaw(dataObject.getRaw());
        entity.setAiCallId(dataObject.getAiCallId());
        entity.setAiStatus(dataObject.getAiStatus());
        entity.setAiErrorType(dataObject.getAiErrorType());
        entity.setAiErrorMessage(dataObject.getAiErrorMessage());
        entity.setRetrievedAt(dataObject.getRetrievedAt());
        return entity;
    }

    public static List<QaMessage> toMessageDomainList(List<QaMessageDO> dataObjects) {
        return dataObjects == null
                ? null
                : dataObjects.stream()
                        .map(QaPersistenceAssembler::toMessageDomain)
                        .toList();
    }

    public static List<QaSource> toSourceDomainList(List<QaSourceDO> dataObjects) {
        return dataObjects == null
                ? null
                : dataObjects.stream()
                        .map(QaPersistenceAssembler::toSourceDomain)
                        .toList();
    }

    public static List<QaSession> toSessionDomainList(List<QaSessionDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<QaSession> entities = new ArrayList<>();
        for (QaSessionDO dataObject : dataObjects) {
            entities.add(toSessionDomain(dataObject));
        }
        return entities;
    }
}
