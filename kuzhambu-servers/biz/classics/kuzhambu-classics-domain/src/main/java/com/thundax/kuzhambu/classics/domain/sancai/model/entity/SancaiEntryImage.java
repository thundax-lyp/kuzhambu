package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntryImage implements Sortable {
    private SancaiEntryImageId id;
    private SancaiEntryId entryId;
    private StorageObjectId storageObjectId;
    private SancaiEntryImageType imageType;
    private String title;
    private boolean currentUsed;
    private int priority;
}
