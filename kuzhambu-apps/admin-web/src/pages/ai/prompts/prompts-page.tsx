import {
    CheckCircleOutlined,
    DeleteOutlined,
    PlusOutlined,
    ReloadOutlined,
    StopOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Select, Typography } from "antd";
import type { Key } from "react";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTagType } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { PromptEditDrawer } from "./components/prompt-edit-drawer";
import * as service from "./prompts-service";
import type { AiPromptTemplateQuery } from "./prompts-service";
import type { AiPromptTemplateRecord } from "./prompts-types";
import "./prompts-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    capability: 200,
    enabled: 112,
    registeredAt: 120,
    description: 360
};

interface PromptFilters {
    capability?: string | null;
    enabled: "ALL" | "ENABLED" | "DISABLED";
}

const DEFAULT_PROMPT_FILTERS: PromptFilters = {
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

const toEnabledQueryValue = (enabled: PromptFilters["enabled"]) => {
    if (enabled === "ENABLED") {
        return true;
    }
    if (enabled === "DISABLED") {
        return false;
    }
    return undefined;
};

const readCapabilityLabel = (capability?: string | null, name?: string | null) => {
    const label = name?.trim();
    return label || capability || "-";
};

const readCapabilityDomainTag = (
    capability?: string | null
): { label: string; type: KuzhambuTagType } => {
    if (capability?.startsWith("classics_")) {
        return { label: "古籍", type: "info" };
    }
    if (capability?.startsWith("discovery_")) {
        return { label: "发现", type: "accent" };
    }
    if (capability?.startsWith("knowledge_")) {
        return { label: "知识", type: "success" };
    }
    if (capability?.startsWith("platform_")) {
        return { label: "平台", type: "warning" };
    }
    if (capability?.startsWith("prompt_")) {
        return { label: "提示词", type: "danger" };
    }
    return { label: "其他", type: "neutral" };
};

const readPromptName = (template: AiPromptTemplateRecord) => {
    return template.name?.trim() || template.capability || `模板 ${template.id ?? ""}`;
};

const readTemplateRowKey = (template: AiPromptTemplateRecord): Key => {
    return template.id || template.capability || "";
};

const readPromptDisplayName = (
    template: AiPromptTemplateRecord,
    capabilityName?: string | null
) => {
    const name = template.name?.trim();
    if (name && !/\bDefault\b/i.test(name)) {
        return name;
    }
    const capabilityLabel = readCapabilityLabel(template.capability, capabilityName);
    return capabilityLabel === "-" ? readPromptName(template) : `${capabilityLabel}提示词`;
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

    const capabilitiesQuery = useQuery({
        queryKey: ["ai", "prompts", "capabilities"],
        queryFn: service.listPromptCapabilities,
        enabled: canViewPrompt,
        retry: false
    });

    const templatesQuery = useQuery({
        queryKey: ["ai", "prompts", "templates", query],
        queryFn: () => service.listPromptTemplates(query),
        enabled: canViewPrompt,
        retry: false
    });

    const statusMutation = useMutation({
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
        return new Map((capabilitiesQuery.data || []).map((record) => [record.capability, record]));
    }, [capabilitiesQuery.data]);

    const capabilityOptions = useMemo(() => {
        return (capabilitiesQuery.data || []).map((record) => ({
            label: readCapabilityLabel(record.capability, record.name),
            value: record.capability
        }));
    }, [capabilitiesQuery.data]);

    const filteredTemplates = useMemo(() => {
        const keyword = searchText.trim().toLowerCase();
        if (!keyword) {
            return templatesQuery.data || [];
        }
        return (templatesQuery.data || []).filter((template) => {
            const capability = capabilityByCode.get(template.capability || "");
            return (
                (template.name || "").toLowerCase().includes(keyword) ||
                (template.description || "").toLowerCase().includes(keyword) ||
                (template.capability || "").toLowerCase().includes(keyword) ||
                readCapabilityLabel(template.capability, capability?.name)
                    .toLowerCase()
                    .includes(keyword)
            );
        });
    }, [capabilityByCode, searchText, templatesQuery.data]);

    const selectedTemplates = useMemo(() => {
        const selectedKeys = new Set(selectedRowKeys.map(String));
        return filteredTemplates.filter((template) =>
            selectedKeys.has(String(readTemplateRowKey(template)))
        );
    }, [filteredTemplates, selectedRowKeys]);

    useEffect(() => {
        if (templatesQuery.isError) {
            const error = templatesQuery.error;
            messageApi.error(error instanceof Error ? error.message : "提示词模板加载失败");
        }
    }, [messageApi, templatesQuery.error, templatesQuery.isError]);

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
        statusMutation.mutate({ enabled, template });
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
            selectedTemplates.map((template) => statusMutation.mutateAsync({ enabled, template }))
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

    const columns: KuzhambuTableProps<AiPromptTemplateRecord>["columns"] = [
        {
            title: "模板名称",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (_, template) => (
                <Text strong>
                    {readPromptDisplayName(
                        template,
                        capabilityByCode.get(template.capability || "")?.name
                    )}
                </Text>
            )
        },
        {
            title: "能力",
            dataIndex: "capability",
            key: "capability",
            width: DEFAULT_COLUMN_WIDTHS.capability,
            render: (capability?: string | null) => {
                const domainTag = readCapabilityDomainTag(capability);
                return (
                    <span className="prompts-capability-tags">
                        <KuzhambuTag type={domainTag.type}>{domainTag.label}</KuzhambuTag>
                        <KuzhambuTag type="neutral">
                            {readCapabilityLabel(
                                capability,
                                capabilityByCode.get(capability || "")?.name
                            )}
                        </KuzhambuTag>
                    </span>
                );
            }
        },
        {
            title: "状态",
            dataIndex: "enabled",
            key: "enabled",
            width: DEFAULT_COLUMN_WIDTHS.enabled,
            align: "center",
            render: (enabled: boolean | null | undefined, template) => (
                <KuzhambuSwitch
                    checked={enabled !== false}
                    checkedChildren="启用"
                    unCheckedChildren="禁用"
                    aria-label={`切换 ${readPromptDisplayName(
                        template,
                        capabilityByCode.get(template.capability || "")?.name
                    )} 状态，当前${enabled === false ? "禁用" : "启用"}`}
                    disabled={!canEditPrompt || statusMutation.isPending}
                    onChange={(checked) => changeEnabled(template, checked)}
                />
            )
        },
        {
            title: "日期",
            dataIndex: "registeredAt",
            key: "registeredAt",
            width: DEFAULT_COLUMN_WIDTHS.registeredAt,
            align: "center",
            render: formatDate
        },
        {
            title: "说明",
            dataIndex: "description",
            key: "description",
            width: DEFAULT_COLUMN_WIDTHS.description,
            ellipsis: true,
            render: (description?: string | null) => description || "-"
        },
        {
            key: "actions",
            options: (template) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${readPromptDisplayName(
                        template,
                        capabilityByCode.get(template.capability || "")?.name
                    )}`,
                    disabled: !canEditPrompt,
                    onClick: () => openEditPromptDrawer(template)
                },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${readPromptDisplayName(
                        template,
                        capabilityByCode.get(template.capability || "")?.name
                    )}`,
                    disabled: !canEditPrompt || deleteMutation.isPending,
                    onClick: () => deleteTemplate(template)
                }
            ]
        }
    ];

    return (
        <>
            <KuzhambuListPage<AiPromptTemplateRecord>
                pageClassName="prompts-page"
                title="提示词管理"
                description="维护 AI 提示词模板、变量、版本对比和回滚。"
                subjectName="提示词"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={changeSearchText}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "capability",
                        label: "能力",
                        render: () => (
                            <Select
                                allowClear
                                placeholder="全部"
                                value={filters.capability || undefined}
                                options={capabilityOptions}
                                loading={capabilitiesQuery.isFetching}
                                onChange={(capability) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        capability: capability || null
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
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-enable-button"
                            icon={<CheckCircleOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompts}
                            loading={statusMutation.isPending}
                            onClick={() => void batchChangeEnabled(true)}
                        >
                            启用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-disable-button"
                            icon={<StopOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompts}
                            loading={statusMutation.isPending}
                            onClick={() => void batchChangeEnabled(false)}
                        >
                            禁用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-batch-delete-button"
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompts}
                            loading={deleteMutation.isPending}
                            onClick={batchDeleteTemplates}
                        >
                            批量删除
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
                pageActions={
                    <>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={templatesQuery.isFetching}
                            onClick={() => void invalidatePrompts()}
                        >
                            刷新
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-create-button"
                            type="primary"
                            icon={<PlusOutlined />}
                            disabled={!canEditPrompt}
                            onClick={openCreatePromptDrawer}
                        >
                            新建
                        </KuzhambuButton>
                    </>
                }
                rowKey={readTemplateRowKey}
                className="prompts-table"
                columns={columns}
                dataSource={filteredTemplates}
                loading={templatesQuery.isFetching}
                pagination={false}
                scroll={{ x: 968 }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: () => ({
                        disabled: !canEditPrompt
                    })
                }}
                locale={{
                    emptyText: templatesQuery.isError
                        ? "提示词模板加载失败，请确认权限和接口状态。"
                        : "暂无提示词模板"
                }}
            />

            <PromptEditDrawer
                key={promptEditDrawerOpen ? editingTemplate?.id || "create" : "closed"}
                canEdit={canEditPrompt}
                capabilityOptions={capabilityOptions}
                open={promptEditDrawerOpen}
                template={editingTemplate}
                onClose={closePromptEditDrawer}
                onSaved={handleSaved}
            />
        </>
    );
};
