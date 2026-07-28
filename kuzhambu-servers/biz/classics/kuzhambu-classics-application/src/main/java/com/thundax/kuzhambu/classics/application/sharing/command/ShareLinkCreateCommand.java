package com.thundax.kuzhambu.classics.application.sharing.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkCreateCommand {
    private String title;
    private ClassicsShareVisibility visibility;
    private ClassicsShareLinkStatus status;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private Date issuedAt;
    private Date expiresAt;
    private List<ShareTargetCreateCommand> targets;
    private Long operatorUserId;
    private Set<String> operatorPermissions;

    public ShareLinkCreateCommand(
            String title,
            ClassicsShareVisibility visibility,
            ClassicsShareLinkStatus status,
            SancaiVisibilityRiskStatus visibilityRiskStatus,
            Date issuedAt,
            Date expiresAt,
            List<ShareTargetCreateCommand> targets) {
        this(title, visibility, status, visibilityRiskStatus, issuedAt, expiresAt, targets, null, null);
    }
}
