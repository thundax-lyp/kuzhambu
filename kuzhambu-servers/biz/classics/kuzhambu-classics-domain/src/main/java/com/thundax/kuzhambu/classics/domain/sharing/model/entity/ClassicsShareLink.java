package com.thundax.kuzhambu.classics.domain.sharing.model.entity;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsShareLink {
    private ClassicsShareLinkId id;
    private String shareToken;
    private String tokenHash;
    private String title;
    private ClassicsShareVisibility visibility;
    private ClassicsShareLinkStatus status;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private Long createdByUserId;
    private Instant issuedAt;
    private Instant expiresAt;
    private long accessCount;
}
