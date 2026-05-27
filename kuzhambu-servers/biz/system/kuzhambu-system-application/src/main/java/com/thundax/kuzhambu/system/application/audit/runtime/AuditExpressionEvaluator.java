package com.thundax.kuzhambu.system.application.audit.runtime;

import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditField;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditSnapshot;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class AuditExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

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
        return changedFields;
    }

    public String stringValue(String expression, Method method, Object[] args) {
        Object value = value(expression, method, args);
        return value == null ? null : String.valueOf(value);
    }

    public boolean booleanValue(String expression, Method method, Object[] args, boolean defaultValue) {
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

    private Object value(String expression, Method method, Object[] args) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        return parser.parseExpression(expression).getValue(context);
    }
}
