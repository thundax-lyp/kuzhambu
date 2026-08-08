package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ImplContractArchitectureRuleSupport {

    private ImplContractArchitectureRuleSupport() {}

    public static void assertImplClassesImplementNamedInterface(
            JavaClasses classes, Collection<String> allowedImplClasses) {
        Set<String> allowlist = new LinkedHashSet<String>(allowedImplClasses);
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!isImplClass(javaClass) || allowlist.contains(javaClass.getName())) {
                continue;
            }
            String expectedInterface = expectedInterfaceSimpleName(javaClass);
            if (!implementsInterfaceNamed(javaClass, expectedInterface)) {
                violations.add(javaClass.getName() + " must implement " + expectedInterface);
            }
        }

        assertTrue("XxxImpl production classes must implement Xxx: " + violations, violations.isEmpty());
    }

    public static void assertProductionCodeDoesNotDependOnImplTypes(
            JavaClasses classes, Collection<String> allowedDependencies) {
        Set<String> allowlist = new LinkedHashSet<String>(allowedDependencies);
        Set<String> violations = new LinkedHashSet<String>();

        for (JavaClass javaClass : classes) {
            if (isTestType(javaClass)) {
                continue;
            }
            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                JavaClass targetClass = dependency.getTargetClass();
                if (!isImplTypeReference(targetClass)
                        || javaClass.equals(targetClass)
                        || isOwnNestedType(javaClass, targetClass)) {
                    continue;
                }
                String dependencyName = dependencyName(javaClass, targetClass);
                if (!allowlist.contains(dependencyName)) {
                    violations.add(dependencyName);
                }
            }
        }

        assertTrue(
                "Production code must depend on Xxx interfaces instead of XxxImpl types: " + violations,
                violations.isEmpty());
    }

    public static String dependency(String originClass, String targetClass) {
        return originClass + " -> " + targetClass;
    }

    private static boolean isImplClass(JavaClass javaClass) {
        return !javaClass.isInterface()
                && !isTestType(javaClass)
                && !isNestedType(javaClass)
                && javaClass.getSimpleName().endsWith("Impl");
    }

    private static boolean isImplTypeReference(JavaClass javaClass) {
        return !isTestType(javaClass) && javaClass.getSimpleName().endsWith("Impl");
    }

    private static boolean isTestType(JavaClass javaClass) {
        String className = javaClass.getName();
        return javaClass.getPackageName().contains(".test.")
                || javaClass.getSimpleName().endsWith("Test")
                || javaClass.getSimpleName().endsWith("IT")
                || javaClass.getSimpleName().endsWith("IntegrationTest")
                || className.contains("Test$")
                || className.contains("IT$")
                || className.contains("IntegrationTest$");
    }

    private static boolean isNestedType(JavaClass javaClass) {
        return javaClass.getName().contains("$");
    }

    private static boolean isOwnNestedType(JavaClass originClass, JavaClass targetClass) {
        return targetClass.getName().startsWith(originClass.getName() + "$");
    }

    private static String expectedInterfaceSimpleName(JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        return simpleName.substring(0, simpleName.length() - "Impl".length());
    }

    private static boolean implementsInterfaceNamed(JavaClass javaClass, String expectedInterface) {
        for (JavaClass interfaceClass : javaClass.getAllRawInterfaces()) {
            if (expectedInterface.equals(interfaceClass.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    private static String dependencyName(JavaClass originClass, JavaClass targetClass) {
        return dependency(originClass.getName(), targetClass.getName());
    }
}
