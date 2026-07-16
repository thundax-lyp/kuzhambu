import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { queryClient } from "@/query/query-client";
import { SancaiEntryList } from "./components/sancai-entry-list";
import { SancaiPage } from "./sancai-page";
import type { SancaiEntryRecord } from "./sancai-types";

const { mockSancaiEntryPanel } = vi.hoisted(() => ({
    mockSancaiEntryPanel: (props: {
        categoryId: number | null;
        keyword?: string | null;
        lifecycleStatus?: string | null;
        refreshVersion: number;
        volumeId: number | null;
        volumes: Array<{ id: number }>;
    }) => (
        <section aria-label="三才图会条目面板">
            <span>{`category:${props.categoryId ?? "none"}`}</span>
            <span>{`volume:${props.volumeId ?? "none"}`}</span>
            <span>{`volumes:${props.volumes.length}`}</span>
            <span>{`refresh:${props.refreshVersion}`}</span>
            <span>{`keyword:${props.keyword ?? "none"}`}</span>
            <span>{`status:${props.lifecycleStatus ?? "none"}`}</span>
        </section>
    )
}));

vi.mock("./components/sancai-entry-panel", () => ({
    SancaiEntryPanel: mockSancaiEntryPanel
}));

const apiResponse = (data: unknown) =>
    Promise.resolve(
        new Response(JSON.stringify({ code: "COMMON-00000", message: "success", data }), {
            headers: { "Content-Type": "application/json" },
            status: 200
        })
    );

const readFetchUrl = (input: RequestInfo | URL) => {
    if (typeof input === "string") {
        return input;
    }
    if (input instanceof URL) {
        return input.href;
    }
    return input.url;
};

let mockCategories = [{ categoryType: "FORMAL", id: 2, title: "天文" }];
let mockVolumes = [{ categoryId: 2, id: 101, title: "天文卷一", volumeType: "MAIN" }];

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");

        if (path.endsWith("/classics/sancai/categories/list")) {
            return apiResponse(mockCategories);
        }
        if (path.endsWith("/classics/sancai/categories/types/list")) {
            return apiResponse([
                { label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" }
            ]);
        }
        if (path.endsWith("/classics/sancai/volumes/types/list")) {
            return apiResponse([{ label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "MAIN" }]);
        }
        if (path.endsWith("/classics/sancai/volumes/list")) {
            return apiResponse(mockVolumes);
        }
        if (path.endsWith("/classics/sancai/entries/page")) {
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalCount: 0,
                totalPage: 0,
                records: []
            });
        }
        if (path.endsWith("/classics/sancai/entries/list")) {
            return apiResponse([
                {
                    id: 3001,
                    volumeId: 101,
                    title: "天地",
                    originalText: "天地玄黄",
                    translationText: "译文",
                    summary: "天地摘要",
                    lifecycleStatus: "PUBLISHED",
                    visibility: "PUBLIC",
                    translationStatus: "READY",
                    imageStatus: "READY",
                    visualAssetStatus: "READY",
                    refinementStatus: "COMPLETE",
                    currentVersionId: 9001,
                    currentVersionNo: 1,
                    currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
                    contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
                    versionDirty: false
                }
            ]);
        }
        if (path.endsWith("/classics/sancai/entries/get")) {
            return apiResponse({
                id: 3001,
                volumeId: 101,
                title: "天地",
                originalText: "天地玄黄",
                translationText: "译文",
                summary: "天地摘要",
                lifecycleStatus: "PUBLISHED",
                visibility: "PUBLIC",
                translationStatus: "READY",
                imageStatus: "READY",
                visualAssetStatus: "READY",
                refinementStatus: "COMPLETE",
                currentVersionId: 9001,
                currentVersionNo: 1,
                currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
                contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
                versionDirty: false
            });
        }
        if (path.endsWith("/classics/sancai/assets/images/list")) {
            return apiResponse([
                {
                    currentUsed: true,
                    entryId: 3001,
                    id: 8001,
                    imageType: "ORIGINAL",
                    originalFilename: "sancai.png",
                    priority: 1,
                    size: 10,
                    storageObjectId: 7001,
                    title: "sancai.png"
                }
            ]);
        }
        if (path.endsWith("/classics/sancai/assets/visual-assets/list")) {
            return apiResponse([
                {
                    id: 5002,
                    visualAssetId: 5002,
                    entryId: 3001,
                    versionNo: 2,
                    status: "READY",
                    sourcePreviewUrl:
                        "/api/classics/sancai/assets/visual-assets/3001/5001/source-content",
                    sourceDownloadUrl:
                        "/api/classics/sancai/assets/visual-assets/3001/5001/source-content?download=true",
                    generatedPreviewUrl:
                        "/api/classics/sancai/assets/visual-assets/3001/5001/generated-content",
                    generatedDownloadUrl:
                        "/api/classics/sancai/assets/visual-assets/3001/5001/generated-content?download=true",
                    currentUsed: true,
                    textWeight: 60,
                    imageWeight: 40,
                    imageAnalysisMarkdown: "图片理解",
                    fusionDescription: "融合说明",
                    visualDescription: "视觉描述",
                    generationParamsJson: '{"style":"gongbi"}'
                }
            ]);
        }
        if (path.endsWith("/classics/sancai/entries/versions/list")) {
            return apiResponse([
                {
                    id: 9001,
                    contentType: "SANCAI_ENTRY",
                    contentId: 3001,
                    versionNo: 1,
                    versionedAt: "2026-06-20T01:00:00.000+08:00",
                    snapshotJson: '{"title":"天地"}',
                    changeType: "MANUAL_SAVE",
                    changeSummary: "手动保存"
                }
            ]);
        }
        if (path.endsWith("/sys/current-user/info")) {
            return apiResponse({
                id: 99,
                loginName: "admin",
                name: "Admin"
            });
        }
        if (path.endsWith("/ai/refinement/task/page")) {
            return apiResponse({
                items: [],
                total: 0,
                pageNo: 1,
                pageSize: 20
            });
        }
        if (path.endsWith("/classics/content/tags/list")) {
            return apiResponse([]);
        }
        if (path.endsWith("/classics/content/qa-pairs/list")) {
            return apiResponse([]);
        }
        if (path.endsWith("/ai/invocation/candidate/list")) {
            return apiResponse([]);
        }

        return apiResponse(true);
    });
};

