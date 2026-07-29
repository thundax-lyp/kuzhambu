import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { MemoryRouter } from "react-router-dom";
import { SancaiVisualPage } from "./sancai-visual-page";
import * as entryService from "./sancai-visual-service";
import type { SancaiEntryRecord } from "./sancai-visual-types";

const { mockVisualSection } = vi.hoisted(() => ({
    mockVisualSection: vi.fn(({ entry }: { entry: SancaiEntryRecord }) => (
        <section aria-label="视觉处理组件">{`visual:${entry.id}:${entry.title ?? ""}`}</section>
    ))
}));

vi.mock("./sancai-entry-visual-section", () => ({
    SancaiEntryVisualSection: mockVisualSection
}));

vi.mock("../sancai/sancai-category-service", () => ({
    list: vi.fn(async () => [{ categoryType: "FORMAL", id: "2", title: "天文" }])
}));

vi.mock("./sancai-visual-service", () => ({
    changeCurrentVisualAsset: vi.fn(),
    get: vi.fn(async (id: string) => ({
        id,
        title: id === "3002" ? "山川" : "天地",
        originalText: id === "3002" ? "山川原文" : "天地原文",
        lifecycleStatus: "PUBLISHED"
    })),
    list: vi.fn(async () => [
        {
            id: "3001",
            title: "天地",
            originalText: "天地原文",
            lifecycleStatus: "PUBLISHED"
        },
        {
            id: "3002",
            title: "山川",
            originalText: "山川原文",
            lifecycleStatus: "DRAFT"
        }
    ]),
    updateVisualAsset: vi.fn()
}));

vi.mock("../sancai/sancai-volume-service", () => ({
    list: vi.fn(async () => [{ categoryId: "2", id: "101", title: "天文卷一", volumeType: "MAIN" }])
}));

const renderVisualPage = (initialPath = "/classics/sancai/visual?entryId=3001") => {
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
                <MemoryRouter initialEntries={[initialPath]}>
                    <SancaiVisualPage />
                </MemoryRouter>
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("SancaiVisualPage", () => {
    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("loads the selected entry from the url and renders the visual workbench", async () => {
        renderVisualPage();

        expect(
            await screen.findByRole("heading", { name: /三才图会\s*视觉处理/ })
        ).toBeInTheDocument();
        expect(await screen.findByText("visual:3001:天地")).toBeInTheDocument();
        expect(
            screen.getByTestId("classics-sancai-visual-entry-context-switch-button")
        ).toBeInTheDocument();
        expect(entryService.get).toHaveBeenCalledWith("3001");
    });

    it("changes the selected entry from the entry picker", async () => {
        const user = userEvent.setup();
        renderVisualPage();

        await user.click(
            await screen.findByTestId("classics-sancai-visual-entry-context-switch-button")
        );
        const entryPicker = await screen.findByLabelText("三才图会视觉处理稿件选择");
        await user.click(await within(entryPicker).findByText("天文"));
        await user.click(await within(entryPicker).findByText("天文卷一"));

        await user.click(await within(entryPicker).findByText("山川"));
        await user.click(
            await screen.findByTestId("classics-sancai-visual-entry-picker-confirm-button")
        );

        await waitFor(() => {
            expect(screen.getByText("visual:3002:山川")).toBeInTheDocument();
        });
        expect(entryService.get).toHaveBeenCalledWith("3002");
        expect(entryService.list).toHaveBeenCalledWith(
            expect.objectContaining({
                categoryId: "2",
                sortDirection: "ASC",
                volumeId: "101"
            })
        );
    });
});
