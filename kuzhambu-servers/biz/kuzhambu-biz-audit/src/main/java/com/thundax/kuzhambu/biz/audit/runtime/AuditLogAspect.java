package com.thundax.kuzhambu.biz.audit.runtime;

import com.thundax.kuzhambu.biz.audit.annotation.AuditLog;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.biz.audit.service.AuditService;
import com.thundax.kuzhambu.biz.audit.service.command.CreateAuditLogCommand;
import java.lang.reflect.Method;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    static final String SERVICE_METHOD_POINTCUT = "execution(public * com.github.thundax.modules..service..*.*(..))";

    private final AuditService auditService;
    private final AuditExpressionEvaluator expressionEvaluator;
    private final AuditObjectLoaderRegistry loaderRegistry;
    private final AuditSnapshotAssemblerRegistry assemblerRegistry;
    private final AuditOperatorResolver operatorResolver;

    public AuditLogAspect(
            AuditService auditService,
            AuditExpressionEvaluator expressionEvaluator,
            AuditObjectLoaderRegistry loaderRegistry,
            AuditSnapshotAssemblerRegistry assemblerRegistry,
            AuditOperatorResolver operatorResolver) {
        this.auditService = auditService;
        this.expressionEvaluator = expressionEvaluator;
        this.loaderRegistry = loaderRegistry;
        this.assemblerRegistry = assemblerRegistry;
        this.operatorResolver = operatorResolver;
    }

    @Around(SERVICE_METHOD_POINTCUT)
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = mostSpecificMethod(joinPoint);
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        if (auditLog == null) {
            return joinPoint.proceed();
        }
        Object[] args = joinPoint.getArgs();
        if (!expressionEvaluator.booleanValue(auditLog.condition(), method, args, true)) {
            return joinPoint.proceed();
        }
        String objectId = expressionEvaluator.stringValue(auditLog.id(), method, args);
        AuditSnapshot before = snapshot(auditLog.type(), objectId);
        Object result = joinPoint.proceed();
        if (objectId == null && result != null) {
            objectId = String.valueOf(result);
        }
        AuditSnapshot after = snapshot(auditLog.type(), objectId);

        CreateAuditLogCommand command = new CreateAuditLogCommand();
        command.setObjectType(auditLog.type());
        command.setObjectId(objectId);
        command.setAction(auditLog.action());
        command.setSummary(auditLog.summary());
        command.setBeforeSnapshot(before);
        command.setAfterSnapshot(after);
        command.setRecordWhenUnchanged(auditLog.recordWhenUnchanged());
        command.setOperatorType(operatorResolver.operatorType());
        command.setOperatorId(operatorResolver.operatorId());
        command.setOperatorName(operatorResolver.operatorName());
        auditService.record(command);
        return result;
    }

    private Method mostSpecificMethod(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass =
                joinPoint.getTarget() == null ? null : joinPoint.getTarget().getClass();
        return targetClass == null ? method : AopUtils.getMostSpecificMethod(method, targetClass);
    }

    private AuditSnapshot snapshot(String objectType, String objectId) {
        if (StringUtils.isBlank(objectId)) {
            return null;
        }
        AuditObjectLoader loader = loaderRegistry.get(objectType);
        AuditSnapshotAssembler assembler = assemblerRegistry.get(objectType);
        if (loader == null || assembler == null) {
            return null;
        }
        return assembler.assemble(loader.load(objectId));
    }
}
