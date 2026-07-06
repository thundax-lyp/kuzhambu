import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { BackupRestorePage } from "./backup-restore-page";
import * as service from "./backup-restore-service";

const confirmDanger = vi.hoisted(() => vi.fn());

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({ danger: confirmDanger })
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
    const Select = ({
        "aria-label": ariaLabel,
        onChange,
        options,
        value
    }: {
        "aria-label"?: string;
        onChange?: (value: string) => void;
        options?: Array<{ label: ReactNode; value: string }>;
        value?: string;
    }) => (
        <select
            aria-label={ariaLabel}
            onChange={(event) => onChange?.(event.target.value)}
            value={value}
        >
            {(options || []).map((option) => (
                <option key={option.value} value={option.value}>
                    {option.label}
                </option>
            ))}
        </select>
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
        Select,
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
                    backupType: "AUTO",
                    backupStatus: "SUCCEEDED",
                    fileName: "backup_20260629-120000.sql",
                    fileSizeBytes: 4096,
                    checksum: "sha256-backup",
                    requesterUserId: null,
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
            count: 1,
            totalCount: 1,
            records: [
                {
                    restoreId: 9101,
                    backupId: 9001,
                    preRestoreBackupId: 9201,
                    restoreMode: "DRILL",
                    restoreStatus: "SUCCEEDED",
                    writeBlockEnabled: false,
                    writeBlockStartedAt: "2026-06-29T12:02:00+08:00",
                    writeBlockReleasedAt: "2026-06-29T12:03:00+08:00",
                    requesterUserId: 1001,
                    startedAt: "2026-06-29T12:02:00+08:00",
                    completedAt: "2026-06-29T12:03:00+08:00"
                }
            ]
        });
        vi.mocked(service.getBackupDetail).mockResolvedValue(null as never);
        vi.mocked(service.getRestoreDetail).mockResolvedValue({
            restoreId: 9101,
            backupId: 9001,
            preRestoreBackupId: 9201,
            restoreMode: "DRILL",
            restoreStatus: "SUCCEEDED",
            writeBlockEnabled: false,
            writeBlockStartedAt: "2026-06-29T12:02:00+08:00",
            writeBlockReleasedAt: "2026-06-29T12:03:00+08:00",
            requesterUserId: 1001,
            startedAt: "2026-06-29T12:02:00+08:00",
            completedAt: "2026-06-29T12:03:00+08:00"
        });
        vi.mocked(service.createManualBackup).mockResolvedValue(null as never);
        vi.mocked(service.recoverBackup).mockResolvedValue({
            restoreId: 9102,
            backupId: 9001,
            restoreMode: "DRILL",
            restoreStatus: "SUCCEEDED"
        });
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
        expect(await screen.findAllByText("自动备份")).not.toHaveLength(0);
        expect(await screen.findAllByText("系统自动")).not.toHaveLength(0);
    }, 30000);

    it("filters restore ledger by drill mode", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <BackupRestorePage />
            </QueryClientProvider>
        );

        fireEvent.change(await screen.findByLabelText("恢复模式"), {
            target: { value: "DRILL" }
        });

        await waitFor(() => {
            expect(service.pageRestores).toHaveBeenLastCalledWith(
                expect.objectContaining({ restoreMode: "DRILL" })
            );
        });
    }, 30000);

    it("confirms drill and real restore with explicit modes", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <BackupRestorePage />
            </QueryClientProvider>
        );

        fireEvent.click((await screen.findByText("演练")).closest("button") as HTMLButtonElement);
        expect(confirmDanger).toHaveBeenLastCalledWith(
            expect.objectContaining({
                title: "执行恢复演练",
                okText: "执行演练"
            })
        );
        await confirmDanger.mock.calls.at(-1)?.[0].onConfirm();
        expect(vi.mocked(service.recoverBackup).mock.calls.at(-1)?.[0]).toEqual({
            backupId: 9001,
            restoreMode: "DRILL"
        });

        fireEvent.click(screen.getByText("恢复").closest("button") as HTMLButtonElement);
        expect(confirmDanger).toHaveBeenLastCalledWith(
            expect.objectContaining({
                title: "执行真实恢复",
                okText: "执行真实恢复"
            })
        );
        await confirmDanger.mock.calls.at(-1)?.[0].onConfirm();
        expect(vi.mocked(service.recoverBackup).mock.calls.at(-1)?.[0]).toEqual({
            backupId: 9001,
            restoreMode: "REAL"
        });
    }, 30000);

    it("shows restore detail mode and write block times", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <BackupRestorePage />
            </QueryClientProvider>
        );

        const detailButtons = await screen.findAllByText("查看");
        fireEvent.click(detailButtons[1].closest("button") as HTMLButtonElement);

        expect(await screen.findByText("恢复详情")).toBeInTheDocument();
        expect(await screen.findByText("恢复模式")).toBeInTheDocument();
        expect(await screen.findAllByText("恢复演练")).not.toHaveLength(0);
        expect(await screen.findByText("写阻断开启时间")).toBeInTheDocument();
        expect(await screen.findByText("写阻断释放时间")).toBeInTheDocument();
    }, 30000);
});
