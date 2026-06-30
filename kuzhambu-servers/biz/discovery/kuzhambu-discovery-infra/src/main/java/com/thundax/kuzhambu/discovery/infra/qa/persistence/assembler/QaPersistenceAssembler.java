package com.thundax.kuzhambu.discovery.infra.qa.persistence.assembler;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaMessageDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaRetrievalTraceDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaSessionDO;
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
        dataObject.setId(entity.getId());
        dataObject.setSessionId(entity.getSessionId());
        dataObject.setOwnerUserId(entity.getOwnerUserId());
        dataObject.setTitle(entity.getTitle());
        dataObject.setScope(entity.getScope());
        dataObject.setContextMode(entity.getContextMode());
        dataObject.setContextContentType(entity.getContextContentType());
        dataObject.setContextContentId(entity.getContextContentId());
        dataObject.setStatus(entity.getStatus());
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
        entity.setId(dataObject.getId());
        entity.setSessionId(dataObject.getSessionId());
        entity.setOwnerUserId(dataObject.getOwnerUserId());
        entity.setTitle(dataObject.getTitle());
        entity.setScope(dataObject.getScope());
        entity.setContextMode(dataObject.getContextMode());
        entity.setContextContentType(dataObject.getContextContentType());
        entity.setContextContentId(dataObject.getContextContentId());
        entity.setStatus(dataObject.getStatus());
        entity.setOpenedAt(dataObject.getOpenedAt());
        entity.setLastMessageAt(dataObject.getLastMessageAt());
        entity.setRemovedAt(dataObject.getRemovedAt());
        return entity;
    }

    public static QaMessageDO toObject(QaMessage entity) {
        if (entity == null) {
            return null;
        }
        QaMessageDO dataObject = new QaMessageDO();
        dataObject.setId(entity.getId());
        dataObject.setMessageId(entity.getMessageId());
        dataObject.setSessionId(entity.getSessionId());
        dataObject.setRole(entity.getRole());
        dataObject.setContent(entity.getContent());
        dataObject.setMessageStatus(entity.getMessageStatus());
        dataObject.setContextTurnCount(entity.getContextTurnCount());
        dataObject.setFailureReason(entity.getFailureReason());
        dataObject.setSentAt(entity.getSentAt());
        dataObject.setAnsweredAt(entity.getAnsweredAt());
        return dataObject;
    }

    public static QaMessage toMessageDomain(QaMessageDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaMessage entity = new QaMessage();
        entity.setId(dataObject.getId());
        entity.setMessageId(dataObject.getMessageId());
        entity.setSessionId(dataObject.getSessionId());
        entity.setRole(dataObject.getRole());
        entity.setContent(dataObject.getContent());
        entity.setMessageStatus(dataObject.getMessageStatus());
        entity.setContextTurnCount(dataObject.getContextTurnCount());
        entity.setFailureReason(dataObject.getFailureReason());
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
        dataObject.setSourceId(entity.getSourceId());
        dataObject.setMessageId(entity.getMessageId());
        dataObject.setContentType(entity.getContentType());
        dataObject.setContentId(entity.getContentId());
        dataObject.setKnowledgeBase(entity.getKnowledgeBase());
        dataObject.setTitleSnapshot(entity.getTitleSnapshot());
        dataObject.setLocationLabel(entity.getLocationLabel());
        dataObject.setSnippet(entity.getSnippet());
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
        entity.setSourceId(dataObject.getSourceId());
        entity.setMessageId(dataObject.getMessageId());
        entity.setContentType(dataObject.getContentType());
        entity.setContentId(dataObject.getContentId());
        entity.setKnowledgeBase(dataObject.getKnowledgeBase());
        entity.setTitleSnapshot(dataObject.getTitleSnapshot());
        entity.setLocationLabel(dataObject.getLocationLabel());
        entity.setSnippet(dataObject.getSnippet());
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
        dataObject.setTraceId(entity.getTraceId());
        dataObject.setMessageId(entity.getMessageId());
        dataObject.setCallId(entity.getCallId());
        dataObject.setRawQuestion(entity.getRawQuestion());
        dataObject.setRewrittenQuestion(entity.getRewrittenQuestion());
        dataObject.setScope(entity.getScope());
        dataObject.setFiltersJson(entity.getFiltersJson());
        dataObject.setExpandedTermsJson(entity.getExpandedTermsJson());
        dataObject.setLinkedEntitiesJson(entity.getLinkedEntitiesJson());
        dataObject.setCandidateCount(entity.getCandidateCount());
        dataObject.setContextSnapshot(entity.getContextSnapshot());
        dataObject.setRetrievedAt(entity.getRetrievedAt());
        return dataObject;
    }

    public static QaRetrievalTrace toTraceDomain(QaRetrievalTraceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        QaRetrievalTrace entity = new QaRetrievalTrace();
        entity.setId(dataObject.getId());
        entity.setTraceId(dataObject.getTraceId());
        entity.setMessageId(dataObject.getMessageId());
        entity.setCallId(dataObject.getCallId());
        entity.setRawQuestion(dataObject.getRawQuestion());
        entity.setRewrittenQuestion(dataObject.getRewrittenQuestion());
        entity.setScope(dataObject.getScope());
        entity.setFiltersJson(dataObject.getFiltersJson());
        entity.setExpandedTermsJson(dataObject.getExpandedTermsJson());
        entity.setLinkedEntitiesJson(dataObject.getLinkedEntitiesJson());
        entity.setCandidateCount(dataObject.getCandidateCount());
        entity.setContextSnapshot(dataObject.getContextSnapshot());
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
