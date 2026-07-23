import {
    BranchesOutlined,
    CheckCircleOutlined,
    DeleteOutlined,
    EyeOutlined,
    PlusOutlined,
    RetweetOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Form, Input, Select, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuModal } from "@/components/kuzhambu-modal";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import {
    findUnsupportedPromptVariableNames,
    getPromptCapabilityVariables
} from "../prompt-capability-variables";
import type { PromptCapabilityVariableDefinition } from "../prompt-capability-variables";
import type { AiPromptTemplateChangeCommand } from "../prompts-service";
import * as service from "../prompts-service";
import type {
    AiPromptTemplateRecord,
    AiPromptVariableRecord,
    AiPromptVersionRecord
} from "../prompts-types";

type PromptFormValues = Omit<AiPromptTemplateChangeCommand, "variables" | "enabled"> & {
    outputStructure?: PromptOutputStructure | null;
    status?: boolean | null;
};

type PromptOutputStructure = "JSON" | "TEXT";

interface PromptEditDrawerProps {
    canEdit: boolean;
    capabilityOptions: Array<{ label: string; value: string }>;
    open: boolean;
    template: AiPromptTemplateRecord | null;
    onClose: () => void;
    onSaved: () => void;
}

const EMPTY_JSON_ARRAY = "[]";
const EMPTY_JSON_OBJECT = "{}";
const JSON_OUTPUT_SCHEMA = '{\n  "type": "object"\n}';
const TEXT_OUTPUT_SCHEMA = '{\n  "type": "text"\n}';

interface PromptMessage {
    content: string;
    role: string;
}

const PROMPT_ROLE_OPTIONS = [
    { label: "系统", value: "system" },
    { label: "用户", value: "user" },
    { label: "助手", value: "assistant" }
];

const promptRoleLabels: Record<string, string> = {
    assistant: "助手",
    system: "系统",
    user: "用户"
};

const normalizeJsonText = (value?: string | null, fallback = EMPTY_JSON_OBJECT) => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : fallback;
};

const parseJson = (value: string) => JSON.parse(value) as unknown;

const formatJsonText = (value?: string | null, fallback = EMPTY_JSON_OBJECT) => {
    const normalized = normalizeJsonText(value, fallback);
    try {
        return JSON.stringify(parseJson(normalized), null, 2);
    } catch {
        return normalized;
    }
};

const assertJsonText = (value?: string | null, fallback = EMPTY_JSON_OBJECT) => {
    try {
        parseJson(normalizeJsonText(value, fallback));
        return Promise.resolve();
    } catch {
        return Promise.reject(new Error("请输入合法 JSON"));
    }
};

const readPromptOutputStructure = (value?: string | null): PromptOutputStructure => {
    try {
        const parsed = parseJson(normalizeJsonText(value, TEXT_OUTPUT_SCHEMA));
        if (
            parsed != null &&
            typeof parsed === "object" &&
            "type" in parsed &&
            (parsed as { type?: unknown }).type === "text"
        ) {
            return "TEXT";
        }
    } catch {
        return "JSON";
    }
    return "JSON";
};

const outputSchemaForStructure = (
    outputStructure: PromptOutputStructure | null | undefined,
    currentSchemaJson?: string | null
) => {
    if (outputStructure === "TEXT") {
        return TEXT_OUTPUT_SCHEMA;
    }
    const normalized = normalizeJsonText(currentSchemaJson, JSON_OUTPUT_SCHEMA);
    try {
        return readPromptOutputStructure(normalized) === "JSON" ? normalized : JSON_OUTPUT_SCHEMA;
    } catch {
        return JSON_OUTPUT_SCHEMA;
    }
};

const toVariableRows = (value?: string | null): AiPromptVariableRecord[] => {
    try {
        const parsed = parseJson(normalizeJsonText(value, EMPTY_JSON_ARRAY));
        if (!Array.isArray(parsed)) {
            return [];
        }
        return parsed
            .filter(
                (item): item is Record<string, unknown> => item != null && typeof item === "object"
            )
            .map((item, index) => ({
                variableName: String(item.variableName || item.name || ""),
                required: item.required !== false,
                description: typeof item.description === "string" ? item.description : null,
                priority: typeof item.priority === "number" ? item.priority : index + 1
            }))
            .filter((item) => item.variableName.trim().length > 0);
    } catch {
        return [];
    }
};

