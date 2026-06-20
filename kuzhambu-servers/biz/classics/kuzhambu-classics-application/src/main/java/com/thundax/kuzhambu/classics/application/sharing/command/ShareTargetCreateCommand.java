package com.thundax.kuzhambu.classics.application.sharing.command;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareTargetCreateCommand {
    private ClassicsContentType contentType;
    private ClassicsContentId contentId;

    public ClassicsShareTarget toTarget() {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setContentType(contentType);
        target.setContentId(contentId);
        return target;
    }
}
