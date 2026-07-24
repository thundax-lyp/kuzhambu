package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.common.audit.annotation.AuditLog;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.common.core.id.Identifier;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.service.AuditApplicationService;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    static final String SERVICE_METHOD_POINTCUT = "execution(public * com.thundax.kuzhambu..service..*.*(..))";

    private final AuditApplicationService auditService;
    private final com.thundax.kuzhambu.common.audit.runtime.AuditObjectLoaderRegistry loaderRegistry;
    private final com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssemblerRegistry assemblerRegistry;

    public AuditLogAspect(
            AuditApplicationService auditService,
            com.thundax.kuzhambu.common.audit.runtime.AuditObjectLoaderRegistry loaderRegistry,
            com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssemblerRegistry assemblerRegistry) {
        this.auditService = auditService;
        this.loaderRegistry = loaderRegistry;
        this.assemblerRegistry = assemblerRegistry;
    }

    @Around(SERVICE_METHOD_POINTCUT)
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = mostSpecificMethod(joinPoint);
        AuditLog auditLog = method.getAnnotation(AuditLog.class);
        if (auditLog == null) {
            return joinPoint.proceed();
        }
        Object[] args = joinPoint.getArgs();
        if (!AuditExpressionEvaluator.booleanValue(auditLog.condition(), method, args, true)) {
            return joinPoint.proceed();
        }
        List<String> objectIds = objectIds(AuditExpressionEvaluator.objectValue(auditLog.id(), method, args));
        Map<String, AuditSnapshot> beforeSnapshots = beforeSnapshots(auditLog.type(), objectIds);
        Object result = joinPoint.proceed();
        if (objectIds.isEmpty() && result != null) {
            objectIds = objectIds(result);
        }
        for (String objectId : objectIds) {
            record(auditLog, objectId, beforeSnapshots.get(objectId), beforeSnapshots.containsKey(objectId));
        }
        return result;
    }

    private void record(AuditLog auditLog, String objectId, AuditSnapshot before, boolean beforeSnapshotCaptured) {
        AuditSnapshot beforeSnapshot = beforeSnapshotCaptured ? before : null;
        AuditSnapshot after = snapshot(auditLog.type(), objectId);

        CreateAuditLogCommand command = new CreateAuditLogCommand();
        command.setObjectType(auditLog.type());
        command.setObjectId(objectId);
        command.setAction(auditLog.action());
        command.setSummary(auditLog.summary());
        command.setBeforeSnapshot(beforeSnapshot);
        command.setAfterSnapshot(after);
        command.setRecordWhenUnchanged(auditLog.recordWhenUnchanged());
        command.setOperatorType(AuditOperatorResolver.operatorType());
        command.setOperatorId(AuditOperatorResolver.operatorId());
        command.setOperatorName(AuditOperatorResolver.operatorName());
        auditService.record(command);
    }

    private Method mostSpecificMethod(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass =
                joinPoint.getTarget() == null ? null : joinPoint.getTarget().getClass();
        return targetClass == null ? method : AopUtils.getMostSpecificMethod(method, targetClass);
    }

    private Map<String, AuditSnapshot> beforeSnapshots(String objectType, List<String> objectIds) {
        Map<String, AuditSnapshot> snapshots = new LinkedHashMap<>();
        for (String objectId : objectIds) {
            snapshots.put(objectId, snapshot(objectType, objectId));
        }
        return snapshots;
    }

    private List<String> objectIds(Object value) {
        List<String> objectIds = new ArrayList<>();
        collectObjectIds(value, objectIds);
        return objectIds;
    }

    private void collectObjectIds(Object value, List<String> objectIds) {
        if (value == null) {
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collectObjectIds(item, objectIds);
            }
            return;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                collectObjectIds(Array.get(value, i), objectIds);
            }
            return;
        }
        if (value instanceof Identifier<?> identifier) {
            objectIds.add(identifier.asString());
            return;
        }
        objectIds.add(String.valueOf(value));
    }

    private AuditSnapshot snapshot(String objectType, String objectId) {
        if (StringUtils.isBlank(objectId)) {
            return null;
        }
        com.thundax.kuzhambu.common.audit.runtime.AuditObjectLoader loader = loaderRegistry.get(objectType);
        com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssembler assembler = assemblerRegistry.get(objectType);
        if (loader == null || assembler == null) {
            return null;
        }
        return assembler.assemble(loader.load(objectId));
    }
}
