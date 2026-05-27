package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserPasswordCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;
import java.io.InputStream;
import java.util.List;

public interface CurrentUserApplicationService {

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
