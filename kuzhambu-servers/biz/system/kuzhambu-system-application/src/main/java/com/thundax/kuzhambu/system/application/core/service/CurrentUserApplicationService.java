package com.thundax.kuzhambu.system.application.core.service;

import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserPasswordCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserAvatarQuery;
import com.thundax.kuzhambu.system.application.core.query.CurrentUserQuery;
import com.thundax.kuzhambu.system.application.core.result.UserAvatarResult;
import com.thundax.kuzhambu.system.domain.core.model.entity.Menu;
import com.thundax.kuzhambu.system.domain.core.model.entity.User;
import java.io.InputStream;
import java.util.List;

public interface CurrentUserApplicationService {

    User changeInfo(ChangeCurrentUserInfoCommand command);

    void changePassword(ChangeCurrentUserPasswordCommand command);

    UserAvatarResult changeAvatar(ChangeCurrentUserAvatarCommand command);

    void removeAvatar(RemoveCurrentUserAvatarCommand command);

    UserAvatarResult getAvatar(CurrentUserAvatarQuery query);

    InputStream getAvatarInputStream(CurrentUserAvatarQuery query);

    boolean existsAvatar(CurrentUserAvatarQuery query);

    List<Menu> listAccessibleMenus(CurrentUserQuery query);

    List<Menu> listVisibleMenus(CurrentUserQuery query);
}
