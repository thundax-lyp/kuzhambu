import {
    DeleteOutlined,
    DownloadOutlined,
    EyeOutlined,
    FileOutlined,
    ReloadOutlined,
    UploadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Input, Select, Space, Typography } from "antd";
import { useMemo, useRef, useState } from "react";
import type { ChangeEvent, Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./storage-object-service";
import type { StoragePageQuery } from "./storage-object-service";
import type { StorageContentMode, StorageRecord } from "./storage-object-types";
import "./storage-object-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 280,
    contentType: 180,
    owner: 170,
    size: 120,
    objectStatus: 120,
    referenceStatus: 130,
    remarks: 240
};

type StorageObjectStatusFilter = "ALL" | "ACTIVE" | "DELETING" | "DELETED";
type StorageReferenceStatusFilter = "ALL" | "REFERENCED" | "UNREFERENCED";

interface StorageObjectFilters {
    contentType: string;
    objectStatus: StorageObjectStatusFilter;
    referenceStatus: StorageReferenceStatusFilter;
    remarks: string;
}

const DEFAULT_STORAGE_OBJECT_FILTERS: StorageObjectFilters = {
    contentType: "",
    objectStatus: "ALL",
    referenceStatus: "ALL",
    remarks: ""
};

const objectStatusLabels: Record<Exclude<StorageObjectStatusFilter, "ALL">, string> = {
    ACTIVE: "可用",
    DELETING: "删除中",
    DELETED: "已删除"
};

const referenceStatusLabels: Record<Exclude<StorageReferenceStatusFilter, "ALL">, string> = {
    REFERENCED: "已引用",
    UNREFERENCED: "未引用"
};

const ownerTypeLabels: Record<string, string> = {
    USER: "后台用户",
    MEMBER: "前台会员"
};

const uploadAccept =
    ".jpg,.jpeg,.png,.gif,.webp,.pdf,.txt,.md,.csv,.json,.html,.zip,.docx,.xlsx,.pptx";

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readFilename = (storage: StorageRecord) => {
    return normalizeSearch(storage.originalFilename) || `对象 ${storage.id}`;
};

const toStorageContentUrl = (storage: StorageRecord, mode: StorageContentMode) => {
    if (!storage.id) {
        return undefined;
    }
    return toAuthenticatedResourceUrl(service.getStorageObjectContentUrl(storage.id, mode));
};

const formatFileSize = (size?: number | null) => {
    if (typeof size !== "number" || !Number.isFinite(size) || size < 0) {
        return null;
    }

    if (size < 1024) {
        return `${size} B`;
    }

    const units = ["KB", "MB", "GB", "TB"];
    let normalizedSize = size / 1024;
    let unitIndex = 0;
    while (normalizedSize >= 1024 && unitIndex < units.length - 1) {
        normalizedSize /= 1024;
        unitIndex += 1;
    }
    return `${normalizedSize.toFixed(normalizedSize >= 10 ? 1 : 2)} ${units[unitIndex]}`;
};

const readStatusFilterValue = <T extends string>(value: T | "ALL") => {
    return value === "ALL" ? undefined : value;
};

const objectStatusTagType = (status?: string | null) => {
    if (status === "ACTIVE") {
        return "success";
    }
    if (status === "DELETING") {
        return "warning";
    }
    if (status === "DELETED") {
        return "danger";
    }
    return "neutral";
};

const referenceStatusTagType = (status?: string | null) => {
    return status === "REFERENCED" ? "success" : "neutral";
};

const sortByMove = (
    storages: StorageRecord[],
    sourceStorage: StorageRecord,
    targetStorage: StorageRecord,
    position: KuzhambuTableSortPosition
) => {
    const sourceIndex = storages.findIndex((storage) => storage.id === sourceStorage.id);
    const targetIndex = storages.findIndex((storage) => storage.id === targetStorage.id);
    if (sourceIndex < 0 || targetIndex < 0) {
        return storages;
    }

    const nextStorages = [...storages];
    const [movedStorage] = nextStorages.splice(sourceIndex, 1);
    const nextTargetIndex = nextStorages.findIndex((storage) => storage.id === targetStorage.id);
    nextStorages.splice(
        position === "before" ? nextTargetIndex : nextTargetIndex + 1,
        0,
        movedStorage
    );
    return nextStorages;
};

