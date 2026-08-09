package com.thundax.kuzhambu.operations.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class OperationsApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private OperationsApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupDetailQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertAckCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.health.command.OperationsHealthAlertRecoverCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.health.query.OperationsHealthAlertPageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.health.query.OperationsHealthPageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.health.query.OperationsHealthTrendQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.report.command.OperationsReportGenerateCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.report.query.OperationsReportPageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.restore.command.OperationsRestoreExecuteCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.restore.query.OperationsRestoreDetailQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.restore.query.OperationsRestorePageQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.task.query.OperationsTaskDetailQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.operations.application.task.query.OperationsTaskPageQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}
