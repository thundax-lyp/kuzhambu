package com.thundax.kuzhambu.classics.domain.content.model;

import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import java.time.Instant;

public interface Versionable {

    ClassicsContentType contentType();

    ClassicsContentId contentId();

    ClassicsContentVersionId currentVersionId();

    Integer currentVersionNo();

    Instant currentVersionedAt();

    Instant contentUpdatedAt();

    void markVersioned(ClassicsContentVersion version);
}
