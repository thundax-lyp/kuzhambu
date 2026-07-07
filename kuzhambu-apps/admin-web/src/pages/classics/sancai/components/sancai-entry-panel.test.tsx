import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
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
import * as entryService from "../services/sancai-entry-service";

const confirmDangerMock = vi.hoisted(() =>
    vi.fn((options: { onConfirm: () => unknown }) => options.onConfirm())
);

vi.mock("@/pages/classics/common/classics-content-service", () => ({
    addQaPair: vi.fn(),
    addTag: vi.fn(),
    changeVisibilityBatch: vi.fn(async () => ({
        failureCount: 1,
        failures: [
            {
                contentId: 3002,
                contentType: "SANCAI_ENTRY",
                failureCode: "PERMISSION_DENIED",
                failureReason: "PERMISSION_DENIED",
                status: "FAILED"
            }
        ],
        successCount: 1,
        successes: [
            {
                contentId: 3001,
                contentType: "SANCAI_ENTRY",
                resultId: 3001,
                status: "PRIVATE"
            }
        ]
    })),
    listQaPairs: vi.fn(async () => [
        {
            id: 6001,
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            qaPairId: 7001,
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
            id: 5001,
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            tagId: 8001,
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
        id: 99,
        loginName: "admin",
        name: "Admin"
    }))
}));

vi.mock("@/pages/classics/common/ai-refinement-task-service", () => ({
    createTask: vi.fn(
        async (command: { capability: string; contentId: number; contentType: string }) => ({
            taskId: 7001,
            status: "PENDING",
            capability: command.capability,
            contentType: command.contentType,
            contentId: command.contentId
        })
    ),
    getTask: vi.fn(async ({ taskId }: { taskId: number }) => ({
        taskId,
        status: "RUNNING",
        capability: "image_analysis",
        contentType: "SANCAI_ENTRY",
        contentId: 3001,
        objectId: 5002,
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
    getTaskCapabilityLabel: vi.fn((capability: string) => capability),
    getTaskFailureText: vi.fn(
        (failureStage?: string | null, errorType?: string | null, errorMessage?: string | null) =>
            [failureStage, errorType, errorMessage].filter(Boolean).join(" / ") || null
    ),
    getTaskRetryable: vi.fn(
        (status: string, capability: string) =>
            ["FAILED", "PARTIAL", "CANCELLED"].includes(status) &&
            ["translate", "summary", "image_analysis", "fusion", "visual", "image_gen"].includes(
                capability
            )
    )
}));
vi.mock("@/pages/classics/common/ai-candidate-service", () => ({
    list: vi.fn(async () => []),
    updateCandidateApplied: vi.fn(async () => ({
        contentType: "SANCAI_ENTRY",
        contentId: 3001,
        versionId: 5002,
        versionNo: 2
    })),
    updateCandidateRejected: vi.fn(async () => ({}))
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
                contentId: 3002,
                contentType: "SANCAI_ENTRY",
                failureCode: "PERMISSION_DENIED",
                failureReason: "PERMISSION_DENIED",
                status: "FAILED"
            }
        ],
        successCount: 1,
        successes: [
            {
                contentId: 3001,
                contentType: "SANCAI_ENTRY",
                resultId: 9001,
                status: "ACTIVE"
            }
        ]
    })),
    create: vi.fn(async () => ({
        id: 9001,
        shareToken: "abc123_-",
        shareUrl: "http://localhost:5174/share/abc123_-",
        title: "天地 分享",
        visibility: "PUBLIC"
    }))
}));

