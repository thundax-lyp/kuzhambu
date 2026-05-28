package com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MingCustomsKeyword implements Sortable {
    private MingCustomsKeywordId id;
    private MingCustomsEntryId customId;
    private String keyword;
    private int priority;
}
