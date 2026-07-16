import { EditOutlined, PlusOutlined, ReloadOutlined, SaveOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Card, Form, Select, Switch, Table, Tag, Tooltip } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import * as service from "./capability-mappings-service";
import type {
    AiCapabilityMappingChangeCommand,
    AiCapabilityQuery
} from "./capability-mappings-service";
import type {
    AiCapabilityMappingRecord,
    AiCapabilityModelRecord,
    AiCapabilityRecord
} from "./capability-mappings-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./capability-mappings-page.css";

type MappingFormValues = AiCapabilityMappingChangeCommand;

interface MappingTableRow extends AiCapabilityMappingRecord {
    capabilityName: string;
    requiredTags: string[];
    outputMode: string;
    modelName: string;
    modelTags: string[];
}

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

const parseEnabled = (value?: string) => {
    if (value === "true") {
        return true;
    }
    if (value === "false") {
        return false;
    }
    return null;
};

const formatModelLabel = (model: AiCapabilityModelRecord) => {
    const displayName = model.displayName || model.modelName;
    return `${displayName} / ${model.modelName}`;
};

const buildTagMatch = (
    capability: AiCapabilityRecord | undefined,
    model: AiCapabilityModelRecord | undefined
) => {
    const requiredTags = capability?.requiredTags || [];
    const modelTags = model?.capabilityTags || [];
    const missingTags = requiredTags.filter((tag) => !modelTags.includes(tag));
    return {
        requiredTags,
        modelTags,
        missingTags,
        matched: missingTags.length === 0
    };
};

