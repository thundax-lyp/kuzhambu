package com.thundax.kuzhambu.system.application.audit.runtime;

import java.lang.reflect.Method;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class AuditExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

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
