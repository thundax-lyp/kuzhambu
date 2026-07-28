package com.thundax.kuzhambu.classics.application.sharing.assembler;

import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareTargetCreateCommand;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;

public final class ClassicsSharingApplicationAssembler {

    private ClassicsSharingApplicationAssembler() {}

    public static ClassicsShareLink toLink(ShareLinkCreateCommand command, String shareToken, String tokenHash) {
        if (command == null) {
            return null;
        }
        return new ClassicsShareLink(
                null,
                shareToken,
                tokenHash,
                command.getTitle(),
                command.getVisibility(),
                command.getStatus(),
                command.getVisibilityRiskStatus(),
                command.getOperatorUserId(),
                command.getIssuedAt(),
                command.getExpiresAt(),
                0L);
    }

    public static ClassicsShareTarget toTarget(ShareTargetCreateCommand command) {
        if (command == null) {
            return null;
        }
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setContentType(command.getContentType());
        target.setContentId(command.getContentId());
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        return target;
    }
}
