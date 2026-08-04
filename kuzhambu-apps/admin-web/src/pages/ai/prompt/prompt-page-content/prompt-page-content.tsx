import {
    CheckCircleOutlined,
    DeleteOutlined,
    PlusOutlined,
    ReloadOutlined,
    StopOutlined
} from "@ant-design/icons";
import { Typography } from "antd";
import type { Dispatch, Key, SetStateAction } from "react";
import {
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuSwitch,
    KuzhambuTag,
    type KuzhambuTableProps
} from "@/components";

import type {
    AiPromptCapabilityRecord,
    AiPromptTemplateRecord
} from "@/pages/ai/prompt/prompt-types";
import {
    readCapabilityDomainTag,
    readCapabilityLabel,
    readPromptDisplayName,
    readTemplateRowKey
} from "@/pages/ai/prompt/prompt-page-content-support";
import type {
    PromptCapabilityOption,
    PromptFilters
} from "@/pages/ai/prompt/prompt-page-content-support";
import { PromptEditDrawer } from "@/pages/ai/prompt/prompt-edit-drawer/prompt-edit-drawer";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    capability: 200,
    enabled: 112,
    registeredAt: 120,
    description: 360
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

interface PromptPageContentProps {
    canEditPrompt: boolean;
    capabilityByCode: Map<string, AiPromptCapabilityRecord>;
    capabilityOptions: PromptCapabilityOption[];
    deletePending: boolean;
    editingTemplate: AiPromptTemplateRecord | null;
    filterActive: boolean;
    filters: PromptFilters;
    hasSelectedPrompt: boolean;
    loading: boolean;
    promptCapabilitiesLoading: boolean;
    promptEditDrawerOpen: boolean;
    promptTemplateLoadError: boolean;
    records: AiPromptTemplateRecord[];
    searchText: string;
    selectedRowKeys: Key[];
    updatePromptStatusPending: boolean;
    onBatchDelete: () => void;
    onBatchDisable: () => void;
    onBatchEnable: () => void;
    onChangeEnabled: (template: AiPromptTemplateRecord, enabled: boolean) => void;
    onClosePromptEditDrawer: () => void;
    onCreatePrompt: () => void;
    onDeletePrompt: (template: AiPromptTemplateRecord) => void;
    onEditPrompt: (template: AiPromptTemplateRecord) => void;
    onFilterApply: () => void;
    onFilterReset: () => void;
    onFiltersChange: Dispatch<SetStateAction<PromptFilters>>;
    onRefresh: () => void;
    onSaved: () => void;
    onSearchChange: (value: string) => void;
    onSelectedRowKeysChange: (selectedRowKeys: Key[]) => void;
}

export const PromptPageContent = ({
    canEditPrompt,
    capabilityByCode,
    capabilityOptions,
    deletePending,
    editingTemplate,
    filterActive,
    filters,
    hasSelectedPrompt,
    loading,
    promptCapabilitiesLoading,
    promptEditDrawerOpen,
    promptTemplateLoadError,
    records,
    searchText,
    selectedRowKeys,
    updatePromptStatusPending,
    onBatchDelete,
    onBatchDisable,
    onBatchEnable,
    onChangeEnabled,
    onClosePromptEditDrawer,
    onCreatePrompt,
    onDeletePrompt,
    onEditPrompt,
    onFilterApply,
    onFilterReset,
    onFiltersChange,
    onRefresh,
    onSaved,
    onSearchChange,
    onSelectedRowKeysChange
}: PromptPageContentProps) => {
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
                    disabled={!canEditPrompt || updatePromptStatusPending}
                    onChange={(checked) => onChangeEnabled(template, checked)}
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
                    onClick: () => onEditPrompt(template)
                },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${readPromptDisplayName(
                        template,
                        capabilityByCode.get(template.capability || "")?.name
                    )}`,
                    disabled: !canEditPrompt || deletePending,
                    onClick: () => onDeletePrompt(template)
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
                onSearchChange={onSearchChange}
                filterActive={filterActive}
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
                                loading={promptCapabilitiesLoading}
                                onChange={(capability) =>
                                    onFiltersChange((currentFilters) => ({
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
                                    onFiltersChange((currentFilters) => ({
                                        ...currentFilters,
                                        enabled
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={onFilterApply}
                onFilterReset={onFilterReset}
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            testId="ai-prompt-prompt-enable-button"
                            icon={<CheckCircleOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompt}
                            loading={updatePromptStatusPending}
                            onClick={onBatchEnable}
                        >
                            启用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompt-prompt-disable-button"
                            icon={<StopOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompt}
                            loading={updatePromptStatusPending}
                            onClick={onBatchDisable}
                        >
                            禁用
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompt-prompt-batch-delete-button"
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditPrompt || !hasSelectedPrompt}
                            loading={deletePending}
                            onClick={onBatchDelete}
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
                            loading={loading}
                            onClick={onRefresh}
                        >
                            刷新
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="ai-prompt-prompt-create-button"
                            type="primary"
                            icon={<PlusOutlined />}
                            disabled={!canEditPrompt}
                            onClick={onCreatePrompt}
                        >
                            新建
                        </KuzhambuButton>
                    </>
                }
                rowKey={readTemplateRowKey}
                className="prompt-table"
                columns={columns}
                dataSource={records}
                loading={loading}
                pagination={false}
                scroll={{ x: 968 }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: onSelectedRowKeysChange,
                    getCheckboxProps: () => ({
                        disabled: !canEditPrompt
                    })
                }}
                locale={{
                    emptyText: promptTemplateLoadError
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
                onClose={onClosePromptEditDrawer}
                onSaved={onSaved}
            />
        </>
    );
};
