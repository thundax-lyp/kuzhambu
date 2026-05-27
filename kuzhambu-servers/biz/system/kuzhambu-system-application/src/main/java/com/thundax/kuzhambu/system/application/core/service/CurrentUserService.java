package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.storage.application.entity.StoredObject;
import com.thundax.kuzhambu.system.application.core.entity.Menu;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeCurrentUserPasswordCommand;
import com.thundax.kuzhambu.system.application.core.service.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.service.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.domain.model.valueobject.UserId;
import java.io.InputStream;
import java.util.List;

public interface CurrentUserService {

    User changeInfo(ChangeCurrentUserInfoCommand command);

    void changePassword(ChangeCurrentUserPasswordCommand command);

    StoredObject changeAvatar(ChangeCurrentUserAvatarCommand command);

    void removeAvatar(RemoveCurrentUserAvatarCommand command);

    StoredObject getAvatar(UserId userId);

    InputStream getAvatarInputStream(UserId userId);

    boolean existsAvatar(UserId userId);

    List<Menu> listAccessibleMenus(CurrentUserQuery query);

    List<Menu> listVisibleMenus(CurrentUserQuery query);
}
