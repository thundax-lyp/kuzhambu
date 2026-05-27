package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.ArrayList;
import java.util.List;

public final class TransactionArchitectureRuleSupport {

    private static final String TRANSACTIONAL_ANNOTATION = "org.springframework.transaction.annotation.Transactional";

    private TransactionArchitectureRuleSupport() {}

    public static void assertTransactionalOnlyOnApplicationServiceUseCases(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (javaClass.isAnnotatedWith(TRANSACTIONAL_ANNOTATION)
                    && !isApplicationServiceClass(javaClass, basePackage)) {
                violations.add(javaClass.getName());
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!method.isAnnotatedWith(TRANSACTIONAL_ANNOTATION)) {
                    continue;
                }
                if (!isApplicationServiceClass(javaClass, basePackage)
                        || !method.getModifiers().contains(JavaModifier.PUBLIC)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "@Transactional must stay on application service classes or public use-case methods: " + violations,
                violations.isEmpty());
    }

    private static boolean isApplicationServiceClass(JavaClass javaClass, String basePackage) {
        String packageName = javaClass.getPackageName();
        String simpleName = javaClass.getSimpleName();
        return packageName.startsWith(basePackage + ".application.")
                && packageName.contains(".service.impl")
                && (simpleName.endsWith("ApplicationServiceImpl") || simpleName.endsWith("ServiceImpl"));
    }
}
