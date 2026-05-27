package com.thundax.kuzhambu.system.interfaces.admin.auth.security;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.system.application.core.entity.User;
import com.thundax.kuzhambu.system.application.core.entity.valueobject.UserIdCodec;
import com.thundax.kuzhambu.system.application.core.service.UserService;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final UserService userService;

    public CurrentUserResolver(UserService userService) {
        this.userService = userService;
    }

    public User currentUser() {
        String subjectId = currentUserId();
        if (StringUtils.isBlank(subjectId)) {
            return new User();
        }
        try {
            User user = userService.get(UserIdCodec.toDomain(Long.valueOf(subjectId)));
            return user == null ? new User() : user;
        } catch (NumberFormatException e) {
            return new User();
        }
    }

    public User requireCurrentUser() {
        User user = currentUser();
        if (user.getId() == null || !user.isEnable()) {
            throw AdminResponseExceptions.invalidToken();
        }
        return user;
    }

    public String currentUserId() {
        KuzhambuSubject subject = KuzhambuContextHolder.currentSubject();
        return subject.getSubjectType() == KuzhambuSubjectType.ADMIN_USER ? subject.getSubjectId() : null;
    }

    public String currentToken() {
        KuzhambuSubject subject = KuzhambuContextHolder.currentSubject();
        return subject.getSubjectType() == KuzhambuSubjectType.ADMIN_USER ? subject.getToken() : null;
    }

    public Set<String> currentAuthorities() {
        return KuzhambuContextHolder.currentAuthorities();
    }
}
