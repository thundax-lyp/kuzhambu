package com.thundax.kuzhambu.discovery.domain.qa.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class QaSessionStatus extends BaseStringId {

    public QaSessionStatus(String value) {
        super(value == null ? null : value.trim());
    }
}