export const StorageObjectPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canEditStorage = hasPermission("storage:object:edit");
    const [query, setQuery] = useState<StoragePageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<StorageObjectFilters>(DEFAULT_STORAGE_OBJECT_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const uploadInputRef = useRef<HTMLInputElement>(null);
    const hasSelectedStorages = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(
        filters.contentType.trim() ||
        filters.remarks.trim() ||
        filters.objectStatus !== "ALL" ||
        filters.referenceStatus !== "ALL"
    );

    const storageQuery = useQuery({
        queryKey: ["storage-object", "page", query],
        queryFn: () => service.pageStorageObjects(query),
        retry: false
    });
    const storagePage = storageQuery.data;
    const storages = useMemo(() => storagePage?.records || [], [storagePage?.records]);
    const totalCount = storagePage?.count ?? storagePage?.totalCount ?? 0;
    const currentPageNo = storagePage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = storagePage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const invalidateStoragePage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["storage-object", "page"] });
    };

    const deleteMutation = useMutation({
        mutationFn: service.removeStorageObjects,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await invalidateStoragePage();
            messageApi.success("存储对象已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const uploadMutation = useMutation({
        mutationFn: service.uploadStorageObject,
        onSuccess: async () => {
            await invalidateStoragePage();
            messageApi.success("文件已上传");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "上传失败");
        }
    });

    const sortMutation = useMutation({
        mutationFn: service.sortStorageObjects,
        onSuccess: async () => {
            await invalidateStoragePage();
            messageApi.success("存储对象顺序已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序失败");
        }
    });

    const updateQuery = (values: Partial<StoragePageQuery>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                contentType: nextQuery.contentType,
                ownerId: nextQuery.ownerId,
                ownerType: nextQuery.ownerType,
                objectStatus: nextQuery.objectStatus,
                referenceStatus: nextQuery.referenceStatus,
                referenceOwnerId: nextQuery.referenceOwnerId,
                referenceOwnerType: nextQuery.referenceOwnerType,
                originalFilename: nextQuery.originalFilename,
                remarks: nextQuery.remarks,
                sortDirection: nextQuery.sortDirection,
                pageNo: values.pageNo || DEFAULT_PAGE_NO,
                pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchStorages = (value: string) => {
        setSearchText(value);
        updateQuery({ originalFilename: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            contentType: normalizeSearch(filters.contentType),
            objectStatus: readStatusFilterValue(filters.objectStatus),
            referenceStatus: readStatusFilterValue(filters.referenceStatus),
            remarks: normalizeSearch(filters.remarks)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_STORAGE_OBJECT_FILTERS);
        updateQuery({
            contentType: undefined,
            objectStatus: undefined,
            referenceStatus: undefined,
            remarks: undefined
        });
    };

    const openUploadPicker = () => {
        uploadInputRef.current?.click();
    };

    const uploadSelectedFile = (event: ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = "";
        if (file) {
            uploadMutation.mutate(file);
        }
    };

    const openDeleteConfirm = (storage: StorageRecord) => {
        confirm.danger({
            title: "删除存储对象",
            message: `确认删除 ${readFilename(storage)}？`,
            description: "删除后需要重新上传。若对象仍被业务引用，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync([storage.id])
        });
    };

    const openBatchDeleteConfirm = () => {
        if (!hasSelectedStorages) {
            return;
        }
        confirm.danger({
            title: "批量删除存储对象",
            message: `确认删除 ${selectedRowKeys.length} 个存储对象？`,
            description: "删除后需要重新上传。若对象仍被业务引用，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(selectedRowKeys.map(String))
        });
    };

    const moveStorage = (
        sourceStorage: StorageRecord,
        targetStorage: StorageRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (!canEditStorage || sourceStorage.id === targetStorage.id) {
            return;
        }
        const nextStorages = sortByMove(storages, sourceStorage, targetStorage, position);
        sortMutation.mutate({
            orderedIds: nextStorages.map((storage) => storage.id)
        });
    };

    const columns: KuzhambuTableProps<StorageRecord>["columns"] = [
        {
            title: "文件",
            dataIndex: "originalFilename",
            key: "originalFilename",
            width: DEFAULT_COLUMN_WIDTHS.name,
            ellipsis: true,
            render: (_, storage) => (
                <Space size={10}>
                    <FileOutlined className="storage-object-file-icon" />
                    <div className="storage-object-name-cell">
                        <Text strong>{readFilename(storage)}</Text>
                        {formatFileSize(storage.size) ? (
                            <Text type="secondary">{formatFileSize(storage.size)}</Text>
                        ) : null}
                    </div>
                </Space>
            )
        },
        {
            title: "MIME",
            dataIndex: "contentType",
            key: "contentType",
            width: DEFAULT_COLUMN_WIDTHS.contentType,
            ellipsis: true,
            render: (contentType?: string | null) =>
                contentType ? <Text code>{contentType}</Text> : null
        },
        {
            title: "归属",
            key: "owner",
            width: DEFAULT_COLUMN_WIDTHS.owner,
            render: (_, storage) => {
                const ownerType = storage.ownerType
                    ? ownerTypeLabels[storage.ownerType] || storage.ownerType
                    : "";
                if (!ownerType && !storage.ownerId) {
                    return null;
                }
                return (
                    <div className="storage-object-owner-cell">
                        {ownerType ? <Text>{ownerType}</Text> : null}
                        {storage.ownerId ? <Text type="secondary">{storage.ownerId}</Text> : null}
                    </div>
                );
            }
        },
        {
            title: "大小",
            dataIndex: "size",
            key: "size",
            width: DEFAULT_COLUMN_WIDTHS.size,
            render: (size?: number | null) => formatFileSize(size) || null
        },
        {
            title: "状态",
            dataIndex: "objectStatus",
            key: "objectStatus",
            width: DEFAULT_COLUMN_WIDTHS.objectStatus,
            render: (status?: string | null) =>
                status ? (
                    <KuzhambuTag type={objectStatusTagType(status)}>
                        {objectStatusLabels[status as Exclude<StorageObjectStatusFilter, "ALL">] ||
                            status}
                    </KuzhambuTag>
                ) : null
        },
        {
            title: "引用",
            dataIndex: "referenceStatus",
            key: "referenceStatus",
            width: DEFAULT_COLUMN_WIDTHS.referenceStatus,
            render: (status?: string | null) =>
                status ? (
                    <KuzhambuTag type={referenceStatusTagType(status)}>
                        {referenceStatusLabels[
                            status as Exclude<StorageReferenceStatusFilter, "ALL">
                        ] || status}
                    </KuzhambuTag>
                ) : null
        },
        {
            title: "备注",
            dataIndex: "remarks",
            key: "remarks",
            width: DEFAULT_COLUMN_WIDTHS.remarks,
            ellipsis: true,
            render: (remarks?: string | null) => remarks || null
        },
        {
            key: "actions",
            options: (storage) => {
                const filename = readFilename(storage);
                const previewUrl = toStorageContentUrl(storage, "preview");
                const downloadUrl = toStorageContentUrl(storage, "download");
                return [
                    ...(previewUrl
                        ? [
                              {
                                  key: "preview",
                                  text: "预览",
                                  icon: <EyeOutlined />,
                                  ariaLabel: `预览 ${filename}`,
                                  onClick: () =>
                                      window.open(previewUrl, "_blank", "noopener,noreferrer")
                              }
                          ]
                        : []),
                    ...(downloadUrl
                        ? [
                              {
                                  key: "download",
                                  text: "下载",
                                  icon: <DownloadOutlined />,
                                  ariaLabel: `下载 ${filename}`,
                                  onClick: () =>
                                      window.open(downloadUrl, "_blank", "noopener,noreferrer")
                              }
                          ]
                        : []),
                    {
                        key: "delete",
                        text: "删除",
                        type: "danger",
                        ariaLabel: `删除 ${filename}`,
                        disabled: !canEditStorage,
                        onClick: () => openDeleteConfirm(storage)
                    }
                ];
            }
        }
    ];

    return (
        <>
            <KuzhambuListPage<StorageRecord>
                pageClassName="storage-object-page"
                title="存储对象"
                description="管理上传后的对象文件、存储状态和业务引用入口。"
                subjectName="存储对象"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                searchPlaceholder="搜索文件名..."
                onSearchChange={searchStorages}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "contentType",
                        label: "MIME",
                        render: () => (
                            <Input
                                allowClear
                                placeholder="image/png"
                                value={filters.contentType}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        contentType: event.target.value
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "objectStatus",
                        label: "对象状态",
                        render: () => (
                            <Select<StorageObjectStatusFilter>
                                value={filters.objectStatus}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    { value: "ACTIVE", label: "可用" },
                                    { value: "DELETING", label: "删除中" },
                                    { value: "DELETED", label: "已删除" }
                                ]}
                                onChange={(objectStatus) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        objectStatus
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "referenceStatus",
                        label: "引用状态",
                        render: () => (
                            <Select<StorageReferenceStatusFilter>
                                value={filters.referenceStatus}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    { value: "REFERENCED", label: "已引用" },
                                    { value: "UNREFERENCED", label: "未引用" }
                                ]}
                                onChange={(referenceStatus) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        referenceStatus
                                    }))
                                }
                            />
                        )
                    },
                    {
                        name: "remarks",
                        label: "备注",
                        render: () => (
                            <Input
                                allowClear
                                placeholder="业务说明"
                                value={filters.remarks}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        remarks: event.target.value
                                    }))
                                }
                            />
                        )
                    }
                ]}
                onFilterApply={applyFilters}
                onFilterReset={resetFilters}
                pageActions={
                    <Space wrap>
                        <input
                            ref={uploadInputRef}
                            aria-label="选择上传文件"
                            className="storage-object-upload-input"
                            type="file"
                            accept={uploadAccept}
                            onChange={uploadSelectedFile}
                        />
                        <Button
                            icon={<UploadOutlined />}
                            disabled={!canEditStorage}
                            loading={uploadMutation.isPending}
                            onClick={openUploadPicker}
                        >
                            上传
                        </Button>
                        <Button
                            icon={<ReloadOutlined />}
                            loading={storageQuery.isFetching}
                            onClick={() => storageQuery.refetch()}
                        >
                            刷新
                        </Button>
                    </Space>
                }
                batchClassName="storage-object-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!hasSelectedStorages || !canEditStorage}
                            loading={deleteMutation.isPending}
                            onClick={openBatchDeleteConfirm}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="storage-object-table"
                columns={columns}
                dataSource={storages}
                loading={
                    storageQuery.isFetching || sortMutation.isPending || uploadMutation.isPending
                }
                onSort={moveStorage}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    showTotal: (total) => `${total} 个存储对象`,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys
                }}
                sortable={canEditStorage}
            />
        </>
    );
};
