package com.thundax.kuzhambu.system.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class SystemApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private SystemApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.query.AuditLogQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.query.AuditMetaQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.audit.query.GetAuditLogQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.AuthenticateIdentityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.AuthenticatePasswordCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalCredentialVerifyStateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ChangePrincipalIdentityStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreateAdminAccessTokenCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePermissionsCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePreAuthSessionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalCredentialCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.CreatePrincipalIdentityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.DeleteAdminAccessTokenCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.InvalidateAdminSessionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.PrincipalCredentialCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.PrincipalIdentityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalCredentialFailureCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RecordPrincipalLoginFailureCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RefreshAdminAccessTokenCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.RefreshPreAuthSessionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.ReleasePreAuthSessionCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.command.UpsertPreAuthSessionValueCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.AdminAccessTokenQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PermissionQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PreAuthSessionValueValidateQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PrincipalCredentialQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.auth.query.PrincipalIdentityQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.AssignRoleUsersCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserAvatarCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeCurrentUserPasswordCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeDepartmentInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeDictInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeMenuInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeMenuVisibilityCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeRoleInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeRoleStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeUserInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeUserStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateDepartmentCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateDictCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateLogCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateMenuCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateRoleCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateUserCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.DictSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.MoveDepartmentCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.MoveMenuCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveCurrentUserAvatarCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveDepartmentCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveDictCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveMenuCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveRoleCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveUserCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RoleSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.CurrentUserAvatarQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.CurrentUserQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.DepartmentQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.DictQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetDepartmentQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetDictQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetLogQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetMenuQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetRoleQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetUserQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.LogQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.MenuQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.RoleQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.UserQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
