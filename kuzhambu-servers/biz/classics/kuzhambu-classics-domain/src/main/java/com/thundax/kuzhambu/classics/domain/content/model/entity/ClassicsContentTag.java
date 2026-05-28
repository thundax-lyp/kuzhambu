package com.thundax.kuzhambu.classics.domain.content.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.KnowledgeTagId;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsContentTag implements Sortable {
    private ClassicsContentTagId id;
    private ClassicsContentType contentType;
    private ClassicsContentId contentId;
    private KnowledgeTagId tagId;
    private String tagNameSnapshot;
    private ClassicsContentSource source;
    private ClassicsContentTagStatus status;
    private int priority;
}
