package com.thundax.kuzhambu.classics.domain.publication.model.valueobject;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;

public record ClassicsPublicationContentState(
        ClassicsContentType contentType,
        ClassicsContentId contentId,
        String contentTitle,
        ClassicsPublicationLifecycleStatus lifecycleStatus,
        ClassicsPublicationTransitionStatus transitionStatus,
        ClassicsPublicationJobId currentJobId) {}
