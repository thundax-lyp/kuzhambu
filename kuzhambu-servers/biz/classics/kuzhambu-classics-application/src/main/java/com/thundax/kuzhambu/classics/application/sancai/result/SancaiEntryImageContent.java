package com.thundax.kuzhambu.classics.application.sancai.result;

import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntryImageContent {
    private Long entryId;
    private Long imageId;
    private Long storageObjectId;
    private StoredObjectContent content;
}
