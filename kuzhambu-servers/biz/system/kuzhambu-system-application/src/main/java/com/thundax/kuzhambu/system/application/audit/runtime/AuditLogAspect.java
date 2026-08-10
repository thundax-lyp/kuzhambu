package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.common.audit.annotation.AuditLog;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.common.core.id.Identifier;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.service.AuditTrailApplicationService;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
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

    private static final AuditLogCommandFactory COMMAND_FACTORY = CreateAuditLogCommand::new;

    private final AuditTrailApplicationService auditService;
    private final com.thundax.kuzhambu.common.audit.runtime.AuditObjectLoaderRegistry loaderRegistry;
    private final com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssemblerRegistry assemblerRegistry;

    public AuditLogAspect(
            AuditTrailApplicationService auditService,
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
        Map<String, AuditSnapshot> beforeSnapshots = StringUtils.isBlank(auditLog.before())
                ? snapshots(auditLog.type(), objectIds)
                : snapshots(
                        auditLog.type(),
                        objectIds,
                        AuditExpressionEvaluator.objectValue(auditLog.before(), method, args));
        Object result = joinPoint.proceed();
        if (objectIds.isEmpty() && result != null) {
            objectIds = objectIds(result);
        }
        Map<String, AuditSnapshot> afterSnapshots = StringUtils.isBlank(auditLog.after())
                ? null
                : snapshots(
                        auditLog.type(),
                        objectIds,
                        AuditExpressionEvaluator.objectValue(auditLog.after(), method, args));
        for (String objectId : objectIds) {
            record(
                    auditLog,
                    objectId,
                    beforeSnapshots.get(objectId),
                    beforeSnapshots.containsKey(objectId),
                    afterSnapshots == null ? null : afterSnapshots.get(objectId),
                    afterSnapshots != null && afterSnapshots.containsKey(objectId));
        }
        return result;
    }

    private void record(
            AuditLog auditLog,
            String objectId,
            AuditSnapshot before,
            boolean beforeSnapshotCaptured,
            AuditSnapshot after,
            boolean afterSnapshotCaptured) {
        AuditSnapshot beforeSnapshot = beforeSnapshotCaptured ? before : null;
        AuditSnapshot afterSnapshot = afterSnapshotCaptured ? after : snapshot(auditLog.type(), objectId);

        CreateAuditLogCommand command = COMMAND_FACTORY.create(
                AuditObjectRef.of(auditLog.type(), objectId),
                auditLog.action(),
                null,
                AuditOperatorRef.of(AuditOperatorResolver.operatorType(), AuditOperatorResolver.operatorId()),
                AuditOperatorResolver.operatorName(),
                null,
                null,
                null,
                null,
                auditLog.summary(),
                beforeSnapshot,
                afterSnapshot,
                auditLog.recordWhenUnchanged());
        auditService.record(command);
    }

    private Method mostSpecificMethod(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass =
                joinPoint.getTarget() == null ? null : joinPoint.getTarget().getClass();
        return targetClass == null ? method : AopUtils.getMostSpecificMethod(method, targetClass);
    }

    private Map<String, AuditSnapshot> snapshots(String objectType, List<String> objectIds) {
        Map<String, AuditSnapshot> snapshots = new LinkedHashMap<>();
        for (String objectId : objectIds) {
            snapshots.put(objectId, snapshot(objectType, objectId));
        }
        return snapshots;
    }

    private Map<String, AuditSnapshot> snapshots(String objectType, List<String> objectIds, Object value) {
        Map<String, AuditSnapshot> snapshots = new LinkedHashMap<>();
        collectSnapshots(objectType, value, snapshots);
        if (snapshots.isEmpty() && value != null && objectIds.size() == 1) {
            snapshots.put(objectIds.get(0), assembleSnapshot(objectType, value));
        }
        return snapshots;
    }

    private void collectSnapshots(String objectType, Object value, Map<String, AuditSnapshot> snapshots) {
        if (value == null) {
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collectSnapshots(objectType, item, snapshots);
            }
            return;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                collectSnapshots(objectType, Array.get(value, i), snapshots);
            }
            return;
        }
        AuditSnapshot snapshot = assembleSnapshot(objectType, value);
        if (snapshot != null && StringUtils.isNotBlank(snapshot.getObjectId())) {
            snapshots.put(snapshot.getObjectId(), snapshot);
        }
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

    private AuditSnapshot assembleSnapshot(String objectType, Object value) {
        if (value instanceof AuditSnapshot snapshot) {
            return snapshot;
        }
        com.thundax.kuzhambu.common.audit.runtime.AuditSnapshotAssembler assembler = assemblerRegistry.get(objectType);
        return assembler == null ? null : assembler.assemble(value);
    }

    @FunctionalInterface
    private interface AuditLogCommandFactory {

        CreateAuditLogCommand create(
                AuditObjectRef objectRef,
                com.thundax.kuzhambu.common.audit.model.enums.AuditAction action,
                String idempotencyKey,
                AuditOperatorRef operatorRef,
                String operatorName,
                String source,
                String requestId,
                String traceId,
                String remoteAddr,
                String summary,
                AuditSnapshot beforeSnapshot,
                AuditSnapshot afterSnapshot,
                boolean recordWhenUnchanged);
    }
}
