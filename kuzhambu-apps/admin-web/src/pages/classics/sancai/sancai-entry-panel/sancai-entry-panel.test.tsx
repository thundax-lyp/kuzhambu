import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { useEffect } from "react";
import { MemoryRouter, useLocation } from "react-router-dom";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as visualPreviewService from "@/pages/classics/common/sancai-visual-preview-service";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import { SancaiEntryPanel } from "./sancai-entry-panel";
import * as entryService from "@/pages/classics/sancai/sancai-entry-service";

const confirmDangerMock = vi.hoisted(() =>
    vi.fn((options: { onConfirm: () => unknown }) => options.onConfirm())
);

vi.mock("@/pages/classics/common/classics-content-service", () => ({
    applyAiCandidatesBatch: vi.fn(),
    rejectAiCandidatesBatch: vi.fn(),
    addQaPair: vi.fn(),
    addTag: vi.fn(),
    changeVisibilityBatch: vi.fn(async () => ({
        failureCount: 1,
        failures: [
            {
                contentId: "3002",
                contentType: "SANCAI_ENTRY",
                failureCode: "PERMISSION_DENIED",
                failureReason: "PERMISSION_DENIED",
                status: "FAILED"
            }
        ],
        successCount: 1,
        successes: [
            {
                contentId: "3001",
                contentType: "SANCAI_ENTRY",
                resultId: "3001",
                status: "PRIVATE"
            }
        ]
    })),
    deleteTag: vi.fn(),
    listQaPairs: vi.fn(async () => [
        {
            id: "6001",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            qaPairId: "7001",
            question: "天地为何不变？",
            answer: "因为天地变化永恒。",
            source: "MANUAL",
            status: "ACTIVE",
            priority: 1,
            updatedAt: "2026-06-20T01:00:00.000+08:00",
            createdAt: "2026-06-20T01:00:00.000+08:00"
        }
    ]),
    listTags: vi.fn(async () => [
        {
            id: "5001",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            tagId: "8001",
            tagNameSnapshot: "三才",
            source: "MANUAL",
            status: "ACTIVE",
            priority: 1,
            updatedAt: "2026-06-20T01:00:00.000+08:00",
            createdAt: "2026-06-20T01:00:00.000+08:00"
        }
    ]),
    sortQaPairs: vi.fn(),
    sortTags: vi.fn(),
    updateQaPair: vi.fn(),
    updateTag: vi.fn()
}));

const normalizeTaskCapabilityMock = vi.hoisted(() => {
    const aliases: Record<string, string> = {
        classics_translate: "translate",
        classics_summary: "summary",
        classics_tags: "tags",
        classics_qa: "qa",
        classics_image_describe: "image_analysis",
        classics_image_prompt_fusion: "fusion",
        classics_visual_describe: "visual",
        classics_image_generate: "image_gen"
    };
    return (capability: string) => aliases[capability] ?? capability;
});

vi.mock("@/pages/classics/common/ai-refinement-task-service", () => ({
    createTask: vi.fn(
        async (command: { capability: string; contentId: string; contentType: string }) => ({
            taskId: "7001",
            status: "PENDING",
            capability: command.capability,
            contentType: command.contentType,
            contentId: command.contentId
        })
    ),
    getTask: vi.fn(async ({ taskId }: { taskId: string }) => ({
        taskId,
        status: "RUNNING",
        capability: "image_analysis",
        contentType: "SANCAI_ENTRY",
        contentId: "3001",
        objectId: "5002",
        streamEnabled: true,
        requestId: "req-stream-1",
        traceId: "trace-stream-1"
    })),
    pageTasks: vi.fn(async () => ({
        items: [],
        total: 0,
        pageNo: 1,
        pageSize: 20
    })),
    cancelTask: vi.fn(),
    requestTaskStream: vi.fn(
        async ({
            onEvent
        }: {
            onEvent: (event: {
                deltaText?: string;
                eventType: string;
                resultFormat?: string;
                resultPayload?: string;
                status?: string;
            }) => void;
        }) => {
            onEvent({
                eventType: "delta",
                deltaText: "流式片段"
            });
            onEvent({
                eventType: "completed",
                status: "SUCCEEDED",
                resultFormat: "MARKDOWN",
                resultPayload: "完整候选"
            });
        }
    ),
    getNormalizedTaskCapability: vi.fn(normalizeTaskCapabilityMock),
    getTaskCapabilityLabel: vi.fn((capability: string) => normalizeTaskCapabilityMock(capability)),
    getTaskFailureText: vi.fn(
        (failureStage?: string | null, errorType?: string | null, errorMessage?: string | null) =>
            [failureStage, errorType, errorMessage].filter(Boolean).join(" / ") || null
    ),
    getTaskStableId: vi.fn((taskId: string, taskIdText?: string | null) => taskIdText || taskId),
    getTaskRetryable: vi.fn(
        (status: string, capability: string) =>
            ["FAILED", "PARTIAL", "CANCELLED"].includes(status) &&
            ["translate", "summary", "image_analysis", "fusion", "visual", "image_gen"].includes(
                normalizeTaskCapabilityMock(capability)
            )
    )
}));
vi.mock("@/pages/classics/common/ai-candidate-service", () => ({
    list: vi.fn(async () => []),
    apply: vi.fn(async () => ({
        contentType: "SANCAI_ENTRY",
        contentId: "3001",
        versionId: "5002",
        versionNo: 2
    })),
    reject: vi.fn(async () => ({}))
}));

