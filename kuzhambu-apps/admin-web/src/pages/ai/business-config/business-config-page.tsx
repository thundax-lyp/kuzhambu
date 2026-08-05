import { ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuSwitch,
    KuzhambuTag,
    type KuzhambuTagType,
    type KuzhambuTableProps
} from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";

import { BusinessConfigEditDrawer } from "./business-config-edit-drawer";
import * as service from "./business-config-service";
import type {
    AiBusinessConfigChangeCommand,
    AiBusinessConfigQuery
} from "./business-config-service";
import type {
    AiBusinessConfigCapabilityRecord,
    AiBusinessConfigModelRecord,
    AiBusinessConfigPromptRecord,
    AiBusinessConfigRecord
} from "./business-config-types";
import "./business-config-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    capability: 260,
    prompt: 260,
    model: 240,
    enabled: 112,
    configuredAt: 140,
    params: 180
};

interface BusinessConfigFilters {
    capability?: string | null;
    enabled: "ALL" | "ENABLED" | "DISABLED";
}

interface BusinessConfigTableRow extends AiBusinessConfigRecord {
    capabilityName: string;
    modelName: string;
    modelTags: string[];
    promptName: string;
    promptVersionNo?: number | null;
}

const DEFAULT_BUSINESS_CONFIG_FILTERS: BusinessConfigFilters = {
    capability: null,
    enabled: "ALL"
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

const centerColumnTitle = (title: string) => (
    <span className="business-config-center-column-title">{title}</span>
);

const toEnabledQueryValue = (enabled: BusinessConfigFilters["enabled"]) => {
    if (enabled === "ENABLED") {
        return true;
    }
    if (enabled === "DISABLED") {
        return false;
    }
    return undefined;
};

const readCapabilityDomainTag = (
    capability?: string | null
): { label: string; type: KuzhambuTagType } => {
    if (capability?.startsWith("CLASSICS_")) {
        return { label: "古籍", type: "info" };
    }
    if (capability?.startsWith("DISCOVERY_")) {
        return { label: "发现", type: "accent" };
    }
    if (capability?.startsWith("KNOWLEDGE_")) {
        return { label: "知识", type: "success" };
    }
    if (capability?.startsWith("PLATFORM_")) {
        return { label: "平台", type: "warning" };
    }
    if (capability?.startsWith("PROMPT_")) {
        return { label: "提示词", type: "danger" };
    }
    return { label: "其他", type: "neutral" };
};

const readCapabilityLabel = (
    capability: string,
    capabilityByCode: Map<string, AiBusinessConfigCapabilityRecord>
) => {
    return capabilityByCode.get(capability)?.name || capability;
};

const readModelLabel = (modelId: string, modelById: Map<string, AiBusinessConfigModelRecord>) => {
    const model = modelById.get(modelId);
    if (!model) {
        return String(modelId);
    }
    const displayName = model.displayName?.trim() || model.modelName;
    return `${displayName} / ${model.modelName}`;
};

const readPromptLabel = (
    promptId: string,
    promptById: Map<string, AiBusinessConfigPromptRecord>,
    capabilityByCode: Map<string, AiBusinessConfigCapabilityRecord>
) => {
    const prompt = promptById.get(promptId);
    if (!prompt) {
        return String(promptId);
    }
    const name = prompt.name?.trim();
    if (name) {
        return name;
    }
    if (prompt.capability) {
        return readCapabilityLabel(prompt.capability, capabilityByCode);
    }
    return `提示词 ${prompt.id ?? promptId}`;
};

const readConfigName = (record: AiBusinessConfigRecord) => {
    return `${record.capability} / ${record.promptTemplateId} / ${record.modelId}`;
};

const normalizeJsonText = (value?: string | null) => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : "{}";
};

