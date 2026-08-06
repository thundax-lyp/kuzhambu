import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    CheckCircleOutlined,
    DeleteOutlined,
    PlusOutlined,
    ReloadOutlined,
    StopOutlined
} from "@ant-design/icons";
import type { Key } from "react";
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
    type KuzhambuTableProps
} from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { PromptEditDrawer } from "./prompt-edit-drawer";
import {
    DEFAULT_PROMPT_FILTERS,
    readCapabilityDomainTag,
    readCapabilityLabel,
    readPromptDisplayName,
    readTemplateRowKey
} from "./prompt-constants";
import type { PromptFilters } from "./prompt-constants";
import * as service from "./prompt-service";
import type { AiPromptTemplateQuery } from "./prompt-service";
import type { AiPromptTemplateRecord } from "./prompt-types";
import "./prompt-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    capability: 200,
    enabled: 112,
    registeredAt: 120
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

export const PromptPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canViewPrompt = hasPermission("ai:prompt:view") || hasPermission("ai:prompt:edit");
    const canEditPrompt = hasPermission("ai:prompt:edit");
    const [query, setQuery] = useState<AiPromptTemplateQuery>({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<PromptFilters>(DEFAULT_PROMPT_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingTemplate, setEditingTemplate] = useState<AiPromptTemplateRecord | null>(null);
    const [promptEditDrawerOpen, setPromptEditDrawerOpen] = useState(false);
    const hasSelectedPrompt = selectedRowKeys.length > 0;
    const hasActiveFilters =
        Boolean(filters.capability) || filters.enabled !== DEFAULT_PROMPT_FILTERS.enabled;

    const promptCapabilitiesQuery = useQuery({
        queryKey: ["ai", "prompt", "capabilities"],
        queryFn: service.listPromptCapabilities,
        enabled: canViewPrompt,
        retry: false
    });

    const promptTemplatePageQuery = useQuery({
        queryKey: ["ai", "prompt", "templates", query],
        queryFn: () => service.listPromptTemplates(query),
        enabled: canViewPrompt,
        retry: false
    });

    const updatePromptStatusMutation = useMutation({
        mutationFn: ({
            enabled,
            template
        }: {
            enabled: boolean;
            template: AiPromptTemplateRecord;
        }) => {
            if (!template.id) {
                throw new Error("提示词模板 ID 缺失");
            }
            return service.changePromptTemplateStatus({
                id: template.id,
                enabled
            });
        },
        onSuccess: async () => {
            await invalidatePrompt();
            messageApi.success("提示词状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "提示词状态更新失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: (template: AiPromptTemplateRecord) => {
            if (!template.id) {
                throw new Error("提示词模板 ID 缺失");
            }
            return service.deletePromptTemplate(template.id);
        },
        onSuccess: async () => {
            await invalidatePrompt();
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

    const invalidatePrompt = async () => {
        await queryClient.invalidateQueries({ queryKey: ["ai", "prompt"] });
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
        void invalidatePrompt();
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
        confirm.danger({
            title: "删除提示词模板",
            message: `确认删除 ${readPromptDisplayName(
                template,
                capabilityByCode.get(template.capability || "")?.name
            )}？删除后模板会被禁用并保留历史版本。`,
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(template)
        });
    };

    const batchChangeEnabled = async (enabled: boolean) => {
        if (!canEditPrompt || !hasSelectedPrompt) {
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
        if (!canEditPrompt || !hasSelectedPrompt) {
            return;
        }
        confirm.danger({
            title: "批量删除提示词模板",
            message: `确认删除 ${selectedTemplates.length} 个提示词模板？删除后模板会被禁用并保留历史版本。`,
            okText: "删除",
            onConfirm: async () => {
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
                    <span className="prompt-capability-tags">
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
                    disabled={!canEditPrompt || updatePromptStatusMutation.isPending}
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
                    key: "delete-divider",
                    type: "divider"
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
                pageClassName="prompt-page"
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
                            <KuzhambuSelect
                                allowClear
                                placeholder="全部"
                                value={filters.capability || undefined}
                                options={capabilityOptions}
                                loading={promptCapabilitiesQuery.isFetching}
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
                            <KuzhambuSelect
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
                            testId="ai-prompt-prompt-enable-button"
                            icon={<CheckCircleOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompt}
                            loading={updatePromptStatusMutation.isPending}
                            onClick={() => void batchChangeEnabled(true)}
                        >
                            启用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompt-prompt-disable-button"
                            icon={<StopOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompt}
                            loading={updatePromptStatusMutation.isPending}
                            onClick={() => void batchChangeEnabled(false)}
                        >
                            禁用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompt-prompt-batch-delete-button"
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompt}
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
                            testId="ai-prompt-prompt-refresh-button"
                            icon={<ReloadOutlined />}
                            loading={promptTemplatePageQuery.isFetching}
                            onClick={() => void invalidatePrompt()}
                        >
                            刷新
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompt-prompt-create-button"
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
                className="prompt-table"
                columns={columns}
                dataSource={filteredTemplates}
                loading={promptTemplatePageQuery.isFetching}
                pagination={false}
                scroll={{ x: 968 }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: () => ({ disabled: !canEditPrompt })
                }}
                locale={{
                    emptyText: promptTemplatePageQuery.isError
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
