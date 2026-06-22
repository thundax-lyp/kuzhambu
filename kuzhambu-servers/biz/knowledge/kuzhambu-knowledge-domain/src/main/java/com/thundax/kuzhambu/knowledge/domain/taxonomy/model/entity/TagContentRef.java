package com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity;

import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagContentRefId;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagContentRef {
    private TagContentRefId id;
    private TagContentRefId refId;
    private TagId tagId;
    private ContentType contentType;
    private Long contentId;
    private String contentTitle;
    private TagSource source;
}
