package com.thundax.kuzhambu.classics.application.publication.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;

public record ClassicsPublicationCreateCommand(
        ClassicsContentType contentType, ClassicsContentId contentId, ClassicsPublicationJobType jobType) {}
