import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import * as shareService from "@/api/classics/share-service";
import { SancaiEntryPanel } from "./sancai-entry-panel";
import * as entryService from "../services/sancai-entry-service";

const confirmDangerMock = vi.hoisted(() =>
    vi.fn((options: { onConfirm: () => unknown }) => options.onConfirm())
);

const entryState = vi.hoisted(() => ({
    restored: false
}));

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: confirmDangerMock
    })
}));

vi.mock("@/api/classics/share-service", () => ({
    create: vi.fn(async () => ({
        id: 9001,
        shareToken: "abc123_-",
        shareUrl: "http://localhost:5174/share/abc123_-",
        title: "天地 分享",
        visibility: "PUBLIC"
    }))
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
    update: vi.fn()
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
    });

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
    });
});