vi.mock("@/pages/classics/common/sancai-visual-preview-service", () => ({
    listVisualAssets: vi.fn(async () => [
        {
            id: "5002",
            visualAssetId: "5002",
            versionNo: 2,
            currentUsed: true,
            visualDescription: "当前版本视觉描述",
            generatedPreviewUrl:
                "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content"
        }
    ])
}));

const entryState = vi.hoisted(() => ({
    restored: false
}));

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: confirmDangerMock
    })
}));

vi.mock("@/pages/classics/common/classics-export-service", () => ({
    create: vi.fn(async () => ({
        id: "1001",
        contentType: "SANCAI_ENTRY",
        exportKind: "CONTENT_DATASET",
        exportFormat: "HTML",
        scopeType: "SELECTED_ITEMS",
        scopeJson: JSON.stringify({
            title: "天地 导出",
            ids: ["3001"]
        }),
        status: "REQUESTED"
    })),
    page: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 10,
        totalPage: 1,
        count: 1,
        records: [
            {
                id: "1001",
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: JSON.stringify({
                    title: "天地 导出",
                    ids: ["3001"]
                }),
                requestedAt: "2026-06-21T10:00:00.000+08:00",
                status: "COMPLETED",
                itemCount: 1,
                assetCount: 0,
                downloadUrl: "/downloads/1001.zip"
            }
        ],
        totalCount: 1
    })),
    deleteById: vi.fn(async () => true),
    getContentUrl: vi.fn()
}));

