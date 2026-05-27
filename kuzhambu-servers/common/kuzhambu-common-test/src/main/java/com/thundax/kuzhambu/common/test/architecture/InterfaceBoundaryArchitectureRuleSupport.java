package com.thundax.kuzhambu.common.test.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;

public final class InterfaceBoundaryArchitectureRuleSupport {

    private InterfaceBoundaryArchitectureRuleSupport() {}

    public static void assertInterfaceNoPersistenceDependency(JavaClasses classes, String basePackage) {
        noClasses()
                .that()
                .resideInAPackage(basePackage + ".interfaces..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        basePackage + ".domain.repository..",
                        basePackage + ".infra.mapper..",
                        basePackage + ".infra.dataobject..",
                        basePackage + ".infra.repository.impl..")
                .check(classes);
    }
}
