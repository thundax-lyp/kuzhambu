import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { Key } from "react";
import { App } from "antd";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { PromptsPageContent } from "./prompts-page-content";
import {
    DEFAULT_PROMPT_FILTERS,
    readCapabilityLabel,
    readPromptDisplayName,
    readTemplateRowKey
} from "./prompts-page-content-support";
import type { PromptFilters } from "./prompts-page-content-support";
import * as service from "./prompts-service";
import type { AiPromptTemplateQuery } from "./prompts-service";
import type { AiPromptTemplateRecord } from "./prompts-types";
import "./prompts-page.css";

const toEnabledQueryValue = (enabled: PromptFilters["enabled"]) => {
    if (enabled === "ENABLED") {
        return true;
    }
    if (enabled === "DISABLED") {
        return false;
    }
    return undefined;
};

const normalizeJsonText = (value?: string | null, fallback = "{}") => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : fallback;
};

const variablesToJson = (
    variables: Array<{
        description?: string | null;
        priority?: number | null;
        required: boolean;
        variableName: string;
    }> = []
) => {
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

export const PromptsPage = () => {
    const { message: messageApi, modal } = App.useApp();
    const queryClient = useQueryClient();
    const canViewPrompt = hasPermission("ai:prompt:view") || hasPermission("ai:prompt:edit");
    const canEditPrompt = hasPermission("ai:prompt:edit");
    const [query, setQuery] = useState<AiPromptTemplateQuery>({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<PromptFilters>(DEFAULT_PROMPT_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingTemplate, setEditingTemplate] = useState<AiPromptTemplateRecord | null>(null);
    const [promptEditDrawerOpen, setPromptEditDrawerOpen] = useState(false);
    const hasSelectedPrompts = selectedRowKeys.length > 0;
    const hasActiveFilters =
        Boolean(filters.capability) || filters.enabled !== DEFAULT_PROMPT_FILTERS.enabled;

    const promptCapabilitiesQuery = useQuery({
        queryKey: ["ai", "prompts", "capabilities"],
        queryFn: service.listPromptCapabilities,
        enabled: canViewPrompt,
        retry: false
    });

    const promptTemplatePageQuery = useQuery({
        queryKey: ["ai", "prompts", "templates", query],
        queryFn: () => service.listPromptTemplates(query),
        enabled: canViewPrompt,
        retry: false
    });

    const updatePromptStatusMutation = useMutation({
        mutationFn: async ({
            enabled,
            template
        }: {
            enabled: boolean;
            template: AiPromptTemplateRecord;
        }) => {
            if (!template.id) {
                throw new Error("提示词模板 ID 缺失");
            }
            const [currentVersion, variables] = await Promise.all([
                service.getCurrentPromptVersion(template.id),
                service.listPromptVariables(template.id)
            ]);
            return service.changePromptTemplate({
                id: template.id,
                capability: template.capability || "",
                name: template.name || "",
                description: template.description || null,
                enabled,
                messageTemplatesJson: normalizeJsonText(currentVersion?.messageTemplatesJson, "[]"),
                variablesSnapshotJson:
                    currentVersion?.variablesSnapshotJson || variablesToJson(variables),
                outputSchemaJson: normalizeJsonText(currentVersion?.outputSchemaJson),
                changeSummary: enabled ? "启用提示词模板" : "禁用提示词模板",
                variables
            });
        },
        onSuccess: async () => {
            await invalidatePrompts();
            messageApi.success("提示词状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "提示词状态更新失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: async (template: AiPromptTemplateRecord) => {
            if (!template.id) {
                throw new Error("提示词模板 ID 缺失");
            }
            const [currentVersion, variables] = await Promise.all([
                service.getCurrentPromptVersion(template.id),
                service.listPromptVariables(template.id)
            ]);
            return service.changePromptTemplate({
                id: template.id,
                capability: template.capability || "",
                name: template.name || "",
                description: template.description || null,
                enabled: false,
                messageTemplatesJson: normalizeJsonText(currentVersion?.messageTemplatesJson, "[]"),
                variablesSnapshotJson:
                    currentVersion?.variablesSnapshotJson || variablesToJson(variables),
                outputSchemaJson: normalizeJsonText(currentVersion?.outputSchemaJson),
                changeSummary: "删除提示词模板",
                variables
            });
        },
        onSuccess: async () => {
            await invalidatePrompts();
            messageApi.success("提示词模板已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "提示词模板删除失败");
        }
    });

    const capabilityByCode = useMemo(() => {
        return new Map(
            (promptCapabilitiesQuery.data || []).map((record) => [record.capability, record])
        );
    }, [promptCapabilitiesQuery.data]);

    const capabilityOptions = useMemo(() => {
        return (promptCapabilitiesQuery.data || []).map((record) => ({
            label: readCapabilityLabel(record.capability, record.name),
            value: record.capability
        }));
    }, [promptCapabilitiesQuery.data]);

    const filteredTemplates = useMemo(() => {
        const normalizedSearchText = searchText.trim().toLowerCase();
        if (!normalizedSearchText) {
            return promptTemplatePageQuery.data || [];
        }
        return (promptTemplatePageQuery.data || []).filter((template) => {
            const capability = capabilityByCode.get(template.capability || "");
            return (
                (template.name || "").toLowerCase().includes(normalizedSearchText) ||
                (template.description || "").toLowerCase().includes(normalizedSearchText) ||
                (template.capability || "").toLowerCase().includes(normalizedSearchText) ||
                readCapabilityLabel(template.capability, capability?.name)
                    .toLowerCase()
                    .includes(normalizedSearchText)
            );
        });
    }, [capabilityByCode, searchText, promptTemplatePageQuery.data]);

    const selectedTemplates = useMemo(() => {
        const selectedKeys = new Set(selectedRowKeys.map(String));
        return filteredTemplates.filter((template) =>
            selectedKeys.has(String(readTemplateRowKey(template)))
        );
    }, [filteredTemplates, selectedRowKeys]);

    useEffect(() => {
        if (promptTemplatePageQuery.isError) {
            const error = promptTemplatePageQuery.error;
            messageApi.error(error instanceof Error ? error.message : "提示词模板加载失败");
        }
    }, [messageApi, promptTemplatePageQuery.error, promptTemplatePageQuery.isError]);

    const invalidatePrompts = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "prompts"] });
    };

    const changeSearchText = (value: string) => {
        setSearchText(value);
        setSelectedRowKeys([]);
    };

    const applyFilters = () => {
        setSelectedRowKeys([]);
        setQuery({
            capability: filters.capability || undefined,
            enabled: toEnabledQueryValue(filters.enabled)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_PROMPT_FILTERS);
        setSelectedRowKeys([]);
        setQuery({});
    };

    const openEditPromptDrawer = (template: AiPromptTemplateRecord) => {
        setEditingTemplate(template);
        setPromptEditDrawerOpen(true);
    };

    const openCreatePromptDrawer = () => {
        setEditingTemplate(null);
        setPromptEditDrawerOpen(true);
    };

    const closePromptEditDrawer = () => {
        setPromptEditDrawerOpen(false);
        setEditingTemplate(null);
    };

    const handleSaved = () => {
        setPromptEditDrawerOpen(false);
        setEditingTemplate(null);
        void invalidatePrompts();
    };

    const changeEnabled = (template: AiPromptTemplateRecord, enabled: boolean) => {
        if (!canEditPrompt) {
            return;
        }
        updatePromptStatusMutation.mutate({ enabled, template });
    };

    const deleteTemplate = (template: AiPromptTemplateRecord) => {
        if (!canEditPrompt) {
            return;
        }
        modal.confirm({
            title: "删除提示词模板",
            content: `确认删除 ${readPromptDisplayName(
                template,
                capabilityByCode.get(template.capability || "")?.name
            )}？删除后模板会被禁用并保留历史版本。`,
            okText: "删除",
            okType: "danger",
            cancelText: "取消",
            onOk: () => deleteMutation.mutateAsync(template)
        });
    };

    const batchChangeEnabled = async (enabled: boolean) => {
        if (!canEditPrompt || !hasSelectedPrompts) {
            return;
        }
        await Promise.all(
            selectedTemplates.map((template) =>
                updatePromptStatusMutation.mutateAsync({ enabled, template })
            )
        );
        setSelectedRowKeys([]);
    };

    const batchDeleteTemplates = () => {
        if (!canEditPrompt || !hasSelectedPrompts) {
            return;
        }
        modal.confirm({
            title: "批量删除提示词模板",
            content: `确认删除 ${selectedTemplates.length} 个提示词模板？删除后模板会被禁用并保留历史版本。`,
            okText: "删除",
            okType: "danger",
            cancelText: "取消",
            onOk: async () => {
                await Promise.all(
                    selectedTemplates.map((template) => deleteMutation.mutateAsync(template))
                );
                setSelectedRowKeys([]);
            }
        });
    };

    return (
        <div className="prompts-page-root">
            <PromptsPageContent
                canEditPrompt={canEditPrompt}
                capabilityByCode={capabilityByCode}
                capabilityOptions={capabilityOptions}
                deletePending={deleteMutation.isPending}
                editingTemplate={editingTemplate}
                filterActive={hasActiveFilters}
                filters={filters}
                hasSelectedPrompts={hasSelectedPrompts}
                loading={promptTemplatePageQuery.isFetching}
                promptCapabilitiesLoading={promptCapabilitiesQuery.isFetching}
                promptEditDrawerOpen={promptEditDrawerOpen}
                promptTemplateLoadError={promptTemplatePageQuery.isError}
                records={filteredTemplates}
                searchText={searchText}
                selectedRowKeys={selectedRowKeys}
                updatePromptStatusPending={updatePromptStatusMutation.isPending}
                onBatchDelete={batchDeleteTemplates}
                onBatchDisable={() => void batchChangeEnabled(false)}
                onBatchEnable={() => void batchChangeEnabled(true)}
                onChangeEnabled={changeEnabled}
                onClosePromptEditDrawer={closePromptEditDrawer}
                onCreatePrompt={openCreatePromptDrawer}
                onDeletePrompt={deleteTemplate}
                onEditPrompt={openEditPromptDrawer}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                onFiltersChange={setFilters}
                onRefresh={() => void invalidatePrompts()}
                onSaved={handleSaved}
                onSearchChange={changeSearchText}
                onSelectedRowKeysChange={setSelectedRowKeys}
            />
        </div>
    );
};
