package com.thundax.kuzhambu.discovery.domain.qa.repository;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import java.util.List;

public interface QaMessageRepository {

    QaMessage getById(Long id);

    default QaMessage getByMessageId(Long messageId) {
        return getById(messageId);
    }

    List<QaMessage> listBySessionId(Long sessionId);

    Long save(QaMessage entity);

    int update(QaMessage entity);
}
