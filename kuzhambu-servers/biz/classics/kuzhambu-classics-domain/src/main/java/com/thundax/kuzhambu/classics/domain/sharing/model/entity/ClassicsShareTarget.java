package com.thundax.kuzhambu.classics.domain.sharing.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsShareTarget implements Sortable {
    private Long id;
    private Long shareLinkId;
    private ClassicsContentType contentType;
    private Long contentId;
    private String titleSnapshot;
    private String contentSnapshotJson;
    private ClassicsSharedContentVisibility contentVisibilitySnapshot;
    private ClassicsShareTargetStatus targetStatus;
    private int priority;
}
