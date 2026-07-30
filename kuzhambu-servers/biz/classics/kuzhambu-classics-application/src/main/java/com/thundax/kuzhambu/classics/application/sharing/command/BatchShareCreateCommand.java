package com.thundax.kuzhambu.classics.application.sharing.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import java.time.Instant;
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
public class BatchShareCreateCommand {
    private String titlePrefix;
    private ClassicsShareVisibility visibility;
    private ClassicsShareLinkStatus status;
    private SancaiVisibilityRiskStatus visibilityRiskStatus;
    private Instant expiresAt;
    private boolean privateContentConfirmed;
    private List<ShareTargetCreateCommand> targets;
    private Long operatorUserId;
    private Set<String> operatorPermissions;

    public BatchShareCreateCommand(
            String titlePrefix,
            ClassicsShareVisibility visibility,
            ClassicsShareLinkStatus status,
            SancaiVisibilityRiskStatus visibilityRiskStatus,
            Instant expiresAt,
            boolean privateContentConfirmed,
            List<ShareTargetCreateCommand> targets) {
        this(
                titlePrefix,
                visibility,
                status,
                visibilityRiskStatus,
                expiresAt,
                privateContentConfirmed,
                targets,
                null,
                null);
    }
}
