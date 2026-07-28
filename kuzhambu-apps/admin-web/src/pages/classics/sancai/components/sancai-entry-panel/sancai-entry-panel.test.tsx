import { act, cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as contentService from "@/pages/classics/common/classics-content-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import * as currentUserService from "@/service/current-user-service";
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

vi.mock("@/service/current-user-service", () => ({
    getCurrentUserInfo: vi.fn(async () => ({
        id: "99",
        loginName: "admin",
        name: "Admin"
    }))
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

const entryState = vi.hoisted(() => ({
    restored: false
}));

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: confirmDangerMock
    })
}));

vi.mock("@/pages/classics/common/classics-share-service", () => ({
    createBatch: vi.fn(async () => ({
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
                resultId: "9001",
                status: "ACTIVE"
            }
        ]
    })),
    create: vi.fn(async () => ({
        id: "9001",
        shareToken: "abc123_-",
        shareUrl: "http://localhost:5174/share/abc123_-",
        title: "天地 分享",
        visibility: "PUBLIC"
    }))
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

const renderEntryPanel = ({ exportJobsDrawerOpen = false } = {}) => {
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
    sectionName: "基础信息" | "视觉处理" | "标签" | "问答" | "版本"
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

const openVisualAssetSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await switchEntryDrawerSection(user, "视觉处理");
    return screen.findByLabelText("三才图会视觉处理面板");
};

const openRefinementSection = async (user: ReturnType<typeof userEvent.setup>) => {
    await switchEntryDrawerSection(user, "视觉处理");
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

describe("SancaiEntryPanel sharing", () => {
    beforeEach(() => {
        replacePermissions([
            "classics:sancai:view",
            "classics:sancai:edit",
            "classics:sharing:edit",
            "classics:content:export"
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

    it("creates a public share from an entry reference", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-share-button"));

        await waitFor(() => {
            expect(shareService.create).toHaveBeenCalled();
        });
        expect(vi.mocked(shareService.create).mock.calls[0]?.[0]).toEqual({
            targets: [
                {
                    contentId: "3001",
                    contentType: "SANCAI_ENTRY"
                }
            ],
            title: "天地 分享",
            visibility: "PUBLIC"
        });
    }, 30000);

    it("creates public shares from selected entries and shows item failures", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        const rowCheckbox = within(entryTable).getAllByRole("checkbox")[1];
        await user.click(rowCheckbox);
        await user.click(screen.getByTestId("classics-sancai-sancai-entry-share-button"));

        await waitFor(() => {
            expect(shareService.createBatch).toHaveBeenCalled();
        });
        expect(vi.mocked(shareService.createBatch).mock.calls[0]?.[0]).toEqual({
            privateContentConfirmed: false,
            status: "ACTIVE",
            targets: [
                {
                    contentId: "3001",
                    contentType: "SANCAI_ENTRY"
                }
            ],
            titlePrefix: "三才图会批量分享 - ",
            visibility: "PUBLIC"
        });
        expect(await screen.findByText("批量分享结果：成功 1，失败 1")).toBeInTheDocument();
        expect(await screen.findByText("SANCAI_ENTRY#3002: PERMISSION_DENIED")).toBeInTheDocument();
    }, 30000);

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

    it("disables share export and visibility controls without content permissions", async () => {
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
            expect(readEntryButton("sancai-entry-3001-share-button")).toBeDisabled();
        });
        expect(readEntryButton("sancai-entry-3001-view-button")).toBeEnabled();
        expect(readEntryButton("sancai-entry-3001-export-button")).toBeDisabled();
        expect(readEntryButton("sancai-entry-3001-lifecycle-button")).toBeDisabled();
        expect(readEntryButton("sancai-entry-3002-lifecycle-button")).toBeDisabled();
        expect(readEntryButton("sancai-entry-3003-lifecycle-button")).toBeDisabled();
        expect(readButtonByText("分享")).toBeDisabled();
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
        ).toEqual(["编辑 天地", "分享 天地", "导出 天地", "下线 天地", "删除 天地"]);
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

    it("creates image analysis task from selected visual asset and carries visual asset objectId", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        const visualAssetPanel = await openVisualAssetSection(user);
        fireEvent.click(
            within(visualAssetPanel).getByTestId("classics-sancai-sancai-entry-action-button-3")
        );

        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        });
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toMatchObject({
            capability: "image_analysis",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            objectId: "5002",
            requestedBy: "99",
            locale: "zh-CN"
        });
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

    it("shows AI stream panel after creating image analysis task", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        const visualAssetPanel = await openVisualAssetSection(user);
        fireEvent.click(
            within(visualAssetPanel).getByTestId("classics-sancai-sancai-entry-action-button-3")
        );
        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
            expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalled();
        });

        expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalledWith(
            expect.objectContaining({
                taskId: "7001"
            })
        );
    }, 30000);

    it("shows failed stream task details", async () => {
        const user = userEvent.setup();
        vi.mocked(aiRefinementTaskService.requestTaskStream).mockImplementationOnce(
            async ({ onEvent }) => {
                await Promise.resolve();
                onEvent({
                    eventType: "error",
                    failureStage: "WORKER_STREAM",
                    errorType: "WORKER_PROTOCOL_FAILURE",
                    errorMessage: "bad stream"
                });
            }
        );
        vi.mocked(aiRefinementTaskService.getTask).mockResolvedValue({
            taskId: "7001",
            status: "FAILED",
            capability: "image_analysis",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            objectId: "5002",
            streamEnabled: true,
            failureStage: "WORKER_STREAM",
            errorType: "WORKER_PROTOCOL_FAILURE",
            errorMessage: "bad stream",
            requestId: "req-stream-1",
            traceId: "trace-stream-1"
        });

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        const visualAssetPanel = await openVisualAssetSection(user);
        await user.click(
            within(visualAssetPanel).getByTestId("classics-sancai-sancai-entry-action-button-3")
        );
        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
            expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalled();
        });

        expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalledWith(
            expect.objectContaining({
                taskId: "7001"
            })
        );
        expect(aiRefinementTaskService.getTask).toHaveBeenCalledWith({
            taskId: "7001"
        });
    }, 30000);

    it("preserves edited visual asset draft when the same asset refetches", async () => {
        const user = userEvent.setup();
        const client = renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        const visualAssetPanel = await openVisualAssetSection(user);
        const descriptionInput =
            within(visualAssetPanel).getByLabelText("三才图会视觉处理视觉描述");
        await user.clear(descriptionInput);
        await user.type(descriptionInput, "用户未保存视觉描述");

        vi.mocked(entryService.listVisualAssets).mockResolvedValueOnce([
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
                imageAnalysisMarkdown: "服务端刷新图片理解",
                fusionDescription: "服务端刷新融合描述",
                visualDescription: "服务端刷新视觉描述",
                generationParamsJson: '{"style":"gongbi"}',
                sourcePreviewUrl:
                    "/api/classics/sancai/assets/visual-assets/3001/5002/source-content",
                sourceDownloadUrl:
                    "/api/classics/sancai/assets/visual-assets/3001/5002/source-content?download=true",
                generatedPreviewUrl:
                    "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content",
                generatedDownloadUrl:
                    "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content?download=true"
            }
        ]);
        await client.invalidateQueries({
            queryKey: ["classics", "sancai", "entries", "visual-assets", "3001"]
        });

        await waitFor(() => {
            expect(entryService.listVisualAssets).toHaveBeenCalledTimes(2);
        });
        expect(descriptionInput).toHaveValue("用户未保存视觉描述");
    }, 30000);

    it("does not poll visual refinement tasks when no task is active", async () => {
        vi.useFakeTimers({ shouldAdvanceTime: true });
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await openVisualAssetSection(user);
        await waitFor(() => {
            expect(aiRefinementTaskService.pageTasks).toHaveBeenCalled();
        });
        const callCount = vi.mocked(aiRefinementTaskService.pageTasks).mock.calls.length;

        await act(async () => {
            await vi.advanceTimersByTimeAsync(3000);
        });

        expect(aiRefinementTaskService.pageTasks).toHaveBeenCalledTimes(callCount);
    }, 30000);

    it("resumes active visual task streaming after switching drawer sections", async () => {
        const user = userEvent.setup();
        vi.mocked(aiRefinementTaskService.pageTasks).mockResolvedValue({
            items: [
                {
                    taskId: "7001",
                    status: "RUNNING",
                    capability: "image_analysis",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3001",
                    objectId: "5002",
                    streamEnabled: true,
                    requestId: "req-stream-1",
                    traceId: "trace-stream-1"
                }
            ],
            total: 1,
            pageNo: 1,
            pageSize: 20
        });
        vi.mocked(aiRefinementTaskService.requestTaskStream).mockImplementation(
            async ({ signal }) =>
                new Promise<void>((resolve) => {
                    signal?.addEventListener("abort", () => resolve(), { once: true });
                })
        );

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        await openVisualAssetSection(user);
        await waitFor(() => {
            expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalledTimes(1);
        });
        await switchEntryDrawerSection(user, "基础信息");
        await openVisualAssetSection(user);
        await waitFor(() => {
            expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalledTimes(2);
        });
        expect(aiRefinementTaskService.requestTaskStream).toHaveBeenLastCalledWith(
            expect.objectContaining({
                taskId: "7001"
            })
        );
    }, 30000);

    it("filters image analysis candidates by selected visual asset", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(aiCandidateService.list)
            .mockResolvedValueOnce([
                {
                    candidateId: "8001",
                    capability: "image_analysis",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3001",
                    objectId: "5002",
                    resultFormat: "TEXT",
                    resultPayload: "候选 A",
                    status: "PENDING",
                    requestedAt: "2026-06-20T01:00:00.000+08:00"
                }
            ])
            .mockResolvedValueOnce([
                {
                    candidateId: "8002",
                    capability: "image_analysis",
                    contentType: "SANCAI_ENTRY",
                    contentId: "3001",
                    objectId: "5001",
                    resultFormat: "TEXT",
                    resultPayload: "候选 B",
                    status: "PENDING",
                    requestedAt: "2026-06-20T01:00:00.000+08:00"
                }
            ]);

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await openRefinementSection(user);

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.list)).toHaveBeenCalledTimes(1);
        });
        expect(vi.mocked(aiCandidateService.list).mock.calls[0]?.[0]).toMatchObject({
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            status: "PENDING",
            objectId: "5002"
        });

        const visualAssetPanel = await openVisualAssetSection(user);
        await user.click(
            within(visualAssetPanel).getByTestId("sancai-visual-asset-5001-select-button")
        );
        await openRefinementSection(user);

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.list)).toHaveBeenCalledTimes(2);
        });
        expect(vi.mocked(aiCandidateService.list).mock.calls[1]?.[0]).toMatchObject({
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            status: "PENDING",
            objectId: "5001"
        });
    }, 30000);

    it("keeps visual asset candidate panel scoped by capability and visual objectId", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: "8005",
                capability: "image_analysis",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: "5002",
                resultFormat: "TEXT",
                resultPayload: "候选 画像",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            },
            {
                candidateId: "8006",
                capability: "summary",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: "5002",
                resultFormat: "STRUCTURED",
                resultPayload: "候选 摘要",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            },
            {
                candidateId: "8007",
                capability: "visual",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: "5001",
                resultFormat: "TEXT",
                resultPayload: "历史 画像",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            }
        ]);

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await openRefinementSection(user);

        await waitFor(() => {
            expect(screen.queryByText("能力：image_analysis")).toBeInTheDocument();
        });
        expect(screen.queryByText("能力：summary")).not.toBeInTheDocument();
        expect(screen.queryByText("候选 画像")).toBeInTheDocument();
        expect(screen.queryByText("候选 摘要")).not.toBeInTheDocument();
        expect(screen.queryByText("历史 画像")).not.toBeInTheDocument();
    }, 30000);

    it("refreshes entry detail, visual assets, and candidate list after applying image analysis", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: "8003",
                capability: "image_analysis",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: "5002",
                resultFormat: "TEXT",
                resultPayload: "候选 C",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            }
        ]);

        const client = renderEntryPanel();
        const invalidateSpy = vi.spyOn(client, "invalidateQueries");

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await openRefinementSection(user);

        await waitFor(() => {
            expect(screen.queryByText("AI 候选确认")).toBeInTheDocument();
        });
        const applyButton = await screen.findByTestId("classics-common-ai-candidate-action-button");
        expect(applyButton).toBeEnabled();
        await user.click(applyButton);

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.apply)).toHaveBeenCalledTimes(1);
        });
        expect(vi.mocked(aiCandidateService.apply).mock.calls[0]?.[0]).toMatchObject({
            candidateId: "8003",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            capability: "image_analysis",
            objectId: "5002",
            resultFormat: "TEXT",
            resultPayload: "候选 C",
            changeSummary: "AI 应用：image_analysis"
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
                        queryKey: ["classics", "sancai", "entries", "visual-assets", "3001"]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["ai", "candidates", "SANCAI_ENTRY", "3001", "5002"]
                    })
            )
        ).toBeTruthy();
    }, 30000);

    it("refreshes entry detail, visual assets, and candidate list after rejecting image analysis", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockReset();
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: "8004",
                capability: "image_analysis",
                contentType: "SANCAI_ENTRY",
                contentId: "3001",
                objectId: "5002",
                resultFormat: "TEXT",
                resultPayload: "候选 D",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            }
        ]);

        const client = renderEntryPanel();
        const invalidateSpy = vi.spyOn(client, "invalidateQueries");

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));
        await openRefinementSection(user);

        await waitFor(() => {
            expect(screen.queryByText("AI 候选确认")).toBeInTheDocument();
        });

        const rejectButton = await screen.findByTestId(
            "classics-common-ai-candidate-action-button-2"
        );
        await user.click(rejectButton);

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.reject)).toHaveBeenCalledTimes(1);
        });
        expect(vi.mocked(aiCandidateService.reject).mock.calls[0]?.[0]).toMatchObject({
            candidateId: "8004",
            errorType: "USER_REJECTED",
            errorMessage: "用户已拒绝该 AI 候选"
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
                        queryKey: ["classics", "sancai", "entries", "visual-assets", "3001"]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["ai", "candidates", "SANCAI_ENTRY", "3001", "5002"]
                    })
            )
        ).toBeTruthy();
    }, 30000);

    it("blocks image analysis task creation when visual asset has no source image", async () => {
        vi.mocked(entryService.listImages).mockResolvedValueOnce([]);
        vi.mocked(entryService.listVisualAssets).mockResolvedValueOnce([
            {
                id: "6002",
                visualAssetId: "6002",
                entryId: "3001",
                versionNo: 1,
                status: "READY",
                sourceImageStorageObjectId: null,
                generatedImageStorageObjectId: null,
                currentUsed: true,
                textWeight: 55,
                imageWeight: 45,
                imageAnalysisMarkdown: "无图版本图片理解",
                fusionDescription: "无图版本融合描述",
                visualDescription: "无图版本视觉描述",
                generationParamsJson: '{"style":"shuimo"}',
                sourcePreviewUrl: undefined,
                sourceDownloadUrl: undefined,
                generatedPreviewUrl: undefined,
                generatedDownloadUrl: undefined
            }
        ]);
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        const visualAssetPanel = await openVisualAssetSection(user);
        expect(
            within(visualAssetPanel).getByLabelText("三才图会视觉处理生成图占位")
        ).toBeInTheDocument();
        await user.click(
            within(visualAssetPanel).getByTestId("classics-sancai-sancai-entry-action-button-3")
        );

        expect(
            await screen.findByText("当前视觉处理缺少原图，无法创建图片相关任务")
        ).toBeInTheDocument();
        expect(aiRefinementTaskService.createTask).not.toHaveBeenCalled();
    }, 30000);

    it("creates batch image analysis task and shows aggregated batch status", async () => {
        const user = userEvent.setup();
        vi.mocked(entryService.createRefinementBatch).mockResolvedValueOnce({
            batchId: "8801",
            scope: "classics",
            capability: "image_analysis",
            contentType: "SANCAI_ENTRY",
            status: "PENDING",
            totalCount: 1,
            successCount: 0,
            failedCount: 0,
            cancelledCount: 0
        });
        vi.mocked(entryService.getRefinementBatch).mockResolvedValue({
            batchId: "8801",
            scope: "classics",
            capability: "image_analysis",
            contentType: "SANCAI_ENTRY",
            status: "FAILED",
            totalCount: 1,
            successCount: 0,
            failedCount: 1,
            cancelledCount: 0,
            failureSummaryJson: '[{"contentId":3001,"errorType":"MODEL_TIMEOUT"}]'
        });

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        const rowCheckbox = within(entryTable).getAllByRole("checkbox")[1];
        await user.click(rowCheckbox);
        await user.click(screen.getByTestId("classics-sancai-sancai-entry-action-button"));

        await waitFor(() => {
            expect(entryService.createRefinementBatch).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.createRefinementBatch).mock.calls[0]?.[0]).toEqual({
            scope: "classics",
            capability: "image_analysis",
            contentType: "SANCAI_ENTRY",
            totalCount: 1
        });
        expect(
            await screen.findByText(/批量任务 #8801 \/ image_analysis \/ FAILED/)
        ).toBeInTheDocument();
        expect(screen.getByText("成功 0 / 失败 1 / 取消 0")).toBeInTheDocument();
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
        expect(vi.mocked(currentUserService.getCurrentUserInfo)).toHaveBeenCalled();
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toMatchObject({
            capability: "translate",
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            requestedBy: "99",
            locale: "zh-CN"
        });
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
        expect(vi.mocked(currentUserService.getCurrentUserInfo)).toHaveBeenCalled();
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toMatchObject({
            capability: "summary",
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: "3001",
            requestedBy: "99",
            locale: "zh-CN"
        });
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
            "classics-sancai-sancai-entry-action-button"
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

    it("renders visual asset section and supports switching current version", async () => {
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格", undefined, {
            timeout: 1000
        });
        fireEvent.click(
            await within(entryTable).findByTestId("sancai-entry-3001-view-button", undefined, {
                timeout: 1000
            })
        );

        fireEvent.click(
            await screen.findByText(
                "视觉处理",
                {
                    selector: ".ant-segmented-item-label"
                },
                {
                    timeout: 1000
                }
            )
        );
        const visualAssetPanel = await screen.findByLabelText("三才图会视觉处理面板", undefined, {
            timeout: 1000
        });
        expect(within(visualAssetPanel).getByText(/当前处理：处理记录 2/)).toBeInTheDocument();
        expect(
            within(visualAssetPanel).getByTestId("sancai-visual-asset-5001-select-button")
        ).toBeInTheDocument();
        expect(within(visualAssetPanel).getByAltText("处理记录 1生成图预览")).toBeInTheDocument();
        expect(within(visualAssetPanel).getByAltText("处理记录 2生成图预览")).toBeInTheDocument();
        const sourceImageSelect = within(visualAssetPanel).getByRole("combobox", {
            name: "三才图会视觉处理来源图片"
        });
        expect(sourceImageSelect.closest(".ant-select-content")).toHaveAttribute(
            "title",
            "sancai.png"
        );
        expect(within(visualAssetPanel).getAllByText("已完成").length).toBeGreaterThan(0);

        const currentVersionButton = within(visualAssetPanel).getByTestId(
            "sancai-visual-asset-5001-use-button"
        );
        expect(currentVersionButton).not.toBeDisabled();
        fireEvent.click(currentVersionButton);

        await waitFor(
            () => {
                expect(entryService.changeCurrentVisualAsset).toHaveBeenCalled();
            },
            { timeout: 1000 }
        );
        expect(vi.mocked(entryService.changeCurrentVisualAsset).mock.calls[0]?.[0]).toEqual({
            entryId: "3001",
            visualAssetId: "5001"
        });
    });

    it("renders formal preview images for visual assets", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        const visualAssetPanel = await openVisualAssetSection(user);
        expect(
            within(visualAssetPanel).queryByLabelText("下载视觉处理来源图片")
        ).not.toBeInTheDocument();
        expect(
            within(visualAssetPanel).queryByLabelText("下载视觉处理生成图")
        ).not.toBeInTheDocument();
        expect(within(visualAssetPanel).getByAltText("三才图会视觉处理来源图片")).toHaveAttribute(
            "src",
            "/kuzhambu-admin-api/api/classics/sancai/assets/images/3001/8001/content"
        );
        expect(within(visualAssetPanel).getByAltText("三才图会视觉处理生成图")).toHaveAttribute(
            "src",
            "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content"
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
                expect(entryService.listVisualAssets).toHaveBeenCalled();
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
            await expect(previewBlob.text()).resolves.toContain("处理记录 2");
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

    it("saves visual asset editable fields through the formal service contract", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByTestId("sancai-entry-3001-view-button"));

        const visualAssetPanel = await openVisualAssetSection(user);
        fireEvent.mouseDown(
            within(visualAssetPanel).getByRole("combobox", {
                name: "三才图会视觉处理来源图片"
            })
        );
        await user.click((await screen.findAllByText("生成图")).at(-1)!);
        expect(
            within(visualAssetPanel).queryByLabelText("三才图会视觉处理记录列表")
        ).not.toBeInTheDocument();
        expect(within(visualAssetPanel).getByText("当前来源图片暂无处理记录")).toBeInTheDocument();
        const descriptionInput =
            within(visualAssetPanel).getByLabelText("三才图会视觉处理视觉描述");
        await user.clear(descriptionInput);
        await user.type(descriptionInput, "更新后的视觉描述");
        await user.click(
            within(visualAssetPanel).getByTestId("classics-sancai-sancai-entry-action-button-7")
        );

        await waitFor(() => {
            expect(entryService.updateVisualAsset).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.updateVisualAsset).mock.calls[0]?.[0]).toEqual(
            expect.objectContaining({
                visualAssetId: "5002",
                entryId: "3001",
                visualDescription: "更新后的视觉描述",
                textWeight: 60,
                imageWeight: 40,
                sourceImageStorageObjectId: "7002"
            })
        );
    });
});
