package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubject;
import com.thundax.kuzhambu.common.security.context.KuzhambuSubjectType;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditOperatorType;
import org.springframework.stereotype.Component;

@Component
public class AuditOperatorResolver {

    public AuditOperatorType operatorType() {
        KuzhambuSubjectType subjectType = KuzhambuContextHolder.currentSubjectType();
        if (subjectType == null) {
            return AuditOperatorType.UNKNOWN;
        }
        switch (subjectType) {
            case ADMIN_USER:
                return AuditOperatorType.USER;
            case FRONT_MEMBER:
                return AuditOperatorType.MEMBER;
            case OPEN_CLIENT:
                return AuditOperatorType.OPEN_CLIENT;
            case SYSTEM:
                return AuditOperatorType.SYSTEM;
            case UNKNOWN:
            case ANONYMOUS:
            default:
                return AuditOperatorType.UNKNOWN;
        }
    }

    public String operatorId() {
        return KuzhambuContextHolder.currentSubjectId();
    }

    public String operatorName() {
        KuzhambuSubject subject = KuzhambuContextHolder.currentSubject();
        return subject == null ? null : subject.getDisplayName();
    }
}
