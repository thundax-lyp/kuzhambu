package com.thundax.kuzhambu.classics.domain.content.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsContentVersion {
    private Long id;
    private ClassicsContentType contentType;
    private Long contentId;
    private int versionNo;
    private LocalDateTime versionedAt;
    private String snapshotJson;
    private ClassicsContentChangeType changeType;
    private String changeSummary;
}
