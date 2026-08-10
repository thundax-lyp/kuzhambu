package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.io.InputStream;

public record ChangeCurrentUserAvatarCommand(UserId userId, InputStream inputStream, String originalFilename) {}
