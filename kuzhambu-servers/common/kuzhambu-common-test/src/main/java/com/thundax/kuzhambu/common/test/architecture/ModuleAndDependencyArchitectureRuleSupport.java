package com.thundax.kuzhambu.common.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;

public final class ModuleAndDependencyArchitectureRuleSupport {

    private static final String[] DOMAINS = {
        "ai", "classics", "discovery", "knowledge", "operations", "storage", "system"
    };

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

    public static void assertPersistenceMappersOnlyCalledByRepositoryImpl(JavaClasses classes, String basePackage) {
        noClasses()
                .that()
                .resideOutsideOfPackage(basePackage + ".infra..repository.impl..")
                .and()
                .resideOutsideOfPackage(basePackage + ".infra..persistence.mapper..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(basePackage + ".infra..persistence.mapper..")
                .check(classes);
    }

    public static void assertCrossDomainDependencyBoundary(JavaClasses classes, String currentDomain) {
        for (String domain : DOMAINS) {
            if (domain.equals(currentDomain)) {
                continue;
            }
            noClasses()
                    .that()
                    .resideInAPackage("com.thundax.kuzhambu." + currentDomain + "..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.thundax.kuzhambu." + domain + ".infra..",
                            "com.thundax.kuzhambu." + domain + ".domain..repository..")
                    .check(classes);
        }
    }
}
