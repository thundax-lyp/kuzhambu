package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.common.audit.model.valueobject.AuditField;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.common.core.id.Identifier;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditChangedField;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public final class AuditExpressionEvaluator {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    private AuditExpressionEvaluator() {}

    public static List<AuditChangedField> diff(AuditSnapshot beforeSnapshot, AuditSnapshot afterSnapshot) {
        List<AuditChangedField> changedFields = new ArrayList<>();
        Map<String, AuditField> beforeFields = toFieldMap(beforeSnapshot);
        Map<String, AuditField> afterFields = toFieldMap(afterSnapshot);
        for (Map.Entry<String, AuditField> entry : afterFields.entrySet()) {
            AuditField before = beforeFields.get(entry.getKey());
            AuditField after = entry.getValue();
            Object beforeValue = before == null ? null : before.getValue();
            if (!Objects.equals(beforeValue, after.getValue())) {
                changedFields.add(new AuditChangedField(
                        after.getFieldName(),
                        after.getFieldLabel(),
                        beforeValue,
                        before == null ? null : before.getDisplayValue(),
                        after.getValue(),
                        after.getDisplayValue()));
            }
        }
        for (Map.Entry<String, AuditField> entry : beforeFields.entrySet()) {
            if (afterFields.containsKey(entry.getKey())) {
                continue;
            }
            AuditField before = entry.getValue();
            changedFields.add(new AuditChangedField(
                    before.getFieldName(),
                    before.getFieldLabel(),
                    before.getValue(),
                    before.getDisplayValue(),
                    null,
                    null));
        }
        return changedFields;
    }

    public static String stringValue(String expression, Method method, Object[] args) {
        Object value = value(expression, method, args);
        return value == null ? null : String.valueOf(value);
    }

    public static Object objectValue(String expression, Method method, Object[] args) {
        return value(expression, method, args);
    }

    public static boolean booleanValue(String expression, Method method, Object[] args, boolean defaultValue) {
        if (expression == null || expression.trim().isEmpty()) {
            return defaultValue;
        }
        Object value = value(expression, method, args);
        return value == null ? defaultValue : Boolean.TRUE.equals(value);
    }

    private static Map<String, AuditField> toFieldMap(AuditSnapshot snapshot) {
        Map<String, AuditField> fieldMap = new HashMap<>();
        if (snapshot == null || snapshot.getFields() == null) {
            return fieldMap;
        }
        for (AuditField field : snapshot.getFields()) {
            fieldMap.put(field.getFieldName(), field);
        }
        return fieldMap;
    }

    private static Object value(String expression, Method method, Object[] args) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        bindStableArgumentAliases(context, args);
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        return PARSER.parseExpression(expression).getValue(context);
    }

    private static void bindStableArgumentAliases(StandardEvaluationContext context, Object[] args) {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
            context.setVariable("arg" + i, args[i]);
        }
        if (args.length == 1) {
            context.setVariable("command", args[0]);
            if (args[0] instanceof Identifier<?>) {
                context.setVariable("id", args[0]);
            }
            String variableName = simpleVariableName(args[0]);
            if (StringUtils.isNotBlank(variableName)) {
                context.setVariable(variableName, args[0]);
            }
        }
    }

    private static String simpleVariableName(Object value) {
        if (value == null) {
            return null;
        }
        String simpleName = value.getClass().getSimpleName();
        if (StringUtils.isBlank(simpleName)) {
            return null;
        }
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
