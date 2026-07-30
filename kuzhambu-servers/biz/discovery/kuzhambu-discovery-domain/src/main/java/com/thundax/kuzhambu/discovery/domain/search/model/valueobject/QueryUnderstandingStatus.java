package com.thundax.kuzhambu.discovery.domain.search.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class QueryUnderstandingStatus extends BaseStringId {

    public QueryUnderstandingStatus(String value) {
        super(value == null ? null : value.trim());
    }
}
