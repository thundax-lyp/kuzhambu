package com.thundax.kuzhambu.common.security.context;

import com.thundax.kuzhambu.common.security.permission.PermissionAuthorities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public final class KuzhambuContextHolder {

    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    private KuzhambuContextHolder() {}

    public static String requestId() {
        return REQUEST_ID_HOLDER.get();
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    public static KuzhambuSubject currentSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return KuzhambuSubject.anonymous();
        }

        KuzhambuSubject subject = resolvePrincipal(authentication);
        subject.setAuthorities(PermissionAuthorities.toPermissions(authentication.getAuthorities()));
        if (subject.getToken() == null && authentication.getCredentials() instanceof String) {
            subject.setToken((String) authentication.getCredentials());
        }
        return subject;
    }

    public static String currentSubjectId() {
        return currentSubject().getSubjectId();
    }

    public static KuzhambuSubjectType currentSubjectType() {
        return currentSubject().getSubjectType();
    }

    public static String currentToken() {
        return currentSubject().getToken();
    }

    public static Set<String> currentAuthorities() {
        return currentSubject().getAuthorities();
    }

    public static boolean isAuthenticated() {
        return currentSubject().isAuthenticated();
    }

    public static void setSubject(KuzhambuSubject subject) {
        if (subject == null || !subject.isAuthenticated()) {
            clearSubject();
            return;
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        subject, subject.getToken(), toAuthorities(subject.getAuthorities())));
    }

    public static void clearSubject() {
        SecurityContextHolder.clearContext();
    }

    public static void clearRequestContext() {
        REQUEST_ID_HOLDER.remove();
    }

    public static void clear() {
        clearSubject();
        clearRequestContext();
    }

    private static KuzhambuSubject resolvePrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof KuzhambuSubject) {
            return (KuzhambuSubject) principal;
        }
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return new KuzhambuSubject(
                    userDetails.getUsername(), KuzhambuSubjectType.UNKNOWN, userDetails.getUsername(), null, null);
        }
        if (principal instanceof String) {
            return new KuzhambuSubject((String) principal, KuzhambuSubjectType.UNKNOWN, (String) principal, null, null);
        }
        return new KuzhambuSubject(
                authentication.getName(), KuzhambuSubjectType.UNKNOWN, authentication.getName(), null, null);
    }

    private static Collection<SimpleGrantedAuthority> toAuthorities(Collection<String> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        return grantedAuthorities;
    }
}
