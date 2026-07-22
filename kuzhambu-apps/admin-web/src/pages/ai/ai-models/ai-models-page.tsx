import { DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Select, Typography } from "antd";
import type { Key } from "react";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { AiModelEditDrawer } from "./components/ai-model-edit-drawer";
import {
    API_SOURCE_OPTIONS,
    normalizeJsonText,
    readApiSourceMeta,
    readCapabilityMeta
} from "./ai-models-metadata";
import * as service from "./ai-models-service";
import type { AiModelChangeCommand, AiModelListQuery } from "./ai-models-service";
import type { AiModelRecord } from "./ai-models-types";
import "./ai-models-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    displayName: 180,
    modelName: 300,
    apiSource: 120,
    capabilities: 112,
    enabled: 96,
    registeredAt: 120
};

interface ModelFilters {
    apiSource?: string | null;
    enabled: "ALL" | "ENABLED" | "DISABLED";
}

const DEFAULT_MODEL_FILTERS: ModelFilters = {
    apiSource: null,
    enabled: "ALL"
};

const formatDateTime = (value?: string | null) => {
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

const toEnabledQueryValue = (enabled: ModelFilters["enabled"]) => {
    if (enabled === "ENABLED") {
        return true;
    }
    if (enabled === "DISABLED") {
        return false;
    }
    return undefined;
};

const readModelName = (record: AiModelRecord) => {
    return record.displayName?.trim() || record.modelName;
};

const centerColumnTitle = (title: string) => (
    <span className="ai-models-center-column-title">{title}</span>
);

export const AiModelsPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canViewConfig = hasPermission("ai:config:view") || hasPermission("ai:config:edit");
    const canEditConfig = hasPermission("ai:config:edit");
    const [query, setQuery] = useState<AiModelListQuery>({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<ModelFilters>(DEFAULT_MODEL_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingModel, setEditingModel] = useState<AiModelRecord | null>(null);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const hasSelectedModels = selectedRowKeys.length > 0;
    const hasActiveFilters =
        Boolean(filters.apiSource) || filters.enabled !== DEFAULT_MODEL_FILTERS.enabled;

    const modelsQuery = useQuery({
        queryKey: ["ai", "ai-models", query],
        queryFn: () => service.listAiModels(query),
        enabled: canViewConfig,
        retry: false
    });

    const filteredModels = useMemo(() => {
        const keyword = searchText.trim().toLowerCase();
        if (!keyword) {
            return modelsQuery.data || [];
        }
        return (modelsQuery.data || []).filter((record) => {
            return (
                record.modelName.toLowerCase().includes(keyword) ||
                (record.displayName || "").toLowerCase().includes(keyword) ||
                (record.description || "").toLowerCase().includes(keyword)
            );
        });
    }, [modelsQuery.data, searchText]);

    const invalidateModels = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "ai-models"] });
    };

    const createMutation = useMutation({
        mutationFn: service.createAiModel,
        onSuccess: async () => {
            await invalidateModels();
            setDrawerOpen(false);
            setEditingModel(null);
            messageApi.success("模型已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "模型新增失败");
        }
    });

    const changeMutation = useMutation({
        mutationFn: service.changeAiModel,
        onSuccess: async () => {
            await invalidateModels();
            setDrawerOpen(false);
            setEditingModel(null);
            messageApi.success("模型已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "模型保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: service.deleteAiModel,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await invalidateModels();
            messageApi.success("模型已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "模型删除失败");
        }
    });

    useEffect(() => {
        if (modelsQuery.isError) {
            const error = modelsQuery.error;
            messageApi.error(error instanceof Error ? error.message : "模型列表加载失败");
        }
    }, [messageApi, modelsQuery.error, modelsQuery.isError]);

    const openCreate = () => {
        setEditingModel(null);
        setDrawerOpen(true);
    };

    const openEdit = (record: AiModelRecord) => {
        setEditingModel(record);
        setDrawerOpen(true);
    };

    const closeEditor = () => {
        if (createMutation.isPending || changeMutation.isPending) {
            return;
        }
        setDrawerOpen(false);
        setEditingModel(null);
    };

    const saveModel = (command: AiModelChangeCommand) => {
        if (command.id) {
            changeMutation.mutate(command);
            return;
        }
        createMutation.mutate(command);
    };

    const changeEnabled = async (record: AiModelRecord, enabled: boolean) => {
        if (!canEditConfig) {
            return;
        }
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

    const applyFilters = () => {
        setSelectedRowKeys([]);
        setQuery({
            apiSource: filters.apiSource || undefined,
            enabled: toEnabledQueryValue(filters.enabled)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_MODEL_FILTERS);
        setSelectedRowKeys([]);
        setQuery({});
    };

    const confirmDeleteModel = (record: AiModelRecord) => {
        confirm.danger({
            title: "删除模型",
            message: `确认删除 ${readModelName(record)}？`,
            description: "删除后需要重新新增。若模型仍被业务配置引用，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(record.id)
        });
    };

    const batchDeleteModels = () => {
        confirm.danger({
            title: "批量删除模型",
            message: `确认删除 ${selectedRowKeys.length} 个模型？`,
            description: "删除后需要重新新增。若模型仍被业务配置引用，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: async () => {
                await Promise.all(
                    selectedRowKeys.map((id) => deleteMutation.mutateAsync(Number(id)))
                );
            }
        });
    };

    const batchUpdateEnabled = async (enabled: boolean) => {
        if (!canEditConfig || !hasSelectedModels) {
            return;
        }
        const selectedModels = (modelsQuery.data || []).filter((model) =>
            selectedRowKeys.includes(model.id)
        );
        await Promise.all(selectedModels.map((model) => changeEnabled(model, enabled)));
        setSelectedRowKeys([]);
    };

    const columns: KuzhambuTableProps<AiModelRecord>["columns"] = [
        {
            title: "模型名称",
            dataIndex: "displayName",
            key: "displayName",
            width: DEFAULT_COLUMN_WIDTHS.displayName,
            ellipsis: true,
            render: (value: string | null, record) => value || record.modelName
        },
        {
            title: "模型标识",
            dataIndex: "modelName",
            key: "modelName",
            minWidth: DEFAULT_COLUMN_WIDTHS.modelName,
            width: DEFAULT_COLUMN_WIDTHS.modelName,
            ellipsis: true,
            render: (modelName: string) => (
                <Text strong ellipsis title={modelName}>
                    {modelName}
                </Text>
            )
        },
        {
            title: centerColumnTitle("供应商"),
            dataIndex: "apiSource",
            key: "apiSource",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.apiSource,
            render: (apiSource: string) => {
                const apiSourceMeta = readApiSourceMeta(apiSource);
                return <KuzhambuTag type={apiSourceMeta.type}>{apiSourceMeta.label}</KuzhambuTag>;
            }
        },
        {
            title: centerColumnTitle("能力"),
            dataIndex: "capabilities",
            key: "capabilities",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.capabilities,
            render: (tags: string[] = []) => (
                <div className="ai-models-capabilities">
                    {tags.map((tag) => {
                        const capabilityMeta = readCapabilityMeta(tag);
                        return (
                            <KuzhambuTag key={tag} type={capabilityMeta.type}>
                                {capabilityMeta.label}
                            </KuzhambuTag>
                        );
                    })}
                </div>
            )
        },
        {
            title: centerColumnTitle("状态"),
            dataIndex: "enabled",
            key: "enabled",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.enabled,
            render: (enabled: boolean, record) => (
                <KuzhambuSwitch
                    checked={enabled}
                    checkedChildren="启用"
                    unCheckedChildren="禁用"
                    aria-label={`切换 ${readModelName(record)} 状态，当前${enabled ? "启用" : "禁用"}`}
                    disabled={!canEditConfig || changeMutation.isPending}
                    onChange={(checked) => void changeEnabled(record, checked)}
                />
            )
        },
        {
            title: centerColumnTitle("注册时间"),
            dataIndex: "registeredAt",
            key: "registeredAt",
            align: "center",
            className: "ai-models-center-column",
            width: DEFAULT_COLUMN_WIDTHS.registeredAt,
            render: formatDateTime
        },
        {
            key: "actions",
            options: (record) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${readModelName(record)}`,
                    disabled: !canEditConfig,
                    onClick: () => openEdit(record)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${readModelName(record)}`,
                    disabled: !canEditConfig,
                    onClick: () => confirmDeleteModel(record)
                }
            ]
        }
    ];

    return (
        <>
            <KuzhambuListPage<AiModelRecord>
                pageClassName="ai-models-page"
                title="模型管理"
                description="维护 AI 模型、供应商、能力和调用参数。"
                subjectName="模型"
                enableAdd={canEditConfig}
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                searchPlaceholder="搜索模型..."
                onSearchChange={setSearchText}
                onAdd={openCreate}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "apiSource",
                        label: "供应商",
                        render: () => (
                            <Select
                                allowClear
                                value={filters.apiSource ?? undefined}
                                options={API_SOURCE_OPTIONS.map((value) => ({
                                    label: readApiSourceMeta(value).label,
                                    value
                                }))}
                                onChange={(apiSource) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        apiSource: apiSource ?? null
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "enabled",
                        label: "状态",
                        render: () => (
                            <Select
                                value={filters.enabled}
                                options={[
                                    { label: "全部", value: "ALL" },
                                    { label: "启用", value: "ENABLED" },
                                    { label: "禁用", value: "DISABLED" }
                                ]}
                                onChange={(enabled) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        enabled
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                pageActions={
                    <KuzhambuButton
                        testId="ai-models-refresh-button"
                        icon={<ReloadOutlined />}
                        loading={modelsQuery.isFetching}
                        onClick={() => void modelsQuery.refetch()}
                    >
                        刷新
                    </KuzhambuButton>
                }
                batchClassName="ai-models-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            testId="ai-models-enable-button"
                            disabled={!canEditConfig || !hasSelectedModels}
                            loading={changeMutation.isPending}
                            onClick={() => void batchUpdateEnabled(true)}
                        >
                            启用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-models-disable-button"
                            disabled={!canEditConfig || !hasSelectedModels}
                            loading={changeMutation.isPending}
                            onClick={() => void batchUpdateEnabled(false)}
                        >
                            禁用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-models-batch-delete-button"
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditConfig || !hasSelectedModels}
                            loading={deleteMutation.isPending}
                            onClick={batchDeleteModels}
                        >
                            批量删除
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
                rowKey="id"
                className="ai-models-table"
                columns={columns}
                dataSource={filteredModels}
                loading={modelsQuery.isFetching}
                pagination={{ pageSize: 10, showSizeChanger: true }}
                scroll={{ x: 1052 }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: () => ({
                        disabled: !canEditConfig
                    })
                }}
                locale={{
                    emptyText: modelsQuery.isError
                        ? "模型列表加载失败，请确认权限和接口状态。"
                        : "暂无模型"
                }}
            />

            <AiModelEditDrawer
                open={drawerOpen}
                model={editingModel}
                canEdit={canEditConfig}
                saving={createMutation.isPending || changeMutation.isPending}
                onClose={closeEditor}
                onSave={saveModel}
            />
        </>
    );
};
