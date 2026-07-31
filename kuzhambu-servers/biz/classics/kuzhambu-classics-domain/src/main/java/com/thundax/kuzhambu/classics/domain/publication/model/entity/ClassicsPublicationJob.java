package com.thundax.kuzhambu.classics.domain.publication.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationCleanupStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsPublicationJob {
    private ClassicsPublicationJobId id;
    private ClassicsPublicationJobType jobType;
    private ClassicsContentType contentType;
    private Long contentId;
    private String contentTitleSnapshot;
    private Instant contentDeletedAt;
    private ClassicsPublicationLifecycleStatus sourceLifecycleStatus;
    private ClassicsPublicationLifecycleStatus targetLifecycleStatus;
    private Long contentVersionId;
    private Integer contentVersionNo;
    private ClassicsPublicationJobStatus jobStatus;
    private ClassicsPublicationJobResultStatus jobResultStatus;
    private ClassicsPublicationExecutionToken executionToken;
    private Instant expiresAt;
    private Instant nextRetryAt;
    private Integer attemptCount;
    private Integer maxAttempts;
    private String esDocumentId;
    private String fastGptCollectionId;
    private String fastGptDataIdsJson;
    private ClassicsPublicationCleanupStatus esCleanupStatus;
    private String esCleanupToken;
    private Instant esCleanupExpiresAt;
    private ClassicsPublicationCleanupStatus fastGptCleanupStatus;
    private String fastGptCleanupToken;
    private Instant fastGptCleanupExpiresAt;
    private String detailJson;
    private Instant requestedAt;
    private Instant startedAt;
    private Instant finishedAt;
    private String failureReason;
}
