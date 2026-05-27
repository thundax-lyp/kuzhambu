package com.thundax.kuzhambu.common.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;

public final class ModuleAndDependencyArchitectureRuleSupport {

    private ModuleAndDependencyArchitectureRuleSupport() {}

    public static void assertApplicationLayerBoundary(JavaClasses classes, String basePackage) {
        noClasses()
                .that()
                .resideInAPackage(basePackage + ".application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        basePackage + ".interfaces..", basePackage + ".infra..", "com.thundax.kuzhambu.starter..")
                .check(classes);
    }

    public static void assertInterfaceLayerBoundary(JavaClasses classes, String basePackage) {
        noClasses()
                .that()
                .resideInAPackage(basePackage + ".interfaces..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(basePackage + ".infra..")
                .check(classes);
    }

    public static void assertInfraLayerBoundary(JavaClasses classes, String basePackage) {
        noClasses()
                .that()
                .resideInAPackage(basePackage + ".infra..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(basePackage + ".interfaces..", "com.thundax.kuzhambu.starter..")
                .check(classes);
    }
}
