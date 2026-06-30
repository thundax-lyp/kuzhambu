import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import * as exportService from "@/pages/classics/common/classics-export-service";
import * as shareService from "@/pages/classics/common/classics-share-service";
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
    }, 20000);

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
        expect(await screen.findByText("三才")).toBeInTheDocument();
        expect(await screen.findByText("天地为何不变？")).toBeInTheDocument();
    });
});