const buildEntry = (id: number, title: string): SancaiEntryRecord =>
    ({
        id,
        volumeId: 101,
        title,
        originalText: `${title}原文`,
        translationText: `${title}译文`,
        summary: `${title}摘要`,
        lifecycleStatus: "PUBLISHED",
        visibility: "PUBLIC",
        translationStatus: "READY",
        imageStatus: "READY",
        visualAssetStatus: "READY",
        refinementStatus: "COMPLETE",
        currentVersionId: 9001,
        currentVersionNo: 1,
        currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
        contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
        versionDirty: false
    }) as SancaiEntryRecord;

const renderEntryList = (entries: SancaiEntryRecord[]) =>
    render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <SancaiEntryList
                    entries={entries}
                    isLoading={false}
                    volumes={[{ id: 101, title: "天文卷一" } as never]}
                    onBatchCandidateGovernance={vi.fn()}
                    onChangeLifecycleStatus={vi.fn()}
                    onDelete={vi.fn()}
                    onExport={vi.fn()}
                    onRefresh={vi.fn()}
                    onShare={vi.fn()}
                    onSort={vi.fn()}
                    onView={vi.fn()}
                />
            </AntdApp>
        </QueryClientProvider>
    );

describe("SancaiPage", () => {
    beforeEach(() => {
        queryClient.clear();
        mockCategories = [{ categoryType: "FORMAL", id: 2, title: "天文" }];
        mockVolumes = [{ categoryId: 2, id: 101, title: "天文卷一", volumeType: "MAIN" }];
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        installFetchMock();
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it("renders page and category tree content", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "三才图会" })).toBeInTheDocument();
        expect(await screen.findByRole("link", { name: "打开门类 天文" })).toBeInTheDocument();
    }, 30000);

    it("switches to the entry panel with the selected catalog context", async () => {
        const user = userEvent.setup();
        mockCategories = [
            { categoryType: "FORMAL", id: 2, title: "天文" },
            { categoryType: "FORMAL", id: 3, title: "地理" }
        ];
        mockVolumes = [
            { categoryId: 2, id: 101, title: "天文卷一", volumeType: "MAIN" },
            { categoryId: 3, id: 202, title: "地理卷一", volumeType: "MAIN" }
        ];

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await user.click(await screen.findByRole("link", { name: "打开门类 天文" }));
        const volumeTable = await screen.findByLabelText("三才图会卷目表格");
        await user.click(
            await within(volumeTable).findByRole("link", { name: "打开卷目 天文卷一" })
        );

        const entryPanel = await screen.findByLabelText("三才图会条目面板");
        expect(within(entryPanel).getByText("category:2")).toBeInTheDocument();
        expect(within(entryPanel).getByText("volume:101")).toBeInTheDocument();
        expect(within(entryPanel).getByText("volumes:2")).toBeInTheDocument();
        expect(within(entryPanel).getByText("refresh:0")).toBeInTheDocument();
        expect(within(entryPanel).getByText("keyword:none")).toBeInTheDocument();
        expect(within(entryPanel).getByText("status:none")).toBeInTheDocument();

        const refreshButton = screen.getByTestId("classics-sancai-sancai-action-button");
        const taskButton = screen.getByTestId("classics-sancai-sancai-action-button-2");
        expect(
            refreshButton.compareDocumentPosition(taskButton) & Node.DOCUMENT_POSITION_FOLLOWING
        ).toBeTruthy();
        expect(taskButton.querySelector(".anticon-schedule")).toBeInTheDocument();
    }, 30000);

    it("keeps entry batch selection scoped to the current page entries", async () => {
        const user = userEvent.setup();
        const firstPageEntries = [buildEntry(3001, "天地")];
        const secondPageEntries = [buildEntry(3002, "山川")];

        const { rerender } = renderEntryList(firstPageEntries);
        const table = await screen.findByLabelText("三才图会条目表格");

        expect(screen.getByText("当前页已选 0 条")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "图片理解" })).toBeDisabled();

        const rowCheckbox = within(table).getAllByRole("checkbox")[1];
        await user.click(rowCheckbox.closest("label") ?? rowCheckbox);

        expect(await screen.findByText("当前页已选 1 条")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "图片理解" })).not.toBeDisabled();

        rerender(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiEntryList
                        entries={secondPageEntries}
                        isLoading={false}
                        volumes={[{ id: 101, title: "天文卷一" } as never]}
                        onBatchCandidateGovernance={vi.fn()}
                        onChangeLifecycleStatus={vi.fn()}
                        onDelete={vi.fn()}
                        onExport={vi.fn()}
                        onRefresh={vi.fn()}
                        onShare={vi.fn()}
                        onSort={vi.fn()}
                        onView={vi.fn()}
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        await waitFor(() => {
            expect(screen.getByText("当前页已选 0 条")).toBeInTheDocument();
        });
        expect(screen.getByRole("button", { name: "图片理解" })).toBeDisabled();
        expect(screen.getByText("山川")).toBeInTheDocument();
    }, 30000);
});
