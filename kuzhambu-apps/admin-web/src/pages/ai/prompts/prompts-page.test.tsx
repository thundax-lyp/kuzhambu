import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { PromptsPage } from "./prompts-page";
import * as service from "./prompts-service";

vi.mock("./prompts-service", () => ({
    changePromptTemplate: vi.fn(),
    previewPromptVersionCompare: vi.fn(),
    regeneratePromptSuggestion: vi.fn(),
    getCurrentPromptVersion: vi.fn(),
    getPromptTemplateByCapability: vi.fn(),
    listPromptCapabilities: vi.fn(),
    listPromptVariables: vi.fn(),
    listPromptVersions: vi.fn(),
    changePromptVersionRollback: vi.fn(),
    confirmPromptVariables: vi.fn()
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
    status: "ACTIVE",
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
                name: "摘要生成",
                requiredTags: ["chat"],
                outputMode: "JSON",
                enabled: true,
                priority: 1
            }
        ]);
        vi.mocked(service.getPromptTemplateByCapability).mockResolvedValue(template);
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

    it("renders prompt workbench and version list", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "AI 提示词版本" })).toBeInTheDocument();
        expect(screen.getByText("版本编辑")).toBeInTheDocument();
        expect(screen.getByText("版本列表")).toBeInTheDocument();
    });

    it("queries template and renders variables", async () => {
        renderPage();

        await screen.findByText("版本编辑");
        await screen.findByText("摘要生成 / classics_summary");
        fireEvent.click(screen.getByRole("button", { name: /查\s*询/ }));

        expect(await screen.findByText("摘要提示词")).toBeInTheDocument();
        await waitFor(() => {
            expect(screen.getByDisplayValue(/"variableName": "title"/)).toBeInTheDocument();
        });
    });

    it("falls back to current version variables when variable registry is empty", async () => {
        vi.mocked(service.listPromptVariables).mockResolvedValue([]);
        renderPage();

        await screen.findByText("版本编辑");
        await screen.findByText("摘要生成 / classics_summary");
        fireEvent.click(screen.getByRole("button", { name: /查\s*询/ }));

        await waitFor(() => {
            expect(screen.getByDisplayValue(/"variableName": "title"/)).toBeInTheDocument();
        });
    });

    it("opens suggestion preview before applying as new version", async () => {
        renderPage();

        await screen.findByText("版本编辑");
        await screen.findByText("摘要生成 / classics_summary");
        fireEvent.click(screen.getByRole("button", { name: /查\s*询/ }));
        await screen.findByText("摘要提示词");

        fireEvent.click(screen.getByRole("button", { name: /生成优化建议/ }));

        expect(await screen.findByRole("heading", { name: "优化建议预览" })).toBeInTheDocument();
        expect(service.changePromptTemplate).not.toHaveBeenCalled();
    });

    it("disables edit actions without edit permission", async () => {
        replacePermissions(["ai:prompt:view"]);
        renderPage();

        expect(await screen.findByRole("button", { name: /保存新版本/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /生成优化建议/ })).toBeDisabled();
    });
});
