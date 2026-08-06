import { CheckCircleOutlined, DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Form, Input, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useRef, useState } from "react";
import {
    KuzhambuButton,
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormHiddenItem,
    KuzhambuFormItem,
    KuzhambuModal,
    KuzhambuSpace,
    KuzhambuSpaceCompact,
    KuzhambuSwitch,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuTag,
    KuzhambuSelect
} from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";

import type { AiPromptTemplateChangeCommand } from "@/pages/ai/prompt/prompt-service";
import * as service from "@/pages/ai/prompt/prompt-service";

import type {
    AiPromptTemplateRecord,
    AiPromptCapabilityVariableRecord,
    AiPromptVariableRecord,
    AiPromptVersionRecord
} from "@/pages/ai/prompt/prompt-types";
import "./prompt-edit-drawer.css";

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
    remoteVariables: AiPromptVariableRecord[] = [],
    capabilityVariables: AiPromptCapabilityVariableRecord[] = []
) => {
    const currentRows = toVariableRows(variablesSnapshotJson);
    const sourceRows = currentRows.length > 0 ? currentRows : remoteVariables;
    const rowByName = new Map(sourceRows.map((variable) => [variable.variableName, variable]));
    const capabilityVariableByName = new Map(
        capabilityVariables.map((variable) => [variable.variableName, variable])
    );
    const placeholderRows = extractPromptVariableNames(messageTemplatesJson).map(
        (variableName, index) => {
            const existing = rowByName.get(variableName);
            const capabilityVariable = capabilityVariableByName.get(variableName);
            return {
                variableName,
                required: capabilityVariable?.required ?? existing?.required !== false,
                description: existing?.description || capabilityVariable?.description || "",
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
                            <KuzhambuSelect
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
                                    testId="ai-prompt-prompt-add-message-button"
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
                                    testId="ai-prompt-prompt-remove-message-button"
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

const PromptVersionSection = ({
    canEdit,
    template
}: {
    canEdit: boolean;
    template: AiPromptTemplateRecord;
}) => {
    const { message } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [viewVersion, setViewVersion] = useState<AiPromptVersionRecord | null>(null);
    const [compareVersions, setCompareVersions] = useState<AiPromptVersionRecord[]>([]);
    const templateId = template.id || null;
    const versionsQuery = useQuery({
        queryKey: ["ai", "prompt", "versions", templateId],
        queryFn: () => service.listPromptVersions(templateId || ""),
        enabled: Boolean(templateId),
        retry: false
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
            await queryClient.invalidateQueries({ queryKey: ["ai", "prompt"] });
            message.success("提示词版本已回滚");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "提示词版本回滚失败");
        }
    });
    const columns: KuzhambuTableProps<AiPromptVersionRecord>["columns"] = [
        { title: "版本号", dataIndex: "versionNo", key: "versionNo", width: 96 },
        {
            title: "状态",
            key: "current",
            width: 96,
            render: (_, record) => {
                const current = record.versionNo === template.currentVersionNo;
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
                const current = record.versionNo === template.currentVersionNo;
                return [
                    { key: "view", text: "查看", onClick: () => setViewVersion(record) },
                    {
                        key: "compare",
                        text: "对比",
                        disabled: !template.currentVersionNo,
                        onClick: () => {
                            if (templateId && record.versionNo && template.currentVersionNo) {
                                compareMutation.mutate({
                                    id: templateId,
                                    leftVersionNo: record.versionNo,
                                    rightVersionNo: template.currentVersionNo
                                });
                            }
                        }
                    },
                    {
                        key: "rollback",
                        text: "回滚",
                        type: "warning",
                        disabled: !canEdit || current,
                        onClick: () =>
                            confirm.danger({
                                title: "回滚版本",
                                message: `确认回滚到版本 ${record.versionNo}？`,
                                okText: "回滚",
                                onConfirm: () =>
                                    rollbackMutation.mutateAsync({
                                        id: templateId || "",
                                        versionNo: record.versionNo || 0
                                    })
                            })
                    }
                ];
            }
        }
    ];

    return (
        <>
            <div className="prompt-version-section">
                <Typography.Title level={5}>版本</Typography.Title>
                <KuzhambuTable<AiPromptVersionRecord>
                    ariaLabel="提示词版本列表"
                    rowKey={(record) => record.id || `${record.templateId}-${record.versionNo}`}
                    columns={columns}
                    dataSource={versionsQuery.data || []}
                    loading={versionsQuery.isFetching}
                    pagination={false}
                    size="small"
                />
            </div>
            <KuzhambuDrawer
                testId="ai-prompt-prompt-editor-2-drawer"
                open={Boolean(viewVersion)}
                title={viewVersion ? versionTitle(viewVersion) : "版本详情"}
                size="large"
                onClose={() => setViewVersion(null)}
            >
                {viewVersion ? <VersionDetail version={viewVersion} /> : null}
            </KuzhambuDrawer>
            <KuzhambuDrawer
                testId="ai-prompt-prompt-editor-3-drawer"
                open={compareVersions.length > 0}
                title="版本对比"
                size="large"
                onClose={() => setCompareVersions([])}
            >
                <div className="prompt-compare-grid">
                    {compareVersions.map((version) => (
                        <VersionDetail key={version.versionNo} version={version} />
                    ))}
                </div>
            </KuzhambuDrawer>
        </>
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
    const queryClient = useQueryClient();
    const [form] = Form.useForm<PromptFormValues>();
    const initializedFormRef = useRef(false);
    const [variableModalOpen, setVariableModalOpen] = useState(false);
    const variablesSnapshotJson = Form.useWatch("variablesSnapshotJson", form);
    const messageTemplatesJson = Form.useWatch("messageTemplatesJson", form);
    const currentCapability = Form.useWatch("capability", form);
    const currentTemplateId = Form.useWatch("id", form);
    const templateId = template?.id || null;

    const currentVersionQuery = useQuery({
        queryKey: ["ai", "prompt", "current-version", templateId],
        queryFn: () => service.getCurrentPromptVersion(templateId || ""),
        enabled: open && Boolean(templateId),
        retry: false
    });

    const variablesQuery = useQuery({
        queryKey: ["ai", "prompt", "variables", templateId],
        queryFn: () => service.listPromptVariables(templateId || ""),
        enabled: open && Boolean(templateId),
        retry: false
    });

    const capabilityVariablesQuery = useQuery({
        queryKey: ["ai", "prompt", "capability-variables", currentCapability],
        queryFn: () => service.listPromptCapabilityVariables(currentCapability || ""),
        enabled: open && Boolean(currentCapability),
        retry: false
    });

    const variableRows = useMemo(() => {
        return mergeVariableRows(
            variablesSnapshotJson,
            messageTemplatesJson,
            variablesQuery.data,
            capabilityVariablesQuery.data
        );
    }, [
        capabilityVariablesQuery.data,
        messageTemplatesJson,
        variablesQuery.data,
        variablesSnapshotJson
    ]);

    const allowedVariableRows = useMemo(
        () => capabilityVariablesQuery.data || [],
        [capabilityVariablesQuery.data]
    );
    const allowedVariableNames = useMemo(
        () => allowedVariableRows.map((variable) => variable.variableName),
        [allowedVariableRows]
    );

    useEffect(() => {
        if (!open) {
            return;
        }
        if (initializedFormRef.current) {
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
            initializedFormRef.current = true;
            return;
        }
        if (currentVersionQuery.isPending || variablesQuery.isPending) {
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
                currentVersion?.variablesSnapshotJson ||
                (variablesQuery.data && variablesQuery.data.length > 0
                    ? variablesToJson(variablesQuery.data)
                    : formatJsonText(currentVersion?.variablesSnapshotJson, EMPTY_JSON_ARRAY)),
            outputSchemaJson,
            outputStructure: readPromptOutputStructure(outputSchemaJson),
            changeSummary: ""
        });
        initializedFormRef.current = true;
    }, [
        currentVersionQuery.data,
        currentVersionQuery.isPending,
        form,
        open,
        template,
        variablesQuery.data,
        variablesQuery.isPending
    ]);

    const changeMutation = useMutation({
        mutationFn: service.changePromptTemplate,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["ai", "prompt"] });
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
        const effectiveCapability = template?.capability || values.capability;
        const placeholderNames = extractPromptVariableNames(messageTemplatesJson);
        const submittedVariableNames = Array.from(
            new Set([...placeholderNames, ...variables.map((variable) => variable.variableName)])
        );
        const variablesSnapshot =
            overrideVersion?.variablesSnapshotJson || variablesToJson(variableRows);
        await changeMutation.mutateAsync({
            id: values.id || null,
            capability: effectiveCapability,
            name: values.name,
            description: values.description || null,
            enabled: enabledFromStatus(values.status),
            messageTemplatesJson: normalizeJsonText(messageTemplatesJson, EMPTY_JSON_ARRAY),
            variablesSnapshotJson: normalizeJsonText(variablesSnapshot, EMPTY_JSON_ARRAY),
            outputSchemaJson: normalizeJsonText(outputSchemaJson, EMPTY_JSON_OBJECT),
            changeSummary:
                values.changeSummary || overrideVersion?.changeSummary || "保存提示词版本",
            variables: variables.filter((variable) =>
                submittedVariableNames.includes(variable.variableName)
            )
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

    const closeDrawer = () => {
        if (changeMutation.isPending) {
            return;
        }
        setVariableModalOpen(false);
        onClose();
    };

    const variableColumns: ColumnsType<AiPromptCapabilityVariableRecord> = [
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

    return (
        <>
            <KuzhambuDrawer
                testId="ai-prompt-prompt-editor-1-drawer"
                className="prompt-edit-drawer"
                open={open}
                title={template ? "编辑提示词" : "新建提示词"}
                size="large"
                onClose={closeDrawer}
                footerActions={[
                    { testId: "ai-prompt-prompt-cancel-button", title: "取消", action: onClose },
                    {
                        testId: "ai-prompt-prompt-save-new-version-button",
                        title: template ? "保存新版本" : "创建模板",
                        type: "primary",
                        disabled: !canEdit,
                        loading: changeMutation.isPending,
                        action: () => void submitTemplate()
                    }
                ]}
            >
                <KuzhambuForm
                    form={form}
                    colon={false}
                    component="div"
                    className="prompt-editor-form"
                >
                    <KuzhambuFormHiddenItem name="id">
                        <Input />
                    </KuzhambuFormHiddenItem>
                    <KuzhambuFormItem
                        label="模板名称"
                        name="name"
                        layoutSize="middle"
                        className="prompt-editor-item-compact"
                        rules={[{ required: true, message: "请输入模板名称" }]}
                    >
                        <Input />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        label="能力"
                        name="capability"
                        layoutSize="middle"
                        className="prompt-editor-item-compact"
                        rules={[{ required: true, message: "请选择能力" }]}
                    >
                        <KuzhambuSelect
                            aria-label="提示词能力"
                            disabled={Boolean(template)}
                            options={capabilityOptions}
                        />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        label="变量"
                        layoutSize="middle"
                        className="prompt-editor-item-compact"
                    >
                        <KuzhambuButton
                            testId="ai-prompt-prompt-view-variables-button"
                            disabled={allowedVariableNames.length === 0}
                            onClick={() => setVariableModalOpen(true)}
                        >
                            查看变量
                        </KuzhambuButton>
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        label="状态"
                        name="status"
                        layoutSize="middle"
                        valuePropName="checked"
                        className="prompt-editor-item-status"
                    >
                        <KuzhambuSwitch
                            checkedChildren="启用"
                            unCheckedChildren="禁用"
                            aria-label="提示词模板状态"
                        />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        label="说明"
                        name="description"
                        layoutSize="large"
                        className="prompt-editor-item-full prompt-editor-item-top"
                    >
                        <Input.TextArea rows={2} />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        label="正文"
                        name="messageTemplatesJson"
                        layoutSize="large"
                        className="prompt-editor-item-top"
                        rules={[
                            { required: true, message: "请输入正文" },
                            { validator: (_, value) => assertJsonText(value, EMPTY_JSON_ARRAY) }
                        ]}
                    >
                        <PromptMarkdownEditor />
                    </KuzhambuFormItem>
                    <KuzhambuFormHiddenItem
                        name="variablesSnapshotJson"
                        rules={[
                            { validator: (_, value) => assertJsonText(value, EMPTY_JSON_ARRAY) }
                        ]}
                    >
                        <Input />
                    </KuzhambuFormHiddenItem>
                    <KuzhambuFormItem
                        label="输出格式"
                        name="outputStructure"
                        layoutSize="small"
                        className="prompt-editor-item-compact"
                    >
                        <KuzhambuSelect
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
                    </KuzhambuFormItem>
                    <KuzhambuFormHiddenItem
                        name="outputSchemaJson"
                        rules={[
                            { validator: (_, value) => assertJsonText(value, TEXT_OUTPUT_SCHEMA) }
                        ]}
                    >
                        <Input />
                    </KuzhambuFormHiddenItem>
                    <KuzhambuFormItem
                        label="变更说明"
                        name="changeSummary"
                        layoutSize="middle"
                        className="prompt-editor-item-wide"
                    >
                        <Input />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        label="辅助操作"
                        layoutSize="large"
                        className="prompt-editor-item-wide"
                    >
                        <KuzhambuSpace wrap>
                            <KuzhambuButton
                                testId="ai-prompt-prompt-validate-variables-button"
                                icon={<CheckCircleOutlined />}
                                disabled={!currentTemplateId}
                                loading={validateMutation.isPending}
                                onClick={() => void validateVariables()}
                            >
                                校验变量
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    </KuzhambuFormItem>
                </KuzhambuForm>
                {template ? <PromptVersionSection canEdit={canEdit} template={template} /> : null}
            </KuzhambuDrawer>

            <KuzhambuModal
                testId="ai-prompt-prompt-variables-modal"
                open={variableModalOpen}
                title="能力变量"
                footer={null}
                onCancel={() => setVariableModalOpen(false)}
            >
                <Table<AiPromptCapabilityVariableRecord>
                    aria-label="能力变量列表"
                    rowKey={(record) => record.variableName}
                    columns={variableColumns}
                    dataSource={allowedVariableRows}
                    loading={capabilityVariablesQuery.isFetching}
                    pagination={false}
                    showHeader={false}
                    size="small"
                />
            </KuzhambuModal>
        </>
    );
};

const VersionDetail = ({ version }: { version: AiPromptVersionRecord }) => (
    <div className="prompt-version-detail">
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
