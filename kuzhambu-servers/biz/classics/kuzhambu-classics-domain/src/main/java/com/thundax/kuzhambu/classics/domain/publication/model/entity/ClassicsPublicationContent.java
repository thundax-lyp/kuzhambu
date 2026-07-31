package com.thundax.kuzhambu.classics.domain.publication.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsPublicationContent {
    private ClassicsContentType contentType;
    private ClassicsContentId contentId;
    private String contentTitle;
    private ClassicsPublicationLifecycleStatus lifecycleStatus;
    private ClassicsPublicationTransitionStatus transitionStatus;
    private ClassicsPublicationJobId currentJobId;
}