export const CapabilityMappingsPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const [form] = Form.useForm<MappingFormValues>();
    const canViewConfig = hasPermission("ai:config:view");
    const canEditConfig = hasPermission("ai:config:edit");
    const [query, setQuery] = useState<AiCapabilityQuery>({});
    const [editingMapping, setEditingMapping] = useState<AiCapabilityMappingRecord | null>(null);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const selectedCapabilityCode = Form.useWatch("capability", form);
    const selectedModelId = Form.useWatch("modelId", form);

    const capabilitiesQuery = useQuery({
        queryKey: ["ai", "capability-mappings", "capabilities"],
        queryFn: () => service.listCapabilities({ enabled: true }),
        enabled: canViewConfig,
        retry: false
    });

    const mappingsQuery = useQuery({
        queryKey: ["ai", "capability-mappings", query],
        queryFn: () => service.listCapabilityMappings(query),
        enabled: canViewConfig,
        retry: false
    });

    const modelsQuery = useQuery({
        queryKey: ["ai", "capability-mappings", "enabled-models"],
        queryFn: () => service.listEnabledModels({ enabled: true }),
        enabled: canViewConfig,
        retry: false
    });

    const capabilityByCode = useMemo(() => {
        return new Map((capabilitiesQuery.data || []).map((record) => [record.capability, record]));
    }, [capabilitiesQuery.data]);

    const modelById = useMemo(() => {
        return new Map((modelsQuery.data || []).map((record) => [record.modelId, record]));
    }, [modelsQuery.data]);

    const capabilityOptions = useMemo(() => {
        return (capabilitiesQuery.data || []).map((record) => ({
            label: `${record.name} / ${record.capability}`,
            value: record.capability
        }));
    }, [capabilitiesQuery.data]);

    const modelOptions = useMemo(() => {
        return (modelsQuery.data || []).map((record) => ({
            label: formatModelLabel(record),
            value: record.modelId
        }));
    }, [modelsQuery.data]);

    const tableData = useMemo<MappingTableRow[]>(() => {
        return (mappingsQuery.data || []).map((mapping) => {
            const capability = capabilityByCode.get(mapping.capability);
            const model = modelById.get(mapping.modelId);
            return {
                ...mapping,
                capabilityName: capability?.name || mapping.capability,
                requiredTags: capability?.requiredTags || [],
                outputMode: capability?.outputMode || "-",
                modelName: model ? formatModelLabel(model) : String(mapping.modelId),
                modelTags: model?.capabilityTags || []
            };
        });
    }, [capabilityByCode, mappingsQuery.data, modelById]);

    const selectedCapability = selectedCapabilityCode
        ? capabilityByCode.get(selectedCapabilityCode)
        : undefined;
    const selectedModel = selectedModelId ? modelById.get(selectedModelId) : undefined;
    const tagMatch = buildTagMatch(selectedCapability, selectedModel);

    const invalidateMappings = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "capability-mappings"] });
    };

    const changeMutation = useMutation({
        mutationFn: service.changeCapabilityMapping,
        onSuccess: async () => {
            await invalidateMappings();
            setDrawerOpen(false);
            setEditingMapping(null);
            message.success("能力映射已保存");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "能力映射保存失败");
        }
    });

    useEffect(() => {
        if (mappingsQuery.isError) {
            const error = mappingsQuery.error;
            message.error(error instanceof Error ? error.message : "能力映射列表加载失败");
        }
    }, [message, mappingsQuery.error, mappingsQuery.isError]);

    const openCreate = () => {
        setEditingMapping(null);
        form.setFieldsValue({
            mappingId: null,
            scope: query.scope || SCOPE_OPTIONS[0]?.value,
            capability: query.capability || capabilityOptions[0]?.value,
            modelId: modelOptions[0]?.value,
            enabled: true
        });
        setDrawerOpen(true);
    };

    const openEdit = (record: AiCapabilityMappingRecord) => {
        setEditingMapping(record);
        form.setFieldsValue({
            mappingId: record.mappingId,
            scope: record.scope,
            capability: record.capability,
            modelId: record.modelId,
            enabled: record.enabled
        });
        setDrawerOpen(true);
    };

    const submitForm = async () => {
        const values = await form.validateFields();
        await changeMutation.mutateAsync({
            mappingId: editingMapping?.mappingId || values.mappingId || null,
            scope: values.scope,
            capability: values.capability,
            modelId: values.modelId,
            enabled: values.enabled
        });
    };

    const changeEnabled = async (record: AiCapabilityMappingRecord, enabled: boolean) => {
        await changeMutation.mutateAsync({
            mappingId: record.mappingId || null,
            scope: record.scope,
            capability: record.capability,
            modelId: record.modelId,
            enabled
        });
    };

    const columns: ColumnsType<MappingTableRow> = [
        {
            title: "scope",
            dataIndex: "scope",
            key: "scope"
        },
        {
            title: "capability",
            dataIndex: "capability",
            key: "capability"
        },
        {
            title: "capabilityName",
            dataIndex: "capabilityName",
            key: "capabilityName"
        },
        {
            title: "requiredTags",
            dataIndex: "requiredTags",
            key: "requiredTags",
            render: (tags: string[] = []) => (
                <KuzhambuSpace>
                    {tags.map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                    ))}
                </KuzhambuSpace>
            )
        },
        {
            title: "outputMode",
            dataIndex: "outputMode",
            key: "outputMode"
        },
        {
            title: "modelName",
            dataIndex: "modelName",
            key: "modelName"
        },
        {
            title: "enabled",
            dataIndex: "enabled",
            key: "enabled",
            render: (enabled: boolean) => (
                <Tag color={enabled ? "green" : "default"}>{enabled ? "启用" : "禁用"}</Tag>
            )
        },
        {
            title: "configuredAt",
            dataIndex: "configuredAt",
            key: "configuredAt",
            render: formatDateTime
        },
        {
            key: "actions",
            render: (_, record) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="ai-capability-mappings-capability-mappings-configure-model-button"
                        icon={<EditOutlined />}
                        disabled={!canEditConfig}
                        onClick={() => openEdit(record)}
                    >
                        配置模型
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="ai-capability-mappings-capability-mappings-disable-or-enable-button"
                        disabled={!canEditConfig}
                        onClick={() => void changeEnabled(record, !record.enabled)}
                    >
                        {record.enabled ? "禁用" : "启用"}
                    </KuzhambuButton>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <KuzhambuPage
            className="capability-mappings-page"
            eyebrow="AI"
            title="AI 能力映射"
            description="配置 scope + capability 到启用模型的治理映射"
            actions={
                <KuzhambuSpace>
                    <Tooltip title="刷新">
                        <KuzhambuButton
                            testId="ai-capability-mappings-capability-mappings-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={
                                mappingsQuery.isFetching ||
                                capabilitiesQuery.isFetching ||
                                modelsQuery.isFetching
                            }
                            onClick={() => void invalidateMappings()}
                        />
                    </Tooltip>
                    <KuzhambuButton
                        testId="ai-capability-mappings-capability-mappings-create-mapping-button"
                        type="primary"
                        icon={<PlusOutlined />}
                        disabled={!canEditConfig}
                        onClick={openCreate}
                    >
                        新增映射
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
        >
            <Card className="capability-mappings-filter-card">
                <Form layout="inline" className="capability-mappings-filter-form">
                    <Form.Item label="scope">
                        <Select
                            allowClear
                            className="capability-mappings-filter-control"
                            options={SCOPE_OPTIONS}
                            value={query.scope ?? undefined}
                            onChange={(scope) =>
                                setQuery((current) => ({
                                    ...current,
                                    scope: scope ?? null
                                }))
                            }
                        />
                    </Form.Item>
                    <Form.Item label="capability">
                        <Select
                            allowClear
                            className="capability-mappings-filter-control"
                            options={capabilityOptions}
                            value={query.capability ?? undefined}
                            onChange={(capability) =>
                                setQuery((current) => ({
                                    ...current,
                                    capability: capability ?? null
                                }))
                            }
                        />
                    </Form.Item>
                    <Form.Item label="enabled">
                        <Select
                            allowClear
                            className="capability-mappings-filter-control"
                            value={query.enabled == null ? undefined : String(query.enabled)}
                            options={[
                                { label: "启用", value: "true" },
                                { label: "禁用", value: "false" }
                            ]}
                            onChange={(enabled) =>
                                setQuery((current) => ({
                                    ...current,
                                    enabled: parseEnabled(enabled)
                                }))
                            }
                        />
                    </Form.Item>
                    <Form.Item>
                        <KuzhambuButton
                            testId="ai-capability-mappings-capability-mappings-reset-button"
                            onClick={() => setQuery({})}
                        >
                            重置
                        </KuzhambuButton>
                    </Form.Item>
                </Form>
            </Card>

            <Table<MappingTableRow>
                aria-label="AI 能力映射列表"
                rowKey={(record) => record.mappingId || `${record.scope}-${record.capability}`}
                className="capability-mappings-table"
                columns={columns}
                dataSource={tableData}
                loading={
                    mappingsQuery.isFetching ||
                    capabilitiesQuery.isFetching ||
                    modelsQuery.isFetching
                }
                pagination={{ pageSize: 10, showSizeChanger: true }}
            />

            <KuzhambuDrawer
                open={drawerOpen}
                title={editingMapping ? "配置模型" : "新增映射"}
                size="large"
                onClose={() => setDrawerOpen(false)}
                footer={
                    <KuzhambuSpace>
                        <KuzhambuButton
                            testId="ai-capability-mappings-capability-mappings-cancel-button"
                            onClick={() => setDrawerOpen(false)}
                        >
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-capability-mappings-capability-mappings-save-button"
                            type="primary"
                            icon={<SaveOutlined />}
                            disabled={!canEditConfig}
                            loading={changeMutation.isPending}
                            onClick={() => void submitForm()}
                        >
                            保存
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
            >
                <Form form={form} layout="vertical" className="capability-mappings-form">
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
                        label="modelId"
                        name="modelId"
                        rules={[{ required: true, message: "请选择启用模型" }]}
                    >
                        <Select options={modelOptions} />
                    </Form.Item>
                    <div className="capability-mappings-match">
                        <div className="capability-mappings-match-row">
                            <span>requiredTags</span>
                            <KuzhambuSpace>
                                {tagMatch.requiredTags.map((tag) => (
                                    <Tag key={tag}>{tag}</Tag>
                                ))}
                                {tagMatch.requiredTags.length === 0 ? "-" : null}
                            </KuzhambuSpace>
                        </div>
                        <div className="capability-mappings-match-row">
                            <span>modelTags</span>
                            <KuzhambuSpace>
                                {tagMatch.modelTags.map((tag) => (
                                    <Tag key={tag}>{tag}</Tag>
                                ))}
                                {tagMatch.modelTags.length === 0 ? "-" : null}
                            </KuzhambuSpace>
                        </div>
                        <Alert
                            type={tagMatch.matched ? "success" : "warning"}
                            showIcon
                            message={
                                tagMatch.matched
                                    ? "能力标签匹配"
                                    : `缺少标签：${tagMatch.missingTags.join(", ")}`
                            }
                        />
                    </div>
                    <Form.Item label="enabled" name="enabled" valuePropName="checked">
                        <Switch checkedChildren="启用" unCheckedChildren="禁用" />
                    </Form.Item>
                </Form>
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
