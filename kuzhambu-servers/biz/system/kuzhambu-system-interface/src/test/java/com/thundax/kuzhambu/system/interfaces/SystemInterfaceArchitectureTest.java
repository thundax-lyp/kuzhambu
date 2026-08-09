package com.thundax.kuzhambu.system.interfaces;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.ApiAnnotationArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.BoundaryAssemblerNullnessAllowances;
import com.thundax.kuzhambu.common.test.architecture.InterfaceBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.thundax.kuzhambu.system.interfaces.admin.auth.configure.SpringSecurityConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

class SystemInterfaceArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.system";

    @Test
    void interfaceLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".interfaces");

        ModuleAndDependencyArchitectureRuleSupport.assertInterfaceLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "system");
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceNoPersistenceDependency(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceOnlyCallsApplicationServices(classes, BASE_PACKAGE);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolModelsStayInSameSubdomain(
                Path.of("src/main/java"));
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceProtocolsDoNotExposeDomainModels(
                Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ApiAnnotationArchitectureRuleSupport.assertAdminControllersDeclareRequiredClassAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertAdminControllerMethodsDeclareRequiredAnnotations(
                Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsUseRequestResponseShape(Path.of("src/main/java"));
        ApiAnnotationArchitectureRuleSupport.assertPostMappingMethodsDoNotUsePathOrQueryParameters(
                Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")),
                BoundaryAssemblerNullnessAllowances.legacyClasses(
                        "com.thundax.kuzhambu.system.interfaces.admin.audit.assembler.AuditInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.auth.assembler.AuthInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.core.assembler.DepartmentInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.core.assembler.DictInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.core.assembler.LogInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.core.assembler.MenuInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.core.assembler.PersonalInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.core.assembler.RoleInterfaceAssembler",
                        "com.thundax.kuzhambu.system.interfaces.admin.core.assembler.UserInterfaceAssembler"));
    }

    @Test
    void securityConfigurationShouldNotBypassSecurityFilterChain() {
        boolean hasWebSecurityCustomizer = Arrays.stream(SpringSecurityConfiguration.class.getDeclaredMethods())
                .anyMatch(method -> WebSecurityCustomizer.class.equals(method.getReturnType()));

        org.junit.jupiter.api.Assertions.assertFalse(
                hasWebSecurityCustomizer, "Public API paths must use permitAll instead of web.ignoring().");
    }
}