vi.mock("@/pages/classics/common/classics-export-service", () => ({
    create: vi.fn(async () => ({
        id: 1001,
        contentType: "SANCAI_ENTRY",
        exportKind: "CONTENT_DATASET",
        exportFormat: "HTML",
        scopeType: "SELECTED_ITEMS",
        scopeJson: JSON.stringify({
            title: "天地 导出",
            ids: [3001]
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
                id: 1001,
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: JSON.stringify({
                    title: "天地 导出",
                    ids: [3001]
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
    getContentUrl: vi.fn()
}));

vi.mock("../services/sancai-entry-service", () => ({
    add: vi.fn(),
    deleteById: vi.fn(),
    get: vi.fn(async () => ({
        id: 3001,
        volumeId: 101,
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
        id: 9001,
        contentType: "SANCAI_ENTRY",
        contentId: 3001,
        versionNo: 1,
        versionedAt: "2026-06-20T01:00:00.000+08:00",
        snapshotJson: JSON.stringify({
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            volumeId: 101,
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
        },
        {
            id: 3002,
            volumeId: 101,
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
            id: 3003,
            volumeId: 101,
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
            currentVersionId: 9003,
            currentVersionNo: 1,
            currentVersionedAt: "2026-06-20T01:00:00.000+08:00",
            contentUpdatedAt: "2026-06-20T01:00:00.000+08:00",
            versionDirty: false
        }
    ]),
    listImages: vi.fn(async () => [
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
        },
        {
            currentUsed: false,
            entryId: 3001,
            id: 8002,
            imageType: "GENERATED",
            originalFilename: "generated.png",
            priority: 2,
            size: 2048,
            storageObjectId: 7002,
            title: "生成图"
        }
    ]),
    deleteImage: vi.fn(async () => true),
    changeLifecycleStatus: vi.fn(async () => true),
    changeCurrentImage: vi.fn(async () => true),
    sortImages: vi.fn(async () => true),
    listVisualAssets: vi.fn(async () => [
        {
            id: 5002,
            visualAssetId: 5002,
            entryId: 3001,
            versionNo: 2,
            status: "READY",
            sourceImageStorageObjectId: 7001,
            generatedImageStorageObjectId: 7002,
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
            id: 5001,
            visualAssetId: 5001,
            entryId: 3001,
            versionNo: 1,
            status: "READY",
            sourceImageStorageObjectId: 7101,
            generatedImageStorageObjectId: 7102,
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
            id: 9001,
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
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
            id: 9002,
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            versionNo: 2,
            changeType: "HISTORY_RESTORED",
            changeSummary: "恢复历史版本 v1"
        };
    }),
    sort: vi.fn(),
    updateVisualAsset: vi.fn(async (request) => request),
    changeCurrentVisualAsset: vi.fn(async () => true),
    getImageContentUrl: vi.fn(
        (request: { entryId: number; imageId: number; mode?: "download" | "preview" }) => {
            const search = request.mode === "download" ? "?download=true" : "";
            return `/kuzhambu-admin-api/api/classics/sancai/assets/images/${request.entryId}/${request.imageId}/content${search}`;
        }
    ),
    getVisualAssetContentUrl: vi.fn(
        (request: {
            entryId: number;
            visualAssetId: number;
            variant: "source" | "generated";
            mode?: "download" | "preview";
        }) => {
            const search = request.mode === "download" ? "?download=true" : "";
            const suffix = request.variant === "source" ? "source-content" : "generated-content";
            return `/kuzhambu-admin-api/api/classics/sancai/assets/visual-assets/${request.entryId}/${request.visualAssetId}/${suffix}${search}`;
        }
    ),
    uploadImage: vi.fn(async () => ({
        id: 8003,
        entryId: 3001,
        storageObjectId: 7003
    })),
    update: vi.fn(),
    createRefinementBatch: vi.fn(async () => ({
        batchId: 8801,
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
        batchId: 8801,
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
        batchId: 8801,
        scope: "classics",
        capability: "image_analysis",
        contentType: "SANCAI_ENTRY",
        status: "CANCELLED",
        totalCount: 1,
        successCount: 0,
        failedCount: 0,
        cancelledCount: 1
    })),
    requestShowcase: vi.fn(async () => ({
        id: 2001,
        status: "REQUESTED"
    })),
    pageShowcases: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 10,
        totalPage: 1,
        count: 1,
        records: [
            {
                id: 2001,
                requestedAt: "2026-06-21T10:30:00.000+08:00",
                status: "COMPLETED",
                entryCount: 1,
                visibilityRiskStatus: "PUBLIC_ONLY",
                downloadUrl: "/downloads/showcase.html"
            }
        ],
        totalCount: 1
    }))
}));

const renderEntryPanel = () => {
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
                    categoryId={2}
                    isCatalogLoading={false}
                    refreshVersion={0}
                    volumeId={101}
                    volumes={[
                        {
                            categoryId: 2,
                            id: 101,
                            title: "天文卷一",
                            volumeType: "MAIN"
                        }
                    ]}
                />
            </AntdApp>
        </QueryClientProvider>
    );

    return client;
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
        vi.clearAllMocks();
        confirmDangerMock.mockClear();
        entryState.restored = false;
        clearPermissions();
    });

    it("creates a public share from an entry reference", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "分享 天地" }));

        await waitFor(() => {
            expect(shareService.create).toHaveBeenCalled();
        });
        expect(vi.mocked(shareService.create).mock.calls[0]?.[0]).toEqual({
            targets: [
                {
                    contentId: 3001,
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
        await user.click(screen.getByRole("button", { name: "批量分享" }));

        await waitFor(() => {
            expect(shareService.createBatch).toHaveBeenCalled();
        });
        expect(vi.mocked(shareService.createBatch).mock.calls[0]?.[0]).toEqual({
            privateContentConfirmed: false,
            status: "ACTIVE",
            targets: [
                {
                    contentId: 3001,
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
        await user.click(screen.getByRole("button", { name: "批量私有" }));

        await waitFor(() => {
            expect(contentService.changeVisibilityBatch).toHaveBeenCalled();
        });
        expect(vi.mocked(contentService.changeVisibilityBatch).mock.calls[0]?.[0]).toEqual({
            contentIds: [3001],
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
        expect(await within(entryTable).findByText("天地")).toBeInTheDocument();
        expect(within(entryTable).getByRole("button", { name: "分享 天地" })).toBeDisabled();
        expect(within(entryTable).getByRole("button", { name: "导出 天地" })).toBeDisabled();
        expect(within(entryTable).getByRole("button", { name: "归档 天地" })).toBeDisabled();
        expect(within(entryTable).getByRole("button", { name: "发布 地理" })).toBeDisabled();
        expect(within(entryTable).getByRole("button", { name: "恢复发布 人物" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "批量分享" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "批量公开" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "批量私有" })).toBeDisabled();
    }, 90000);

    it("renders lifecycle controls by entry status", async () => {
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        expect(await within(entryTable).findByRole("button", { name: "发布 地理" })).toBeEnabled();
        expect(within(entryTable).getByRole("button", { name: "归档 天地" })).toBeEnabled();
        expect(within(entryTable).getByRole("button", { name: "恢复发布 人物" })).toBeEnabled();
    }, 30000);

    it("publishes a draft entry after confirmation", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "发布 地理" }));

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
            id: 3002,
            lifecycleStatus: "PUBLISHED"
        });
        expect(await screen.findByText("三才图会条目已发布")).toBeInTheDocument();
    }, 30000);

    it("refreshes the open entry drawer after lifecycle changes", async () => {
        const user = userEvent.setup();
        const client = renderEntryPanel();
        const invalidateSpy = vi.spyOn(client, "invalidateQueries");

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));
        expect(await screen.findByLabelText("三才图会版本历史面板")).toBeInTheDocument();

        await user.click(within(entryTable).getByRole("button", { name: "归档 天地" }));

        await waitFor(() => {
            expect(entryService.changeLifecycleStatus).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.changeLifecycleStatus).mock.calls[0]?.[0]).toEqual({
            id: 3001,
            lifecycleStatus: "ARCHIVED"
        });
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "detail", 3001]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "versions", 3001]
                    })
            )
        ).toBeTruthy();
        expect(await screen.findByText("三才图会条目已归档")).toBeInTheDocument();
    }, 30000);

    it("creates image analysis task from selected visual asset and carries visual asset objectId", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        await user.click(
            within(visualAssetPanel).getByRole("button", { name: "创建图片理解任务" })
        );

        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        });
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toMatchObject({
            capability: "image_analysis",
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            objectId: 5002,
            requestedBy: 99,
            serviceRole: "PRIMARY",
            modelId: 1,
            modelName: "gpt-5.5",
            locale: "zh-CN"
        });
    }, 30000);

    it("shows AI stream panel after creating image analysis task", async () => {
        const user = userEvent.setup();
        vi.mocked(aiRefinementTaskService.requestTaskStream).mockImplementationOnce(
            async ({ onEvent }) => {
                await Promise.resolve();
                onEvent({
                    eventType: "delta",
                    deltaText: "流式片段"
                });
            }
        );

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        await user.click(
            within(visualAssetPanel).getByRole("button", { name: "创建图片理解任务" })
        );

        const streamPanel = await screen.findByLabelText("三才图会 AI 流式过程");
        expect(streamPanel).toBeInTheDocument();
        expect(within(streamPanel).getByText(/流式片段/)).toBeInTheDocument();
        expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalledWith(
            expect.objectContaining({
                taskId: 7001
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
            taskId: 7001,
            status: "FAILED",
            capability: "image_analysis",
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            objectId: 5002,
            streamEnabled: true,
            failureStage: "WORKER_STREAM",
            errorType: "WORKER_PROTOCOL_FAILURE",
            errorMessage: "bad stream",
            requestId: "req-stream-1",
            traceId: "trace-stream-1"
        });

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        await user.click(
            within(visualAssetPanel).getByRole("button", { name: "创建图片理解任务" })
        );

        expect(
            await screen.findByText("WORKER_STREAM / WORKER_PROTOCOL_FAILURE / bad stream")
        ).toBeInTheDocument();
        expect(aiRefinementTaskService.requestTaskStream).toHaveBeenCalledWith(
            expect.objectContaining({
                taskId: 7001
            })
        );
    }, 30000);

    it("filters image analysis candidates by selected visual asset", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockClear();
        vi.mocked(aiCandidateService.list)
            .mockResolvedValueOnce([
                {
                    candidateId: 8001,
                    capability: "image_analysis",
                    contentType: "SANCAI_ENTRY",
                    contentId: 3001,
                    objectId: 5002,
                    resultFormat: "TEXT",
                    resultPayload: "候选 A",
                    status: "PENDING",
                    requestedAt: "2026-06-20T01:00:00.000+08:00"
                }
            ])
            .mockResolvedValueOnce([
                {
                    candidateId: 8002,
                    capability: "image_analysis",
                    contentType: "SANCAI_ENTRY",
                    contentId: 3001,
                    objectId: 5001,
                    resultFormat: "TEXT",
                    resultPayload: "候选 B",
                    status: "PENDING",
                    requestedAt: "2026-06-20T01:00:00.000+08:00"
                }
            ]);

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.list)).toHaveBeenCalledTimes(1);
        });
        expect(vi.mocked(aiCandidateService.list).mock.calls[0]?.[0]).toMatchObject({
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            status: "PENDING",
            objectId: 5002
        });

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        await user.click(within(visualAssetPanel).getByRole("button", { name: "版本 1" }));

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.list)).toHaveBeenCalledTimes(2);
        });
        expect(vi.mocked(aiCandidateService.list).mock.calls[1]?.[0]).toMatchObject({
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            status: "PENDING",
            objectId: 5001
        });
    }, 30000);

    it("keeps visual asset candidate panel scoped by capability and visual objectId", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: 8005,
                capability: "image_analysis",
                contentType: "SANCAI_ENTRY",
                contentId: 3001,
                objectId: 5002,
                resultFormat: "TEXT",
                resultPayload: "候选 画像",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            },
            {
                candidateId: 8006,
                capability: "summary",
                contentType: "SANCAI_ENTRY",
                contentId: 3001,
                objectId: 5002,
                resultFormat: "STRUCTURED",
                resultPayload: "候选 摘要",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            },
            {
                candidateId: 8007,
                capability: "visual",
                contentType: "SANCAI_ENTRY",
                contentId: 3001,
                objectId: 5001,
                resultFormat: "TEXT",
                resultPayload: "历史 画像",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            }
        ]);

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

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
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: 8003,
                capability: "image_analysis",
                contentType: "SANCAI_ENTRY",
                contentId: 3001,
                objectId: 5002,
                resultFormat: "TEXT",
                resultPayload: "候选 C",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            }
        ]);

        const client = renderEntryPanel();
        const invalidateSpy = vi.spyOn(client, "invalidateQueries");

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        await waitFor(() => {
            expect(screen.queryByText("AI 候选确认")).toBeInTheDocument();
        });
        const applyButton = await screen.findByRole("button", {
            name: (value) => value.replace(/\s/g, "") === "应用"
        });
        expect(applyButton).toBeEnabled();
        await user.click(applyButton);

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.updateCandidateApplied)).toHaveBeenCalledTimes(1);
        });
        expect(
            vi.mocked(aiCandidateService.updateCandidateApplied).mock.calls[0]?.[0]
        ).toMatchObject({
            candidateId: 8003,
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            capability: "image_analysis",
            objectId: 5002,
            resultFormat: "TEXT",
            resultPayload: "候选 C",
            changeSummary: "AI 应用：image_analysis"
        });

        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "detail", 3001]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "visual-assets", 3001]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["ai", "candidates", "SANCAI_ENTRY", 3001, 5002]
                    })
            )
        ).toBeTruthy();
    }, 30000);

    it("refreshes entry detail, visual assets, and candidate list after rejecting image analysis", async () => {
        const user = userEvent.setup();
        vi.mocked(aiCandidateService.list).mockResolvedValue([
            {
                candidateId: 8004,
                capability: "image_analysis",
                contentType: "SANCAI_ENTRY",
                contentId: 3001,
                objectId: 5002,
                resultFormat: "TEXT",
                resultPayload: "候选 D",
                status: "PENDING",
                requestedAt: "2026-06-20T01:00:00.000+08:00"
            }
        ]);

        const client = renderEntryPanel();
        const invalidateSpy = vi.spyOn(client, "invalidateQueries");

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        await waitFor(() => {
            expect(screen.queryByText("AI 候选确认")).toBeInTheDocument();
        });

        const rejectButton = await screen.findByRole("button", {
            name: (value) => value.replace(/\s/g, "") === "拒绝"
        });
        await user.click(rejectButton);

        await waitFor(() => {
            expect(vi.mocked(aiCandidateService.updateCandidateRejected)).toHaveBeenCalledTimes(1);
        });
        expect(
            vi.mocked(aiCandidateService.updateCandidateRejected).mock.calls[0]?.[0]
        ).toMatchObject({
            candidateId: 8004,
            errorType: "USER_REJECTED",
            errorMessage: "用户已拒绝该 AI 候选"
        });

        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "detail", 3001]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["classics", "sancai", "entries", "visual-assets", 3001]
                    })
            )
        ).toBeTruthy();
        expect(
            invalidateSpy.mock.calls.some(
                (call) =>
                    JSON.stringify(call[0]) ===
                    JSON.stringify({
                        queryKey: ["ai", "candidates", "SANCAI_ENTRY", 3001, 5002]
                    })
            )
        ).toBeTruthy();
    }, 30000);

    it("blocks image analysis task creation when visual asset has no source image", async () => {
        vi.mocked(entryService.listVisualAssets).mockResolvedValueOnce([
            {
                id: 6002,
                visualAssetId: 6002,
                entryId: 3001,
                versionNo: 1,
                status: "READY",
                sourceImageStorageObjectId: null,
                generatedImageStorageObjectId: 7102,
                currentUsed: true,
                textWeight: 55,
                imageWeight: 45,
                imageAnalysisMarkdown: "无图版本图片理解",
                fusionDescription: "无图版本融合描述",
                visualDescription: "无图版本视觉描述",
                generationParamsJson: '{"style":"shuimo"}',
                sourcePreviewUrl: undefined,
                sourceDownloadUrl: undefined,
                generatedPreviewUrl:
                    "/api/classics/sancai/assets/visual-assets/3001/6002/generated-content",
                generatedDownloadUrl:
                    "/api/classics/sancai/assets/visual-assets/3001/6002/generated-content?download=true"
            }
        ]);
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        await user.click(
            within(visualAssetPanel).getByRole("button", { name: "创建图片理解任务" })
        );

        expect(
            await screen.findByText("当前视觉资产缺少原图，无法创建图片相关任务")
        ).toBeInTheDocument();
        expect(aiRefinementTaskService.createTask).not.toHaveBeenCalled();
    }, 30000);

    it("creates batch image analysis task and shows aggregated batch status", async () => {
        const user = userEvent.setup();
        vi.mocked(entryService.createRefinementBatch).mockResolvedValueOnce({
            batchId: 8801,
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
            batchId: 8801,
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
        await user.click(screen.getByRole("button", { name: "批量图片理解" }));

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
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        expect(await screen.findByLabelText("三才图会版本历史面板")).toBeInTheDocument();
        await user.click(await screen.findByRole("button", { name: "查看三才图会版本 1" }));
        expect(await screen.findByText("历史：历史天地")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "恢复三才图会版本 1" }));

        await waitFor(() => {
            expect(entryService.resetVersion).toHaveBeenCalledWith(3001, 9001);
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

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "导出 天地" }));

        await waitFor(() => {
            expect(exportService.create).toHaveBeenCalledWith({
                contentType: "SANCAI_ENTRY",
                exportKind: "CONTENT_DATASET",
                exportFormat: "HTML",
                scopeType: "SELECTED_ITEMS",
                scopeJson: JSON.stringify({
                    title: "天地 导出",
                    ids: [3001]
                })
            });
        });

        expect(await screen.findByText("导出任务")).toBeInTheDocument();
        const exportSection = screen.getByText("导出任务").closest("section") as HTMLElement;
        expect(
            await within(exportSection).findByRole("button", { name: /下\s*载/ })
        ).toBeInTheDocument();
    });

    it("creates translate refinement task from the entry detail drawer", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));
        await user.click(await screen.findByRole("button", { name: "创建译文任务" }));

        await waitFor(() => {
            expect(aiRefinementTaskService.createTask).toHaveBeenCalled();
        });
        expect(vi.mocked(currentUserService.getCurrentUserInfo)).toHaveBeenCalled();
        expect(vi.mocked(aiRefinementTaskService.createTask).mock.calls[0]?.[0]).toMatchObject({
            capability: "translate",
            scope: "classics",
            contentType: "SANCAI_ENTRY",
            contentId: 3001,
            requestedBy: 99,
            serviceRole: "PRIMARY",
            modelId: 1,
            modelName: "gpt-5.5",
            locale: "zh-CN"
        });
    }, 30000);

    it("shows expired export task as disabled download", async () => {
        vi.mocked(exportService.page).mockResolvedValueOnce({
            pageNo: 1,
            pageSize: 10,
            totalPage: 1,
            count: 1,
            records: [
                {
                    id: 1002,
                    contentType: "SANCAI_ENTRY",
                    exportKind: "CONTENT_DATASET",
                    exportFormat: "HTML",
                    scopeType: "SELECTED_ITEMS",
                    scopeJson: JSON.stringify({
                        title: "天地 导出",
                        ids: [3001]
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

        renderEntryPanel();

        const exportSection = (await screen
            .findByText("导出任务")
            .then((node) => node.closest("section"))) as HTMLElement;
        expect(exportSection).toBeTruthy();
        expect(await within(exportSection).findByText("已过期")).toBeInTheDocument();
        expect(
            await within(exportSection).findByRole("button", { name: /下\s*载/ })
        ).toBeDisabled();
    });

    it("creates showcase job and shows showcase task", async () => {
        const user = userEvent.setup();

        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(
            await within(entryTable).findByRole("button", { name: "生成静态展示 天地" })
        );

        await waitFor(() => {
            expect(entryService.requestShowcase).toHaveBeenCalledTimes(1);
        });
        const request = vi.mocked(entryService.requestShowcase).mock.calls.at(-1)?.at(0) || {};
        expect(request).toMatchObject({
            entryCount: 1,
            visibilityRiskStatus: "PUBLIC_ONLY"
        });
        const scopeJson = JSON.parse(request.scopeJson as string);
        expect(scopeJson.title).toContain("静态展示");
        expect(scopeJson.entries).toEqual([
            {
                id: 3001,
                title: "天地",
                volumeId: 101
            }
        ]);
        expect(await screen.findByText("静态展示任务")).toBeInTheDocument();
        const showcaseSection = screen.getByText("静态展示任务").closest("section") as HTMLElement;
        expect(
            await within(showcaseSection).findByRole("button", { name: /下\s*载/ })
        ).toBeInTheDocument();
    });

    it("renders tags and qa governance panel in editor", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        expect(await screen.findByText("三才图会标签治理")).toBeInTheDocument();
        expect(await screen.findByText("三才图会问答对治理")).toBeInTheDocument();
        const contextSection = await screen.findByLabelText("三才图会内容上下文");
        expect(within(contextSection).getByText("三才")).toBeInTheDocument();
        expect(await screen.findByText("天地为何不变？")).toBeInTheDocument();
    });

    it("renders image management controls and supports current switch and delete", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const imagePanel = await screen.findByLabelText("三才图会配图管理");
        expect(within(imagePanel).getByText("sancai.png")).toBeInTheDocument();
        expect(within(imagePanel).getByText("生成图")).toBeInTheDocument();
        expect(within(imagePanel).getAllByRole("button", { name: "预览图片" })).toHaveLength(2);
        expect(within(imagePanel).getAllByRole("button", { name: "下载图片" })).toHaveLength(2);
        expect(
            within(imagePanel).getAllByRole("button", { name: "设为当前使用图片" })
        ).toHaveLength(2);
        expect(within(imagePanel).getAllByRole("button", { name: "删除图片" })).toHaveLength(2);

        const generatedImage = within(imagePanel).getByLabelText("配图 生成图");
        await user.click(within(generatedImage).getByRole("button", { name: "设为当前使用图片" }));
        await waitFor(() => {
            expect(vi.mocked(entryService.changeCurrentImage).mock.calls.at(-1)?.at(0)).toEqual({
                entryId: 3001,
                imageId: 8002
            });
        });

        await user.click(within(generatedImage).getByRole("button", { name: "删除图片" }));
        await waitFor(() => {
            expect(vi.mocked(entryService.deleteImage).mock.calls.at(-1)?.at(0)).toEqual({
                entryId: 3001,
                imageId: 8002
            });
        });
    });

    it("renders empty image management state", async () => {
        vi.mocked(entryService.listImages).mockResolvedValueOnce([]);
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const imagePanel = await screen.findByLabelText("三才图会配图管理");
        expect(await within(imagePanel).findByText("暂无配图")).toBeInTheDocument();
    });

    it("uploads image with current flag and sorts images with complete ordered ids", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const imagePanel = await screen.findByLabelText("三才图会配图管理");
        await user.type(within(imagePanel).getByLabelText("图片标题"), "新增配图");
        await user.selectOptions(within(imagePanel).getByLabelText("图片类型"), "GENERATED");

        const uploadButton = within(imagePanel).getByRole("button", { name: "上传配图" });
        const uploadInput = uploadButton
            .closest(".ant-upload")
            ?.querySelector('input[type="file"]') as HTMLInputElement;
        await user.upload(
            uploadInput,
            new File(["image-bin"], "new-image.png", { type: "image/png" })
        );

        await waitFor(() => {
            expect(entryService.uploadImage).toHaveBeenCalledWith({
                currentUsed: true,
                entryId: 3001,
                file: expect.any(File),
                imageType: "GENERATED",
                title: "新增配图"
            });
        });

        const currentImage = within(imagePanel).getByLabelText("配图 sancai.png");
        await user.click(within(currentImage).getByRole("button", { name: "下移图片" }));

        await waitFor(() => {
            expect(vi.mocked(entryService.sortImages).mock.calls.at(-1)?.at(0)).toEqual({
                entryId: 3001,
                orderedIds: [8002, 8001],
                sortDirection: "ASC"
            });
        });
    });

    it("opens image preview drawer and switches between images", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const imagePanel = await screen.findByLabelText("三才图会配图管理");
        const generatedImage = within(imagePanel).getByLabelText("配图 生成图");
        await user.click(within(generatedImage).getByRole("button", { name: "预览图片" }));

        const previewDrawer = await screen.findByLabelText("配图预览");
        expect(within(previewDrawer).getByAltText("生成图")).toHaveAttribute(
            "src",
            "/kuzhambu-admin-api/api/classics/sancai/assets/images/3001/8002/content"
        );
        expect(within(previewDrawer).getByRole("button", { name: "下一张" })).toBeDisabled();
        expect(within(previewDrawer).getByRole("link", { name: "下载当前图片" })).toHaveAttribute(
            "href",
            "/kuzhambu-admin-api/api/classics/sancai/assets/images/3001/8002/content?download=true"
        );

        await user.click(within(previewDrawer).getByRole("button", { name: "上一张" }));

        expect(within(previewDrawer).getByAltText("sancai.png")).toHaveAttribute(
            "src",
            "/kuzhambu-admin-api/api/classics/sancai/assets/images/3001/8001/content"
        );
    });

    it("disables image preview navigation for a single image", async () => {
        vi.mocked(entryService.listImages).mockResolvedValueOnce([
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
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const imagePanel = await screen.findByLabelText("三才图会配图管理");
        await user.click(within(imagePanel).getByRole("button", { name: "预览图片" }));

        const previewDrawer = await screen.findByLabelText("配图预览");
        expect(within(previewDrawer).getByRole("button", { name: "上一张" })).toBeDisabled();
        expect(within(previewDrawer).getByRole("button", { name: "下一张" })).toBeDisabled();
    });

    it("renders visual asset section and supports switching current version", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        expect(within(visualAssetPanel).getByText(/当前版本：版本 2/)).toBeInTheDocument();
        expect(
            within(visualAssetPanel).getByRole("button", { name: "版本 1" })
        ).toBeInTheDocument();

        await user.click(within(visualAssetPanel).getByRole("button", { name: "版本 1" }));
        await user.click(
            within(visualAssetPanel).getByRole("button", { name: "设为当前使用版本" })
        );

        await waitFor(() => {
            expect(entryService.changeCurrentVisualAsset).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.changeCurrentVisualAsset).mock.calls[0]?.[0]).toEqual({
            entryId: 3001,
            visualAssetId: 5001
        });
    });

    it("renders formal preview and download links for visual asset images", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        const previewSourceLink = within(visualAssetPanel).getByLabelText("预览视觉资产原图");
        const downloadSourceLink = within(visualAssetPanel).getByLabelText("下载视觉资产原图");
        const previewGeneratedLink = within(visualAssetPanel).getByLabelText("预览视觉资产生成图");
        const downloadGeneratedLink = within(visualAssetPanel).getByLabelText("下载视觉资产生成图");

        expect(previewSourceLink).toHaveAttribute(
            "href",
            "/api/classics/sancai/assets/visual-assets/3001/5002/source-content"
        );
        expect(downloadSourceLink).toHaveAttribute(
            "href",
            "/api/classics/sancai/assets/visual-assets/3001/5002/source-content?download=true"
        );
        expect(previewGeneratedLink).toHaveAttribute(
            "href",
            "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content"
        );
        expect(downloadGeneratedLink).toHaveAttribute(
            "href",
            "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content?download=true"
        );
        expect(within(visualAssetPanel).getByAltText("三才图会视觉资产原图")).toHaveAttribute(
            "src",
            "/api/classics/sancai/assets/visual-assets/3001/5002/source-content"
        );
        expect(within(visualAssetPanel).getByAltText("三才图会视觉资产生成图")).toHaveAttribute(
            "src",
            "/api/classics/sancai/assets/visual-assets/3001/5002/generated-content"
        );
    });

    it("saves visual asset editable fields through the formal service contract", async () => {
        const user = userEvent.setup();
        renderEntryPanel();

        const entryTable = await screen.findByLabelText("三才图会条目表格");
        await user.click(await within(entryTable).findByRole("button", { name: "查看 天地" }));

        const visualAssetPanel = await screen.findByLabelText("三才图会视觉资产面板");
        const descriptionInput =
            within(visualAssetPanel).getByLabelText("三才图会视觉资产视觉描述");
        await user.clear(descriptionInput);
        await user.type(descriptionInput, "更新后的视觉描述");
        await user.click(
            within(visualAssetPanel).getByRole("button", { name: "保存视觉资产字段" })
        );

        await waitFor(() => {
            expect(entryService.updateVisualAsset).toHaveBeenCalled();
        });
        expect(vi.mocked(entryService.updateVisualAsset).mock.calls[0]?.[0]).toEqual(
            expect.objectContaining({
                visualAssetId: 5002,
                entryId: 3001,
                visualDescription: "更新后的视觉描述",
                textWeight: 60,
                imageWeight: 40
            })
        );
    });
});
