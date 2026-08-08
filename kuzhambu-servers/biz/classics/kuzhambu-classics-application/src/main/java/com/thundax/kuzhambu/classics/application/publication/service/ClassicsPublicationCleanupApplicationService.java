package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import java.time.Instant;

public interface ClassicsPublicationCleanupApplicationService {

    boolean claimEs(ClassicsPublicationJob job, String token, Instant now, Instant expiresAt);

    boolean claimFastGpt(ClassicsPublicationJob job, String token, Instant now, Instant expiresAt);

    boolean qualify(ClassicsPublicationJob claimedJob, String token, boolean es);

    boolean complete(ClassicsPublicationJob job, String token, boolean es);

    boolean fail(ClassicsPublicationJob job, String token, boolean es, String detailJson);
}