const variablesToJson = (variables: AiPromptVariableRecord[] = []) => {
    return JSON.stringify(
        variables.map((variable, index) => ({
            variableName: variable.variableName,
            required: variable.required !== false,
            description: variable.description || "",
            priority: variable.priority ?? index + 1
        })),
        null,
        2
    );
};

const extractPromptVariableNames = (messageTemplatesJson?: string | null) => {
    const names = new Set<string>();
    for (const message of toPromptMessages(messageTemplatesJson)) {
        for (const match of message.content.matchAll(/\{\{\s*([A-Za-z][A-Za-z0-9_]*)\s*\}\}/g)) {
            names.add(match[1]);
        }
    }
    return Array.from(names);
};

const mergeVariableRows = (
    variablesSnapshotJson?: string | null,
    messageTemplatesJson?: string | null,
    remoteVariables: AiPromptVariableRecord[] = []
) => {
    const currentRows = toVariableRows(variablesSnapshotJson);
    const sourceRows = currentRows.length > 0 ? currentRows : remoteVariables;
    const rowByName = new Map(sourceRows.map((variable) => [variable.variableName, variable]));
    const placeholderRows = extractPromptVariableNames(messageTemplatesJson).map(
        (variableName, index) => {
            const existing = rowByName.get(variableName);
            return {
                variableName,
                required: existing?.required !== false,
                description: existing?.description || "",
                priority: existing?.priority ?? index + 1
            };
        }
    );
    return placeholderRows.length > 0 ? placeholderRows : sourceRows;
};

