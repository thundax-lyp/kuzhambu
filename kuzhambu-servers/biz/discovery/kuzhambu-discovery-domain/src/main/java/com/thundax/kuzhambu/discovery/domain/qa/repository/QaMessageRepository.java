package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import java.util.List;

public interface QaMessageRepository {

    QaMessage getById(QaMessageId id);

    default QaMessage getByMessageId(QaMessageId messageId) {
        return getById(messageId);
    }

    List<QaMessage> listBySessionId(QaSessionId sessionId);

    QaMessageId save(QaMessage entity);

    int update(QaMessage entity);
}
