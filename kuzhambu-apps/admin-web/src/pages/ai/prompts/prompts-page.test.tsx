import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { PromptsPage } from "./prompts-page";
import * as service from "./prompts-service";

vi.mock("./prompts-service", () => ({
    changePromptTemplate: vi.fn(),
    changePromptVersionRollback: vi.fn(),
    confirmPromptVariables: vi.fn(),
    getCurrentPromptVersion: vi.fn(),
    getPromptTemplateByCapability: vi.fn(),
    listPromptCapabilities: vi.fn(),
    listPromptTemplates: vi.fn(),
    listPromptVariables: vi.fn(),
    listPromptVersions: vi.fn(),
    previewPromptVersionCompare: vi.fn(),
    regeneratePromptSuggestion: vi.fn()
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        footer,
        open,
        title
    }: {
        children: React.ReactNode;
        footer?: React.ReactNode;
        open?: boolean;
        title?: React.ReactNode;
    }) =>
        open ? (
            <div>
                <h3>{title}</h3>
                {children}
                {footer}
            </div>
        ) : null;

    return {
        KuzhambuDrawer: mockDrawer
    };
});

const template = {
    id: 1001,
    capability: "classics_summary",
    name: "摘要提示词",
    description: "生成摘要",
    enabled: true,
    currentVersionNo: 2,
    registeredAt: "2026-07-01T00:00:00.000Z"
};

const currentVersion = {
    id: 2002,
    templateId: 1001,
    versionNo: 2,
    messageTemplatesJson: '[{"role":"user","content":"{{title}}"}]',
    variablesSnapshotJson: '[{"variableName":"title","required":true,"priority":1}]',
    outputSchemaJson: '{"type":"object"}',
    changeSummary: "current",
    registeredAt: "2026-07-02T00:00:00.000Z"
};

const versions = [
    {
        ...currentVersion,
        id: 2001,
        versionNo: 1,
        changeSummary: "initial"
    },
    currentVersion
];

const variables = [
    {
        id: 3001,
        templateId: 1001,
        variableName: "title",
        required: true,
        description: "标题",
        priority: 1
    }
];

const renderPage = () =>
    render(
        <App>
            <QueryClientProvider client={queryClient}>
                <PromptsPage />
            </QueryClientProvider>
        </App>
    );