vi.mock("@/pages/classics/sancai/sancai-entry-service", () => ({
    add: vi.fn(),
    deleteById: vi.fn(),
    get: vi.fn(async () => ({
        id: "3001",
        volumeId: "101",
        title: entryState.restored ? "历史天地" : "天地",
        originalText: entryState.restored ? "历史原文" : "天地玄黄",
        translationText: entryState.restored ? "历史译文" : "译文",
        summary: entryState.restored ? "历史摘要" : "天地摘要",
        lifecycleStatus: "PUBLISHED",
        visibility: "PUBLIC",
        translationStatus: "READY",
        imageStatus: "READY",
        visualAssetStatus: "READY",
        refinementStatus: "COMPLETE",
        currentVersionId: entryState.restored ? 9002 : 9001,
        currentVersionNo: entryState.restored ? 2 : 1,
        currentVersionedAt: entryState.restored
            ? "2026-06-21T01:00:00.000+08:00"
            : "2026-06-20T01:00:00.000+08:00",
        contentUpdatedAt: entryState.restored
            ? "2026-06-21T01:00:00.000+08:00"
            : "2026-06-20T01:00:00.000+08:00",
        versionDirty: false
    })),
    getVersion: vi.fn(async () => ({
        id: "9001",
        contentType: "SANCAI_ENTRY",
        contentId: "3001",
        versionNo: 1,
        versionedAt: "2026-06-20T01:00:00.000+08:00",
        snapshotJson: JSON.stringify({
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            volumeId: "101",
            title: "历史天地",
            originalText: "历史原文",
            translationText: "历史译文",
            summary: "历史摘要",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "READY",
            imageStatus: "READY",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE",
            priority: 1
        }),
        changeType: "MANUAL_SAVE",
        changeSummary: "手动保存"
    })),
    list: vi.fn(async () => [
        {
            id: "3001",
            volumeId: "101",
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
            currentVersionId: "9001",
            currentVersionNo: 1,
            currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
            contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
            versionDirty: false
        },
        {
            id: "3002",
            volumeId: "101",
            title: "地理",
            originalText: "山川",
            translationText: "译文",
            summary: "地理摘要",
            lifecycleStatus: "DRAFT",
            visibility: "PRIVATE",
            translationStatus: "PENDING",
            imageStatus: "PENDING",
            visualAssetStatus: "PENDING",
            refinementStatus: "PENDING",
            currentVersionId: null,
            currentVersionNo: null,
            currentVersionedAt: null,
            contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
            versionDirty: false
        },
        {
            id: "3003",
            volumeId: "101",
            title: "人物",
            originalText: "人物",
            translationText: "译文",
            summary: "人物摘要",
            lifecycleStatus: "ARCHIVED",
            visibility: "PUBLIC",
            translationStatus: "READY",
            imageStatus: "READY",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE",
            currentVersionId: "9003",
            currentVersionNo: 1,
            currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
            contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
            versionDirty: false
        }
    ]),
    listImages: vi.fn(async () => [
        {
            currentUsed: true,
            entryId: "3001",
            id: "8001",
            imageType: "ORIGINAL",
            originalFilename: "sancai.png",
            priority: 1,
            size: 10,
            storageObjectId: "7001",
            title: "sancai.png"
        },
        {
            currentUsed: false,
            entryId: "3001",
            id: "8002",
            imageType: "GENERATED",
            originalFilename: "generated.png",
            priority: 2,
            size: 2048,
            storageObjectId: "7002",
            title: "生成图"
        }
    ]),
    deleteImage: vi.fn(async () => true),
    changeLifecycleStatus: vi.fn(async () => true),
    changeCurrentImage: vi.fn(async () => true),
    sortImages: vi.fn(async () => true),
    listVisualAssets: vi.fn(async () => [
        {
            id: "5002",
            visualAssetId: "5002",
            entryId: "3001",
            versionNo: 2,
            status: "READY",
            sourceImageStorageObjectId: "7001",
            generatedImageStorageObjectId: "7002",
            currentUsed: true,
            textWeight: 60,
            imageWeight: 40,
            imageAnalysisMarkdown: "当前版本图片理解",
            fusionDescription: "当前版本融合描述",
            visualDescription: "当前版本视觉描述",
            generationParamsJson: '{"style":"gongbi"}',
            sourcePreviewUrl: "/api/classics/sancai/assets/visual-assets/3001/5002/source-content",
            sourceDownloadUrl:
                "/api/classics/sancai/assets/visual-assets/3001/5002/source-content?download=true",
            generatedPreviewUrl:
                "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content",
            generatedDownloadUrl:
                "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content?download=true"
        },
        {
            id: "5001",
            visualAssetId: "5001",
            entryId: "3001",
            versionNo: 1,
            status: "READY",
            sourceImageStorageObjectId: "7001",
            generatedImageStorageObjectId: "7102",
            currentUsed: false,
            textWeight: 55,
            imageWeight: 45,
            imageAnalysisMarkdown: "历史版本图片理解",
            fusionDescription: "历史版本融合描述",
            visualDescription: "历史版本视觉描述",
            generationParamsJson: '{"style":"shuimo"}',
            sourcePreviewUrl: "/api/classics/sancai/assets/visual-assets/3001/5001/source-content",
            sourceDownloadUrl:
                "/api/classics/sancai/assets/visual-assets/3001/5001/source-content?download=true",
            generatedPreviewUrl:
                "/api/classics/sancai/assets/visual-assets/3001/5001/generated-content",
            generatedDownloadUrl:
                "/api/classics/sancai/assets/visual-assets/3001/5001/generated-content?download=true"
        }
    ]),
    listVersions: vi.fn(async () => [
        {
            id: "9001",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            versionNo: 1,
            versionedAt: "2026-06-20T01:00:00.000+08:00",
            snapshotJson: JSON.stringify({
                title: "历史天地"
            }),
            changeType: "MANUAL_SAVE",
            changeSummary: "手动保存"
        }
    ]),
    resetVersion: vi.fn(async () => {
        entryState.restored = true;
        return {
            id: "9002",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            versionNo: 2,
            changeType: "HISTORY_RESTORED",
            changeSummary: "恢复历史版本 v1"
        };
    }),
    sort: vi.fn(),
    updateVisualAsset: vi.fn(async (request) => request),
    changeCurrentVisualAsset: vi.fn(async () => true),
    getImageContentUrl: vi.fn(
        (request: { entryId: string; imageId: string; mode?: "download" | "preview" }) => {
            const search = request.mode === "download" ? "?download=true" : "";
            return `/kuzhambu-admin-api/api/classics/sancai/assets/images/${request.entryId}/${request.imageId}/content${search}`;
        }
    ),
    getVisualAssetContentUrl: vi.fn(
        (request: {
            entryId: string;
            visualAssetId: string;
            variant: "source" | "generated";
            mode?: "download" | "preview";
        }) => {
            const search = request.mode === "download" ? "?download=true" : "";
            const suffix = request.variant === "source" ? "source-content" : "generated-content";
            return `/kuzhambu-admin-api/api/classics/sancai/assets/visual-assets/${request.entryId}/${request.visualAssetId}/${suffix}${search}`;
        }
    ),
    uploadImage: vi.fn(async () => ({
        id: "8003",
        entryId: "3001",
        storageObjectId: "7003"
    })),
    update: vi.fn(),
    createRefinementBatch: vi.fn(async () => ({
        batchId: "8801",
        scope: "classics",
        capability: "image_analysis",
        contentType: "SANCAI_ENTRY",
        status: "PENDING",
        totalCount: 1,
        successCount: 0,
        failedCount: 0,
        cancelledCount: 0
    })),
    getRefinementBatch: vi.fn(async () => ({
        batchId: "8801",
        scope: "classics",
        capability: "image_analysis",
        contentType: "SANCAI_ENTRY",
        status: "PENDING",
        totalCount: 1,
        successCount: 0,
        failedCount: 0,
        cancelledCount: 0
    })),
    cancelRefinementBatch: vi.fn(async () => ({
        batchId: "8801",
        scope: "classics",
        capability: "image_analysis",
        contentType: "SANCAI_ENTRY",
        status: "CANCELLED",
        totalCount: 1,
        successCount: 0,
        failedCount: 0,
        cancelledCount: 1
    }))
}));

