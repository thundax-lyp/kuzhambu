import { DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Typography } from "antd";
import type { Key } from "react";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuSwitch,
    type KuzhambuTableProps,
    KuzhambuTag
} from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { AiModelEditDrawer } from "./ai-model-edit-drawer";
import {
    API_SOURCE_OPTIONS,
    normalizeJsonText,
    readApiSourceMeta,
    readCapabilityMeta
} from "./ai-model-constants";
import * as service from "./ai-model-service";
import type { AiModelChangeCommand, AiModelListQuery } from "./ai-model-service";
import type { AiModelRecord } from "./ai-model-types";
import "./ai-model-page.css";

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

const toEnabledCommand = (record: AiModelRecord, enabled: boolean): AiModelChangeCommand => ({
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

const centerColumnTitle = (title: string) => (
    <span className="ai-model-center-column-title">{title}</span>
);

export const AiModelPage = () => {
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
    const [aiModelEditDrawerOpen, setAiModelEditDrawerOpen] = useState(false);
    const hasSelectedModels = selectedRowKeys.length > 0;
    const hasActiveFilters =
        Boolean(filters.apiSource) || filters.enabled !== DEFAULT_MODEL_FILTERS.enabled;

    const aiModelListQuery = useQuery({
        queryKey: ["ai", "ai-model", query],
        queryFn: () => service.listAiModels(query),
        enabled: canViewConfig,
        retry: false
    });

    const filteredModels = useMemo(() => {
        const normalizedSearchText = searchText.trim().toLowerCase();
        if (!normalizedSearchText) {
            return aiModelListQuery.data || [];
        }
        return (aiModelListQuery.data || []).filter((record) => {
            return (
                record.modelName.toLowerCase().includes(normalizedSearchText) ||
                (record.displayName || "").toLowerCase().includes(normalizedSearchText) ||
                (record.description || "").toLowerCase().includes(normalizedSearchText)
            );
        });
    }, [aiModelListQuery.data, searchText]);

    const invalidateModels = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "ai-model"] });
    };

    const changeEnabledMutation = useMutation({
        mutationFn: service.changeAiModel,
        onSuccess: async () => {
            await invalidateModels();
            messageApi.success("模型状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "模型状态更新失败");
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

    const batchDeleteMutation = useMutation({
        mutationFn: service.deleteAiModels,
        onSuccess: async (results, { ids }) => {
            const failedIds = ids.filter((_, index) => results[index]?.status === "rejected");
            setSelectedRowKeys(failedIds);
            await invalidateModels();
            if (failedIds.length === 0) {
                messageApi.success(`已删除 ${ids.length} 个模型`);
                return;
            }
            const successCount = ids.length - failedIds.length;
            messageApi.warning(`批量删除完成：成功 ${successCount}，失败 ${failedIds.length}`);
        }
    });

    const batchChangeEnabledMutation = useMutation({
        mutationFn: service.changeAiModels,
        onSuccess: async (results, { commands }) => {
            const enabled = commands[0]?.enabled ?? false;
            const failedIds = commands
                .filter((_, index) => results[index]?.status === "rejected")
                .flatMap((command) => (command.id ? [command.id] : []));
            setSelectedRowKeys(failedIds);
            await invalidateModels();
            if (failedIds.length === 0) {
                messageApi.success(`已${enabled ? "启用" : "禁用"} ${commands.length} 个模型`);
                return;
            }
            const successCount = commands.length - failedIds.length;
            messageApi.warning(
                `批量${enabled ? "启用" : "禁用"}完成：成功 ${successCount}，失败 ${failedIds.length}`
            );
        }
    });

    useEffect(() => {
        if (aiModelListQuery.isError) {
            const error = aiModelListQuery.error;
            messageApi.error(error instanceof Error ? error.message : "模型列表加载失败");
        }
    }, [messageApi, aiModelListQuery.error, aiModelListQuery.isError]);

    const openCreateAiModelDrawer = () => {
        setEditingModel(null);
        setAiModelEditDrawerOpen(true);
    };

    const openEditAiModelDrawer = (record: AiModelRecord) => {
        setEditingModel(record);
        setAiModelEditDrawerOpen(true);
    };

    const closeAiModelEditDrawer = () => {
        setAiModelEditDrawerOpen(false);
        setEditingModel(null);
    };

    const changeEnabled = (record: AiModelRecord, enabled: boolean) => {
        if (!canEditConfig) {
            return;
        }
        changeEnabledMutation.mutate(toEnabledCommand(record, enabled));
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
            onConfirm: () => batchDeleteMutation.mutateAsync({ ids: selectedRowKeys.map(String) })
        });
    };

    const batchUpdateEnabled = async (enabled: boolean) => {
        if (!canEditConfig || !hasSelectedModels) {
            return;
        }
        const selectedModels = (aiModelListQuery.data || []).filter((model) =>
            selectedRowKeys.includes(model.id)
        );
        await batchChangeEnabledMutation.mutateAsync({
            commands: selectedModels.map((model) => toEnabledCommand(model, enabled))
        });
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
            className: "ai-model-center-column",
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
            className: "ai-model-center-column",
            width: DEFAULT_COLUMN_WIDTHS.capabilities,
            render: (tags: string[] = []) => (
                <div className="ai-model-capabilities">
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
            className: "ai-model-center-column",
            width: DEFAULT_COLUMN_WIDTHS.enabled,
            render: (enabled: boolean, record) => (
                <KuzhambuSwitch
                    checked={enabled}
                    checkedChildren="启用"
                    unCheckedChildren="禁用"
                    aria-label={`切换 ${readModelName(record)} 状态，当前${enabled ? "启用" : "禁用"}`}
                    disabled={!canEditConfig || changeEnabledMutation.isPending}
                    onChange={(checked) => changeEnabled(record, checked)}
                />
            )
        },
        {
            title: centerColumnTitle("注册时间"),
            dataIndex: "registeredAt",
            key: "registeredAt",
            align: "center",
            className: "ai-model-center-column",
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
                    onClick: () => openEditAiModelDrawer(record)
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
                pageClassName="ai-model-page"
                title="模型管理"
                description="维护 AI 模型、供应商、能力和调用参数。"
                subjectName="模型"
                enableAdd={canEditConfig}
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                searchPlaceholder="搜索模型..."
                onSearchChange={(value) => {
                    setSearchText(value);
                    setSelectedRowKeys([]);
                }}
                onAdd={openCreateAiModelDrawer}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "apiSource",
                        label: "供应商",
                        render: () => (
                            <KuzhambuSelect
                                allowClear
                                value={filters.apiSource ?? undefined}
                                options={API_SOURCE_OPTIONS.map((value) => ({
                                    label: readApiSourceMeta(value).label,
                                    value
                                }))}
                                onChange={(apiSource) =>
                                    setFilters((current) => ({
                                        ...current,
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
                            <KuzhambuSelect
                                value={filters.enabled}
                                options={[
                                    { label: "全部", value: "ALL" },
                                    { label: "启用", value: "ENABLED" },
                                    { label: "禁用", value: "DISABLED" }
                                ]}
                                onChange={(enabled) =>
                                    setFilters((current) => ({
                                        ...current,
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
                        testId="ai-model-refresh-button"
                        icon={<ReloadOutlined />}
                        loading={aiModelListQuery.isFetching}
                        onClick={() => void aiModelListQuery.refetch()}
                    >
                        刷新
                    </KuzhambuButton>
                }
                batchActions={
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            testId="ai-model-enable-button"
                            disabled={!canEditConfig || !hasSelectedModels}
                            loading={batchChangeEnabledMutation.isPending}
                            onClick={() => void batchUpdateEnabled(true)}
                        >
                            启用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-model-disable-button"
                            disabled={!canEditConfig || !hasSelectedModels}
                            loading={batchChangeEnabledMutation.isPending}
                            onClick={() => void batchUpdateEnabled(false)}
                        >
                            禁用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-model-batch-delete-button"
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditConfig || !hasSelectedModels}
                            loading={batchDeleteMutation.isPending}
                            onClick={batchDeleteModels}
                        >
                            批量删除
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
                batchClassName="ai-model-table-toolbar"
                selectedCount={selectedRowKeys.length}
                ariaLabel="模型列表"
                rowKey="id"
                className="ai-model-table"
                columns={columns}
                dataSource={filteredModels}
                loading={aiModelListQuery.isFetching}
                locale={{
                    emptyText: aiModelListQuery.isError
                        ? "模型列表加载失败，请确认权限和接口状态。"
                        : "暂无模型"
                }}
                pagination={{ pageSize: 10, showSizeChanger: true }}
                scroll={{ x: 1052 }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: () => ({
                        disabled: !canEditConfig
                    })
                }}
            />

            <AiModelEditDrawer
                open={aiModelEditDrawerOpen}
                model={editingModel}
                canEdit={canEditConfig}
                onClose={closeAiModelEditDrawer}
            />
        </>
    );
};
