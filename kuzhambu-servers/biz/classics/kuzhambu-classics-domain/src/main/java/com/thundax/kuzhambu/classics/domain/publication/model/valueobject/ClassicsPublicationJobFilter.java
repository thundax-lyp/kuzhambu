package com.thundax.kuzhambu.classics.domain.publication.model.valueobject;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;

public record ClassicsPublicationJobFilter(
        ClassicsPublicationJobType jobType,
        ClassicsPublicationJobResultStatus resultStatus,
        ClassicsContentType contentType,
        String keyword) {}