const LocationProbe = ({ onLocationChange }: { onLocationChange: (location: string) => void }) => {
    const location = useLocation();
    useEffect(() => {
        onLocationChange(`${location.pathname}${location.search}`);
    }, [location.pathname, location.search, onLocationChange]);
    return null;
};

const renderEntryPanel = ({
    exportJobsDrawerOpen = false,
    onLocationChange
}: {
    exportJobsDrawerOpen?: boolean;
    onLocationChange?: (location: string) => void;
} = {}) => {
    const client = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

    render(
        <QueryClientProvider client={client}>
            <AntdApp>
                <MemoryRouter>
                    <SancaiEntryPanel
                        categories={[
                            {
                                categoryType: "FORMAL",
                                id: "2",
                                priority: 10,
                                title: "天文"
                            },
                            {
                                categoryType: "FORMAL",
                                id: "3",
                                priority: 20,
                                title: "地理"
                            }
                        ]}
                        categoryId="2"
                        exportJobsDrawerOpen={exportJobsDrawerOpen}
                        isCatalogLoading={false}
                        refreshVersion={0}
                        volumeId="101"
                        volumes={[
                            {
                                categoryId: "2",
                                id: "101",
                                title: "天文卷一",
                                volumeType: "MAIN"
                            },
                            {
                                categoryId: "3",
                                id: "202",
                                title: "地理卷一",
                                volumeType: "MAIN"
                            }
                        ]}
                    />
                    {onLocationChange ? (
                        <LocationProbe onLocationChange={onLocationChange} />
                    ) : null}
                </MemoryRouter>
            </AntdApp>
        </QueryClientProvider>
    );

    return client;
};

const openSelectAndChoose = async (label: string, optionText: string) => {
    const select = await screen.findByRole("combobox", { name: label });
    fireEvent.mouseDown(select);
    const options = await screen.findAllByText(optionText);
    await userEvent.click(options.at(-1)!);
};

const switchEntryDrawerSection = async (
    user: ReturnType<typeof userEvent.setup>,
    sectionName: "基础信息" | "标签" | "问答" | "版本"
) => {
    await user.click(
        await screen.findByText(sectionName, {
            selector: ".ant-segmented-item-label"
        })
    );
};

const openImageSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await switchEntryDrawerSection(user, "基础信息");
};

const openTagSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await switchEntryDrawerSection(user, "标签");
};

const openQaSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await switchEntryDrawerSection(user, "问答");
};

const openVersionSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await switchEntryDrawerSection(user, "版本");
};

