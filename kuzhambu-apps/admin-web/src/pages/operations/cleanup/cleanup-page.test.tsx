import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
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
});
