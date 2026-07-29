import { BookOutlined, DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Input, Typography } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSpace,
    KuzhambuTag,
    type KuzhambuTableProps
} from "@/components";
import { DictionaryEditDrawer } from "./dictionary-edit-drawer";
import * as dictionaryService from "./dictionary-service";
import type { DictPageQuery, DictSaveCommand } from "./dictionary-service";
import type { DictRecord } from "./dictionary-types";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";

import "./dictionary-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    type: 220,
    label: 180,
    value: 180,
    remarks: 320
};

interface DictionaryFilters {
    remarks: string;
    type: string;
}

const DEFAULT_DICTIONARY_FILTERS: DictionaryFilters = {
    remarks: "",
    type: ""
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

export const DictionaryPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canEditDictionary = hasPermission("sys:dict:edit");
    const [query, setQuery] = useState<DictPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<DictionaryFilters>(DEFAULT_DICTIONARY_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingDictionary, setEditingDictionary] = useState<DictRecord | null>(null);
    const [dictionaryEditDrawerOpen, setDictionaryEditDrawerOpen] = useState(false);
    const hasSelectedDictionaries = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.type.trim()) || Boolean(filters.remarks.trim());

    const dictionaryPageQuery = useQuery({
        queryKey: ["dictionary", "page", query],
        queryFn: () => dictionaryService.page(query),
        retry: false
    });
    const dictionaryPage = dictionaryPageQuery.data;
    const dictionaries = useMemo(() => dictionaryPage?.records || [], [dictionaryPage?.records]);
    const totalCount = dictionaryPage?.count ?? dictionaryPage?.totalCount ?? 0;
    const currentPageNo = dictionaryPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = dictionaryPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const saveDictionaryMutation = useMutation({
        mutationFn: (values: DictSaveCommand) =>
            values.id
                ? dictionaryService.changeDictionaryInfo(values)
                : dictionaryService.addDictionary(values),
        onSuccess: async () => {
            setDictionaryEditDrawerOpen(false);
            setEditingDictionary(null);
            await queryClient.invalidateQueries({ queryKey: ["dictionary", "page"] });
            messageApi.success("字典项已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: dictionaryService.removeDictionaries,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await queryClient.invalidateQueries({ queryKey: ["dictionary", "page"] });
            messageApi.success("字典项已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const updateQuery = (values: Partial<DictPageQuery>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                type: nextQuery.type,
                label: nextQuery.label,
                remarks: nextQuery.remarks,
                pageNo: DEFAULT_PAGE_NO,
                pageSize: currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchDictionaries = (value: string) => {
        setSearchText(value);
        updateQuery({ label: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            remarks: normalizeSearch(filters.remarks),
            type: normalizeSearch(filters.type)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_DICTIONARY_FILTERS);
        updateQuery({
            remarks: undefined,
            type: undefined
        });
    };

    const openCreateDictionaryDrawer = () => {
        setEditingDictionary(null);
        setDictionaryEditDrawerOpen(true);
    };

    const openEditDictionaryDrawer = (dictionary: DictRecord) => {
        setEditingDictionary(dictionary);
        setDictionaryEditDrawerOpen(true);
    };

    const closeDictionaryEditDrawer = () => {
        if (saveDictionaryMutation.isPending) {
            return;
        }
        setDictionaryEditDrawerOpen(false);
        setEditingDictionary(null);
    };

    const saveDictionary = (request: DictSaveCommand) => {
        saveDictionaryMutation.mutate(request);
    };

    const confirmDelete = (ids: string[]) => {
        confirm.danger({
            title: "删除字典项",
            message: `确认删除 ${ids.length} 个字典项？`,
            description: "删除后需要重新新增。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(ids)
        });
    };

    const columns: KuzhambuTableProps<DictRecord>["columns"] = [
        {
            title: "字典类型",
            dataIndex: "type",
            key: "type",
            width: DEFAULT_COLUMN_WIDTHS.type,
            render: (type: string) => <KuzhambuTag type="info">{type}</KuzhambuTag>
        },
        {
            title: "标签",
            dataIndex: "label",
            key: "label",
            width: DEFAULT_COLUMN_WIDTHS.label,
            render: (label: string) => <Text strong>{label}</Text>
        },
        {
            title: "值",
            dataIndex: "value",
            key: "value",
            width: DEFAULT_COLUMN_WIDTHS.value,
            render: (value: string) => <Text code>{value}</Text>
        },
        {
            title: "备注",
            dataIndex: "remarks",
            key: "remarks",
            width: DEFAULT_COLUMN_WIDTHS.remarks,
            ellipsis: true,
            render: (remarks?: string | null) => remarks || <Text type="secondary">未填写</Text>
        },
        {
            key: "actions",
            options: (dictionary) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${dictionary.label}`,
                    disabled: !canEditDictionary,
                    onClick: () => openEditDictionaryDrawer(dictionary)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${dictionary.label}`,
                    disabled: !canEditDictionary,
                    onClick: () => confirmDelete([dictionary.id])
                }
            ]
        }
    ];

    return (
        <>
            <KuzhambuListPage<DictRecord>
                pageClassName="dictionary-page"
                title="字典管理"
                description="维护系统字典类型、展示标签、业务值和备注说明。"
                subjectName="字典项"
                enableAdd={canEditDictionary}
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={searchDictionaries}
                onAdd={openCreateDictionaryDrawer}
                filterActive={hasActiveFilters}
                filterFields={[
                    {
                        name: "type",
                        label: "字典类型",
                        render: () => (
                            <Input
                                allowClear
                                placeholder="user_status"
                                prefix={<BookOutlined />}
                                value={filters.type}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        type: event.target.value
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
                                placeholder="备注关键词"
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
                    <KuzhambuButton
                        testId="system-dictionary-dictionary-refresh-button"
                        icon={<ReloadOutlined />}
                        onClick={() => dictionaryPageQuery.refetch()}
                    >
                        刷新
                    </KuzhambuButton>
                }
                batchClassName="dictionary-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <KuzhambuSpace wrap>
                        <KuzhambuButton
                            testId="system-dictionary-dictionary-batch-delete-button"
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditDictionary || !hasSelectedDictionaries}
                            loading={deleteMutation.isPending}
                            onClick={() => confirmDelete(selectedRowKeys.map(String))}
                        >
                            批量删除
                        </KuzhambuButton>
                    </KuzhambuSpace>
                }
                rowKey="id"
                className="dictionary-table"
                columns={columns}
                dataSource={dictionaries}
                loading={dictionaryPageQuery.isFetching}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: () => ({
                        disabled: !canEditDictionary
                    })
                }}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    showTotal: (total) => `共 ${total} 项`,
                    onChange: (pageNo, pageSize) => {
                        setQuery((currentQuery) => ({
                            ...currentQuery,
                            pageNo,
                            pageSize
                        }));
                    }
                }}
                locale={{
                    emptyText: dictionaryPageQuery.isError
                        ? "字典列表加载失败，请确认权限和接口状态。"
                        : "暂无字典项"
                }}
            />

            <DictionaryEditDrawer
                open={dictionaryEditDrawerOpen}
                dictionary={editingDictionary}
                saving={saveDictionaryMutation.isPending}
                onClose={closeDictionaryEditDrawer}
                onSave={saveDictionary}
            />
        </>
    );
};
