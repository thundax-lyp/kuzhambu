package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiCategoryOverview {
    private SancaiCategoryId categoryId;
    private long publicEntryCount;
    private long illustratedEntryCount;
    private SancaiEntryId representativeEntryId;
    private SancaiEntryImageId representativeImageId;
    private String representativeImageTitle;
}
