package com.thundax.kuzhambu.classics.application.publication.query;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;

public record ClassicsPublicationJobQuery(
        ClassicsPublicationJobType jobType,
        ClassicsPublicationJobResultStatus jobResultStatus,
        ClassicsPublicationJobStatus jobStatus,
        ClassicsContentType contentType,
        String keyword) {}
