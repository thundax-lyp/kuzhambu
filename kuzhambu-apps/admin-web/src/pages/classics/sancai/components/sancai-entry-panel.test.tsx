import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import * as shareService from "@/api/classics/share-service";
import { SancaiEntryPanel } from "./sancai-entry-panel";

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
            refinementStatus: "COMPLETE"
        }
    ]),
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
});
