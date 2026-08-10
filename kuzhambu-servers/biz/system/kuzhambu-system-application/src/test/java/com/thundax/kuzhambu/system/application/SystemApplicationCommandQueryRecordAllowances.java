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
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.AssignRoleUsersCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeRoleInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeRoleStatusCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeUserInfoCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.ChangeUserStatusCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateLogCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateRoleCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.CreateUserCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.DeleteLogCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveRoleCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RemoveUserCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.command.RoleSortCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetLogQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetRoleQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.GetUserQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.LogQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.RoleQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.system.application.core.query.UserQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
