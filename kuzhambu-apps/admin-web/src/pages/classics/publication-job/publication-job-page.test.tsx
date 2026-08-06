import { AdminQueryProvider } from "@/query/query-client";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { PublicationJobPage } from "./publication-job-page";

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

const findReadyViewButton = async () => {
    const viewButton = await screen.findByRole("button", { name: "查看" });
    await waitFor(() => {
        expect(window.getComputedStyle(viewButton).pointerEvents).not.toBe("none");
    });
    return viewButton;
};

let pageRequestFails = false;
let detailRequestFails = false;
const pageRequestBodies: unknown[] = [];

describe("PublicationJobPage", () => {
    beforeEach(() => {
        pageRequestFails = false;
        detailRequestFails = false;
        pageRequestBodies.length = 0;
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        localStorage.setItem(
            "kuzhambu.admin.accessTokenExpireAt",
            String(Date.now() + 3600 * 1000)
        );
        vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
            const path = String(input).replace("/kuzhambu-admin-api/api", "");
            if (path.endsWith("/classics/publication-jobs/page")) {
                pageRequestBodies.push(JSON.parse(String(init?.body)));
                if (pageRequestFails) {
                    return Promise.reject(new Error("发布任务服务暂不可用"));
                }
                return apiResponse({
                    pageNo: 1,
                    pageSize: 20,
                    count: 1,
                    totalPage: 1,
                    records: [job]
                });
            }
            if (path.endsWith("/classics/publication-jobs/get")) {
                if (detailRequestFails) {
                    return Promise.reject(new Error("发布任务详情服务暂不可用"));
                }
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
                    <PublicationJobPage />
                </AntdApp>
            </AdminQueryProvider>
        );

        expect(await screen.findByText("三才图会｜天地")).toBeInTheDocument();
        expect(screen.queryByText("9007199254740993")).not.toBeInTheDocument();
        expect(screen.queryByRole("checkbox")).not.toBeInTheDocument();
        expect(screen.getByText("发布")).toHaveClass("kuzhambu-tag", "kuzhambu-tag-accent");
        expect(screen.getByText("搜索索引已写入")).toHaveClass("kuzhambu-tag", "kuzhambu-tag-info");
        expect(
            screen.queryByRole("button", { name: /重试|取消|清理|编辑/ })
        ).not.toBeInTheDocument();

        await user.click(await findReadyViewButton());

        expect(await screen.findByText("ES probe failed")).toBeInTheDocument();
        expect(screen.getAllByText("搜索索引已写入")).toHaveLength(3);
        expect(screen.getByText("草稿 → 已发布")).toBeInTheDocument();
        expect(screen.getByText("等待清理")).toBeInTheDocument();
        expect(screen.getByText("无需清理")).toBeInTheDocument();
        await waitFor(() => {
            expect(globalThis.fetch).toHaveBeenCalledTimes(2);
        });
    });

    it("shows recoverable list and detail errors", async () => {
        const user = userEvent.setup({ delay: null });
        pageRequestFails = true;
        render(
            <AdminQueryProvider>
                <AntdApp>
                    <PublicationJobPage />
                </AntdApp>
            </AdminQueryProvider>
        );

        expect(await screen.findByText("发布任务加载失败")).toBeInTheDocument();
        expect(screen.getByText("发布任务服务暂不可用")).toBeInTheDocument();
        expect(screen.queryByRole("table", { name: "发布任务列表" })).not.toBeInTheDocument();
        pageRequestFails = false;
        await user.click(screen.getByRole("button", { name: "重试加载发布任务" }));
        expect(await screen.findByText("三才图会｜天地")).toBeInTheDocument();

        detailRequestFails = true;
        await user.click(await findReadyViewButton());
        expect(await screen.findByText("发布任务详情加载失败")).toBeInTheDocument();
        expect(screen.getByText("发布任务详情服务暂不可用")).toBeInTheDocument();
        detailRequestFails = false;
        await user.click(screen.getByRole("button", { name: "重试加载发布任务详情" }));
        expect(await screen.findByText("ES probe failed")).toBeInTheDocument();
    });

    it("debounces server searches", async () => {
        const user = userEvent.setup({ delay: null });
        render(
            <AdminQueryProvider>
                <AntdApp>
                    <PublicationJobPage />
                </AntdApp>
            </AdminQueryProvider>
        );
        await screen.findByText("三才图会｜天地");

        await user.type(screen.getByRole("textbox", { name: "搜索发布任务" }), "天地");
        expect(pageRequestBodies).toHaveLength(1);
        await waitFor(() => expect(pageRequestBodies).toHaveLength(2));
        expect(pageRequestBodies.at(-1)).toMatchObject({ keyword: "天地", pageNo: 1 });
    });
});
