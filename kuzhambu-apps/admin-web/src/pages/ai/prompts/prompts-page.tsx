import { PlusOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Select, Typography } from "antd";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { PromptEditDrawer } from "./components/prompt-edit-drawer";
import * as service from "./prompts-service";
import type { AiPromptTemplateQuery } from "./prompts-service";
import type { AiPromptTemplateRecord } from "./prompts-types";
import "./prompts-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    domain: 120,
    capability: 160,
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

const readCapabilityDomain = (capability?: string | null) => {
    if (!capability) {
        return "其他";
    }
    if (capability.startsWith("classics_")) {
        return "古籍管理";
    }
    if (capability.startsWith("discovery_")) {
        return "知识发现";
    }
    if (capability.startsWith("knowledge_")) {
        return "知识治理";
    }
    if (capability.startsWith("platform_")) {
        return "平台";
    }
    if (capability.startsWith("prompt_")) {
        return "提示词";
    }
    return "其他";
};

const trimCapabilityDomainPrefix = (name: string) => {
    return name
        .replace(/^古籍/, "")
        .replace(/^知识发现/, "")
        .replace(/^知识/, "")
        .replace(/^平台/, "")
        .replace(/^提示词/, "")
        .trim();
};

const readCapabilityLabel = (capability?: string | null, name?: string | null) => {
    const label = name?.trim();
    return label ? trimCapabilityDomainPrefix(label) || label : capability || "-";
};

const readPromptName = (template: AiPromptTemplateRecord) => {
    return template.name?.trim() || template.capability || `模板 ${template.id ?? ""}`;
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
    const [editingTemplate, setEditingTemplate] = useState<AiPromptTemplateRecord | null>(null);
    const [drawerOpen, setDrawerOpen] = useState(false);
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
            label: `${readCapabilityDomain(record.capability)} / ${readCapabilityLabel(
                record.capability,
                record.name
            )}`,
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
                readCapabilityDomain(template.capability).toLowerCase().includes(keyword) ||
                readCapabilityLabel(template.capability, capability?.name)
                    .toLowerCase()
                    .includes(keyword)
            );
        });
    }, [capabilityByCode, searchText, templatesQuery.data]);

    useEffect(() => {
        if (templatesQuery.isError) {
            const error = templatesQuery.error;
            messageApi.error(error instanceof Error ? error.message : "提示词模板加载失败");
        }
    }, [messageApi, templatesQuery.error, templatesQuery.isError]);

    const invalidatePrompts = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "prompts"] });
    };

    const applyFilters = () => {
        setQuery({
            capability: filters.capability || undefined,
            enabled: toEnabledQueryValue(filters.enabled)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_PROMPT_FILTERS);
        setQuery({});
    };

    const openEdit = (template: AiPromptTemplateRecord) => {
        setEditingTemplate(template);
        setDrawerOpen(true);
    };

    const openCreate = () => {
        setEditingTemplate(null);
        setDrawerOpen(true);
    };

    const closeEditor = () => {
        setDrawerOpen(false);
        setEditingTemplate(null);
    };

    const handleSaved = () => {
        setDrawerOpen(false);
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
            title: "业务域",
            key: "domain",
            width: DEFAULT_COLUMN_WIDTHS.domain,
            render: (_, template) => (
                <KuzhambuTag type="info">{readCapabilityDomain(template.capability)}</KuzhambuTag>
            )
        },
        {
            title: "能力",
            dataIndex: "capability",
            key: "capability",
            width: DEFAULT_COLUMN_WIDTHS.capability,
            render: (capability?: string | null) => (
                <KuzhambuTag type="accent">
                    {readCapabilityLabel(capability, capabilityByCode.get(capability || "")?.name)}
                </KuzhambuTag>
            )
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
            title: "注册时间",
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
                    onClick: () => openEdit(template)
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
                onSearchChange={setSearchText}
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
                pageActions={
                    <>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-create-button"
                            type="primary"
                            icon={<PlusOutlined />}
                            disabled={!canEditPrompt}
                            onClick={openCreate}
                        >
                            新建模板
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompts-prompts-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={templatesQuery.isFetching}
                            onClick={() => void invalidatePrompts()}
                        >
                            刷新
                        </KuzhambuButton>
                    </>
                }
                rowKey={(template) => template.id || template.capability || ""}
                className="prompts-table"
                columns={columns}
                dataSource={filteredTemplates}
                loading={templatesQuery.isFetching}
                pagination={false}
                scroll={{ x: 1088 }}
                locale={{
                    emptyText: templatesQuery.isError
                        ? "提示词模板加载失败，请确认权限和接口状态。"
                        : "暂无提示词模板"
                }}
            />

            <PromptEditDrawer
                key={drawerOpen ? editingTemplate?.id || "create" : "closed"}
                canEdit={canEditPrompt}
                capabilityOptions={capabilityOptions}
                open={drawerOpen}
                template={editingTemplate}
                onClose={closeEditor}
                onSaved={handleSaved}
            />
        </>
    );
};
