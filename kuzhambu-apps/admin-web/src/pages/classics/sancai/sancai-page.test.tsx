import { QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
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

const showcasePageRequests: unknown[] = [];
const showcaseCreateRequests: unknown[] = [];

const parseJsonBody = (init?: RequestInit) => {
    if (typeof init?.body !== "string") {
        return null;
    }
    return JSON.parse(init.body);
};

const selectDropdownOption = async (user: ReturnType<typeof userEvent.setup>, text: string) => {
    const option = await screen
        .findAllByText(text)
        .then((nodes) =>
            nodes.find((node) => node.classList.contains("ant-select-item-option-content"))
        );
    expect(option).toBeTruthy();
    await user.click(option as HTMLElement);
};

const installFetchMock = () => {
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
        const path = readFetchUrl(input).replace("/kuzhambu-admin-api/api", "");

        if (path.endsWith("/classics/sancai/categories/list")) {
            return apiResponse([{ categoryType: "FORMAL", id: 2, title: "天文" }]);
        }
        if (path.endsWith("/classics/sancai/categories/types")) {
            return apiResponse([
                { label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" }
            ]);
        }
        if (path.endsWith("/classics/sancai/volumes/types")) {
            return apiResponse([{ label: "正式卷目", type: "SANCAI_VOLUME_TYPE", value: "MAIN" }]);
        }
        if (path.endsWith("/classics/sancai/volumes/list")) {
            return apiResponse([{ categoryId: 2, id: 101, title: "天文卷一", volumeType: "MAIN" }]);
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
        if (path.endsWith("/classics/sancai/assets/showcases/page")) {
            showcasePageRequests.push(parseJsonBody(init));
            return apiResponse({
                pageNo: 1,
                pageSize: 20,
                totalPage: 1,
                count: 1,
                totalCount: 1,
                records: [
                    {
                        id: 2001,
                        status: "COMPLETED",
                        requestedAt: "2026-06-21T10:30:00.000+08:00",
                        completedAt: "2026-06-21T10:31:00.000+08:00",
                        scopeTitle: "全部三才图会公开条目",
                        entryCount: 3,
                        assetCount: 2,
                        visibilityRiskStatus: "PUBLIC_ONLY",
                        filename: "sancai-showcase.html",
                        sizeBytes: 2048,
                        contentUrl: "/showcases/2001.html",
                        downloadUrl: "/downloads/showcase.html"
                    }
                ]
            });
        }
        if (path.endsWith("/classics/sancai/assets/showcases/request")) {
            showcaseCreateRequests.push(parseJsonBody(init));
            return apiResponse({
                id: 2002,
                status: "REQUESTED"
            });
        }
        if (path.endsWith("/classics/sancai/entries/3001")) {
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
        if (path.endsWith("/classics/sancai/assets/images/3001")) {
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
        if (path.endsWith("/classics/sancai/assets/visual-assets/3001")) {
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
        if (path.includes("/classics/content/tags?")) {
            return apiResponse([]);
        }
        if (path.includes("/classics/content/qa-pairs?")) {
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
                    onShare={vi.fn()}
                    onShowcase={vi.fn()}
                    onSort={vi.fn()}
                    onView={vi.fn()}
                />
            </AntdApp>
        </QueryClientProvider>
    );

describe("SancaiPage", () => {
    beforeEach(() => {
        queryClient.clear();
        showcasePageRequests.length = 0;
        showcaseCreateRequests.length = 0;
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
        expect(within(entryPanel).getByText("volumes:1")).toBeInTheDocument();
        expect(within(entryPanel).getByText("refresh:0")).toBeInTheDocument();
        expect(within(entryPanel).getByText("keyword:none")).toBeInTheDocument();
        expect(within(entryPanel).getByText("status:none")).toBeInTheDocument();
    }, 30000);

    it("renders showcase jobs and opens preview or download urls", async () => {
        const openSpy = vi.spyOn(window, "open").mockImplementation(() => null);
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const showcaseSection = await screen.findByLabelText("静态展示任务");
        expect(
            await within(showcaseSection).findByText("全部三才图会公开条目")
        ).toBeInTheDocument();
        expect(
            await within(showcaseSection).findByText("sancai-showcase.html · 2.0 KB")
        ).toBeInTheDocument();

        await user.click(await within(showcaseSection).findByRole("button", { name: /预\s*览/ }));
        await user.click(await within(showcaseSection).findByRole("button", { name: /下\s*载/ }));

        expect(openSpy).toHaveBeenCalledWith(
            "/showcases/2001.html",
            "_blank",
            "noopener,noreferrer"
        );
        expect(openSpy).toHaveBeenCalledWith(
            "/downloads/showcase.html",
            "_blank",
            "noopener,noreferrer"
        );
    }, 30000);

    it("filters showcase jobs by keyword, status, and visibility risk", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const showcaseSection = await screen.findByLabelText("静态展示任务");
        await user.type(within(showcaseSection).getByLabelText("搜索静态展示任务"), "天地");
        fireEvent.mouseDown(within(showcaseSection).getByLabelText("静态展示任务状态"));
        await selectDropdownOption(user, "已完成");
        fireEvent.mouseDown(within(showcaseSection).getByLabelText("静态展示可见性风险"));
        await selectDropdownOption(user, "仅公开内容");
        await user.click(within(showcaseSection).getByRole("button", { name: /筛\s*选/ }));

        await waitFor(() => {
            expect(showcasePageRequests.at(-1)).toMatchObject({
                keyword: "天地",
                pageNo: 1,
                pageSize: 20,
                status: "COMPLETED",
                visibilityRiskStatus: "PUBLIC_ONLY"
            });
        });
    }, 30000);

    it("creates a public showcase job with the current catalog scope", async () => {
        const user = userEvent.setup();

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <SancaiPage />
                </AntdApp>
            </QueryClientProvider>
        );

        const showcaseSection = await screen.findByLabelText("静态展示任务");
        await user.click(within(showcaseSection).getByRole("button", { name: /生成静态展示/ }));

        await waitFor(() => {
            expect(showcaseCreateRequests).toHaveLength(1);
        });
        const request = showcaseCreateRequests[0] as {
            privateConfirmed?: boolean;
            scopeJson?: string;
            scopeTitle?: string;
            visibilityRiskStatus?: string;
        };
        expect(request).toMatchObject({
            privateConfirmed: false,
            scopeTitle: "全部三才图会公开条目",
            visibilityRiskStatus: "PUBLIC_ONLY"
        });
        const scope = JSON.parse(request.scopeJson || "{}");
        expect(scope.scope).toMatchObject({
            scopeType: "ALL_PUBLIC",
            categoryIds: [2],
            volumeIds: [101],
            filters: {
                keyword: null,
                lifecycleStatus: "PUBLISHED",
                visibility: "PUBLIC"
            }
        });
        expect(scope.visibilityRisk).toMatchObject({
            status: "PUBLIC_ONLY",
            privateConfirmed: false
        });
    }, 30000);

    it("keeps entry batch selection scoped to the current page entries", async () => {
        const user = userEvent.setup();
        const firstPageEntries = [buildEntry(3001, "天地")];
        const secondPageEntries = [buildEntry(3002, "山川")];

        const { rerender } = renderEntryList(firstPageEntries);
        const table = await screen.findByLabelText("三才图会条目表格");

        expect(screen.getByText("当前页已选 0 条")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "批量图片理解" })).toBeDisabled();

        const rowCheckbox = within(table).getAllByRole("checkbox")[1];
        await user.click(rowCheckbox.closest("label") ?? rowCheckbox);

        expect(await screen.findByText("当前页已选 1 条")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "批量图片理解" })).not.toBeDisabled();

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
                        onShare={vi.fn()}
                        onShowcase={vi.fn()}
                        onSort={vi.fn()}
                        onView={vi.fn()}
                    />
                </AntdApp>
            </QueryClientProvider>
        );

        await waitFor(() => {
            expect(screen.getByText("当前页已选 0 条")).toBeInTheDocument();
        });
        expect(screen.getByRole("button", { name: "批量图片理解" })).toBeDisabled();
        expect(screen.getByText("山川")).toBeInTheDocument();
    }, 30000);
});
