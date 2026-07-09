import {
    DeleteOutlined,
    EditOutlined,
    HistoryOutlined,
    PlusOutlined,
    ReloadOutlined,
    SaveOutlined,
    ThunderboltOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    App,
    Button,
    Card,
    Form,
    Input,
    Popconfirm,
    Select,
    Switch,
    Table,
    Tag,
    Tooltip
} from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import * as service from "./model-configs-service";
import type { AiModelChangeCommand, AiModelListQuery } from "./model-configs-service";
import type { AiModelCheckRecord, AiModelRecord } from "./model-configs-types";
import "./model-configs-page.css";

type ModelFormValues = AiModelChangeCommand;

const DEFAULT_PARAMS = "{}";

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
    const [historyModel, setHistoryModel] = useState<AiModelRecord | null>(null);

    const servicesQuery = useQuery({
        queryKey: ["ai", "model-configs", "services"],
        queryFn: service.listModelServices,
        enabled: canViewConfig,
        retry: false
    });

    const modelsQuery = useQuery({
        queryKey: ["ai", "model-configs", query],
        queryFn: () => service.listModelConfigs(query),
        enabled: canViewConfig,
        retry: false
    });

    const historyQuery = useQuery({
        queryKey: ["ai", "model-configs", "check-records", historyModel?.modelId],
        queryFn: () => service.listModelCheckRecords(historyModel?.modelId || 0),
        enabled: Boolean(historyModel?.modelId) && canViewConfig,
        retry: false
    });

    const serviceOptions = useMemo(() => {
        return (servicesQuery.data || []).map((record) => ({
            label: `${record.serviceRole} / ${record.serviceId}`,
            value: record.serviceId
        }));
    }, [servicesQuery.data]);

    const serviceById = useMemo(() => {
        return new Map((servicesQuery.data || []).map((record) => [record.serviceId, record]));
    }, [servicesQuery.data]);

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

    const checkMutation = useMutation({
        mutationFn: service.refreshModelCheck,
        onSuccess: async (record) => {
            await invalidateModels();
            if (historyModel?.modelId === record.modelId) {
                await historyQuery.refetch();
            }
            message.success(record.status === "SUCCEEDED" ? "检测成功" : "检测已记录失败结果");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "模型检测失败");
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
            serviceId: serviceOptions[0]?.value,
            modelName: "",
            displayName: "",
            capabilityTags: [],
            defaultParamsJson: DEFAULT_PARAMS,
            description: "",
            enabled: true
        });
        setDrawerOpen(true);
    };

    const openEdit = (record: AiModelRecord) => {
        setEditingModel(record);
        form.setFieldsValue({
            modelId: record.modelId,
            serviceId: record.serviceId,
            modelName: record.modelName,
            displayName: record.displayName || "",
            capabilityTags: record.capabilityTags || [],
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
            modelId: editingModel?.modelId || values.modelId || null,
            defaultParamsJson: normalizeJsonText(values.defaultParamsJson),
            displayName: values.displayName?.trim() || null,
            description: values.description?.trim() || null,
            capabilityTags: values.capabilityTags || []
        };
        if (editingModel) {
            await changeMutation.mutateAsync(command);
            return;
        }
        await createMutation.mutateAsync(command);
    };

    const changeEnabled = async (record: AiModelRecord, enabled: boolean) => {
        await changeMutation.mutateAsync({
            modelId: record.modelId,
            serviceId: record.serviceId,
            modelName: record.modelName,
            displayName: record.displayName || null,
            capabilityTags: record.capabilityTags || [],
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
            title: "serviceId",
            dataIndex: "serviceId",
            key: "serviceId",
            render: (value: number) => {
                const serviceRecord = serviceById.get(value);
                return serviceRecord ? `${serviceRecord.serviceRole} / ${value}` : value;
            }
        },
        {
            title: "capabilityTags",
            dataIndex: "capabilityTags",
            key: "capabilityTags",
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
                    <Button
                        icon={<EditOutlined />}
                        disabled={!canEditConfig}
                        onClick={() => openEdit(record)}
                    >
                        编辑
                    </Button>
                    <Button
                        icon={<ThunderboltOutlined />}
                        disabled={!canEditConfig}
                        loading={checkMutation.isPending}
                        onClick={() => checkMutation.mutate(record.modelId)}
                    >
                        检测
                    </Button>
                    <Button icon={<HistoryOutlined />} onClick={() => setHistoryModel(record)}>
                        检测历史
                    </Button>
                    <Button
                        disabled={!canEditConfig}
                        onClick={() => void changeEnabled(record, !record.enabled)}
                    >
                        {record.enabled ? "禁用" : "启用"}
                    </Button>
                    <Popconfirm
                        title="删除模型"
                        description="确认删除该模型？"
                        okText="删除"
                        cancelText="取消"
                        disabled={!canEditConfig}
                        onConfirm={() => deleteMutation.mutate(record.modelId)}
                    >
                        <Button icon={<DeleteOutlined />} danger disabled={!canEditConfig}>
                            删除
                        </Button>
                    </Popconfirm>
                </KuzhambuSpaceCompact>
            )
        }
    ];

    const historyColumns: ColumnsType<AiModelCheckRecord> = [
        {
            title: "status",
            dataIndex: "status",
            key: "status",
            render: (status: string) => (
                <Tag color={status === "SUCCEEDED" ? "green" : "red"}>{status}</Tag>
            )
        },
        {
            title: "latencyMs",
            dataIndex: "latencyMs",
            key: "latencyMs",
            render: (value?: number | null) => value ?? "-"
        },
        {
            title: "errorType",
            dataIndex: "errorType",
            key: "errorType",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "errorMessage",
            dataIndex: "errorMessage",
            key: "errorMessage",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "checkedAt",
            dataIndex: "checkedAt",
            key: "checkedAt",
            render: formatDateTime
        }
    ];

    return (
        <KuzhambuPage
            className="model-configs-page"
            eyebrow="AI"
            title="AI 模型配置"
            description="管理模型、能力标签和检测历史"
            actions={
                <KuzhambuSpace>
                    <Tooltip title="刷新">
                        <Button
                            aria-label="刷新"
                            icon={<ReloadOutlined />}
                            loading={modelsQuery.isFetching || servicesQuery.isFetching}
                            onClick={() => void invalidateModels()}
                        />
                    </Tooltip>
                    <Button
                        type="primary"
                        icon={<PlusOutlined />}
                        disabled={!canEditConfig}
                        onClick={openCreate}
                    >
                        新增模型
                    </Button>
                </KuzhambuSpace>
            }
        >
            <Card className="model-configs-filter-card">
                <Form layout="inline" className="model-configs-filter-form">
                    <Form.Item label="服务">
                        <Select
                            allowClear
                            className="model-configs-filter-control"
                            options={serviceOptions}
                            value={query.serviceId ?? undefined}
                            onChange={(serviceId) =>
                                setQuery((current) => ({
                                    ...current,
                                    serviceId: serviceId ?? null
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
                        <Button
                            onClick={() => {
                                setQuery({});
                                setModelNameKeyword("");
                            }}
                        >
                            重置
                        </Button>
                    </Form.Item>
                </Form>
            </Card>

            <Table<AiModelRecord>
                aria-label="AI 模型列表"
                rowKey="modelId"
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
                        <Button onClick={() => setDrawerOpen(false)}>取消</Button>
                        <Button
                            type="primary"
                            icon={<SaveOutlined />}
                            disabled={!canEditConfig}
                            loading={createMutation.isPending || changeMutation.isPending}
                            onClick={() => void submitForm()}
                        >
                            保存
                        </Button>
                    </KuzhambuSpace>
                }
            >
                <Form form={form} layout="vertical" className="model-configs-form">
                    <Form.Item name="modelId" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item
                        label="serviceId"
                        name="serviceId"
                        rules={[{ required: true, message: "请选择服务" }]}
                    >
                        <Select options={serviceOptions} />
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
                    <Form.Item label="capabilityTags" name="capabilityTags">
                        <Select mode="tags" tokenSeparators={[",", " "]} />
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

            <KuzhambuDrawer
                open={Boolean(historyModel)}
                title="检测历史"
                size="large"
                onClose={() => setHistoryModel(null)}
            >
                <Table<AiModelCheckRecord>
                    aria-label="模型检测历史"
                    rowKey="checkId"
                    columns={historyColumns}
                    dataSource={historyQuery.data || []}
                    loading={historyQuery.isFetching}
                    pagination={false}
                />
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
