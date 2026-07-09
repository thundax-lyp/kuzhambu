package com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject;

public final class MingCustomsTagCloudItem {

    private final Long tagId;
    private final String tagNameSnapshot;
    private final Long count;

    public MingCustomsTagCloudItem(Long tagId, String tagNameSnapshot, Long count) {
        this.tagId = tagId;
        this.tagNameSnapshot = tagNameSnapshot;
        this.count = count;
    }

    public Long getTagId() {
        return tagId;
    }

    public String getTagNameSnapshot() {
        return tagNameSnapshot;
    }

    public Long getCount() {
        return count;
    }
}
