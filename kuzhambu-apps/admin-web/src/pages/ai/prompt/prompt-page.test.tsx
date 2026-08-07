import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { replacePermissions } from "@/auth/permission-storage";
import { PromptPage } from "./prompt-page";
import * as service from "./prompt-service";

vi.mock("./prompt-service", () => ({
    changePromptTemplate: vi.fn(),
    changePromptTemplateStatus: vi.fn(),
    changePromptVersionRollback: vi.fn(),
    confirmPromptVariables: vi.fn(),
    getCurrentPromptVersion: vi.fn(),
    getPromptTemplateByCapability: vi.fn(),
    listPromptCapabilities: vi.fn(),
    listPromptCapabilityVariables: vi.fn(),
    listPromptTemplates: vi.fn(),
    listPromptVariables: vi.fn(),
    listPromptVersions: vi.fn(),
    previewPromptVersionCompare: vi.fn(),
    deletePromptTemplate: vi.fn()
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

const template = {
    id: "1001",
    capability: "CLASSICS_SUMMARY",
    name: "摘要提示词",
    description: "生成摘要",
    enabled: true,
    currentVersionNo: 2,
    registeredAt: "2026-07-01T00:00:00.000Z"
};

const currentVersion = {
    id: "2002",
    templateId: "1001",
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
        id: "2001",
        versionNo: 1,
        messageTemplatesJson: '[{"role":"user","content":"回滚后的正文"}]',
        changeSummary: "initial"
    },
    currentVersion
];

const variables = [
    {
        id: "3001",
        templateId: "1001",
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
                <PromptPage />
            </QueryClientProvider>
        </App>
    );

const openSelectAndChoose = async (label: string, optionText: string) => {
    const select = await screen.findByRole("combobox", { name: label });
    fireEvent.mouseDown(select);
    const options = await screen.findAllByText(optionText);
    await userEvent.click(options.at(-1)!);
};

describe("PromptPage", () => {
    beforeEach(() => {
        replacePermissions(["ai:prompt:view", "ai:prompt:edit"]);
        vi.mocked(service.listPromptCapabilities).mockResolvedValue([
            {
                capability: "CLASSICS_SUMMARY",
                name: "古籍摘要",
                requiredTags: ["chat"],
                outputMode: "JSON",
                enabled: true,
                priority: 1
            },
            {
                capability: "CLASSICS_TRANSLATE",
                name: "古籍翻译",
                requiredTags: ["chat"],
                outputMode: "TEXT",
                enabled: true,
                priority: 2
            }
        ]);
        vi.mocked(service.listPromptTemplates).mockResolvedValue([template]);
        vi.mocked(service.listPromptCapabilityVariables).mockImplementation(async (capability) => {
            if (capability === "CLASSICS_TRANSLATE") {
                return [
                    { variableName: "contextPath", required: false, description: "上下文路径" },
                    { variableName: "sourceText", required: true, description: "待翻译的源文本" }
                ];
            }
            return [
                { variableName: "bodyText", required: false, description: "正文内容" },
                { variableName: "contentType", required: true, description: "内容类型" },
                { variableName: "title", required: false, description: "内容标题" }
            ];
        });
        vi.mocked(service.getCurrentPromptVersion).mockResolvedValue(currentVersion);
        vi.mocked(service.listPromptVersions).mockResolvedValue(versions);
        vi.mocked(service.listPromptVariables).mockResolvedValue(variables);
        vi.mocked(service.confirmPromptVariables).mockResolvedValue(true);
        vi.mocked(service.previewPromptVersionCompare).mockResolvedValue(versions);
        vi.mocked(service.changePromptTemplate).mockResolvedValue(template);
        vi.mocked(service.changePromptTemplateStatus).mockResolvedValue(true);
        vi.mocked(service.deletePromptTemplate).mockResolvedValue(true);
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
        expect(screen.queryByRole("table", { name: "提示词版本列表" })).not.toBeInTheDocument();
        expect(screen.getByRole("combobox", { name: "提示词能力" })).toBeDisabled();
        const variablesButton = screen.getByTestId("ai-prompt-prompt-view-variables-button");
        await waitFor(() => {
            expect(variablesButton).not.toBeDisabled();
        });
        fireEvent.click(variablesButton);
        const variableDialog = await screen.findByTestId("ai-prompt-prompt-variables-modal");
        expect(within(variableDialog).getByText("bodyText")).toBeInTheDocument();
        expect(within(variableDialog).getByText("contentType")).toBeInTheDocument();
        expect(within(variableDialog).getByText("内容类型")).toBeInTheDocument();
        expect(within(variableDialog).queryByRole("columnheader", { name: "变量名" })).toBeNull();
        expect(screen.queryByText("变量快照 JSON")).not.toBeInTheDocument();
    });

    it("rolls back a prompt version from the version drawer", async () => {
        vi.mocked(service.changePromptVersionRollback).mockResolvedValue(versions[0]);
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: "查看 摘要提示词 版本" }));
        expect(await screen.findByRole("heading", { name: "提示词版本" })).toBeInTheDocument();

        const rollbackButtons = await screen.findAllByRole("button", { name: "回滚" });
        fireEvent.click(rollbackButtons.find((button) => !button.hasAttribute("disabled"))!);
        fireEvent.click(
            within(screen.getByRole("dialog")).getByRole("button", { name: /回\s*滚/ })
        );

        await waitFor(() => {
            expect(service.changePromptVersionRollback).toHaveBeenCalledWith(
                {
                    id: "1001",
                    versionNo: 1
                },
                expect.anything()
            );
        });
        const versionTable = screen.getByRole("table", { name: "提示词版本列表" });
        const versionOneRow = within(versionTable).getByText("1").closest("tr");
        const versionTwoRow = within(versionTable).getByText("2").closest("tr");
        expect(versionOneRow).not.toBeNull();
        expect(versionTwoRow).not.toBeNull();
        expect(within(versionOneRow!).getByText("当前")).toBeInTheDocument();
        expect(within(versionOneRow!).getByRole("button", { name: "回滚" })).toBeDisabled();
        expect(within(versionTwoRow!).getByText("历史")).toBeInTheDocument();
        expect(within(versionTwoRow!).getByRole("button", { name: "回滚" })).toBeEnabled();
    });

    it("updates create drawer variable modal content when capability changes", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: /新建/ }));
        await openSelectAndChoose("提示词能力", "古籍翻译");
        const variablesButton = screen.getByTestId("ai-prompt-prompt-view-variables-button");
        await waitFor(() => {
            expect(variablesButton).not.toBeDisabled();
        });
        fireEvent.click(variablesButton);

        const variableDialog = await screen.findByTestId("ai-prompt-prompt-variables-modal");
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
        expect(screen.queryByRole("table", { name: "提示词版本列表" })).not.toBeInTheDocument();
    });

    it("preserves optional capability variables when creating a template", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: /新建/ }));
        await openSelectAndChoose("提示词能力", "古籍摘要");
        await waitFor(() => {
            expect(screen.getByTestId("ai-prompt-prompt-view-variables-button")).toBeEnabled();
        });
        fireEvent.change(screen.getByLabelText("模板名称"), {
            target: { value: "新摘要提示词" }
        });
        fireEvent.change(screen.getByLabelText("用户消息正文"), {
            target: { value: "请总结 {{title}}" }
        });
        await userEvent.click(screen.getByRole("button", { name: /创建模板/ }));

        await waitFor(() => {
            expect(service.changePromptTemplate).toHaveBeenCalledWith(
                expect.objectContaining({
                    variables: [expect.objectContaining({ variableName: "title", required: false })]
                }),
                expect.anything()
            );
        });
    });

    it("delegates capability variable validation to the backend", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("button", { name: "编辑 摘要提示词" }));

        const userMessage = await screen.findByLabelText("用户消息正文");
        fireEvent.change(userMessage, { target: { value: "{{unknownName}}" } });
        fireEvent.click(screen.getByRole("button", { name: /保存新版本/ }));

        await waitFor(() => {
            expect(service.changePromptTemplate).toHaveBeenCalled();
        });
    });

    it("changes prompt enabled state from table switch", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        fireEvent.click(screen.getByRole("switch", { name: "切换 摘要提示词 状态，当前启用" }));

        await waitFor(() => {
            expect(service.changePromptTemplateStatus).toHaveBeenCalledWith({
                enabled: false,
                id: template.id
            });
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
            expect(service.deletePromptTemplate).toHaveBeenCalledWith(template.id);
        });
    });

    it("batch disables selected prompt templates", async () => {
        renderPage();

        await screen.findByText("摘要提示词");
        const rowCheckbox = screen.getAllByRole("checkbox")[1];
        fireEvent.click(rowCheckbox);
        fireEvent.click(screen.getByRole("button", { name: /禁用/ }));

        await waitFor(() => {
            expect(service.changePromptTemplateStatus).toHaveBeenCalledWith({
                enabled: false,
                id: template.id
            });
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
            expect(service.deletePromptTemplate).toHaveBeenCalledWith(template.id);
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
