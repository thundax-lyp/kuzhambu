import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import * as service from "./cleanup-service";
import { CleanupPage } from "./cleanup-page";

vi.mock("./cleanup-service", () => ({
    requestCleanup: vi.fn(),
    pageCleanups: vi.fn(),
    getCleanupDetail: vi.fn()
}));

const confirmDangerMock = vi.hoisted(() =>
    vi.fn((options: { onConfirm: () => Promise<unknown> | unknown }) => options.onConfirm())
);

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: confirmDangerMock
    })
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const kuzhambuDrawer = ({
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
        KuzhambuDrawer: kuzhambuDrawer
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
                message: {
                    success: vi.fn(),
                    error: vi.fn()
                },
                notification: {} as never,
                modal: {} as never
            })
        },
        Descriptions
    };
});

const renderPage = () => {
    render(
        <QueryClientProvider client={queryClient}>
            <CleanupPage />
        </QueryClientProvider>
    );
};

describe("CleanupPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["operations:cleanup:view", "operations:cleanup:execute"]);
        confirmDangerMock.mockClear();

        vi.mocked(service.pageCleanups).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 2,
            totalCount: 2,
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
                },
                {
                    cleanupId: 9201,
                    cleanupType: "EXPIRED_SHARE",
                    cleanupStatus: "FAILED",
                    totalCount: 5,
                    successCount: 2,
                    failedCount: 3,
                    failureReason: "share file missing",
                    requesterUserId: 1001,
                    startedAt: "2026-06-29T11:00:00+08:00",
                    completedAt: "2026-06-29T11:03:00+08:00"
                }
            ]
        });
        vi.mocked(service.getCleanupDetail).mockResolvedValue({
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
        });
        vi.mocked(service.requestCleanup).mockResolvedValue({
            cleanupId: 9301,
            cleanupType: "EXPIRED_DRAFT",
            cleanupStatus: "RUNNING",
            totalCount: 0,
            successCount: 0,
            failedCount: 0,
            failureReason: null,
            requesterUserId: 1001,
            startedAt: "2026-06-29T11:10:00+08:00",
            completedAt: null
        });
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders page and opens cleanup detail", async () => {
        const user = userEvent.setup();
        renderPage();

        expect(await screen.findByRole("heading", { name: "清理任务台账" })).toBeInTheDocument();
        expect(await screen.findByText("EXPIRED_BACKUP")).toBeInTheDocument();
        expect(await screen.findByText("EXPIRED_SHARE")).toBeInTheDocument();
        const detailButtons = await screen.findAllByRole("button", { name: "详情" });
        await user.click(detailButtons[0]);

        expect(await screen.findByText("清理任务详情")).toBeInTheDocument();
        expect(service.getCleanupDetail).toHaveBeenCalledWith({ cleanupId: 9101 });
    });

    it("opens failure item section from failed record", async () => {
        const user = userEvent.setup();
        vi.mocked(service.getCleanupDetail).mockResolvedValueOnce({
            cleanupId: 9201,
            cleanupType: "EXPIRED_SHARE",
            cleanupStatus: "FAILED",
            totalCount: 5,
            successCount: 2,
            failedCount: 3,
            failureReason: "share file missing",
            requesterUserId: 1001,
            startedAt: "2026-06-29T11:00:00+08:00",
            completedAt: "2026-06-29T11:03:00+08:00"
        });
        renderPage();

        expect(await screen.findByText("失败项")).toBeInTheDocument();
        const failButtons = await screen.findAllByRole("button", { name: "失败项" });
        await user.click(failButtons[0]);

        expect(await screen.findByText("清理失败项")).toBeInTheDocument();
        expect(await screen.findByText(/3 条失败项/)).toBeInTheDocument();
        expect(await screen.findByText("share file missing")).toBeInTheDocument();
        expect(service.getCleanupDetail).toHaveBeenCalledWith({ cleanupId: 9201 });
    });

    it("executes cleanup by confirmation", async () => {
        const user = userEvent.setup();
        renderPage();

        await user.click(
            screen.getByRole("combobox", {
                name: "执行清理类型"
            })
        );
        await user.click(await screen.findByText("过期草稿"));

        expect(confirmDangerMock).toHaveBeenCalledTimes(1);
        expect(vi.mocked(service.requestCleanup)).toHaveBeenCalledWith({
            cleanupType: "EXPIRED_DRAFT"
        });
    });
});
