package com.thundax.kuzhambu.classics.application.sharing.result;

import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SharePortalResult {
    private String title;
    private ClassicsShareVisibility visibility;
    private ClassicsShareLinkStatus status;
    private Date issuedAt;
    private Date expiresAt;
    private List<ClassicsShareTarget> targets;
}
