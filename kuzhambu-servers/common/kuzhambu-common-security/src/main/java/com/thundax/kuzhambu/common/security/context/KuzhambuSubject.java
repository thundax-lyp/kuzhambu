package com.thundax.kuzhambu.common.security.context;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class KuzhambuSubject implements Serializable {

    @Setter
    private String subjectId;

    private KuzhambuSubjectType subjectType = KuzhambuSubjectType.ANONYMOUS;

    @Setter
    private String displayName;

    @Setter
    private String token;

    private final Set<String> authorities = new LinkedHashSet<>();

    public KuzhambuSubject() {}

    public KuzhambuSubject(
            String subjectId,
            KuzhambuSubjectType subjectType,
            String displayName,
            String token,
            Collection<String> authorities) {
        this.subjectId = subjectId;
        this.subjectType = subjectType == null ? KuzhambuSubjectType.UNKNOWN : subjectType;
        this.displayName = displayName;
        this.token = token;
        setAuthorities(authorities);
    }

    public static KuzhambuSubject anonymous() {
        return new KuzhambuSubject();
    }

    public boolean isAuthenticated() {
        return subjectId != null && !subjectId.trim().isEmpty() && subjectType != KuzhambuSubjectType.ANONYMOUS;
    }

    public synchronized boolean hasAuthority(String authority) {
        return authorities.contains(authority);
    }

    public void setSubjectType(KuzhambuSubjectType subjectType) {
        this.subjectType = subjectType == null ? KuzhambuSubjectType.UNKNOWN : subjectType;
    }

    public synchronized Set<String> getAuthorities() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(authorities));
    }

    public synchronized void setAuthorities(Collection<String> authorities) {
        this.authorities.clear();
        if (authorities == null) {
            return;
        }
        for (String authority : authorities) {
            if (authority != null && !authority.trim().isEmpty()) {
                this.authorities.add(authority);
            }
        }
    }
}
