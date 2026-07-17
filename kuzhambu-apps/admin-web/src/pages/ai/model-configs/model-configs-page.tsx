import {
    DeleteOutlined,
    EditOutlined,
    PlusOutlined,
    ReloadOutlined,
    SaveOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Card, Form, Input, Popconfirm, Select, Switch, Table, Tag, Tooltip } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import * as service from "./model-configs-service";
import type { AiModelChangeCommand, AiModelListQuery } from "./model-configs-service";
import type { AiModelRecord } from "./model-configs-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./model-configs-page.css";

type ModelFormValues = AiModelChangeCommand;

const DEFAULT_PARAMS = "{}";
const API_SOURCE_OPTIONS = ["OPENAI", "BYTEDANCE"];
const MODEL_CAPABILITY_OPTIONS = ["TEXT2TEXT", "TEXT2IMAGE", "IMAGE2TEXT", "IMAGE2IMAGE"];

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

const normalizeJsonText = (value?: string | null) => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : DEFAULT_PARAMS;
};

const assertJsonText = (value?: string | null) => {
    try {
        JSON.parse(normalizeJsonText(value));
        return Promise.resolve();
    } catch {
        return Promise.reject(new Error("请输入合法 JSON"));
    }
};

const omitBlankApiKey = (command: AiModelChangeCommand): AiModelChangeCommand => {
    if (!command.apiKey?.trim()) {
        const sanitized = { ...command };
        delete sanitized.apiKey;
        return sanitized;
    }
    return command;
};

