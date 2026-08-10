package com.thundax.kuzhambu.system.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import com.thundax.kuzhambu.common.test.architecture.ImplContractArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.LayerArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.PathArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SortableArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SourceHardRuleArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.SpringBeanArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.TransactionArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class SystemApplicationArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.system";

    @Test
    void applicationLayerShouldKeepArchitectureBoundary() throws Exception {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".application");

        ModuleAndDependencyArchitectureRuleSupport.assertApplicationLayerBoundary(classes, BASE_PACKAGE);
        ModuleAndDependencyArchitectureRuleSupport.assertCrossDomainDependencyBoundary(classes, "system");
        AnnotationBoundaryArchitectureRuleSupport.assertApplicationNoHttpAnnotations(classes, BASE_PACKAGE);
        TransactionArchitectureRuleSupport.assertTransactionalOnlyOnApplicationServiceUseCases(classes, BASE_PACKAGE);
        LayerArchitectureRuleSupport.assertServiceBoundaryTypesClean(classes);
        LayerArchitectureRuleSupport.assertApplicationServiceBoundaryClean(
                classes, legacyApplicationServiceBoundaryAllowances());
        SpringBeanArchitectureRuleSupport.assertDirectSpringBeansHaveSingleConstructor(classes);
        ImplContractArchitectureRuleSupport.assertImplClassesImplementNamedInterface(classes, Collections.emptySet());
        ImplContractArchitectureRuleSupport.assertProductionCodeDoesNotDependOnImplTypes(
                classes, Collections.emptySet());
        NamingArchitectureRuleSupport.assertApplicationServicesUseApplicationServiceSuffix(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertConfigurationClassNames(classes);
        PathArchitectureRuleSupport.assertConfigurationClassPlacement(classes);
        SourceHardRuleArchitectureRuleSupport.assertConfigurationPropertiesDoNotDeclareBusinessControlFlow(
                Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertCodecPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesAreRecords(
                Path.of("src/main/java"), SystemApplicationCommandQueryRecordAllowances.legacyAllowances());
        NamingArchitectureRuleSupport.assertApplicationCommandQueryConstructionInAssemblersOrApplicationServices(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-system-interface/src/main/java")),
                legacyCommandQueryConstructionAllowances());
        NamingArchitectureRuleSupport.assertAssemblersDoNotReturnNullApplicationCommandOrQuery(
                List.of(Path.of("src/main/java"), Path.of("../kuzhambu-system-interface/src/main/java")),
                Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationQueriesDoNotOwnPageState(
                Path.of("src/main/java"), Collections.emptyList());
        NamingArchitectureRuleSupport.assertApplicationContractSourcesUnderDedicatedPackages(Path.of("src/main/java"));
        NamingArchitectureRuleSupport.assertBoundaryAssemblerPublicMethodsUseNonNullContracts(
                Collections.singletonList(Path.of("src/main/java")), Collections.emptyList());
        NamingArchitectureRuleSupport.assertEntityPlacement(classes, BASE_PACKAGE);
        NamingArchitectureRuleSupport.assertServiceQueryObjectsUnderServiceQueryPackage(classes);
        SortableArchitectureRuleSupport.assertSortCommandsUseOrderedIdsOnly(Path.of("src/main/java"));
    }

    private static List<ArchitectureRuleAllowance> legacyApplicationServiceBoundaryAllowances() {
        return Collections.emptyList();
    }

    private static List<ArchitectureRuleAllowance> legacyCommandQueryConstructionAllowances() {
        return List.of(
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.audit.controller.AuditController#PageQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.AdminAuthServiceImpl#PrincipalIdentityQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.PermissionServiceImpl#CreatePermissionsCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.PermissionServiceImpl#PermissionQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.auth.service.impl.PermissionServiceImpl#PermissionQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#ChangeCurrentUserAvatarCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#ChangeCurrentUserInfoCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#ChangeCurrentUserPasswordCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#CurrentUserAvatarQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#CurrentUserQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#PreAuthSessionQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#PreAuthSessionValueQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.CurrentUserController#RemoveCurrentUserAvatarCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DepartmentController#DepartmentQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DepartmentController#DepartmentQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DepartmentController#GetDepartmentQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DepartmentController#MoveDepartmentCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DepartmentController#RemoveDepartmentCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DictController#DictSortCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DictController#GetDictQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.DictController#RemoveDictCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.LogController#GetDepartmentQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.LogController#GetUserQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.LogController#PrincipalIdentityQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.MenuController#ChangeMenuVisibilityCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.MenuController#GetMenuQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.MenuController#MenuQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.MenuController#MenuQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.MenuController#MoveMenuCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.MenuController#RemoveMenuCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#AssignRoleUsersCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#ChangeRoleStatusCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#DepartmentQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#DictQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#DictQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#GetDepartmentQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#GetMenuQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#GetRoleQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#GetUserQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#GetUserQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#MenuQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#PrincipalIdentityQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#RemoveRoleCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#RoleQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#RoleQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#RoleSortCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#UserQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.RoleController#UserQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#ChangeCurrentUserAvatarCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#ChangeUserStatusCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#CurrentUserAvatarQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#CurrentUserAvatarQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#DepartmentQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#DictQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#DictQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#GetDepartmentQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#GetRoleQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#GetUserQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#PreAuthSessionQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#PreAuthSessionValueQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#RemoveCurrentUserAvatarCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#RemoveUserCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#RoleQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#UserQuery:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#UserQuery:2"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.controller.UserController#UserQuery:3"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.service.impl.SysLogMessageServiceImpl#CreateLogCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.service.impl.SysLogMessageServiceImpl#DeleteLogCommand:1"),
                constructionViolation(
                        "com.thundax.kuzhambu.system.interfaces.admin.core.service.impl.SysLogMessageServiceImpl#LogQuery:1"));
    }

    private static ArchitectureRuleAllowance constructionViolation(String ownerAndType) {
        return ArchitectureRuleAllowance.of(
                "COMMAND_QUERY_CONSTRUCTION:" + ownerAndType,
                "System controller or runtime support code directly constructs an application Command/Query.",
                "Move request conversion into an InterfaceAssembler or application facade, then remove this allowance.");
    }
}
