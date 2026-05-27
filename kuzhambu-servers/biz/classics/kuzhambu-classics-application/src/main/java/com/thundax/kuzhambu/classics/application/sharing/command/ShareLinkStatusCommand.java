package com.thundax.kuzhambu.classics.application.sharing.command;

import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkStatusCommand {
    private Long id;
    private ClassicsShareLinkStatus status;
}
