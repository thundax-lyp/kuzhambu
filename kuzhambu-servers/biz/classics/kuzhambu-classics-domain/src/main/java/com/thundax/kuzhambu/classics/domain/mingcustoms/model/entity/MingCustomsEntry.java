package com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MingCustomsEntry {
    private MingCustomsEntryId id;
    private String title;
    private String category;
    private String chapter;
    private String section;
    private String summary;
    private MingCustomsContentFormat contentFormat;
    private String content;
    private String originalExcerpts;
    private MingCustomsVisibility visibility;
}
