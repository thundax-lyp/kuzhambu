import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import * as aiRefinementTaskService from "@/pages/classics/common/ai-refinement-task-service";
import * as aiCandidateService from "@/pages/classics/common/ai-candidate-service";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
import * as currentUserService from "@/service/current-user-service";
import { SancaiEntryPanel } from "./sancai-entry-panel";
import * as entryService from "../services/sancai-entry-service";

const confirmDangerMock = vi.hoisted(() =>
    vi.fn((options: { onConfirm: () => unknown }) => options.onConfirm())
);

vi.mock("@/pages/classics/common/classics-content-service", () => ({
    addQaPair: vi.fn(),
    addTag: vi.fn(),
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
    createTask: vi.fn(async () => ({
        taskId: 7001,
        status: "PENDING",
        capability: "translate",
        contentType: "SANCAI_ENTRY",
        contentId: 3001
    })),
    getTask: vi.fn(),
    pageTasks: vi.fn(async () => ({
        items: [],
        total: 0,
        pageNo: 1,
        pageSize: 20
    })),
    cancelTask: vi.fn()
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
        }
    ]),
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
            sourcePreviewUrl: "/api/storage/object/7001/content",
            sourceDownloadUrl: "/api/storage/object/7001/content?download=true",
            generatedPreviewUrl: "/api/storage/object/7002/content",
            generatedDownloadUrl: "/api/storage/object/7002/content?download=true"
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
            sourcePreviewUrl: "/api/storage/object/7101/content",
            sourceDownloadUrl: "/api/storage/object/7101/content?download=true",
            generatedPreviewUrl: "/api/storage/object/7102/content",
            generatedDownloadUrl: "/api/storage/object/7102/content?download=true"
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
    uploadImage: vi.fn(),
    update: vi.fn(),
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
    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
        confirmDangerMock.mockClear();
        entryState.restored = false;
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
                generatedPreviewUrl: "/api/storage/object/7102/content",
                generatedDownloadUrl: "/api/storage/object/7102/content?download=true"
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
            await screen.findByText("当前视觉资产缺少原图，无法创建图片理解任务")
        ).toBeInTheDocument();
        expect(aiRefinementTaskService.createTask).not.toHaveBeenCalled();
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
