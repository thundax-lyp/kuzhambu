package com.thundax.kuzhambu.system.application.core.service.command;

import com.thundax.kuzhambu.system.domain.core.valueobject.UserId;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCurrentUserAvatarCommand {
    private UserId userId;
    private InputStream inputStream;
    private String originalFilename;
}
