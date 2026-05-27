package com.thundax.kuzhambu.classics.domain.content.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
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
    private Long id;
    private ClassicsContentType contentType;
    private Long contentId;
    private Long tagId;
    private String tagNameSnapshot;
    private ClassicsContentSource source;
    private ClassicsContentTagStatus status;
    private int priority;
}