export const ModelsPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const [form] = Form.useForm<ModelFormValues>();
    const canViewConfig = hasPermission("ai:config:view");
    const canEditConfig = hasPermission("ai:config:edit");
    const [query, setQuery] = useState<AiModelListQuery>({});
    const [modelNameKeyword, setModelNameKeyword] = useState("");
    const [editingModel, setEditingModel] = useState<AiModelRecord | null>(null);
    const [drawerOpen, setDrawerOpen] = useState(false);

    const modelsQuery = useQuery({
        queryKey: ["ai", "model-configs", query],
        queryFn: () => service.listModelConfigs(query),
        enabled: canViewConfig,
        retry: false
    });

    const filteredModels = useMemo(() => {
        const keyword = modelNameKeyword.trim().toLowerCase();
        if (!keyword) {
            return modelsQuery.data || [];
        }
        return (modelsQuery.data || []).filter((record) => {
            return (
                record.modelName.toLowerCase().includes(keyword) ||
                (record.displayName || "").toLowerCase().includes(keyword)
            );
        });
    }, [modelNameKeyword, modelsQuery.data]);

    const invalidateModels = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "model-configs"] });
    };

    const createMutation = useMutation({
        mutationFn: service.createModelConfig,
        onSuccess: async () => {
            await invalidateModels();
            setDrawerOpen(false);
            setEditingModel(null);
            message.success("模型已新增");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "模型新增失败");
        }
    });

    const changeMutation = useMutation({
        mutationFn: service.changeModelConfig,
        onSuccess: async () => {
            await invalidateModels();
            setDrawerOpen(false);
            setEditingModel(null);
            message.success("模型已保存");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "模型保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: service.deleteModelConfig,
        onSuccess: async () => {
            await invalidateModels();
            message.success("模型已删除");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "模型删除失败");
        }
    });

    useEffect(() => {
        if (modelsQuery.isError) {
            const error = modelsQuery.error;
            message.error(error instanceof Error ? error.message : "模型列表加载失败");
        }
    }, [message, modelsQuery.error, modelsQuery.isError]);

    const openCreate = () => {
        setEditingModel(null);
        form.setFieldsValue({
            apiSource: API_SOURCE_OPTIONS[0],
            baseUrl: "",
            apiKey: "",
            modelName: "",
            displayName: "",
            capabilities: [],
            defaultParamsJson: DEFAULT_PARAMS,
            description: "",
            enabled: true
        });
        setDrawerOpen(true);
    };

    const openEdit = (record: AiModelRecord) => {
        setEditingModel(record);
        form.setFieldsValue({
            id: record.id,
            apiSource: record.apiSource,
            baseUrl: record.baseUrl || "",
            apiKey: "",
            modelName: record.modelName,
            displayName: record.displayName || "",
            capabilities: record.capabilities || [],
            defaultParamsJson: normalizeJsonText(record.defaultParamsJson),
            description: record.description || "",
            enabled: record.enabled
        });
        setDrawerOpen(true);
    };

    const submitForm = async () => {
        const values = await form.validateFields();
        const command = {
            ...values,
            id: editingModel?.id || values.id || null,
            defaultParamsJson: normalizeJsonText(values.defaultParamsJson),
            displayName: values.displayName?.trim() || null,
            description: values.description?.trim() || null,
            capabilities: values.capabilities || []
        };
        if (editingModel) {
            await changeMutation.mutateAsync(omitBlankApiKey(command));
            return;
        }
        await createMutation.mutateAsync(command);
    };

    const changeEnabled = async (record: AiModelRecord, enabled: boolean) => {
        await changeMutation.mutateAsync({
            id: record.id,
            apiSource: record.apiSource,
            baseUrl: record.baseUrl || "",
            modelName: record.modelName,
            displayName: record.displayName || null,
            capabilities: record.capabilities || [],
            defaultParamsJson: normalizeJsonText(record.defaultParamsJson),
            description: record.description || null,
            enabled
        });
    };

    const columns: ColumnsType<AiModelRecord> = [
        {
            title: "displayName",
            dataIndex: "displayName",
            key: "displayName",
            render: (value: string | null, record) => value || record.modelName
        },
        {
            title: "modelName",
            dataIndex: "modelName",
            key: "modelName"
        },
        {
            title: "apiSource",
            dataIndex: "apiSource",
            key: "apiSource"
        },
        {
            title: "capabilities",
            dataIndex: "capabilities",
            key: "capabilities",
            render: (tags: string[] = []) => (
                <KuzhambuSpace>
                    {tags.map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                    ))}
                </KuzhambuSpace>
            )
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
            title: "registeredAt",
            dataIndex: "registeredAt",
            key: "registeredAt",
            render: formatDateTime
        },
        {
            key: "actions",
            render: (_, record) => (
                <KuzhambuSpaceCompact>
                    <KuzhambuButton
                        testId="ai-model-configs-model-configs-edit-button"
                        icon={<EditOutlined />}
                        disabled={!canEditConfig}
                        onClick={() => openEdit(record)}
                    >
                        编辑
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="ai-model-configs-model-configs-disable-or-enable-button"
                        disabled={!canEditConfig}
                        onClick={() => void changeEnabled(record, !record.enabled)}
                    >
                        {record.enabled ? "禁用" : "启用"}
                    </KuzhambuButton>
                    <Popconfirm
                        title="删除模型"
                        description="确认删除该模型？"
                        okText="删除"
                        cancelText="取消"
                        disabled={!canEditConfig}
                        onConfirm={() => deleteMutation.mutate(record.id)}
                    >
                        <KuzhambuButton
                            testId="ai-model-configs-model-configs-delete-button"
                            icon={<DeleteOutlined />}
                            danger
                            disabled={!canEditConfig}
                        >
                            删除
                        </KuzhambuButton>
                    </Popconfirm>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    return (
        <KuzhambuPage
            className="model-configs-page"
            eyebrow="AI"
            title="AI 模型配置"
            description="管理模型和能力标签"
            actions={
                <KuzhambuSpace>
                    <Tooltip title="刷新">
                        <KuzhambuButton
                            testId="ai-model-configs-model-configs-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={modelsQuery.isFetching}
                            onClick={() => void invalidateModels()}
                        />
                    </Tooltip>
                    <KuzhambuButton
                        testId="ai-model-configs-model-configs-create-model-button"
                        type="primary"
                        icon={<PlusOutlined />}
                        disabled={!canEditConfig}
                        onClick={openCreate}
                    >
                        新增模型
                    </KuzhambuButton>
                </KuzhambuSpace>
            }
        >
            <Card className="model-configs-filter-card">
                <Form layout="inline" className="model-configs-filter-form">
                    <Form.Item label="供应商">
                        <Select
                            allowClear
                            className="model-configs-filter-control"
                            options={API_SOURCE_OPTIONS.map((value) => ({ label: value, value }))}
                            value={query.apiSource ?? undefined}
                            onChange={(apiSource) =>
                                setQuery((current) => ({
                                    ...current,
                                    apiSource: apiSource ?? null
                                }))
                            }
                        />
                    </Form.Item>
                    <Form.Item label="启用状态">
                        <Select
                            allowClear
                            className="model-configs-filter-control"
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
                    <Form.Item label="模型名称">
                        <Input.Search
                            aria-label="模型名称"
                            allowClear
                            className="model-configs-search"
                            placeholder="modelName / displayName"
                            onSearch={setModelNameKeyword}
                        />
                    </Form.Item>
                    <Form.Item>
                        <KuzhambuButton
                            testId="ai-model-configs-model-configs-reset-button"
                            onClick={() => {
                                setQuery({});
                                setModelNameKeyword("");
                            }}
                        >
                            重置
                        </KuzhambuButton>
                    </Form.Item>
                </Form>
            </Card>

            <Table<AiModelRecord>
                aria-label="AI 模型列表"
                rowKey="id"
                className="model-configs-table"
                columns={columns}
                dataSource={filteredModels}
                loading={modelsQuery.isFetching}
                pagination={{ pageSize: 10, showSizeChanger: true }}
            />

            <KuzhambuDrawer
                open={drawerOpen}
                title={editingModel ? "编辑模型" : "新增模型"}
                size="large"
                onClose={() => setDrawerOpen(false)}
                footer={
                    <KuzhambuSpace>
                        <KuzhambuButton
                            testId="ai-model-configs-model-configs-cancel-button"
                            onClick={() => setDrawerOpen(false)}
                        >
                            取消
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-model-configs-model-configs-save-button"
                            type="primary"
                            icon={<SaveOutlined />}
                            disabled={!canEditConfig}
                            loading={createMutation.isPending || changeMutation.isPending}
                            onClick={() => void submitForm()}
                        >
                            保存
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
            >
                <Form form={form} layout="vertical" className="model-configs-form">
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item
                        label="apiSource"
                        name="apiSource"
                        rules={[{ required: true, message: "请选择供应商" }]}
                    >
                        <Select
                            options={API_SOURCE_OPTIONS.map((value) => ({ label: value, value }))}
                        />
                    </Form.Item>
                    <Form.Item
                        label="baseUrl"
                        name="baseUrl"
                        rules={[{ required: true, message: "请输入服务地址" }]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item label="apiKey" name="apiKey">
                        <Input.Password
                            placeholder={
                                editingModel?.apiKeyConfigured ? "已配置，留空则不更新" : ""
                            }
                        />
                    </Form.Item>
                    <Form.Item
                        label="modelName"
                        name="modelName"
                        rules={[{ required: true, message: "请输入模型名称" }]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item label="displayName" name="displayName">
                        <Input />
                    </Form.Item>
                    <Form.Item label="capabilities" name="capabilities">
                        <Select
                            mode="multiple"
                            options={MODEL_CAPABILITY_OPTIONS.map((value) => ({
                                label: value,
                                value
                            }))}
                        />
                    </Form.Item>
                    <Form.Item
                        label="defaultParamsJson"
                        name="defaultParamsJson"
                        rules={[{ validator: (_, value) => assertJsonText(value) }]}
                    >
                        <Input.TextArea rows={6} />
                    </Form.Item>
                    <Form.Item label="description" name="description">
                        <Input.TextArea rows={3} />
                    </Form.Item>
                    <Form.Item label="enabled" name="enabled" valuePropName="checked">
                        <Switch checkedChildren="启用" unCheckedChildren="禁用" />
                    </Form.Item>
                </Form>
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