describe("SancaiEntryPanel batch operations", () => {
    beforeEach(() => {
        replacePermissions([
            "classics:sancai:view",
            "classics:sancai:edit",
            "classics:sharing:edit",
            "classics:content:export",
            "classics:content:edit",
            "ai:refinement:edit"
        ]);
    });

    afterEach(() => {
        cleanup();
        vi.useRealTimers();
        vi.clearAllMocks();
        confirmDangerMock.mockClear();
        entryState.restored = false;
        localStorage.removeItem("kuzhambu.admin.accessToken");
        clearPermissions();
    });

    it("changes selected entries visibility and shows item failures", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        const rowCheckbox = within(entryTable).getAllByRole("checkbox")[1];
        await user.click(rowCheckbox);
        await user.click(screen.getByTestId("classics-sancai-sancai-entry-action-button-4"));

        await waitFor(() => {
            expect(contentService.changeVisibilityBatch).toHaveBeenCalled();
        });
        expect(vi.mocked(contentService.changeVisibilityBatch).mock.calls[0]?.[0]).toEqual({
            contentIds: ["3001"],
            contentType: "SANCAI_ENTRY",
            visibility: "PRIVATE"
        });
        expect(await screen.findByText("批量可见性结果：成功 1，失败 1")).toBeInTheDocument();
        expect(await screen.findByText("SANCAI_ENTRY#3002: PERMISSION_DENIED")).toBeInTheDocument();
    }, 30000);

    it("disables export and visibility controls without content permissions", async () => {
        clearPermissions();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        const readEntryButton = (testId: string) =>
            within(entryTable).getByTestId(testId) as HTMLButtonElement;
        const readButtonByText = (text: string) =>
            [...document.querySelectorAll<HTMLButtonElement>("button")].find(
                (button) => button.textContent?.replace(/\s/g, "") === text
            );

        await waitFor(() => {
            expect(readEntryButton("sancai-entry-3001-export-button")).toBeDisabled();
        });
        expect(readEntryButton("sancai-entry-3001-view-button")).toBeEnabled();
        expect(readEntryButton("sancai-entry-3001-export-button")).toBeDisabled();
        expect(readEntryButton("sancai-entry-3001-visual-button")).toBeDisabled();
        expect(readEntryButton("sancai-entry-3001-lifecycle-button")).toBeDisabled();
        expect(readEntryButton("sancai-entry-3002-lifecycle-button")).toBeDisabled();
        expect(readEntryButton("sancai-entry-3003-lifecycle-button")).toBeDisabled();
        expect(readButtonByText("公开")).toBeDisabled();
        expect(readButtonByText("私有")).toBeDisabled();
        expect(readButtonByText("候选治理")).toBeDisabled();
    });

    it("opens batch candidate governance drawer from selected entries", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        const rowCheckbox = within(entryTable).getAllByRole("checkbox")[1];
        await user.click(rowCheckbox);
        await user.click(screen.getByTestId("classics-sancai-sancai-entry-action-button-5"));

        expect(await screen.findByText("AI 候选批量治理")).toBeInTheDocument();
        expect(await screen.findByText("暂无待处理候选")).toBeInTheDocument();
    }, 30000);

    it("opens the visual page from an entry action with the entry id", async () => {
        const user = userEvent.setup();
        const visitedLocations: string[] = [];

        renderEntryPanel({
            onLocationChange: (location) => visitedLocations.push(location)
        });

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-visual-button"));

        await waitFor(() => {
            expect(visitedLocations.at(-1)).toBe("/classics/sancai/visual?entryId=3001");
        });
    }, 30000);

    it("renders lifecycle controls by entry status", async () => {
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        expect(
            await within(entryTable).findByTestId("sancai-entry-3002-lifecycle-button")
        ).toBeEnabled();
        expect(within(entryTable).getByTestId("sancai-entry-3001-lifecycle-button")).toBeEnabled();
        expect(within(entryTable).getByTestId("sancai-entry-3003-lifecycle-button")).toBeEnabled();
        expect(
            [...entryTable.querySelectorAll<HTMLButtonElement>("button[aria-label$=' 天地']")]
                .map((button) => button.getAttribute("aria-label"))
                .filter((label) => label !== "拖动 天地")
        ).toEqual(["编辑 天地", "导出 天地", "视觉处理 天地", "下线 天地", "删除 天地"]);
    }, 30000);

    it("moves an edited entry to the selected category volume", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("link", { name: "打开条目 天地" }));
        await screen.findByRole("textbox", { name: "三才图会条目标题" });
        await waitFor(() => {
            expect(screen.getAllByText("天文卷一").length).toBeGreaterThan(1);
        });

        await openSelectAndChoose("三才图会条目门类", "地理");
        vi.mocked(entryService.update).mockClear();
        await user.click(screen.getByTestId("classics-sancai-sancai-entry-create-button"));
        expect(entryService.update).not.toHaveBeenCalled();
        expect(await screen.findByText("请选择卷")).toBeInTheDocument();

        await openSelectAndChoose("三才图会条目卷", "地理卷一");
        await user.click(screen.getByTestId("classics-sancai-sancai-entry-create-button"));

        await waitFor(() => {
            expect(entryService.update).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.update).mock.calls.at(-1)?.at(0)).toEqual({
            id: "3001",
            volumeId: "202",
            title: "天地",
            originalText: "天地玄黄",
            translationText: "译文",
            summary: "天地摘要",
            lifecycleStatus: "PUBLISHED",
            visibility: "PUBLIC",
            translationStatus: "READY",
            imageStatus: "READY",
            visualAssetStatus: "READY",
            refinementStatus: "COMPLETE"
        });
    }, 30000);

    it("publishes a draft entry after confirmation", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(
            await within(entryTable).findByTestId("sancai-entry-3002-lifecycle-button")
        );

        expect(confirmDangerMock).toHaveBeenCalledWith(
            expect.objectContaining({
                title: "发布三才图会条目",
                message: "确认发布 地理？",
                description: "发布后条目进入已发布治理范围，公开或私有仍由可见性字段决定。",
                okText: "发布"
            })
        );
        await waitFor(() => {
            expect(entryService.changeLifecycleStatus).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.changeLifecycleStatus).mock.calls[0]?.[0]).toEqual({
            id: "3002",
            lifecycleStatus: "PUBLISHED"
        });
        expect(await screen.findByText("三才图会条目已发布")).toBeInTheDocument();
    }, 30000);

    it("refreshes the open entry drawer after lifecycle changes", async () => {
        const user = userEvent.setup();
        const client = renderEntryPanel();
        const invalidateSpy = vi.spyOn(client, "invalidateQueries");

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await openVersionSection(user);
        expect(await screen.findByLabelText("三才图会版本面板")).toBeInTheDocument();

        await user.click(within(entryTable).getByTestId("sancai-entry-3001-lifecycle-button"));

        await waitFor(() => {
            expect(entryService.changeLifecycleStatus).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.changeLifecycleStatus).mock.calls[0]?.[0]).toEqual({
            id: "3001",
            lifecycleStatus: "ARCHIVED"
        });
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "detail", "3001"]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "versions", "3001"]
                    })
            )
        ).toBeTruthy();
        expect(await screen.findByText("三才图会条目已下线")).toBeInTheDocument();
    }, 30000);

    it("loads version detail, restores it and refreshes the open drawer", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        await openVersionSection(user);
        expect(await screen.findByLabelText("三才图会版本面板")).toBeInTheDocument();
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-version-history-action-button")
        );
        expect(await screen.findByText("历史：历史天地")).toBeInTheDocument();

        await user.click(
            screen.getByTestId("classics-sancai-sancai-version-history-action-button-2")
        );

        await waitFor(() => {
            expect(entryService.resetVersion).toHaveBeenCalledWith("3001", "9001");
        });
        expect(confirmDangerMock).toHaveBeenCalledWith(
            expect.objectContaining({
                title: "恢复三才图会版本"
            })
        );
        expect(await screen.findAllByText("三才图会版本已恢复")).not.toHaveLength(0);
        expect(await screen.findByDisplayValue("历史天地")).toBeInTheDocument();
    }, 30000);

    it("creates export job and shows download section", async () => {
        const user = userEvent.setup();

        renderEntryPanel({ exportJobsDrawerOpen: true });

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-export-button"));

        await waitFor(() => {
            expect(exportService.create).toHaveBeenCalledWith({
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: JSON.stringify({
                    title: "天地 导出",
                    ids: ["3001"]
                })
            });
        });

        const exportSection = await screen.findByLabelText("任务列表表格");
        expect(
            await within(exportSection).findByTestId("classics-export-job-1001-download-button")
        ).toBeInTheDocument();
    });

    it("creates translate refinement task from the entry detail drawer", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await user.click(await screen.findByTestId("classics-sancai-sancai-entry-ai-button"));
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-create-ai-text-task-button")
        );

        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        });
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toMatchObject({
            capability: "translate",
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            locale: "zh-CN"
        });
        const command = vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0];
        expect(JSON.parse(command?.inputPayloadJson || "{}")).toMatchObject({
            sourceText: "天地玄黄"
        });
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "requestedBy"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "serviceId"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "serviceRole"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "modelId"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "modelName"
        );
    }, 30000);

    it("tracks normalized backend translate refinement task in the AI translation modal", async () => {
        const user = userEvent.setup();
        vi.mocked(aiRefinementTaskService.pageTasks).mockResolvedValueOnce({
            items: [
                {
                    taskId: "8202",
                    status: "RUNNING",
                    capability: "classics_translate",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3001",
                    requestedAt: "2026-06-22T01:00:00.000+08:00"
                }
            ],
            total: 1,
            pageNo: 1,
            pageSize: 20
        });
        vi.mocked(aiRefinementTaskService.getTask).mockResolvedValueOnce({
            taskId: "8202",
            status: "RUNNING",
            capability: "classics_translate",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            requestedAt: "2026-06-22T01:00:00.000+08:00"
        });

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await user.click(await screen.findByTestId("classics-sancai-sancai-entry-ai-button"));

        expect(await screen.findByText("翻译任务：处理中")).toBeInTheDocument();
    }, 30000);

    it("creates summary refinement task from the entry basic information drawer", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-ai-summary-button")
        );
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-create-ai-text-task-button")
        );

        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        });
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toMatchObject({
            capability: "summary",
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            locale: "zh-CN"
        });
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "requestedBy"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "serviceId"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "serviceRole"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "modelId"
        );
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).not.toHaveProperty(
            "modelName"
        );
    }, 30000);

    it("creates summary refinement task from the current unsaved entry draft", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        const originalTextInput = await screen.findByLabelText("三才图会原文");
        await user.clear(originalTextInput);
        await user.type(originalTextInput, "编辑后的天地原文");
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-ai-summary-button")
        );
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-create-ai-text-task-button")
        );

        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        });
        const command = vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0];
        expect(JSON.parse(command?.inputPayloadJson || "{}")).toMatchObject({
            capability: "summary",
            originalText: "编辑后的天地原文",
            translationText: "译文"
        });
        const promptMessages = JSON.parse(command?.promptMessagesJson || "[]");
        expect(JSON.parse(promptMessages[1]?.content || "{}")).toMatchObject({
            originalText: "编辑后的天地原文",
            translationText: "译文"
        });
    }, 30000);

    it("applies loaded summary candidate when adopting AI summary draft", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: "8101",
                capability: "summary",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: null,
                resultFormat: "TEXT",
                resultPayload: "候选摘要",
                status: "PENDING",
                requestedAt: "2026-06-22T01:00:00.000+08:00"
            }
        ]);

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-ai-summary-button")
        );
        expect(await screen.findByDisplayValue("候选摘要")).toBeInTheDocument();
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-apply-ai-text-button")
        );

        await waitFor(() => {
            expect(aiCandidateService.apply).toHaveBeenCalledTimes(1);
        });
        expect(vi.mocked(aiCandidateService.apply).mock.calls[0]?.[0]).toMatchObject({
            candidateId: "8101",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            capability: "summary",
            objectId: null,
            resultFormat: "TEXT",
            resultPayload: "候选摘要",
            changeSummary: "AI 应用：摘要"
        });
    }, 30000);

    it("keeps summary form unchanged when loaded candidate application fails", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: "8102",
                capability: "summary",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: null,
                resultFormat: "TEXT",
                resultPayload: "失败候选摘要",
                status: "PENDING",
                requestedAt: "2026-06-22T01:00:00.000+08:00"
            }
        ]);
        vi.mocked(aiCandidateService.apply).mockRejectedValueOnce(new Error("候选已处理"));

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-ai-summary-button")
        );
        expect(await screen.findByDisplayValue("失败候选摘要")).toBeInTheDocument();
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-apply-ai-text-button")
        );

        await waitFor(() => {
            expect(aiCandidateService.apply).toHaveBeenCalledTimes(1);
        });
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-cancel-ai-text-button")
        );

        expect(await screen.findByLabelText("三才图会摘要")).toHaveValue("天地摘要");
    }, 30000);

    it("disables AI text adoption while summary candidates are loading", async () => {
        const user = userEvent.setup();
        let resolveCandidates: (
            records: Awaited<ReturnType<typeof aiCandidateService.list>>
        ) => void = () => undefined;
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(aiCandidateService.list).mockImplementation(
            () =>
                new Promise((resolve) => {
                    resolveCandidates = resolve;
                })
        );

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-ai-summary-button")
        );

        expect(
            await screen.findByTestId("classics-sancai-sancai-entry-apply-ai-text-button")
        ).toBeDisabled();
        resolveCandidates([]);
    }, 30000);

    it("labels running summary refinement task as summary work", async () => {
        const user = userEvent.setup();
        vi.mocked(aiRefinementTaskService.pageTasks).mockResolvedValueOnce({
            items: [
                {
                    taskId: "8201",
                    status: "RUNNING",
                    capability: "summary",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3001",
                    requestedAt: "2026-06-22T01:00:00.000+08:00"
                }
            ],
            total: 1,
            pageNo: 1,
            pageSize: 20
        });
        vi.mocked(aiRefinementTaskService.getTask).mockResolvedValueOnce({
            taskId: "8201",
            status: "RUNNING",
            capability: "summary",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            requestedAt: "2026-06-22T01:00:00.000+08:00"
        });

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await user.click(
            await screen.findByTestId("classics-sancai-sancai-entry-ai-summary-button")
        );

        expect(await screen.findByText("摘要任务：处理中")).toBeInTheDocument();
        expect(screen.queryByText("摘要任务：摘要中")).not.toBeInTheDocument();
    }, 30000);

    it("shows expired export task as disabled download", async () => {
        vi.mocked(exportService.page).mockResolvedValueOnce({
            pageNo: 1,
            pageSize: 10,
            totalPage: 1,
            count: 1,
            records: [
                {
                    id: "1002",
                    contentType: "SANCAI_ENTRY",
                    exportKind: "CONTENT_DATASET",
                    exportFormat: "HTML",
                    scopeType: "SELECTED_ITEMS",
                    scopeJson: JSON.stringify({
                        title: "天地 导出",
                        ids: ["3001"]
                    }),
                    requestedAt: "2026-06-10T10:00:00.000+08:00",
                    expiresAt: "2026-06-13T10:00:00.000+08:00",
                    status: "COMPLETED",
                    itemCount: 1,
                    assetCount: 0,
                    downloadUrl: "/downloads/1002.zip"
                }
            ],
            totalCount: 1
        });

        renderEntryPanel({ exportJobsDrawerOpen: true });

        const exportSection = await screen.findByLabelText("任务列表表格");
        expect(await within(exportSection).findByText("已过期")).toBeInTheDocument();
        expect(
            await within(exportSection).findByTestId("classics-export-job-1002-download-button")
        ).toBeDisabled();
    });

    it("does not expose static showcase generation from entry actions", async () => {
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        expect(within(entryTable).queryByText("生成静态展示")).not.toBeInTheDocument();
        expect(screen.queryByText("静态展示任务")).not.toBeInTheDocument();
    });

    it("renders tags and qa governance panel in editor", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        await openTagSection(user);
        expect(await screen.findByText("三才图会标签治理")).toBeInTheDocument();
        await openQaSection(user);
        expect(await screen.findByText("三才图会问答对治理")).toBeInTheDocument();
        expect(screen.queryByLabelText("三才图会内容上下文")).not.toBeInTheDocument();
        expect(await screen.findByText("天地为何不变？")).toBeInTheDocument();
    });

    it("renders empty image management state", async () => {
        vi.mocked(entryService.listImages).mockResolvedValueOnce([]);
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        await openImageSection(user);
        const imagePanel = await screen.findByLabelText("三才图会图片管理");
        expect(await within(imagePanel).findByText("暂无图片")).toBeInTheDocument();
    });

    it("uploads image and sorts images with complete ordered ids", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        await openImageSection(user);
        const imagePanel = await screen.findByLabelText("三才图会图片管理");

        const uploadButton = within(imagePanel).getByTestId(
            "classics-sancai-sancai-entry-image-upload-button"
        );
        const uploadInput = uploadButton
            .closest(".ant-upload")
            ?.querySelector('input[type="file"]') as HTMLInputElement;
        await user.upload(
            uploadInput,
            new File(["image-bin"], "new-image.png", { type: "image/png" })
        );

        await waitFor(() => {
            expect(entryService.uploadImage).toHaveBeenCalledWith({
                currentUsed: false,
                entryId: "3001",
                file: expect.any(File),
                imageType: "ORIGINAL",
                title: "new-image.png"
            });
        });

        const sourceRow = within(imagePanel).getByAltText("sancai.png").closest("tr");
        const targetRow = within(imagePanel).getByAltText("生成图").closest("tr");
        expect(sourceRow).not.toBeNull();
        expect(targetRow).not.toBeNull();
        fireEvent.dragStart(sourceRow as HTMLTableRowElement, {
            dataTransfer: { effectAllowed: "", setData: vi.fn() }
        });
        fireEvent.drop(targetRow as HTMLTableRowElement, {
            clientY: 9999,
            dataTransfer: { dropEffect: "" }
        });

        await waitFor(() => {
            expect(vi.mocked(entryService.sortImages).mock.calls.at(-1)?.at(0)).toEqual({
                entryId: "3001",
                orderedIds: ["8002", "8001"],
                sortDirection: "ASC"
            });
        });
    });

    it("renders image preview group thumbnails", async () => {
        localStorage.setItem("kuzhambu.admin.accessToken", "test-token");
        const openSpy = vi.spyOn(window, "open").mockImplementation(() => null);
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        await openImageSection(user);
        const imagePanel = await screen.findByLabelText("三才图会图片管理");
        expect(within(imagePanel).getByAltText("生成图")).toHaveAttribute(
            "src",
            "/kuzhambu-admin-api/api/classics/sancai/assets/images/3001/8002/content?token=test-token"
        );
        expect(within(imagePanel).getByAltText("sancai.png")).toHaveAttribute(
            "src",
            "/kuzhambu-admin-api/api/classics/sancai/assets/images/3001/8001/content?token=test-token"
        );
        await user.click(within(imagePanel).getByLabelText("下载 sancai.png"));
        expect(openSpy).toHaveBeenCalledWith(
            "/kuzhambu-admin-api/api/classics/sancai/assets/images/3001/8001/content?download=true&token=test-token",
            "_blank",
            "noopener,noreferrer"
        );
    });

    it("previews current visual asset without opening the visual section", async () => {
        const user = userEvent.setup();
        const createObjectURL = vi.fn((object: Blob | MediaSource) => {
            void object;
            return "blob:sancai-entry-preview";
        });
        const revokeObjectURL = vi.fn();
        const openWindow = vi.spyOn(window, "open").mockImplementation(() => null);
        const originalCreateObjectURL = URL.createObjectURL;
        const originalRevokeObjectURL = URL.revokeObjectURL;
        Object.defineProperty(URL, "createObjectURL", {
            configurable: true,
            value: createObjectURL
        });
        Object.defineProperty(URL, "revokeObjectURL", {
            configurable: true,
            value: revokeObjectURL
        });

        try {
            renderEntryPanel();

            const entryTable = await screen.findByLabelText("三才图会条目表格");
            await user.click(
                await within(entryTable).findByTestId("sancai-entry-3001-view-button")
            );
            await waitFor(() => {
                expect(visualPreviewService.listVisualAssets).toHaveBeenCalled();
            });
            await user.click(
                await screen.findByTestId(
                    "classics-sancai-sancai-entry-preview-sancai-entry-button"
                )
            );

            await waitFor(() => {
                expect(createObjectURL).toHaveBeenCalled();
            });
            const previewBlob = createObjectURL.mock.calls[0]?.[0];
            if (!(previewBlob instanceof Blob)) {
                throw new Error("Expected preview object URL payload to be a Blob");
            }
            await expect(previewBlob.text()).resolves.toContain("历史记录 2");
            await expect(previewBlob.text()).resolves.toContain("当前版本视觉描述");
            await expect(previewBlob.text()).resolves.toContain(
                "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content"
            );
            expect(openWindow).toHaveBeenCalledWith(
                "blob:sancai-entry-preview",
                "_blank",
                "noopener,noreferrer"
            );
        } finally {
            Object.defineProperty(URL, "createObjectURL", {
                configurable: true,
                value: originalCreateObjectURL
            });
            Object.defineProperty(URL, "revokeObjectURL", {
                configurable: true,
                value: originalRevokeObjectURL
            });
            openWindow.mockRestore();
        }
    });
});
