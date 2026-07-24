import { ReloadOutlined, SyncOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Card, Form, Table, Tag, Tooltip, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import * as service from "./action-status-service";
import type { AiActionStatusQuery } from "./action-status-service";
import type { AiActionStatusRecord } from "./action-status-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuForm, KuzhambuFormItem } from "@/components/kuzhambu-form";
import "./action-status-page.css";
import { KuzhambuSelect } from "@/components/kuzhambu-select";

const SCOPE_OPTIONS = [
    { label: "classics", value: "classics" },
    { label: "knowledge", value: "knowledge" },
    { label: "discovery", value: "discovery" },
    { label: "platform", value: "platform" }
];

const parseAvailable = (value?: string) => {
    if (value === "true") {
        return true;
    }
    if (value === "false") {
        return false;
    }
    return null;
};

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

const statusKey = (record: AiActionStatusRecord) =>
    `${record.scope || "-"}-${record.capability || "-"}`;

export const ActionStatusPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const [form] = Form.useForm<AiActionStatusQuery>();
    const canViewConfig = hasPermission("ai:config:view");
    const canEditConfig = hasPermission("ai:config:edit");
    const [query, setQuery] = useState<AiActionStatusQuery>({});
    const [refreshingKey, setRefreshingKey] = useState<string | null>(null);

    const actionCapabilitiesQuery = useQuery({
        queryKey: ["ai", "action-status", "capabilities"],
        queryFn: service.listActionCapabilities,
        enabled: canViewConfig,
        retry: false
    });

    const actionStatusPageQuery = useQuery({
        queryKey: ["ai", "action-status", query],
        queryFn: () => service.listActionStatuses(query),
        enabled: canViewConfig,
        retry: false
    });

    const capabilityOptions = useMemo(() => {
        return (actionCapabilitiesQuery.data || []).map((record) => ({
            label: `${record.name} / ${record.capability}`,
            value: record.capability
        }));
    }, [actionCapabilitiesQuery.data]);

    const refreshMutation = useMutation({
        mutationFn: service.refreshActionStatus,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["ai", "action-status"] });
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "动作状态刷新失败");
        },
        onSettled: () => {
            setRefreshingKey(null);
        }
    });

    useEffect(() => {
        if (actionStatusPageQuery.isError) {
            const error = actionStatusPageQuery.error;
            message.error(error instanceof Error ? error.message : "动作状态加载失败");
        }
    }, [message, actionStatusPageQuery.error, actionStatusPageQuery.isError]);

    const applyFilter = async () => {
        const values = await form.validateFields();
        setQuery(values);
    };

    const resetFilter = () => {
        form.resetFields();
        setQuery({});
    };

    const refreshOne = async (record: AiActionStatusRecord) => {
        if (!record.scope || !record.capability) {
            return;
        }
        setRefreshingKey(statusKey(record));
        await refreshMutation.mutateAsync({
            scope: record.scope,
            capability: record.capability
        });
        message.success("动作状态已刷新");
    };

    const refreshAll = async () => {
        const records = actionStatusPageQuery.data || [];
        for (const record of records) {
            if (record.scope && record.capability) {
                await refreshMutation.mutateAsync({
                    scope: record.scope,
                    capability: record.capability
                });
            }
        }
        message.success("动作状态已全部刷新");
    };

    const columns: ColumnsType<AiActionStatusRecord> = [
        {
            title: "scope",
            dataIndex: "scope",
            key: "scope",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "capability",
            dataIndex: "capability",
            key: "capability",
            render: (value?: string | null) => value || "-"
        },
        {
            title: "available",
            dataIndex: "available",
            key: "available",
            render: (available?: boolean | null) => (
                <Tag color={available ? "green" : "red"}>{available ? "可用" : "不可用"}</Tag>
            )
        },
        {
            title: "unavailableReason",
            dataIndex: "unavailableReason",
            key: "unavailableReason",
            render: (value?: string | null) => (
                <Typography.Text type={value ? "warning" : undefined}>
                    {value || "-"}
                </Typography.Text>
            )
        },
        {
            title: "checkedAt",
            dataIndex: "checkedAt",
            key: "checkedAt",
            render: formatDateTime
        },
        {
            key: "actions",
            render: (_, record) => (
                <KuzhambuButton
                    testId="ai-action-status-action-status-refresh-status-button"
                    icon={<SyncOutlined />}
                    disabled={!canEditConfig}
                    loading={refreshingKey === statusKey(record)}
                    onClick={() => void refreshOne(record)}
                >
                    刷新状态
                </KuzhambuButton>
            )
        }
    ];

    return (
        <KuzhambuPage
            className="action-status-page"
            title="AI 动作状态"
            description="检查 scope + capability 的可用状态和不可用原因"
            actions={
                <Tooltip title="刷新">
                    <KuzhambuButton
                        testId="ai-action-status-action-status-refresh-button"
                        icon={<ReloadOutlined />}
                        loading={
                            actionStatusPageQuery.isFetching || actionCapabilitiesQuery.isFetching
                        }
                        onClick={() => void actionStatusPageQuery.refetch()}
                    />
                </Tooltip>
            }
        >
            <Card className="action-status-filter-card">
                <KuzhambuForm form={form} className="action-status-filter-form">
                    <KuzhambuFormItem label="scope" name="scope" layoutSize="small">
                        <KuzhambuSelect
                            allowClear
                            className="action-status-filter-control"
                            options={SCOPE_OPTIONS}
                        />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem label="capability" name="capability" layoutSize="small">
                        <KuzhambuSelect
                            allowClear
                            className="action-status-filter-control"
                            options={capabilityOptions}
                        />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem label="available" name="available" layoutSize="small">
                        <KuzhambuSelect
                            allowClear
                            className="action-status-filter-control"
                            options={[
                                { label: "可用", value: "true" },
                                { label: "不可用", value: "false" }
                            ]}
                            onChange={(value) =>
                                form.setFieldValue("available", parseAvailable(value))
                            }
                        />
                    </KuzhambuFormItem>
                </KuzhambuForm>
                <KuzhambuSpace>
                    <KuzhambuButton
                        testId="ai-action-status-action-status-query-button"
                        type="primary"
                        onClick={() => void applyFilter()}
                    >
                        查询
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="ai-action-status-action-status-reset-button"
                        onClick={resetFilter}
                    >
                        重置
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="ai-action-status-action-status-refresh-all-button"
                        icon={<SyncOutlined />}
                        disabled={!canEditConfig || (actionStatusPageQuery.data || []).length === 0}
                        loading={refreshMutation.isPending}
                        onClick={() => void refreshAll()}
                    >
                        刷新全部
                    </KuzhambuButton>
                </KuzhambuSpace>
            </Card>

            <Table<AiActionStatusRecord>
                aria-label="AI 动作状态列表"
                rowKey={statusKey}
                className="action-status-table"
                columns={columns}
                dataSource={actionStatusPageQuery.data || []}
                loading={actionStatusPageQuery.isFetching}
                pagination={{ pageSize: 10, showSizeChanger: true }}
            />
        </KuzhambuPage>
    );
};
