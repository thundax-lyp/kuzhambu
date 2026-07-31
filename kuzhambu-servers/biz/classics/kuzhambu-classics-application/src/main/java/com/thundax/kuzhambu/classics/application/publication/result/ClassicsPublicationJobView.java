package com.thundax.kuzhambu.classics.application.publication.result;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;

public record ClassicsPublicationJobView(ClassicsPublicationJob job, ClassicsPublicationJobStatus failureStep) {}
