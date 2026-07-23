import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { replacePermissions } from "@/auth/permission-storage";
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
    previewPromptVersionCompare: vi.fn()
}));

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        footer,
        footerActions,
        open,
        title
    }: {
        children: React.ReactNode;
        footer?: React.ReactNode;
        footerActions?: Array<{
            action: () => void;
            disabled?: boolean;
            testId: string;
            title: React.ReactNode;
            type?: "default" | "primary";
        }>;
        open?: boolean;
        title?: React.ReactNode;
    }) =>
        open ? (
            <div>
                <h3>{title}</h3>
                {children}
                {footer}
                {footerActions?.map((action) => (
                    <button
                        key={action.testId}
                        data-testid={action.testId}
                        disabled={action.disabled}
                        type="button"
                        onClick={action.action}
                    >
                        {action.title}
                    </button>
                ))}
            </div>
        ) : null;

    return {
        KuzhambuDrawer: mockDrawer
    };
});

vi.mock("@/components/kuzhambu-modal", () => {
    const mockModal = ({
        children,
        footer,
        open,
        testId,
        title
    }: {
        children: React.ReactNode;
        footer?: React.ReactNode;
        open?: boolean;
        testId?: string;
        title?: React.ReactNode;
    }) =>
        open ? (
            <div role="dialog" aria-label={String(title)} data-testid={testId}>
                <h3>{title}</h3>
                {children}
                {footer}
            </div>
        ) : null;

    return {
        KuzhambuModal: mockModal
    };
});

vi.mock("@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm", () => ({
    useKuzhambuConfirm: () => ({
        danger: vi.fn()
    })
}));

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

const createTestQueryClient = () =>
    new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

const renderPage = () =>
    render(
        <App>
            <QueryClientProvider client={createTestQueryClient()}>
                <PromptsPage />
            </QueryClientProvider>
        </App>
    );

const openSelectAndChoose = async (label: string, optionText: string) => {
    const select = await screen.findByRole("combobox", { name: label });
    fireEvent.mouseDown(select);
    const options = await screen.findAllByText(optionText);
    await userEvent.click(options.at(-1)!);
};

describe("PromptsPage", () => {
    beforeEach(() => {
        replacePermissions(["ai:prompt:view", "ai:prompt:edit"]);
        vi.mocked(service.listPromptCapabilities).mockResolvedValue([
            {
                capability: "classics_summary",
                name: "古籍摘要",
                requiredTags: ["chat"],
                outputMode: "JSON",
                enabled: true,
                priority: 1
            },
            {
                capability: "classics_translate",
                name: "古籍翻译",
                requiredTags: ["chat"],
                outputMode: "TEXT",
                enabled: true,
                priority: 2
            }
        ]);
        vi.mocked(service.listPromptTemplates).mockResolvedValue([template]);
        vi.mocked(service.getCurrentPromptVersion).mockResolvedValue(currentVersion);
        vi.mocked(service.listPromptVersions).mockResolvedValue(versions);
        vi.mocked(service.listPromptVariables).mockResolvedValue(variables);
        vi.mocked(service.confirmPromptVariables).mockResolvedValue(true);
        vi.mocked(service.previewPromptVersionCompare).mockResolvedValue(versions);
        vi.mocked(service.changePromptTemplate).mockResolvedValue(template);
        vi.mocked(service.changePromptVersionRollback).mockResolvedValue(currentVersion);
    });

    afterEach(() => {
        cleanup();
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
        expect(await screen.findByText("版本")).toBeInTheDocument();
        expect(screen.getByRole("table", { name: "提示词版本列表" })).toBeInTheDocument();
        const variablesButton = screen.getByTestId("ai-prompts-prompts-view-variables-button");
        await waitFor(() => {
            expect(variablesButton).not.toBeDisabled();
        });
        fireEvent.click(variablesButton);
        const variableDialog = await screen.findByTestId("ai-prompts-prompt-variables-modal");
        expect(within(variableDialog).getByText("bodyText")).toBeInTheDocument();
        expect(within(variableDialog).getByText("contentType")).toBeInTheDocument();
        expect(within(variableDialog).getByText("内容类型")).toBeInTheDocument();
        expect(within(variableDialog).queryByRole("columnheader", { name: "变量名" })).toBeNull();
        expect(screen.queryByText("变量快照 JSON")).not.toBeInTheDocument();
    });

    it("updates variable modal content when capability changes", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: "编辑 摘要提示词" }));
        await openSelectAndChoose("提示词能力", "古籍翻译");
        const variablesButton = screen.getByTestId("ai-prompts-prompts-view-variables-button");
        await waitFor(() => {
            expect(variablesButton).not.toBeDisabled();
        });
        fireEvent.click(variablesButton);

        const variableDialog = await screen.findByTestId("ai-prompts-prompt-variables-modal");
        expect(await within(variableDialog).findByText("contextPath")).toBeInTheDocument();
        expect(within(variableDialog).getByText("sourceText")).toBeInTheDocument();
        expect(within(variableDialog).getByText("待翻译的源文本")).toBeInTheDocument();
        expect(within(variableDialog).queryByText("bodyText")).not.toBeInTheDocument();
    });

    it("opens create drawer", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: /新建/ }));

        expect(await screen.findByRole("heading", { name: "新建提示词" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /创建模板/ })).toBeInTheDocument();
        expect(screen.queryByText("版本")).not.toBeInTheDocument();
    });

    it("blocks unsupported prompt variables for a fixed capability", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: "编辑 摘要提示词" }));

        const userMessage = await screen.findByLabelText("用户消息正文");
        fireEvent.change(userMessage, { target: { value: "{{unknownName}}" } });
        fireEvent.click(screen.getByRole("button", { name: /保存新版本/ }));

        expect(await screen.findByText("当前能力不支持变量：unknownName")).toBeInTheDocument();
        await waitFor(() => {
            expect(service.changePromptTemplate).not.toHaveBeenCalled();
        });
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
