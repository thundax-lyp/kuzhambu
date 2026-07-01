import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import type { ReactNode } from "react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { BackupRestorePage } from "./backup-restore-page";
import * as service from "./backup-restore-service";

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({ danger: vi.fn() })
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        open,
        title
    }: {
        children: ReactNode;
        open?: boolean;
        title?: ReactNode;
    }) =>
        open ? (
            <div>
                {title}
                {children}
            </div>
        ) : null;

    return {
        KuzhambuDrawer: mockDrawer
    };
});

vi.mock("antd", async () => {
    const actual = await vi.importActual<typeof import("antd")>("antd");
    const Table = ({
        columns,
        dataSource
    }: {
        columns: Array<Record<string, unknown>>;
        dataSource?: Array<Record<string, unknown>>;
    }) => (
        <table>
            <tbody>
                {(dataSource || []).map((record, rowIndex) => (
                    <tr key={String(record.backupId || record.restoreId || rowIndex)}>
                        {columns.map((column, columnIndex) => {
                            const dataIndex = column.dataIndex as string | undefined;
                            const value = dataIndex ? record[dataIndex] : undefined;
                            const rendered =
                                typeof column.render === "function"
                                    ? column.render(value, record, rowIndex)
                                    : value;
                            return (
                                <td key={String(column.key || dataIndex || columnIndex)}>
                                    {rendered as ReactNode}
                                </td>
                            );
                        })}
                    </tr>
                ))}
            </tbody>
        </table>
    );
    const Statistic = ({ title, value }: { title: ReactNode; value: ReactNode }) => (
        <div>
            <div>{title}</div>
            <div>{value}</div>
        </div>
    );
    const Descriptions = ({
        items
    }: {
        items?: Array<{ label: ReactNode; children: ReactNode }>;
    }) => (
        <dl>
            {(items || []).map((item) => (
                <div key={String(item.label)}>
                    <dt>{item.label}</dt>
                    <dd>{item.children}</dd>
                </div>
            ))}
        </dl>
    );
    return {
        ...actual,
        App: {
            ...actual.App,
            useApp: () => ({
                message: { success: vi.fn(), error: vi.fn() },
                notification: {} as never,
                modal: {} as never
            })
        },
        Descriptions,
        Statistic,
        Table
    };
});

vi.mock("./backup-restore-service", () => ({
    createManualBackup: vi.fn(),
    pageBackups: vi.fn(),
    getBackupDetail: vi.fn(),
    recoverBackup: vi.fn(),
    pageRestores: vi.fn(),
    getRestoreDetail: vi.fn()
}));

describe("BackupRestorePage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions([
            "operations:backup:view",
            "operations:backup:execute",
            "operations:restore:view",
            "operations:restore:execute"
        ]);
        vi.mocked(service.pageBackups).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [
                {
                    backupId: 9001,
                    backupType: "MANUAL",
                    backupStatus: "SUCCEEDED",
                    fileName: "backup_20260629-120000.sql",
                    fileSizeBytes: 4096,
                    checksum: "sha256-backup",
                    requesterUserId: 1001,
                    startedAt: "2026-06-29T12:00:00+08:00",
                    completedAt: "2026-06-29T12:01:00+08:00",
                    expiresAt: "2026-07-29T12:01:00+08:00"
                }
            ]
        });
        vi.mocked(service.pageRestores).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 0,
            totalCount: 0,
            records: []
        });
        vi.mocked(service.getBackupDetail).mockResolvedValue(null as never);
        vi.mocked(service.getRestoreDetail).mockResolvedValue(null as never);
        vi.mocked(service.createManualBackup).mockResolvedValue(null as never);
        vi.mocked(service.recoverBackup).mockResolvedValue(null as never);
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders page shell", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <BackupRestorePage />
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "备份与恢复" })).toBeInTheDocument();
        expect((await screen.findAllByText("backup_20260629-120000.sql")).length).toBeGreaterThan(
            0
        );
    }, 30000);
});
