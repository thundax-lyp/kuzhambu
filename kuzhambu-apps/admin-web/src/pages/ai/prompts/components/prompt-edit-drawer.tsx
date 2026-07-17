import {
    BranchesOutlined,
    CheckCircleOutlined,
    EyeOutlined,
    RetweetOutlined,
    SaveOutlined,
    ThunderboltOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Form, Input, Popconfirm, Select, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { AiPromptTemplateChangeCommand } from "../prompts-service";
import * as service from "../prompts-service";
import type {
    AiPromptTemplateRecord,
    AiPromptVariableRecord,
    AiPromptVersionRecord
} from "../prompts-types";

type PromptFormValues = Omit<AiPromptTemplateChangeCommand, "variables" | "enabled"> & {
    status?: string | null;
};

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

const statusFromEnabled = (enabled?: boolean | null) => (enabled === false ? "INACTIVE" : "ACTIVE");

const enabledFromStatus = (status?: string | null) => status !== "INACTIVE";

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
                            <KuzhambuSpaceCompact>
                                <KuzhambuButton
                                    testId="ai-prompts-prompts-add-message-button"
                                    onClick={() => {
                                        const nextMessages = [...messages];
                                        nextMessages.splice(index + 1, 0, {
                                            role: "user",
                                            content: ""
                                        });
                                        updateMessages(nextMessages);
                                    }}
                                >
                                    添加
                                </KuzhambuButton>
                                <KuzhambuButton
                                    testId="ai-prompts-prompts-remove-message-button"
                                    disabled={messages.length <= 1}
                                    onClick={() => {
                                        updateMessages(
                                            messages.filter(
                                                (_item, itemIndex) => itemIndex !== index
                                            )
                                        );
                                    }}
                                >
                                    删除
                                </KuzhambuButton>
                            </KuzhambuSpaceCompact>
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
    const queryClient = useQueryClient();
    const [form] = Form.useForm<PromptFormValues>();
    const [viewVersion, setViewVersion] = useState<AiPromptVersionRecord | null>(null);
    const [compareVersions, setCompareVersions] = useState<AiPromptVersionRecord[]>([]);
    const [suggestionVersion, setSuggestionVersion] = useState<AiPromptVersionRecord | null>(null);
    const variablesSnapshotJson = Form.useWatch("variablesSnapshotJson", form);
    const messageTemplatesJson = Form.useWatch("messageTemplatesJson", form);
    const currentTemplateId = Form.useWatch("id", form);
    const currentChangeSummary = Form.useWatch("changeSummary", form);
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
                status: "ACTIVE",
                messageTemplatesJson: promptMessagesToJson([
                    { role: "system", content: "" },
                    { role: "user", content: "" }
                ]),
                variablesSnapshotJson: EMPTY_JSON_ARRAY,
                outputSchemaJson: EMPTY_JSON_OBJECT,
                changeSummary: "创建提示词模板"
            });
            return;
        }
        const currentVersion = currentVersionQuery.data;
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
            outputSchemaJson: formatJsonText(currentVersion?.outputSchemaJson, EMPTY_JSON_OBJECT),
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

    const suggestionMutation = useMutation({
        mutationFn: service.regeneratePromptSuggestion,
        onSuccess: setSuggestionVersion,
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "优化建议生成失败");
        }
    });

    const submitTemplate = async (overrideVersion?: AiPromptVersionRecord | null) => {
        const values = await form.validateFields();
        const messageTemplatesJson =
            overrideVersion?.messageTemplatesJson || values.messageTemplatesJson;
        const outputSchemaJson = overrideVersion?.outputSchemaJson || values.outputSchemaJson;
        const variablesSnapshot =
            overrideVersion?.variablesSnapshotJson || variablesToJson(variableRows);
        const variables = overrideVersion?.variablesSnapshotJson
            ? toVariableRows(overrideVersion.variablesSnapshotJson)
            : variableRows;
        await changeMutation.mutateAsync({
            id: values.id || null,
            capability: values.capability,
            name: values.name,
            description: values.description || null,
            enabled: enabledFromStatus(values.status),
            messageTemplatesJson: normalizeJsonText(messageTemplatesJson, EMPTY_JSON_ARRAY),
            variablesSnapshotJson: normalizeJsonText(variablesSnapshot, EMPTY_JSON_ARRAY),
            outputSchemaJson: normalizeJsonText(outputSchemaJson, EMPTY_JSON_OBJECT),
            changeSummary: values.changeSummary || overrideVersion?.changeSummary || "应用优化建议",
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

    const generateSuggestion = async () => {
        if (!currentTemplateId) {
            message.warning("请先保存模板后再生成优化建议");
            return;
        }
        await suggestionMutation.mutateAsync({
            id: currentTemplateId,
            changeSummary: currentChangeSummary || "生成优化建议"
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

    const closeDrawer = () => {
        if (changeMutation.isPending) {
            return;
        }
        onClose();
    };

    const variableColumns: ColumnsType<AiPromptVariableRecord> = [
        { title: "变量名", dataIndex: "variableName", key: "variableName" },
        {
            title: "是否必填",
            dataIndex: "required",
            key: "required",
            render: (required: boolean) => (
                <KuzhambuTag type={required ? "warning" : "info"}>
                    {required ? "必填" : "可选"}
                </KuzhambuTag>
            )
        },
        {
            title: "说明",
            dataIndex: "description",
            key: "description",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "排序",
            dataIndex: "priority",
            key: "priority",
            render: (value?: number | null) => value ?? "-"
        }
    ];

    const versionColumns: ColumnsType<AiPromptVersionRecord> = [
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
            title: "注册时间",
            dataIndex: "registeredAt",
            key: "registeredAt",
            width: 120,
            render: formatDate
        },
        {
            key: "actions",
            width: 220,
            render: (_, record) => {
                const current = record.versionNo === template?.currentVersionNo;
                return (
                    <KuzhambuSpaceCompact>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-view-button"
                            icon={<EyeOutlined />}
                            onClick={() => setViewVersion(record)}
                        >
                            查看
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-compare-button"
                            icon={<BranchesOutlined />}
                            disabled={!template?.currentVersionNo}
                            onClick={() => void compareWithCurrent(record)}
                        >
                            对比
                        </KuzhambuButton>
                        <Popconfirm
                            title="回滚版本"
                            description={`确认回滚到版本 ${record.versionNo}？`}
                            okText="回滚"
                            cancelText="取消"
                            disabled={!canEdit || current}
                            onConfirm={() => {
                                if (templateId && record.versionNo) {
                                    rollbackMutation.mutate({
                                        id: templateId,
                                        versionNo: record.versionNo
                                    });
                                }
                            }}
                        >
                            <KuzhambuButton
                                testId="ai-prompts-prompts-rollback-button"
                                icon={<RetweetOutlined />}
                                disabled={!canEdit || current}
                            >
                                回滚
                            </KuzhambuButton>
                        </Popconfirm>
                    </KuzhambuSpaceCompact>
                );
            }
        }
    ];

    return (
        <>
            <KuzhambuDrawer
                className="prompt-edit-drawer"
                open={open}
                title={template ? "编辑提示词" : "新建提示词"}
                size="full"
                onClose={closeDrawer}
                footer={
                    <div className="prompts-drawer-footer">
                        <KuzhambuButton testId="ai-prompts-prompts-cancel-button" onClick={onClose}>
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-save-new-version-button"
                            type="primary"
                            icon={<SaveOutlined />}
                            disabled={!canEdit}
                            loading={changeMutation.isPending}
                            onClick={() => void submitTemplate()}
                        >
                            {template ? "保存新版本" : "创建模板"}
                        </KuzhambuButton>
                    </div>
                }
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
                        <Form.Item
                            label="能力"
                            name="capability"
                            className="prompts-editor-item-compact"
                            rules={[{ required: true, message: "请选择能力" }]}
                        >
                            <Select options={capabilityOptions} />
                        </Form.Item>
                        <Form.Item
                            label="状态"
                            name="status"
                            className="prompts-editor-item-status"
                        >
                            <Select
                                options={[
                                    { label: "启用", value: "ACTIVE" },
                                    { label: "禁用", value: "INACTIVE" }
                                ]}
                            />
                        </Form.Item>
                    </div>
                    <Form.Item
                        label="说明"
                        name="description"
                        className="prompts-editor-item-wide prompts-editor-item-top"
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
                    <Table<AiPromptVariableRecord>
                        aria-label="提示词变量预览"
                        rowKey={(record) => record.id || record.variableName}
                        columns={variableColumns}
                        dataSource={variableRows}
                        pagination={false}
                        size="small"
                    />
                    <Form.Item
                        label="输出结构 JSON"
                        name="outputSchemaJson"
                        className="prompts-editor-item-top"
                        rules={[
                            { validator: (_, value) => assertJsonText(value, EMPTY_JSON_OBJECT) }
                        ]}
                    >
                        <Input.TextArea rows={5} />
                    </Form.Item>
                    <Form.Item
                        label="变更说明"
                        name="changeSummary"
                        className="prompts-editor-item-wide"
                    >
                        <Input />
                    </Form.Item>
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
                        <KuzhambuButton
                            testId="ai-prompts-prompts-generate-suggestions-button"
                            icon={<ThunderboltOutlined />}
                            disabled={!canEdit || !currentTemplateId}
                            loading={suggestionMutation.isPending}
                            onClick={() => void generateSuggestion()}
                        >
                            生成优化建议
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </Form>
                {template ? (
                    <div className="prompts-version-section">
                        <Typography.Title level={5}>版本列表</Typography.Title>
                        <Table<AiPromptVersionRecord>
                            aria-label="提示词版本列表"
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

            <KuzhambuDrawer
                open={Boolean(viewVersion)}
                title={viewVersion ? versionTitle(viewVersion) : "版本详情"}
                size="large"
                onClose={() => setViewVersion(null)}
            >
                {viewVersion ? <VersionDetail version={viewVersion} /> : null}
            </KuzhambuDrawer>

            <KuzhambuDrawer
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

            <KuzhambuDrawer
                open={Boolean(suggestionVersion)}
                title="优化建议预览"
                size="large"
                onClose={() => setSuggestionVersion(null)}
                footer={
                    <div className="prompts-drawer-footer">
                        <KuzhambuButton
                            testId="ai-prompts-prompts-discard-button"
                            onClick={() => setSuggestionVersion(null)}
                        >
                            放弃
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-apply-new-version-button"
                            type="primary"
                            icon={<SaveOutlined />}
                            disabled={!canEdit}
                            loading={changeMutation.isPending}
                            onClick={() => void submitTemplate(suggestionVersion)}
                        >
                            应用为新版本
                        </KuzhambuButton>
                    </div>
                }
            >
                {suggestionVersion ? <VersionDetail version={suggestionVersion} /> : null}
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
        <Typography.Text strong>输出结构 JSON</Typography.Text>
        <pre>{formatJsonText(version.outputSchemaJson, EMPTY_JSON_OBJECT)}</pre>
        <Typography.Text strong>变更说明</Typography.Text>
        <pre>{version.changeSummary || "-"}</pre>
    </div>
);
