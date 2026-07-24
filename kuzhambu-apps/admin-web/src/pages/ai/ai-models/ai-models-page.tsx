import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App } from "antd";
import type { Key } from "react";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { AiModelFilterPanel, type ModelFilters } from "./components/ai-model-filter-panel";
import { AiModelTable } from "./components/ai-model-table";
import { AiModelEditDrawer } from "./components/ai-model-edit-drawer";
import { normalizeJsonText } from "./ai-models-metadata";
import * as service from "./ai-models-service";
import type { AiModelChangeCommand, AiModelListQuery } from "./ai-models-service";
import type { AiModelRecord } from "./ai-models-types";
import "./ai-models-page.css";

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
    const [aiModelEditDrawerOpen, setAiModelEditDrawerOpen] = useState(false);
    const hasSelectedModels = selectedRowKeys.length > 0;
    const hasActiveFilters =
        Boolean(filters.apiSource) || filters.enabled !== DEFAULT_MODEL_FILTERS.enabled;

    const aiModelListQuery = useQuery({
        queryKey: ["ai", "ai-models", query],
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
        await queryClient.invalidateQueries({ queryKey: ["ai", "ai-models"] });
    };

    const createMutation = useMutation({
        mutationFn: service.createAiModel,
        onSuccess: async () => {
            await invalidateModels();
            setAiModelEditDrawerOpen(false);
            setEditingModel(null);
            messageApi.success("模型已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "模型新增失败");
        }
    });

    const updateModelMutation = useMutation({
        mutationFn: service.changeAiModel,
        onSuccess: async () => {
            await invalidateModels();
            setAiModelEditDrawerOpen(false);
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
        if (createMutation.isPending || updateModelMutation.isPending) {
            return;
        }
        setAiModelEditDrawerOpen(false);
        setEditingModel(null);
    };

    const saveModel = (command: AiModelChangeCommand) => {
        if (command.id) {
            updateModelMutation.mutate(command);
            return;
        }
        createMutation.mutate(command);
    };

    const changeEnabled = async (record: AiModelRecord, enabled: boolean) => {
        if (!canEditConfig) {
            return;
        }
        await updateModelMutation.mutateAsync({
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
        const selectedModels = (aiModelListQuery.data || []).filter((model) =>
            selectedRowKeys.includes(model.id)
        );
        await Promise.all(selectedModels.map((model) => changeEnabled(model, enabled)));
        setSelectedRowKeys([]);
    };

    return (
        <div className="ai-models-page-root">
            <AiModelFilterPanel
                batchDeleting={deleteMutation.isPending}
                batchUpdating={updateModelMutation.isPending}
                canEditConfig={canEditConfig}
                filterActive={hasActiveFilters}
                filters={filters}
                hasSelectedModels={hasSelectedModels}
                loading={aiModelListQuery.isFetching}
                onAdd={openCreateAiModelDrawer}
                onBatchDelete={batchDeleteModels}
                onBatchUpdateEnabled={(enabled) => void batchUpdateEnabled(enabled)}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                onFiltersChange={setFilters}
                onRefresh={() => void aiModelListQuery.refetch()}
                onSearchChange={setSearchText}
                searchText={searchText}
                selectedCount={selectedRowKeys.length}
                table={(batchActionBar) => (
                    <AiModelTable
                        batchActionBar={batchActionBar}
                        canEditConfig={canEditConfig}
                        changing={updateModelMutation.isPending}
                        dataSource={filteredModels}
                        loading={aiModelListQuery.isFetching}
                        locale={{
                            emptyText: aiModelListQuery.isError
                                ? "模型列表加载失败，请确认权限和接口状态。"
                                : "暂无模型"
                        }}
                        onChangeEnabled={(record, enabled) => void changeEnabled(record, enabled)}
                        onDelete={confirmDeleteModel}
                        onOpenEdit={openEditAiModelDrawer}
                        onSelectedRowKeysChange={setSelectedRowKeys}
                        selectedRowKeys={selectedRowKeys}
                    />
                )}
            />

            <AiModelEditDrawer
                open={aiModelEditDrawerOpen}
                model={editingModel}
                canEdit={canEditConfig}
                saving={createMutation.isPending || updateModelMutation.isPending}
                onClose={closeAiModelEditDrawer}
                onSave={saveModel}
            />
        </div>
    );
};
