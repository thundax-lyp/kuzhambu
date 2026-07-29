package com.thundax.kuzhambu.system.application.core.query;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.UserId;

public class CurrentUserAvatarQuery {

    private UserId userId;

    public CurrentUserAvatarQuery() {}

    public CurrentUserAvatarQuery(UserId userId) {
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }
}