describe("PromptsPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["ai:prompt:view", "ai:prompt:edit"]);
        vi.mocked(service.listPromptCapabilities).mockResolvedValue([
            {
                capability: "classics_summary",
                name: "古籍摘要",
                requiredTags: ["chat"],
                outputMode: "JSON",
                enabled: true,
                priority: 1
            }
        ]);
        vi.mocked(service.listPromptTemplates).mockResolvedValue([template]);
        vi.mocked(service.getCurrentPromptVersion).mockResolvedValue(currentVersion);
        vi.mocked(service.listPromptVersions).mockResolvedValue(versions);
        vi.mocked(service.listPromptVariables).mockResolvedValue(variables);
        vi.mocked(service.confirmPromptVariables).mockResolvedValue(true);
        vi.mocked(service.previewPromptVersionCompare).mockResolvedValue(versions);
        vi.mocked(service.regeneratePromptSuggestion).mockResolvedValue({
            ...currentVersion,
            changeSummary: "建议改写"
        });
        vi.mocked(service.changePromptTemplate).mockResolvedValue(template);
        vi.mocked(service.changePromptVersionRollback).mockResolvedValue(currentVersion);
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders prompt filter and table", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "提示词管理" })).toBeInTheDocument();
        expect(screen.getByText("维护 AI 提示词模板、变量、版本对比和回滚。")).toBeInTheDocument();
        expect(await screen.findByText("摘要提示词")).toBeInTheDocument();
        expect(screen.getByText("古籍")).toBeInTheDocument();
        expect(screen.getByText("古籍摘要")).toBeInTheDocument();
        expect(screen.queryByRole("columnheader", { name: "业务域" })).not.toBeInTheDocument();
        expect(screen.getByText("2026-07-01")).toBeInTheDocument();
        expect(screen.queryByRole("columnheader", { name: "当前版本" })).not.toBeInTheDocument();
    });

    it("applies capability filter", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: /筛选/ }));
        expect((await screen.findAllByText("能力")).length).toBeGreaterThan(1);
        expect(screen.getAllByText("状态").length).toBeGreaterThan(1);
        fireEvent.click(screen.getByRole("button", { name: /查\s*询/ }));

        await waitFor(() => {
            expect(service.listPromptTemplates).toHaveBeenLastCalledWith({});
        });
    });

    it("opens edit drawer for selected prompt", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: "编辑 摘要提示词" }));

        expect(await screen.findByRole("heading", { name: "编辑提示词" })).toBeInTheDocument();
        expect(await screen.findByText("版本列表")).toBeInTheDocument();
        await waitFor(() => {
            expect(screen.getByText("title")).toBeInTheDocument();
        });
        expect(screen.queryByText("变量快照 JSON")).not.toBeInTheDocument();
    });

    it("opens create drawer", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: /新建/ }));

        expect(await screen.findByRole("heading", { name: "新建提示词" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /创建模板/ })).toBeInTheDocument();
        expect(screen.queryByText("版本列表")).not.toBeInTheDocument();
    });

    it("changes prompt enabled state from table switch", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("switch", { name: "切换 摘要提示词 状态，当前启用" }));

        await waitFor(() => {
            expect(service.changePromptTemplate).toHaveBeenCalledWith(
                expect.objectContaining({
                    enabled: false,
                    id: template.id,
                    messageTemplatesJson: currentVersion.messageTemplatesJson
                })
            );
        });
    });

    it("deletes prompt template as a soft delete", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: "删除 摘要提示词" }));

        expect((await screen.findAllByText("删除提示词模板")).length).toBeGreaterThan(0);
        fireEvent.click(
            within(screen.getByRole("dialog")).getByRole("button", { name: /删\s*除/ })
        );

        await waitFor(() => {
            expect(service.changePromptTemplate).toHaveBeenCalledWith(
                expect.objectContaining({
                    enabled: false,
                    id: template.id,
                    changeSummary: "删除提示词模板"
                })
            );
        });
    });

    it("batch disables selected prompt templates", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        const rowCheckbox = screen.getAllByRole("checkbox")[1];
        fireEvent.click(rowCheckbox);
        fireEvent.click(screen.getByRole("button", { name: /禁用/ }));

        await waitFor(() => {
            expect(service.changePromptTemplate).toHaveBeenCalledWith(
                expect.objectContaining({
                    enabled: false,
                    id: template.id,
                    changeSummary: "禁用提示词模板"
                })
            );
        });
    });

    it("batch deletes selected prompt templates as a soft delete", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        const rowCheckbox = screen.getAllByRole("checkbox")[1];
        fireEvent.click(rowCheckbox);
        fireEvent.click(screen.getByRole("button", { name: /批量删除/ }));

        expect((await screen.findAllByText("批量删除提示词模板")).length).toBeGreaterThan(0);
        fireEvent.click(
            within(screen.getByRole("dialog")).getByRole("button", { name: /删\s*除/ })
        );

        await waitFor(() => {
            expect(service.changePromptTemplate).toHaveBeenCalledWith(
                expect.objectContaining({
                    enabled: false,
                    id: template.id,
                    changeSummary: "删除提示词模板"
                })
            );
        });
    });

    it("disables edit action without edit permission", async () => {
        replacePermissions(["ai:prompt:view"]);
        renderPage();

        await screen.findByText("摘要提示词");
        expect(screen.getByRole("button", { name: "编辑 摘要提示词" })).toBeDisabled();
        expect(screen.getByRole("button", { name: /新建/ })).toBeDisabled();
    });
});
