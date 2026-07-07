import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import * as service from "./cleanup-service";
import { CleanupPage } from "./cleanup-page";

vi.mock("./cleanup-service", () => ({
    requestCleanup: vi.fn(),
    pageCleanups: vi.fn(),
    getCleanupDetail: vi.fn()
}));

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({ danger: vi.fn() })
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        open,
        title
    }: {
        children: React.ReactNode;
        open?: boolean;
        title?: React.ReactNode;
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
    const Descriptions = ({
        items
    }: {
        items?: Array<{ label: React.ReactNode; children: React.ReactNode }>;
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
        Descriptions
    };
});

describe("CleanupPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["operations:cleanup:view", "operations:cleanup:execute"]);
        vi.mocked(service.pageCleanups).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [
                {
                    cleanupId: 9101,
                    cleanupType: "EXPIRED_BACKUP",
                    cleanupStatus: "SUCCEEDED",
                    totalCount: 10,
                    successCount: 10,
                    failedCount: 0,
                    failureReason: null,
                    requesterUserId: 1001,
                    startedAt: "2026-06-29T10:00:00+08:00",
                    completedAt: "2026-06-29T10:05:00+08:00"
                }
            ]
        });
        vi.mocked(service.getCleanupDetail).mockResolvedValue(null as never);
        vi.mocked(service.requestCleanup).mockResolvedValue(null as never);
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders page shell", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <CleanupPage />
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "清理任务台账" })).toBeInTheDocument();
        expect(await screen.findByText("EXPIRED_BACKUP")).toBeInTheDocument();
    }, 30000);

    it("renders cleanup failure hints from task and item fields", async () => {
        vi.mocked(service.pageCleanups).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [
                {
                    cleanupId: 9102,
                    cleanupType: "EXPIRED_EXPORT",
                    cleanupStatus: "FAILED",
                    totalCount: 2,
                    successCount: 1,
                    failedCount: 1,
                    failureReason: "object delete denied",
                    requesterUserId: 1001
                }
            ]
        });
        vi.mocked(service.getCleanupDetail).mockResolvedValue({
            cleanupId: 9102,
            cleanupType: "EXPIRED_EXPORT",
            cleanupStatus: "FAILED",
            totalCount: 2,
            successCount: 1,
            failedCount: 1,
            failureReason: "object delete denied",
            requesterUserId: 1001,
            items: [
                {
                    cleanupItemId: 11,
                    targetType: "EXPORT_OBJECT",
                    targetId: 8801,
                    itemStatus: "FAILED",
                    failureReason: "storage object locked"
                }
            ]
        });

        render(
            <QueryClientProvider client={queryClient}>
                <CleanupPage />
            </QueryClientProvider>
        );

        expect(await screen.findByText("object delete denied")).toBeInTheDocument();
        expect(screen.getByText("查看告警")).toHaveAttribute(
            "href",
            "/operations/dashboard?sourceRefType=CLEANUP&sourceRefId=9102"
        );

        fireEvent.click(await screen.findByRole("button", { name: "失败项" }));

        expect(await screen.findByText("清理任务执行失败")).toBeInTheDocument();
        expect(screen.getByText("清理项失败")).toBeInTheDocument();
        expect(screen.getByText("storage object locked")).toBeInTheDocument();
    }, 30000);
});
