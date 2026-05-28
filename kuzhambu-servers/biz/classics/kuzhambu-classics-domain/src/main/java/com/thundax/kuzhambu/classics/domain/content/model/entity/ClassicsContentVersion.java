package com.thundax.kuzhambu.classics.domain.content.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsContentVersion {
    private ClassicsContentVersionId id;
    private ClassicsContentType contentType;
    private ClassicsContentId contentId;
    private int versionNo;
    private Date versionedAt;
    private String snapshotJson;
    private ClassicsContentChangeType changeType;
    private String changeSummary;
}
