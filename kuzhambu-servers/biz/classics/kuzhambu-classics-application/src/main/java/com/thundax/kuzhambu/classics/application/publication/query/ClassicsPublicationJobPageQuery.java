package com.thundax.kuzhambu.classics.application.publication.query;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;

public record ClassicsPublicationJobPageQuery(
        ClassicsPublicationJobType jobType,
        ClassicsPublicationJobResultStatus resultStatus,
        ClassicsContentType contentType,
        String keyword) {}
