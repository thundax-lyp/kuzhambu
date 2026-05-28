package com.thundax.kuzhambu.classics.domain.wangqi.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocument {
    private WangqiDocumentId id;
    private String title;
    private String summary;
    private WangqiContentFormat contentFormat;
    private String content;
    private Date documentTime;
    private StorageObjectId storageObjectId;
    private WangqiDocumentVisibility visibility;
}
