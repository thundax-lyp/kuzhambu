import { ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Select, Space, Typography } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { SandwishListPage } from "@/components/sandwish-list-page";
import { useSandwishConfirm } from "@/components/sandwish-confirm-modal/hooks/use-sandwish-confirm";
import { SandwishTag } from "@/components/sandwish-tag";
import type { SandwishTableProps } from "@/components/sandwish-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { OpenClientEdit } from "./components/open-client-edit";
import { OpenClientSecretModal } from "./components/open-client-secret-modal";
import * as service from "./open-client-service";
import type {
    OpenClientPageQuery,
    OpenClientSaveCommand,
    OpenClientStatus
} from "./open-client-service";
import type { OpenClientRecord, OpenClientSecretRecord } from "./open-client-types";
import "./open-client-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 260,
    status: 120,
    expiredAt: 180,
    permissions: 280
};

interface OpenClientFilters {
    status: OpenClientStatus | "ALL";
}

const DEFAULT_OPEN_CLIENT_FILTERS: OpenClientFilters = {
    status: "ALL"
};

const statusLabels: Record<OpenClientStatus, string> = {
    ENABLED: "启用",
    DISABLED: "停用"
};

const statusOptions: Array<{ label: string; value: OpenClientStatus }> = [
    { value: "ENABLED", label: statusLabels.ENABLED },
    { value: "DISABLED", label: statusLabels.DISABLED }
];

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readStatusFilterValue = (value: OpenClientStatus | "ALL") => {
    return value === "ALL" ? undefined : value;
};

const readStatusLabel = (status?: string | null) => {
    return status && status in statusLabels ? statusLabels[status as OpenClientStatus] : "未知";
};

const statusTagType = (status?: string | null) => {
    if (status === "ENABLED") {
        return "success";
    }
    if (status === "DISABLED") {
        return "warning";
    }
    return "neutral";
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
};

