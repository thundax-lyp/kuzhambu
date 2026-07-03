package com.thundax.kuzhambu.classics.application.sharing.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import java.util.Date;
import java.util.List;
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
    private Date expiresAt;
    private boolean privateContentConfirmed;
    private List<ShareTargetCreateCommand> targets;
}
