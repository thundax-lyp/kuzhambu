package com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject;

public final class MingCustomsKeywordCloudItem {

    private final String keyword;
    private final Long count;

    public MingCustomsKeywordCloudItem(String keyword, Long count) {
        this.keyword = keyword;
        this.count = count;
    }

    public String getKeyword() {
        return keyword;
    }

    public Long getCount() {
        return count;
    }
}