export const OpenClientPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useSandwishConfirm();
    const queryClient = useQueryClient();
    const canEditOpenClient = hasPermission("open:client:edit");
    const [query, setQuery] = useState<OpenClientPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<OpenClientFilters>(DEFAULT_OPEN_CLIENT_FILTERS);
    const [editingClient, setEditingClient] = useState<OpenClientRecord | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const [secretResponse, setSecretResponse] = useState<OpenClientSecretRecord | null>(null);
    const hasActiveFilters = filters.status !== "ALL";

    const openClientQuery = useQuery({
        queryKey: ["open-client", "page", query],
        queryFn: () => service.pageOpenClients(query),
        retry: false
    });
    const openClientPage = openClientQuery.data;
    const openClients = useMemo(() => openClientPage?.records || [], [openClientPage?.records]);
    const totalCount = openClientPage?.count ?? openClientPage?.totalCount ?? 0;
    const currentPageNo = openClientPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = openClientPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const invalidateOpenClientPage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["open-client", "page"] });
    };

    const saveMutation = useMutation<
        OpenClientRecord | OpenClientSecretRecord,
        Error,
        OpenClientSaveCommand
    >({
        mutationFn: (request: OpenClientSaveCommand) =>
            request.id ? service.changeOpenClientInfo(request) : service.createOpenClient(request),
        onSuccess: async (response, variables) => {
            setEditorOpen(false);
            setEditingClient(null);
            await invalidateOpenClientPage();
            if (!variables.id) {
                setSecretResponse(response as OpenClientSecretRecord);
            }
            messageApi.success(variables.id ? "开放客户端已更新" : "开放客户端已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const statusMutation = useMutation({
        mutationFn: service.changeOpenClientStatus,
        onSuccess: async () => {
            await invalidateOpenClientPage();
            messageApi.success("开放客户端状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "状态更新失败");
        }
    });

    const detailMutation = useMutation({
        mutationFn: service.getOpenClient,
        onSuccess: (client) => {
            setEditingClient(client);
            setEditorOpen(true);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "获取开放客户端失败");
        }
    });

    const resetSecretMutation = useMutation({
        mutationFn: service.resetOpenClientSecret,
        onSuccess: async (response) => {
            await invalidateOpenClientPage();
            setEditingClient((current) =>
                current && current.id === response.id
                    ? { ...current, apiKey: response.apiKey }
                    : current
            );
            setSecretResponse(response);
            messageApi.success("API SECRET 已重置");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "重置失败");
        }
    });

    const updateQuery = (values: Partial<OpenClientPageQuery>) => {
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                name: nextQuery.name,
                status: nextQuery.status,
                pageNo: values.pageNo || DEFAULT_PAGE_NO,
                pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchOpenClients = (value: string) => {
        setSearchText(value);
        updateQuery({ name: normalizeText(value) });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_OPEN_CLIENT_FILTERS);
        updateQuery({
            status: undefined
        });
    };

    const applyFilters = () => {
        updateQuery({
            status: readStatusFilterValue(filters.status)
        });
    };

    const openCreateEditor = () => {
        setEditingClient(null);
        setEditorOpen(true);
    };

    const openUpdateEditor = (client: OpenClientRecord) => {
        detailMutation.mutate({ id: client.id });
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingClient(null);
    };

    const saveOpenClient = (request: OpenClientSaveCommand) => {
        saveMutation.mutate(request);
    };

    const toggleStatus = (client: OpenClientRecord) => {
        statusMutation.mutate({
            id: client.id,
            status: client.status === "ENABLED" ? "DISABLED" : "ENABLED"
        });
    };

    const confirmResetSecret = (client: OpenClientRecord) => {
        confirm.danger({
            title: "重置 API SECRET",
            message: `确认重置 ${client.name || ""} 的 API SECRET？`,
            description: "原 API SECRET 会立即失效，新明文只会在本次结果中显示。",
            okText: "重置",
            onConfirm: () => resetSecretMutation.mutateAsync({ id: client.id })
        });
    };

    const copySecretValue = async (label: string, value?: string | null) => {
        if (!value) {
            return;
        }
        try {
            await navigator.clipboard.writeText(value);
            messageApi.success(`${label} 已复制`);
        } catch {
            messageApi.error("复制失败");
        }
    };

    const columns: SandwishTableProps<OpenClientRecord>["columns"] = [
        {
            key: "name",
            title: "客户端",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (_, client) => (
                <div className="open-client-name-cell">
                    <Text strong>{client.name}</Text>
                    {client.remarks ? <Text type="secondary">{client.remarks}</Text> : null}
                </div>
            )
        },
        {
            dataIndex: "status",
            key: "status",
            title: "状态",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (status?: string | null) => (
                <SandwishTag type={statusTagType(status)}>{readStatusLabel(status)}</SandwishTag>
            )
        },
        {
            dataIndex: "expiredAt",
            key: "expiredAt",
            title: "过期时间",
            width: DEFAULT_COLUMN_WIDTHS.expiredAt,
            render: (expiredAt?: string | null) => formatDateTime(expiredAt)
        },
        {
            dataIndex: "permissions",
            key: "permissions",
            title: "权限",
            width: DEFAULT_COLUMN_WIDTHS.permissions,
            render: (permissions?: string[] | null) => (
                <Space wrap size={[4, 4]} className="open-client-permission-list">
                    {(permissions || []).map((permission) => (
                        <SandwishTag key={permission}>{permission}</SandwishTag>
                    ))}
                    {!permissions?.length ? <Text type="secondary">-</Text> : null}
                </Space>
            )
        },
        {
            key: "actions",
            options: (client) => {
                const enabled = client.status === "ENABLED";
                return [
                    {
                        key: "edit",
                        text: "编辑",
                        ariaLabel: `编辑 ${client.name}`,
                        disabled: !canEditOpenClient,
                        onClick: () => openUpdateEditor(client)
                    },
                    {
                        key: "status",
                        text: enabled ? "停用" : "启用",
                        type: "warning" as const,
                        disabled: !canEditOpenClient,
                        onClick: () => toggleStatus(client)
                    },
                    {
                        key: "secret",
                        text: "重置密钥",
                        type: "warning" as const,
                        disabled: !canEditOpenClient,
                        onClick: () => confirmResetSecret(client)
                    }
                ];
            }
        }
    ];

    return (
        <>
            <SandwishListPage<OpenClientRecord>
                pageClassName="open-client-page"
                title="开放客户端"
                description="管理第三方系统访问 Open API 使用的 API KEY、IP 白名单、有效期和最小权限集合。"
                subjectName="开放客户端"
                enableSearch
                searchValue={searchText}
                searchPlaceholder="搜索客户端..."
                onSearchChange={searchOpenClients}
                enableFilter
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "status",
                        label: "状态",
                        render: () => (
                            <Select
                                value={filters.status}
                                options={[{ value: "ALL", label: "全部" }, ...statusOptions]}
                                onChange={(status) => setFilters({ status })}
                            />
                        )
                    }
                ]}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                enableAdd={canEditOpenClient}
                addText="新增"
                onAdd={openCreateEditor}
                pageActions={
                    <Button
                        icon={<ReloadOutlined />}
                        loading={openClientQuery.isFetching}
                        onClick={() => openClientQuery.refetch()}
                    >
                        刷新
                    </Button>
                }
                tableLayout="fixed"
                columns={columns}
                dataSource={openClients}
                rowKey="id"
                loading={openClientQuery.isLoading || openClientQuery.isFetching}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
            />

            <OpenClientEdit
                open={editorOpen}
                client={editingClient}
                saving={saveMutation.isPending}
                canEdit={canEditOpenClient}
                resetSecretLoading={resetSecretMutation.isPending}
                onClose={closeEditor}
                onSave={saveOpenClient}
                onGenerateSecret={confirmResetSecret}
                onCopySecret={copySecretValue}
            />

            <OpenClientSecretModal
                secret={secretResponse}
                onClose={() => setSecretResponse(null)}
                onCopySecret={copySecretValue}
            />
        </>
    );
};
