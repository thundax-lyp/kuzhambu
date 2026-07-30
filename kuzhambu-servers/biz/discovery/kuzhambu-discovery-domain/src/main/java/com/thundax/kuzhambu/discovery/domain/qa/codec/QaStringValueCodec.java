package com.thundax.kuzhambu.discovery.domain.qa.codec;

import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.KnowledgeContentRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.KnowledgeSourceId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaContextContentRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaKnowledgeSyncStatus;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageRole;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaOwnerRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionStatus;

public final class QaStringValueCodec {

    private QaStringValueCodec() {}

    public static QaOwnerRef toOwnerRef(String ownerType, String ownerId) {
        return isBlank(ownerType) && isBlank(ownerId) ? null : new QaOwnerRef(trim(ownerType), trim(ownerId));
    }

    public static QaContextContentRef toContextContentRef(String contentType, Long contentId) {
        return isBlank(contentType) && contentId == null ? null : new QaContextContentRef(trim(contentType), contentId);
    }

    public static KnowledgeContentRef toKnowledgeContentRef(String contentType, Long contentId) {
        return isBlank(contentType) && contentId == null ? null : new KnowledgeContentRef(trim(contentType), contentId);
    }

    public static KnowledgeSourceId toKnowledgeSourceId(String value) {
        return isBlank(value) ? null : new KnowledgeSourceId(value);
    }

    public static String toValue(KnowledgeSourceId value) {
        return value == null ? null : value.value();
    }

    public static QaSessionStatus toSessionStatus(String value) {
        return isBlank(value) ? null : new QaSessionStatus(value);
    }

    public static String toValue(QaSessionStatus value) {
        return value == null ? null : value.value();
    }

    public static QaMessageRole toMessageRole(String value) {
        return isBlank(value) ? null : new QaMessageRole(value);
    }

    public static String toValue(QaMessageRole value) {
        return value == null ? null : value.value();
    }

    public static QaKnowledgeSyncStatus toKnowledgeSyncStatus(String value) {
        return isBlank(value) ? null : new QaKnowledgeSyncStatus(value);
    }

    public static String toValue(QaKnowledgeSyncStatus value) {
        return value == null ? null : value.value();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
