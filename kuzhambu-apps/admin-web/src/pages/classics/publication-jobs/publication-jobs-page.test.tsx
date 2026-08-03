import { AdminQueryProvider } from "@/query/query-client";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { PublicationJobsPage } from "./publication-jobs-page";

const job = {
    id: "9007199254740993",
    jobType: "PUBLISH",
    jobStatus: "ES_PREPARED",
    jobResultStatus: "FAILED",
    failureStep: "ES_PREPARED",
    contentType: "SANCAI_ENTRY",
    contentId: "8001",
    contentTitleSnapshot: "天地",
    sourceLifecycleStatus: "DRAFT",
    targetLifecycleStatus: "PUBLISHED",
    attemptCount: 4,
    maxAttempts: 4,
    esDocumentId: "classics-sancai-8001",
    esCleanupStatus: "PENDING",
    fastgptCollectionId: null,
    fastgptCleanupStatus: "NONE",
    failureReason: "ES probe failed",
    detailJsonSummary: '{"provider":"ES"}',
    requestedAt: "2026-08-02T06:00:00Z"
};

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

describe("PublicationJobsPage", () => {
    beforeEach(() => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const path = String(input).replace("/kuzhambu-admin-api/api", "");
            if (path.endsWith("/classics/publication-jobs/page")) {
                return apiResponse({
                    pageNo: 1,
                    pageSize: 20,
                    count: 1,
                    totalPage: 1,
                    records: [job]
                });
            }
            if (path.endsWith("/classics/publication-jobs/get")) {
                return apiResponse(job);
            }
            return apiResponse(null);
        });
    });

    afterEach(() => {
        cleanup();
        vi.restoreAllMocks();
        localStorage.clear();
    });

    it("shows a read-only task list and loads details on demand", async () => {
        const user = userEvent.setup({ delay: null });
        render(
            <AdminQueryProvider>
                <AntdApp>
                    <PublicationJobsPage />
                </AntdApp>
            </AdminQueryProvider>
        );

        expect(await screen.findByText("天地")).toBeInTheDocument();
        expect(screen.getByText("搜索索引已预备")).toHaveClass("kuzhambu-tag", "kuzhambu-tag-info");
        expect(
            screen.queryByRole("button", { name: /重试|取消|清理|编辑/ })
        ).not.toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "查看" }));

        expect(await screen.findByText("ES probe failed")).toBeInTheDocument();
        expect(screen.getAllByText("搜索索引已预备")).toHaveLength(2);
        await waitFor(() => {
            expect(globalThis.fetch).toHaveBeenCalledTimes(2);
        });
    });
});
