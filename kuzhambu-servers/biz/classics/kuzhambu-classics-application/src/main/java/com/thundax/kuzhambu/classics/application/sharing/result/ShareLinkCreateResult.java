package com.thundax.kuzhambu.classics.application.sharing.result;

import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShareLinkCreateResult {
    private ClassicsShareLinkId id;
    private String shareToken;
    private String shareUrl;
    private String title;
    private ClassicsShareVisibility visibility;
    private ClassicsShareLinkStatus status;
    private Date expiresAt;
    private List<ClassicsShareTarget> targets;
}