const formatDate = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${date.getFullYear()}-${month}-${day}`;
};

const statusFromEnabled = (enabled?: boolean | null) => enabled !== false;

const enabledFromStatus = (status?: boolean | null) => status !== false;

const versionTitle = (version: AiPromptVersionRecord) => `版本 ${version.versionNo ?? "-"}`;

const toPromptMessages = (value?: string | null): PromptMessage[] => {
    try {
        const parsed = parseJson(normalizeJsonText(value, EMPTY_JSON_ARRAY));
        if (!Array.isArray(parsed)) {
            return [{ role: "system", content: "" }];
        }
        const messages = parsed
            .filter(
                (item): item is Record<string, unknown> => item != null && typeof item === "object"
            )
            .map((item) => ({
                role: typeof item.role === "string" && item.role.trim() ? item.role : "user",
                content: typeof item.content === "string" ? item.content : ""
            }));
        return messages.length > 0 ? messages : [{ role: "system", content: "" }];
    } catch {
        return [{ role: "system", content: value || "" }];
    }
};

const promptMessagesToJson = (messages: PromptMessage[]) => {
    return JSON.stringify(
        messages.map((message) => ({
            role: message.role || "user",
            content: message.content || ""
        })),
        null,
        2
    );
};

const PromptMarkdownEditor = ({
    value,
    onChange
}: {
    value?: string | null;
    onChange?: (value: string) => void;
}) => {
    const messages = toPromptMessages(value);

    const updateMessages = (nextMessages: PromptMessage[]) => {
        onChange?.(promptMessagesToJson(nextMessages));
    };

    return (
        <div className="prompt-markdown-editor">
            {messages.map((message, index) => {
                const roleLabel = promptRoleLabels[message.role] || message.role || "消息";
                return (
                    <section
                        key={`${index}-${message.role}`}
                        className="prompt-markdown-editor-message"
                    >
                        <div className="prompt-markdown-editor-toolbar">
                            <Select
                                aria-label={`第 ${index + 1} 条消息角色`}
                                value={message.role}
                                options={PROMPT_ROLE_OPTIONS}
                                className="prompt-markdown-editor-role"
                                onChange={(role) => {
                                    const nextMessages = [...messages];
                                    nextMessages[index] = { ...message, role };
                                    updateMessages(nextMessages);
                                }}
                            />
                        </div>
                        <Input.TextArea
                            aria-label={`${roleLabel}消息正文`}
                            value={message.content}
                            rows={10}
                            onChange={(event) => {
                                const nextMessages = [...messages];
                                nextMessages[index] = {
                                    ...message,
                                    content: event.target.value
                                };
                                updateMessages(nextMessages);
                            }}
                        />
                        <div className="prompt-markdown-editor-actions">
                            <KuzhambuSpaceCompact>
                                <KuzhambuButton
                                    testId="ai-prompts-prompts-add-message-button"
                                    icon={<PlusOutlined />}
                                    onClick={() => {
                                        const nextMessages = [...messages];
                                        nextMessages.splice(index + 1, 0, {
                                            role: "user",
                                            content: ""
                                        });
                                        updateMessages(nextMessages);
                                    }}
                                >
                                    添加消息
                                </KuzhambuButton>
                                <KuzhambuButton
                                    testId="ai-prompts-prompts-remove-message-button"
                                    icon={<DeleteOutlined />}
                                    disabled={messages.length <= 1}
                                    onClick={() => {
                                        updateMessages(
                                            messages.filter(
                                                (_item, itemIndex) => itemIndex !== index
                                            )
                                        );
                                    }}
                                >
                                    删除消息
                                </KuzhambuButton>
                            </KuzhambuSpaceCompact>
                        </div>
                    </section>
                );
            })}
        </div>
    );
};

export const PromptEditDrawer = ({
    canEdit,
    capabilityOptions,
    open,
    template,
    onClose,
    onSaved
}: PromptEditDrawerProps) => {
    const { message } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [form] = Form.useForm<PromptFormValues>();
    const [viewVersion, setViewVersion] = useState<AiPromptVersionRecord | null>(null);
    const [compareVersions, setCompareVersions] = useState<AiPromptVersionRecord[]>([]);
    const [variableModalOpen, setVariableModalOpen] = useState(false);
    const variablesSnapshotJson = Form.useWatch("variablesSnapshotJson", form);
    const messageTemplatesJson = Form.useWatch("messageTemplatesJson", form);
    const currentCapability = Form.useWatch("capability", form);
    const currentTemplateId = Form.useWatch("id", form);
    const templateId = template?.id || null;

    const currentVersionQuery = useQuery({
        queryKey: ["ai", "prompts", "current-version", templateId],
        queryFn: () => service.getCurrentPromptVersion(templateId || 0),
        enabled: open && Boolean(templateId),
        retry: false
    });

    const versionsQuery = useQuery({
        queryKey: ["ai", "prompts", "versions", templateId],
        queryFn: () => service.listPromptVersions(templateId || 0),
        enabled: open && Boolean(templateId),
        retry: false
    });

    const variablesQuery = useQuery({
        queryKey: ["ai", "prompts", "variables", templateId],
        queryFn: () => service.listPromptVariables(templateId || 0),
        enabled: open && Boolean(templateId),
        retry: false
    });

    const variableRows = useMemo(() => {
        return mergeVariableRows(variablesSnapshotJson, messageTemplatesJson, variablesQuery.data);
    }, [messageTemplatesJson, variablesQuery.data, variablesSnapshotJson]);

    const allowedVariableRows = useMemo(
        () => getPromptCapabilityVariables(currentCapability),
        [currentCapability]
    );
    const allowedVariableNames = useMemo(
        () => allowedVariableRows.map((variable) => variable.variableName),
        [allowedVariableRows]
    );

    useEffect(() => {
        if (!open) {
            return;
        }
        if (!template) {
            form.setFieldsValue({
                id: null,
                capability: "",
                name: "",
                description: "",
                status: true,
                messageTemplatesJson: promptMessagesToJson([
                    { role: "system", content: "" },
                    { role: "user", content: "" }
                ]),
                variablesSnapshotJson: EMPTY_JSON_ARRAY,
                outputSchemaJson: TEXT_OUTPUT_SCHEMA,
                outputStructure: "TEXT",
                changeSummary: "创建提示词模板"
            });
            return;
        }
        const currentVersion = currentVersionQuery.data;
        const outputSchemaJson = formatJsonText(
            currentVersion?.outputSchemaJson,
            TEXT_OUTPUT_SCHEMA
        );
        form.setFieldsValue({
            id: template.id || null,
            capability: template.capability || "",
            name: template.name || "",
            description: template.description || "",
            status: statusFromEnabled(template.enabled),
            messageTemplatesJson: formatJsonText(
                currentVersion?.messageTemplatesJson,
                EMPTY_JSON_ARRAY
            ),
            variablesSnapshotJson:
                variablesQuery.data && variablesQuery.data.length > 0
                    ? variablesToJson(variablesQuery.data)
                    : formatJsonText(currentVersion?.variablesSnapshotJson, EMPTY_JSON_ARRAY),
            outputSchemaJson,
            outputStructure: readPromptOutputStructure(outputSchemaJson),
            changeSummary: ""
        });
    }, [currentVersionQuery.data, form, open, template, variablesQuery.data]);

    const changeMutation = useMutation({
        mutationFn: service.changePromptTemplate,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["ai", "prompts"] });
            message.success("提示词模板已保存");
            onSaved();
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "提示词模板保存失败");
        }
    });

    const validateMutation = useMutation({
        mutationFn: service.confirmPromptVariables,
        onSuccess: () => {
            message.success("必填变量校验通过");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "必填变量校验失败");
        }
    });

    const compareMutation = useMutation({
        mutationFn: service.previewPromptVersionCompare,
        onSuccess: setCompareVersions,
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "版本对比失败");
        }
    });

    const rollbackMutation = useMutation({
        mutationFn: service.changePromptVersionRollback,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["ai", "prompts"] });
            message.success("提示词版本已回滚");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "提示词版本回滚失败");
        }
    });

    const submitTemplate = async (overrideVersion?: AiPromptVersionRecord | null) => {
        const values = await form.validateFields();
        const messageTemplatesJson =
            overrideVersion?.messageTemplatesJson || values.messageTemplatesJson;
        const outputSchemaJson =
            overrideVersion?.outputSchemaJson ||
            outputSchemaForStructure(values.outputStructure, values.outputSchemaJson);
        const variables = overrideVersion?.variablesSnapshotJson
            ? toVariableRows(overrideVersion.variablesSnapshotJson)
            : variableRows;
        const placeholderNames = extractPromptVariableNames(messageTemplatesJson);
        const submittedVariableNames = Array.from(
            new Set([...placeholderNames, ...variables.map((variable) => variable.variableName)])
        );
        const unsupportedVariableNames = findUnsupportedPromptVariableNames(
            values.capability,
            submittedVariableNames
        );
        if (unsupportedVariableNames.length > 0) {
            message.error(`当前能力不支持变量：${unsupportedVariableNames.join("、")}`);
            return;
        }
        const variablesSnapshot =
            overrideVersion?.variablesSnapshotJson || variablesToJson(variableRows);
        await changeMutation.mutateAsync({
            id: values.id || null,
            capability: values.capability,
            name: values.name,
            description: values.description || null,
            enabled: enabledFromStatus(values.status),
            messageTemplatesJson: normalizeJsonText(messageTemplatesJson, EMPTY_JSON_ARRAY),
            variablesSnapshotJson: normalizeJsonText(variablesSnapshot, EMPTY_JSON_ARRAY),
            outputSchemaJson: normalizeJsonText(outputSchemaJson, EMPTY_JSON_OBJECT),
            changeSummary:
                values.changeSummary || overrideVersion?.changeSummary || "保存提示词版本",
            variables
        });
    };

    const validateVariables = async () => {
        if (!currentTemplateId) {
            message.warning("请先保存模板后再校验变量");
            return;
        }
        await validateMutation.mutateAsync({
            id: currentTemplateId,
            providedNames: variableRows.map((variable) => variable.variableName)
        });
    };

    const compareWithCurrent = async (version: AiPromptVersionRecord) => {
        if (!templateId || !version.versionNo || !template?.currentVersionNo) {
            return;
        }
        await compareMutation.mutateAsync({
            id: templateId,
            leftVersionNo: version.versionNo,
            rightVersionNo: template.currentVersionNo
        });
    };

    const confirmRollbackVersion = (version: AiPromptVersionRecord) => {
        if (!templateId || !version.versionNo) {
            return;
        }
        confirm.danger({
            title: "回滚版本",
            message: `确认回滚到版本 ${version.versionNo}？`,
            okText: "回滚",
            onConfirm: () =>
                rollbackMutation.mutateAsync({
                    id: templateId,
                    versionNo: version.versionNo || 0
                })
        });
    };

    const closeDrawer = () => {
        if (changeMutation.isPending) {
            return;
        }
        setVariableModalOpen(false);
        onClose();
    };

    const variableColumns: ColumnsType<PromptCapabilityVariableDefinition> = [
        {
            title: "变量名",
            dataIndex: "variableName",
            key: "variableName",
            render: (variableName: string, record) => (
                <div>
                    <Typography.Text strong={record.required}>{variableName}</Typography.Text>
                    <div>
                        <Typography.Text type="secondary">{record.description}</Typography.Text>
                    </div>
                </div>
            )
        }
    ];

    const versionColumns: KuzhambuTableProps<AiPromptVersionRecord>["columns"] = [
        { title: "版本号", dataIndex: "versionNo", key: "versionNo", width: 96 },
        {
            title: "状态",
            key: "current",
            width: 96,
            render: (_, record) => {
                const current = record.versionNo === template?.currentVersionNo;
                return (
                    <KuzhambuTag type={current ? "success" : "neutral"}>
                        {current ? "当前" : "历史"}
                    </KuzhambuTag>
                );
            }
        },
        {
            title: "变更说明",
            dataIndex: "changeSummary",
            key: "changeSummary",
            ellipsis: true,
            render: (value?: string | null) => value || "-"
        },
        {
            title: "日期",
            dataIndex: "registeredAt",
            key: "registeredAt",
            width: 120,
            render: formatDate
        },
        {
            key: "actions",
            options: (record) => {
                const current = record.versionNo === template?.currentVersionNo;
                return [
                    {
                        key: "view",
                        text: "查看",
                        ariaLabel: `查看版本 ${record.versionNo ?? "-"}`,
                        icon: <EyeOutlined />,
                        testId: "ai-prompts-prompts-view-button",
                        onClick: () => setViewVersion(record)
                    },
                    {
                        key: "compare",
                        text: "对比",
                        ariaLabel: `对比版本 ${record.versionNo ?? "-"}`,
                        disabled: !template?.currentVersionNo,
                        icon: <BranchesOutlined />,
                        testId: "ai-prompts-prompts-compare-button",
                        onClick: () => void compareWithCurrent(record)
                    },
                    {
                        key: "rollback",
                        text: "回滚",
                        ariaLabel: `回滚版本 ${record.versionNo ?? "-"}`,
                        disabled: !canEdit || current,
                        icon: <RetweetOutlined />,
                        testId: "ai-prompts-prompts-rollback-button",
                        type: "warning",
                        onClick: () => confirmRollbackVersion(record)
                    }
                ];
            }
        }
    ];

    return (
        <>
            <KuzhambuDrawer
                testId="ai-prompts-prompt-editor-1-drawer"
                className="prompt-edit-drawer"
                open={open}
                title={template ? "编辑提示词" : "新建提示词"}
                size="large"
                onClose={closeDrawer}
                footerActions={[
                    { testId: "ai-prompts-prompts-cancel-button", title: "取消", action: onClose },
                    {
                        testId: "ai-prompts-prompts-save-new-version-button",
                        title: template ? "保存新版本" : "创建模板",
                        type: "primary",
                        disabled: !canEdit,
                        loading: changeMutation.isPending,
                        action: () => void submitTemplate()
                    }
                ]}
            >
                <Form
                    form={form}
                    colon={false}
                    component="div"
                    labelCol={{ flex: "112px" }}
                    layout="horizontal"
                    className="prompts-editor-form"
                >
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    <div className="prompts-editor-grid">
                        <Form.Item
                            label="模板名称"
                            name="name"
                            className="prompts-editor-item-compact"
                            rules={[{ required: true, message: "请输入模板名称" }]}
                        >
                            <Input />
                        </Form.Item>
                        <Form.Item label="能力" className="prompts-editor-item-compact">
                            <KuzhambuSpaceCompact block>
                                <Form.Item
                                    name="capability"
                                    noStyle
                                    rules={[{ required: true, message: "请选择能力" }]}
                                >
                                    <Select aria-label="提示词能力" options={capabilityOptions} />
                                </Form.Item>
                                <KuzhambuButton
                                    testId="ai-prompts-prompts-view-variables-button"
                                    disabled={allowedVariableNames.length === 0}
                                    onClick={() => setVariableModalOpen(true)}
                                >
                                    变量
                                </KuzhambuButton>
                            </KuzhambuSpaceCompact>
                        </Form.Item>
                        <Form.Item
                            label="状态"
                            name="status"
                            valuePropName="checked"
                            className="prompts-editor-item-status"
                        >
                            <KuzhambuSwitch
                                checkedChildren="启用"
                                unCheckedChildren="禁用"
                                aria-label="提示词模板状态"
                            />
                        </Form.Item>
                    </div>
                    <Form.Item
                        label="说明"
                        name="description"
                        className="prompts-editor-item-full prompts-editor-item-top"
                    >
                        <Input.TextArea rows={2} />
                    </Form.Item>
                    <Form.Item
                        label="正文"
                        name="messageTemplatesJson"
                        className="prompts-editor-item-top"
                        rules={[
                            { required: true, message: "请输入正文" },
                            { validator: (_, value) => assertJsonText(value, EMPTY_JSON_ARRAY) }
                        ]}
                    >
                        <PromptMarkdownEditor />
                    </Form.Item>
                    <Form.Item
                        name="variablesSnapshotJson"
                        hidden
                        rules={[
                            { validator: (_, value) => assertJsonText(value, EMPTY_JSON_ARRAY) }
                        ]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item
                        label="输出格式"
                        name="outputStructure"
                        className="prompts-editor-item-compact"
                    >
                        <Select
                            aria-label="输出格式"
                            options={[
                                { label: "TEXT", value: "TEXT" },
                                { label: "JSON", value: "JSON" }
                            ]}
                            onChange={(outputStructure: PromptOutputStructure) => {
                                form.setFieldValue(
                                    "outputSchemaJson",
                                    outputSchemaForStructure(
                                        outputStructure,
                                        form.getFieldValue("outputSchemaJson")
                                    )
                                );
                            }}
                        />
                    </Form.Item>
                    <Form.Item
                        name="outputSchemaJson"
                        hidden
                        rules={[
                            { validator: (_, value) => assertJsonText(value, TEXT_OUTPUT_SCHEMA) }
                        ]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item
                        label="变更说明"
                        name="changeSummary"
                        className="prompts-editor-item-wide"
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item label="辅助操作" className="prompts-editor-item-wide">
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                testId="ai-prompts-prompts-validate-variables-button"
                                icon={<CheckCircleOutlined />}
                                disabled={!currentTemplateId}
                                loading={validateMutation.isPending}
                                onClick={() => void validateVariables()}
                            >
                                校验变量
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    </Form.Item>
                </Form>
                {template ? (
                    <div className="prompts-version-section">
                        <Typography.Title level={5}>版本</Typography.Title>
                        <KuzhambuTable<AiPromptVersionRecord>
                            ariaLabel="提示词版本列表"
                            rowKey={(record) =>
                                record.id || `${record.templateId}-${record.versionNo}`
                            }
                            columns={versionColumns}
                            dataSource={versionsQuery.data || []}
                            loading={versionsQuery.isFetching}
                            pagination={false}
                            size="small"
                        />
                    </div>
                ) : null}
            </KuzhambuDrawer>

            <KuzhambuModal
                testId="ai-prompts-prompt-variables-modal"
                open={variableModalOpen}
                title="能力变量"
                footer={null}
                onCancel={() => setVariableModalOpen(false)}
            >
                <Table<PromptCapabilityVariableDefinition>
                    aria-label="能力变量列表"
                    rowKey={(record) => record.variableName}
                    columns={variableColumns}
                    dataSource={allowedVariableRows}
                    pagination={false}
                    showHeader={false}
                    size="small"
                />
            </KuzhambuModal>

            <KuzhambuDrawer
                testId="ai-prompts-prompt-editor-2-drawer"
                open={Boolean(viewVersion)}
                title={viewVersion ? versionTitle(viewVersion) : "版本详情"}
                size="large"
                onClose={() => setViewVersion(null)}
            >
                {viewVersion ? <VersionDetail version={viewVersion} /> : null}
            </KuzhambuDrawer>

            <KuzhambuDrawer
                testId="ai-prompts-prompt-editor-3-drawer"
                open={compareVersions.length > 0}
                title="版本对比"
                size="large"
                onClose={() => setCompareVersions([])}
            >
                <div className="prompts-compare-grid">
                    {compareVersions.map((version) => (
                        <VersionDetail key={version.versionNo} version={version} />
                    ))}
                </div>
            </KuzhambuDrawer>
        </>
    );
};

const VersionDetail = ({ version }: { version: AiPromptVersionRecord }) => (
    <div className="prompts-version-detail">
        <Typography.Title level={5}>{versionTitle(version)}</Typography.Title>
        <Typography.Text strong>消息模板 JSON</Typography.Text>
        <pre>{formatJsonText(version.messageTemplatesJson, EMPTY_JSON_ARRAY)}</pre>
        <Typography.Text strong>变量快照 JSON</Typography.Text>
        <pre>{formatJsonText(version.variablesSnapshotJson, EMPTY_JSON_ARRAY)}</pre>
        <Typography.Text strong>输出格式</Typography.Text>
        <pre>{readPromptOutputStructure(version.outputSchemaJson)}</pre>
        <Typography.Text strong>输出格式详情 JSON</Typography.Text>
        <pre>{formatJsonText(version.outputSchemaJson, EMPTY_JSON_OBJECT)}</pre>
        <Typography.Text strong>变更说明</Typography.Text>
        <pre>{version.changeSummary || "-"}</pre>
    </div>
);
