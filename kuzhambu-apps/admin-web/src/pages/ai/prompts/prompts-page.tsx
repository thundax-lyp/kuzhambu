import {
    BranchesOutlined,
    CheckCircleOutlined,
    EyeOutlined,
    ReloadOutlined,
    RetweetOutlined,
    SaveOutlined,
    ThunderboltOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    App,
    Button,
    Card,
    Descriptions,
    Form,
    Input,
    Popconfirm,
    Select,
    Table,
    Tag,
    Tooltip,
    Typography
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import * as service from "./prompts-service";
import type { AiPromptTemplateChangeCommand, AiPromptTemplateQuery } from "./prompts-service";
import type {
    AiPromptTemplateRecord,
    AiPromptVariableRecord,
    AiPromptVersionRecord
} from "./prompts-types";
import "./prompts-page.css";

type PromptFormValues = Omit<AiPromptTemplateChangeCommand, "variables">;

const EMPTY_JSON_ARRAY = "[]";
const EMPTY_JSON_OBJECT = "{}";
const SCOPE_OPTIONS = [
    { label: "classics", value: "classics" },
    { label: "knowledge", value: "knowledge" },
    { label: "discovery", value: "discovery" },
    { label: "platform", value: "platform" }
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

const normalizeJsonText = (value?: string | null, fallback = EMPTY_JSON_OBJECT) => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : fallback;
};

const parseJson = (value: string) => {
    return JSON.parse(value) as unknown;
};

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
                variableName: String(item.variableName || ""),
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

const versionTitle = (version: AiPromptVersionRecord) => {
    return `版本 ${version.versionNo ?? "-"}`;
};

export const PromptsPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const [filterForm] = Form.useForm<AiPromptTemplateQuery>();
    const [editorForm] = Form.useForm<PromptFormValues>();
    const canViewPrompt = hasPermission("ai:prompt:view");
    const canEditPrompt = hasPermission("ai:prompt:edit");
    const [query, setQuery] = useState<AiPromptTemplateQuery>({});
    const [viewVersion, setViewVersion] = useState<AiPromptVersionRecord | null>(null);
    const [compareVersions, setCompareVersions] = useState<AiPromptVersionRecord[]>([]);
    const [suggestionVersion, setSuggestionVersion] = useState<AiPromptVersionRecord | null>(null);
    const variablesSnapshotJson = Form.useWatch("variablesSnapshotJson", editorForm);
    const currentTemplateId = Form.useWatch("templateId", editorForm);
    const currentChangeSummary = Form.useWatch("changeSummary", editorForm);

    const capabilitiesQuery = useQuery({
        queryKey: ["ai", "prompts", "capabilities"],
        queryFn: service.listPromptCapabilities,
        enabled: canViewPrompt,
        retry: false
    });

    const templateQuery = useQuery({
        queryKey: ["ai", "prompts", "template", query],
        queryFn: () => service.getPromptTemplateByScope(query),
        enabled: Boolean(query.scope && query.capability) && canViewPrompt,
        retry: false
    });

    const templateId = templateQuery.data?.templateId || null;

    const currentVersionQuery = useQuery({
        queryKey: ["ai", "prompts", "current-version", templateId],
        queryFn: () => service.getCurrentPromptVersion(templateId || 0),
        enabled: Boolean(templateId) && canViewPrompt,
        retry: false
    });

    const versionsQuery = useQuery({
        queryKey: ["ai", "prompts", "versions", templateId],
        queryFn: () => service.listPromptVersions(templateId || 0),
        enabled: Boolean(templateId) && canViewPrompt,
        retry: false
    });

    const variablesQuery = useQuery({
        queryKey: ["ai", "prompts", "variables", templateId],
        queryFn: () => service.listPromptVariables(templateId || 0),
        enabled: Boolean(templateId) && canViewPrompt,
        retry: false
    });

    const actionStatusQuery = useQuery({
        queryKey: ["ai", "prompts", "action-status", query],
        queryFn: () => service.getPromptActionStatus(query),
        enabled: Boolean(query.scope && query.capability) && canViewPrompt,
        retry: false
    });

    const capabilityOptions = useMemo(() => {
        return (capabilitiesQuery.data || []).map((record) => ({
            label: `${record.name} / ${record.capability}`,
            value: record.capability
        }));
    }, [capabilitiesQuery.data]);

    useEffect(() => {
        if (!filterForm.getFieldValue("scope")) {
            filterForm.setFieldValue("scope", SCOPE_OPTIONS[0]?.value);
        }
        if (capabilityOptions.length > 0 && !filterForm.getFieldValue("capability")) {
            filterForm.setFieldValue("capability", capabilityOptions[0].value);
        }
    }, [capabilityOptions, filterForm]);

    const variableRows = useMemo(() => {
        const parsedVariables = toVariableRows(variablesSnapshotJson);
        if (parsedVariables.length > 0) {
            return parsedVariables;
        }
        return variablesQuery.data || [];
    }, [variablesQuery.data, variablesSnapshotJson]);

    const invalidatePrompt = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "prompts"] });
    };

    useEffect(() => {
        const template = templateQuery.data;
        const currentVersion = currentVersionQuery.data;
        if (!template) {
            return;
        }
        editorForm.setFieldsValue({
            templateId: template.templateId || null,
            scope: template.scope || query.scope || "",
            capability: template.capability || query.capability || "",
            name: template.name || "",
            description: template.description || "",
            status: template.status || "ACTIVE",
            messageTemplatesJson: formatJsonText(
                currentVersion?.messageTemplatesJson,
                EMPTY_JSON_ARRAY
            ),
            variablesSnapshotJson: variablesToJson(variablesQuery.data || []),
            outputSchemaJson: formatJsonText(currentVersion?.outputSchemaJson, EMPTY_JSON_OBJECT),
            changeSummary: ""
        });
    }, [
        currentVersionQuery.data,
        editorForm,
        query.capability,
        query.scope,
        templateQuery.data,
        variablesQuery.data
    ]);

    const changeMutation = useMutation({
        mutationFn: service.changePromptTemplate,
        onSuccess: async (template) => {
            setQuery({
                scope: template.scope || query.scope,
                capability: template.capability || query.capability
            });
            await invalidatePrompt();
            message.success("提示词模板已保存");
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
        onSuccess: (versions) => {
            setCompareVersions(versions);
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "版本对比失败");
        }
    });

    const rollbackMutation = useMutation({
        mutationFn: service.changePromptVersionRollback,
        onSuccess: async () => {
            await invalidatePrompt();
            message.success("提示词版本已回滚");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "提示词版本回滚失败");
        }
    });

    const suggestionMutation = useMutation({
        mutationFn: service.regeneratePromptSuggestion,
        onSuccess: (version) => {
            setSuggestionVersion(version);
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "优化建议生成失败");
        }
    });

    const submitTemplate = async (overrideVersion?: AiPromptVersionRecord | null) => {
        const values = await editorForm.validateFields();
        const messageTemplatesJson =
            overrideVersion?.messageTemplatesJson || values.messageTemplatesJson;
        const outputSchemaJson = overrideVersion?.outputSchemaJson || values.outputSchemaJson;
        const variablesSnapshot =
            overrideVersion?.variablesSnapshotJson || values.variablesSnapshotJson;
        const variables = toVariableRows(variablesSnapshot);
        await changeMutation.mutateAsync({
            templateId: values.templateId || null,
            scope: values.scope,
            capability: values.capability,
            name: values.name,
            description: values.description || null,
            status: values.status || "ACTIVE",
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
            templateId: currentTemplateId,
            providedNames: variableRows.map((variable) => variable.variableName)
        });
    };

    const generateSuggestion = async () => {
        if (!currentTemplateId) {
            message.warning("请先保存模板后再生成优化建议");
            return;
        }
        await suggestionMutation.mutateAsync({
            templateId: currentTemplateId,
            changeSummary: currentChangeSummary || "生成优化建议"
        });
    };

    const compareWithCurrent = async (version: AiPromptVersionRecord) => {
        if (!templateId || !version.versionNo || !templateQuery.data?.currentVersionNo) {
            return;
        }
        await compareMutation.mutateAsync({
            templateId,
            leftVersionNo: version.versionNo,
            rightVersionNo: templateQuery.data.currentVersionNo
        });
    };

    const resetFilter = () => {
        filterForm.resetFields();
        setQuery({});
    };

    const applyFilter = async () => {
        const values = await filterForm.validateFields();
        setQuery(values);
    };

    const variableColumns: ColumnsType<AiPromptVariableRecord> = [
        {
            title: "variableName",
            dataIndex: "variableName",
            key: "variableName"
        },
        {
            title: "required",
            dataIndex: "required",
            key: "required",
            render: (required: boolean) => (
                <Tag color={required ? "red" : "default"}>{required ? "必填" : "可选"}</Tag>
            )
        },
        {
            title: "description",
            dataIndex: "description",
            key: "description",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "priority",
            dataIndex: "priority",
            key: "priority",
            render: (value?: number | null) => value ?? "-"
        }
    ];

    const versionColumns: ColumnsType<AiPromptVersionRecord> = [
        {
            title: "versionNo",
            dataIndex: "versionNo",
            key: "versionNo"
        },
        {
            title: "current",
            dataIndex: "current",
            key: "current",
            render: (current: boolean) => (
                <Tag color={current ? "green" : "default"}>{current ? "当前" : "历史"}</Tag>
            )
        },
        {
            title: "changeSummary",
            dataIndex: "changeSummary",
            key: "changeSummary",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "registeredAt",
            dataIndex: "registeredAt",
            key: "registeredAt",
            render: formatDateTime
        },
        {
            key: "actions",
            render: (_, record) => (
                <KuzhambuSpaceCompact>
                    <Button icon={<EyeOutlined />} onClick={() => setViewVersion(record)}>
                        查看
                    </Button>
                    <Button
                        icon={<BranchesOutlined />}
                        disabled={!templateQuery.data?.currentVersionNo}
                        onClick={() => void compareWithCurrent(record)}
                    >
                        对比
                    </Button>
                    <Popconfirm
                        title="回滚版本"
                        description={`确认回滚到版本 ${record.versionNo}？`}
                        okText="回滚"
                        cancelText="取消"
                        disabled={!canEditPrompt || record.current === true}
                        onConfirm={() => {
                            if (templateId && record.versionNo) {
                                rollbackMutation.mutate({
                                    templateId,
                                    versionNo: record.versionNo
                                });
                            }
                        }}
                    >
                        <Button
                            icon={<RetweetOutlined />}
                            disabled={!canEditPrompt || record.current === true}
                        >
                            回滚
                        </Button>
                    </Popconfirm>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    const template = templateQuery.data || ({} as AiPromptTemplateRecord);
    const actionStatus = actionStatusQuery.data;

    return (
        <KuzhambuPage
            className="prompts-page"
            eyebrow="AI"
            title="AI 提示词版本"
            description="管理提示词模板、变量、版本对比和回滚"
            actions={
                <Tooltip title="刷新">
                    <Button
                        aria-label="刷新"
                        icon={<ReloadOutlined />}
                        loading={
                            templateQuery.isFetching ||
                            currentVersionQuery.isFetching ||
                            versionsQuery.isFetching ||
                            variablesQuery.isFetching
                        }
                        onClick={() => void invalidatePrompt()}
                    />
                </Tooltip>
            }
        >
            <Card className="prompts-filter-card">
                <Form
                    form={filterForm}
                    layout="inline"
                    className="prompts-filter-form"
                    initialValues={{ scope: SCOPE_OPTIONS[0]?.value }}
                >
                    <Form.Item
                        label="scope"
                        name="scope"
                        rules={[{ required: true, message: "请选择 scope" }]}
                    >
                        <Select
                            aria-label="筛选 scope"
                            className="prompts-filter-control"
                            options={SCOPE_OPTIONS}
                        />
                    </Form.Item>
                    <Form.Item
                        label="capability"
                        name="capability"
                        rules={[{ required: true, message: "请选择 capability" }]}
                    >
                        <Select
                            aria-label="筛选 capability"
                            className="prompts-filter-control"
                            options={capabilityOptions}
                        />
                    </Form.Item>
                    <Form.Item>
                        <KuzhambuSpace>
                            <Button type="primary" onClick={() => void applyFilter()}>
                                查询
                            </Button>
                            <Button onClick={resetFilter}>重置</Button>
                        </KuzhambuSpace>
                    </Form.Item>
                </Form>
            </Card>

            <div className="prompts-workbench">
                <Card className="prompts-template-card" title="模板信息">
                    <Descriptions column={1} size="small">
                        <Descriptions.Item label="templateId">
                            {template.templateId || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="name">{template.name || "-"}</Descriptions.Item>
                        <Descriptions.Item label="description">
                            {template.description || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="status">
                            {template.status || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="currentVersionNo">
                            {template.currentVersionNo || "-"}
                        </Descriptions.Item>
                        <Descriptions.Item label="registeredAt">
                            {formatDateTime(template.registeredAt)}
                        </Descriptions.Item>
                        <Descriptions.Item label="actionStatus">
                            <Tag color={actionStatus?.available ? "green" : "red"}>
                                {actionStatus?.available ? "可用" : "不可用"}
                            </Tag>
                            {actionStatus?.unavailableReason || ""}
                        </Descriptions.Item>
                    </Descriptions>
                </Card>

                <Card className="prompts-editor-card" title="版本编辑">
                    <Form form={editorForm} layout="vertical" className="prompts-editor-form">
                        <Form.Item name="templateId" hidden>
                            <Input />
                        </Form.Item>
                        <div className="prompts-editor-grid">
                            <Form.Item
                                label="scope"
                                name="scope"
                                rules={[{ required: true, message: "请选择 scope" }]}
                            >
                                <Select options={SCOPE_OPTIONS} />
                            </Form.Item>
                            <Form.Item
                                label="capability"
                                name="capability"
                                rules={[{ required: true, message: "请选择 capability" }]}
                            >
                                <Select options={capabilityOptions} />
                            </Form.Item>
                            <Form.Item
                                label="name"
                                name="name"
                                rules={[{ required: true, message: "请输入模板名称" }]}
                            >
                                <Input />
                            </Form.Item>
                            <Form.Item label="status" name="status">
                                <Select
                                    options={[
                                        { label: "ACTIVE", value: "ACTIVE" },
                                        { label: "DISABLED", value: "DISABLED" }
                                    ]}
                                />
                            </Form.Item>
                        </div>
                        <Form.Item label="description" name="description">
                            <Input.TextArea rows={2} />
                        </Form.Item>
                        <Form.Item
                            label="messageTemplatesJson"
                            name="messageTemplatesJson"
                            rules={[
                                { required: true, message: "请输入消息模板 JSON" },
                                { validator: (_, value) => assertJsonText(value, EMPTY_JSON_ARRAY) }
                            ]}
                        >
                            <Input.TextArea rows={8} />
                        </Form.Item>
                        <Form.Item
                            label="variablesSnapshotJson"
                            name="variablesSnapshotJson"
                            rules={[
                                { validator: (_, value) => assertJsonText(value, EMPTY_JSON_ARRAY) }
                            ]}
                        >
                            <Input.TextArea rows={5} />
                        </Form.Item>
                        <Table<AiPromptVariableRecord>
                            aria-label="提示词变量预览"
                            rowKey={(record) => record.variableId || record.variableName}
                            columns={variableColumns}
                            dataSource={variableRows}
                            pagination={false}
                            size="small"
                        />
                        <Form.Item
                            label="outputSchemaJson"
                            name="outputSchemaJson"
                            rules={[
                                {
                                    validator: (_, value) =>
                                        assertJsonText(value, EMPTY_JSON_OBJECT)
                                }
                            ]}
                        >
                            <Input.TextArea rows={5} />
                        </Form.Item>
                        <Form.Item label="changeSummary" name="changeSummary">
                            <Input />
                        </Form.Item>
                        <KuzhambuSpace>
                            <Button
                                icon={<CheckCircleOutlined />}
                                disabled={!currentTemplateId}
                                loading={validateMutation.isPending}
                                onClick={() => void validateVariables()}
                            >
                                校验变量
                            </Button>
                            <Button
                                type="primary"
                                icon={<SaveOutlined />}
                                disabled={!canEditPrompt}
                                loading={changeMutation.isPending}
                                onClick={() => void submitTemplate()}
                            >
                                保存新版本
                            </Button>
                            <Button
                                icon={<ThunderboltOutlined />}
                                disabled={!canEditPrompt || !currentTemplateId}
                                loading={suggestionMutation.isPending}
                                onClick={() => void generateSuggestion()}
                            >
                                生成优化建议
                            </Button>
                        </KuzhambuSpace>
                    </Form>
                </Card>

                <Card className="prompts-version-card" title="版本列表">
                    <Table<AiPromptVersionRecord>
                        aria-label="提示词版本列表"
                        rowKey={(record) =>
                            record.promptVersionId || `${record.templateId}-${record.versionNo}`
                        }
                        columns={versionColumns}
                        dataSource={versionsQuery.data || []}
                        loading={versionsQuery.isFetching}
                        pagination={false}
                        size="small"
                    />
                </Card>
            </div>

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
                    <KuzhambuSpace>
                        <Button onClick={() => setSuggestionVersion(null)}>放弃</Button>
                        <Button
                            type="primary"
                            icon={<SaveOutlined />}
                            disabled={!canEditPrompt}
                            loading={changeMutation.isPending}
                            onClick={() => void submitTemplate(suggestionVersion)}
                        >
                            应用为新版本
                        </Button>
                    </KuzhambuSpace>
                }
            >
                {suggestionVersion ? <VersionDetail version={suggestionVersion} /> : null}
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};

const VersionDetail = ({ version }: { version: AiPromptVersionRecord }) => (
    <div className="prompts-version-detail">
        <Typography.Title level={5}>{versionTitle(version)}</Typography.Title>
        <Typography.Text strong>messageTemplatesJson</Typography.Text>
        <pre>{formatJsonText(version.messageTemplatesJson, EMPTY_JSON_ARRAY)}</pre>
        <Typography.Text strong>variablesSnapshotJson</Typography.Text>
        <pre>{formatJsonText(version.variablesSnapshotJson, EMPTY_JSON_ARRAY)}</pre>
        <Typography.Text strong>outputSchemaJson</Typography.Text>
        <pre>{formatJsonText(version.outputSchemaJson, EMPTY_JSON_OBJECT)}</pre>
        <Typography.Text strong>changeSummary</Typography.Text>
        <pre>{version.changeSummary || "-"}</pre>
    </div>
);
