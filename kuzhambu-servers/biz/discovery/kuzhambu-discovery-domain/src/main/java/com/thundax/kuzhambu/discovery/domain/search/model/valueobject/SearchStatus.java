package com.thundax.kuzhambu.discovery.domain.search.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class SearchStatus extends BaseStringId {

    public SearchStatus(String value) {
        super(value == null ? null : value.trim());
    }
}