export const BusinessConfigPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canViewConfig = hasPermission("ai:config:view") || hasPermission("ai:config:edit");
    const canEditConfig = hasPermission("ai:config:edit");
    const [query, setQuery] = useState<AiBusinessConfigQuery>({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<BusinessConfigFilters>(DEFAULT_BUSINESS_CONFIG_FILTERS);
    const [editingConfig, setEditingConfig] = useState<AiBusinessConfigRecord | null>(null);
    const [businessConfigEditDrawerOpen, setBusinessConfigEditDrawerOpen] = useState(false);
    const hasActiveFilters =
        Boolean(filters.capability) || filters.enabled !== DEFAULT_BUSINESS_CONFIG_FILTERS.enabled;

    const businessConfigCapabilitiesQuery = useQuery({
        queryKey: ["ai", "business-config", "capabilities"],
        queryFn: service.listBusinessConfigCapabilities,
        enabled: canViewConfig,
        retry: false
    });

    const businessConfigModelsQuery = useQuery({
        queryKey: ["ai", "business-config", "models"],
        queryFn: service.listBusinessConfigModels,
        enabled: canViewConfig,
        retry: false
    });

    const businessConfigPromptsQuery = useQuery({
        queryKey: ["ai", "business-config", "prompts"],
        queryFn: service.listBusinessConfigPrompts,
        enabled: canViewConfig,
        retry: false
    });

    const businessConfigListQuery = useQuery({
        queryKey: ["ai", "business-config", "list", query],
        queryFn: () => service.listBusinessConfigs(query),
        enabled: canViewConfig,
        retry: false
    });

    const capabilityByCode = useMemo(() => {
        return new Map(
            (businessConfigCapabilitiesQuery.data || []).map((capability) => [
                capability.capability,
                capability
            ])
        );
    }, [businessConfigCapabilitiesQuery.data]);

    const modelById = useMemo(() => {
        return new Map((businessConfigModelsQuery.data || []).map((model) => [model.id, model]));
    }, [businessConfigModelsQuery.data]);

    const promptById = useMemo(() => {
        return new Map(
            (businessConfigPromptsQuery.data || [])
                .filter((prompt) => prompt.id != null)
                .map((prompt) => [prompt.id ?? "", prompt])
        );
    }, [businessConfigPromptsQuery.data]);

    const capabilityOptions = useMemo(() => {
        return (businessConfigCapabilitiesQuery.data || []).map((capability) => ({
            label: `${capability.name} / ${capability.capability}`,
            value: capability.capability
        }));
    }, [businessConfigCapabilitiesQuery.data]);

    const tableData = useMemo<BusinessConfigTableRow[]>(() => {
        return (businessConfigListQuery.data || []).map((config) => {
            const model = modelById.get(config.modelId);
            const prompt = promptById.get(config.promptTemplateId);
            return {
                ...config,
                capabilityName: readCapabilityLabel(config.capability, capabilityByCode),
                modelName: readModelLabel(config.modelId, modelById),
                modelTags: model?.capabilities || [],
                promptName: readPromptLabel(config.promptTemplateId, promptById, capabilityByCode),
                promptVersionNo: prompt?.currentVersionNo ?? null
            };
        });
    }, [businessConfigListQuery.data, capabilityByCode, modelById, promptById]);

    const filteredConfigs = useMemo(() => {
        const normalizedSearchText = searchText.trim().toLowerCase();
        if (!normalizedSearchText) {
            return tableData;
        }
        return tableData.filter((record) => {
            return (
                record.capability.toLowerCase().includes(normalizedSearchText) ||
                record.capabilityName.toLowerCase().includes(normalizedSearchText) ||
                record.promptName.toLowerCase().includes(normalizedSearchText) ||
                record.modelName.toLowerCase().includes(normalizedSearchText)
            );
        });
    }, [searchText, tableData]);

    const invalidateBusinessConfigs = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "business-config"] });
    };

    const createMutation = useMutation({
        mutationFn: service.createBusinessConfig,
        onSuccess: async () => {
            await invalidateBusinessConfigs();
            setBusinessConfigEditDrawerOpen(false);
            setEditingConfig(null);
            messageApi.success("业务配置已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "业务配置新增失败");
        }
    });

    const updateMutation = useMutation({
        mutationFn: service.changeBusinessConfig,
        onSuccess: async () => {
            await invalidateBusinessConfigs();
            setBusinessConfigEditDrawerOpen(false);
            setEditingConfig(null);
            messageApi.success("业务配置已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "业务配置保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: service.deleteBusinessConfig,
        onSuccess: async () => {
            await invalidateBusinessConfigs();
            messageApi.success("业务配置已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "业务配置删除失败");
        }
    });

    useEffect(() => {
        if (businessConfigListQuery.isError) {
            const error = businessConfigListQuery.error;
            messageApi.error(error instanceof Error ? error.message : "业务配置列表加载失败");
        }
    }, [businessConfigListQuery.error, businessConfigListQuery.isError, messageApi]);

    const openCreateBusinessConfigDrawer = () => {
        setEditingConfig(null);
        setBusinessConfigEditDrawerOpen(true);
    };

    const openEditBusinessConfigDrawer = (record: AiBusinessConfigRecord) => {
        setEditingConfig(record);
        setBusinessConfigEditDrawerOpen(true);
    };

    const closeBusinessConfigEditDrawer = () => {
        if (createMutation.isPending || updateMutation.isPending) {
            return;
        }
        setBusinessConfigEditDrawerOpen(false);
        setEditingConfig(null);
    };

    const saveBusinessConfig = (command: AiBusinessConfigChangeCommand) => {
        if (command.id) {
            updateMutation.mutate(command);
            return;
        }
        createMutation.mutate(command);
    };

    const changeBusinessConfigEnabled = async (
        record: AiBusinessConfigRecord,
        enabled: boolean
    ) => {
        if (!record.id || !canEditConfig) {
            return;
        }
        await updateMutation.mutateAsync({
            id: record.id,
            capability: record.capability,
            promptTemplateId: record.promptTemplateId,
            modelId: record.modelId,
            defaultParamsJson: normalizeJsonText(record.defaultParamsJson),
            enabled
        });
    };

    const confirmDeleteBusinessConfig = (record: AiBusinessConfigRecord) => {
        if (!record.id) {
            return;
        }
        confirm.danger({
            title: "删除业务配置",
            message: `确认删除 ${readConfigName(record)}？`,
            description: "删除后对应业务能力将无法通过该配置解析提示词和模型。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(record.id ?? "")
        });
    };

    const applyFilters = () => {
        setQuery({
            capability: filters.capability || undefined,
            enabled: toEnabledQueryValue(filters.enabled)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_BUSINESS_CONFIG_FILTERS);
        setQuery({});
    };

    const columns: KuzhambuTableProps<BusinessConfigTableRow>["columns"] = [
        {
            title: "业务能力",
            dataIndex: "capability",
            key: "capability",
            width: DEFAULT_COLUMN_WIDTHS.capability,
            render: (capability: string, record) => {
                const domainTag = readCapabilityDomainTag(capability);
                return (
                    <div className="business-config-cell-title">
                        <Text strong ellipsis title={record.capabilityName}>
                            {record.capabilityName}
                        </Text>
                        <KuzhambuSpace>
                            <KuzhambuTag type={domainTag.type}>{domainTag.label}</KuzhambuTag>
                        </KuzhambuSpace>
                    </div>
                );
            }
        },
        {
            title: "提示词模板",
            dataIndex: "promptName",
            key: "promptName",
            width: DEFAULT_COLUMN_WIDTHS.prompt,
            ellipsis: true,
            render: (promptName: string) => (
                <Text ellipsis title={promptName}>
                    {promptName}
                </Text>
            )
        },
        {
            title: "模型",
            dataIndex: "modelName",
            key: "modelName",
            width: DEFAULT_COLUMN_WIDTHS.model,
            ellipsis: true,
            render: (modelName: string) => (
                <Text ellipsis title={modelName}>
                    {modelName}
                </Text>
            )
        },
        {
            title: centerColumnTitle("状态"),
            dataIndex: "enabled",
            key: "enabled",
            align: "center",
            className: "business-config-center-column",
            width: DEFAULT_COLUMN_WIDTHS.enabled,
            render: (enabled: boolean | null | undefined, record) => (
                <KuzhambuSwitch
                    checked={enabled !== false}
                    checkedChildren="启用"
                    unCheckedChildren="禁用"
                    aria-label={`切换 ${record.capabilityName} 业务配置状态`}
                    disabled={!canEditConfig || updateMutation.isPending}
                    onChange={(checked) => void changeBusinessConfigEnabled(record, checked)}
                />
            )
        },
        {
            title: centerColumnTitle("配置时间"),
            dataIndex: "configuredAt",
            key: "configuredAt",
            align: "center",
            className: "business-config-center-column",
            width: DEFAULT_COLUMN_WIDTHS.configuredAt,
            render: formatDate
        },
        {
            key: "actions",
            options: (record) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${record.capabilityName} 业务配置`,
                    disabled: !canEditConfig,
                    onClick: () => openEditBusinessConfigDrawer(record)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${record.capabilityName} 业务配置`,
                    disabled: !canEditConfig || !record.id,
                    onClick: () => confirmDeleteBusinessConfig(record)
                }
            ]
        }
    ];

    return (
        <>
            <KuzhambuListPage<BusinessConfigTableRow>
                pageClassName="business-config-page"
                title="业务配置"
                description="绑定业务能力、提示词模板和模型，统一控制业务 AI 调用配置"
                subjectName="业务配置"
                enableSearch
                enableFilter
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        label: "业务能力",
                        name: "capability",
                        render: () => (
                            <KuzhambuSelect
                                allowClear
                                options={capabilityOptions}
                                value={filters.capability ?? undefined}
                                showSearch
                                onChange={(capability) =>
                                    setFilters((current) => ({
                                        ...current,
                                        capability: capability ?? null
                                    }))
                                }
                            />
                        )
                    },
                    {
                        label: "状态",
                        name: "enabled",
                        render: () => (
                            <KuzhambuSelect
                                options={[
                                    { label: "全部", value: "ALL" },
                                    { label: "启用", value: "ENABLED" },
                                    { label: "禁用", value: "DISABLED" }
                                ]}
                                value={filters.enabled}
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
                pageActions={
                    <KuzhambuSpace>
                        <KuzhambuButton
                            testId="ai-business-config-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={
                                businessConfigListQuery.isFetching ||
                                businessConfigCapabilitiesQuery.isFetching ||
                                businessConfigModelsQuery.isFetching ||
                                businessConfigPromptsQuery.isFetching
                            }
                            onClick={() => void invalidateBusinessConfigs()}
                        >
                            刷新
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-business-config-create-button"
                            type="primary"
                            disabled={!canEditConfig}
                            onClick={openCreateBusinessConfigDrawer}
                        >
                            新增业务配置
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
                columns={columns}
                dataSource={filteredConfigs}
                loading={businessConfigListQuery.isFetching}
                locale={{
                    emptyText: businessConfigListQuery.isError
                        ? "业务配置列表加载失败，请确认权限和接口状态。"
                        : "暂无业务配置"
                }}
                rowKey={(record) => record.id || `${record.capability}-${record.modelId}`}
                pagination={{ pageSize: 10, showSizeChanger: true }}
                scroll={{ x: 1400 }}
                searchValue={searchText}
                searchPlaceholder="搜索业务能力、提示词或模型..."
                onAdd={openCreateBusinessConfigDrawer}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                onSearchChange={setSearchText}
            />

            <BusinessConfigEditDrawer
                canEdit={canEditConfig}
                capabilities={businessConfigCapabilitiesQuery.data || []}
                config={editingConfig}
                models={businessConfigModelsQuery.data || []}
                open={businessConfigEditDrawerOpen}
                prompts={businessConfigPromptsQuery.data || []}
                saving={createMutation.isPending || updateMutation.isPending}
                onClose={closeBusinessConfigEditDrawer}
                onSave={saveBusinessConfig}
            />
        </>
    );
};
