import { BookOutlined, DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Input, Space, Typography } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { SandwishListPage } from "@/components/sandwish-list-page";
import { useSandwishConfirm } from "@/components/sandwish-confirm-modal/hooks/use-sandwish-confirm";
import { SandwishTag } from "@/components/sandwish-tag";
import type { SandwishTableProps } from "@/components/sandwish-table";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { DictionaryEdit } from "./components/dictionary-edit";
import * as dictionaryService from "./dictionary-service";
import type { DictPageQuery, DictSaveCommand } from "./dictionary-service";
import type { DictRecord } from "./dictionary-types";
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
    const confirm = useSandwishConfirm();
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
    const [editorOpen, setEditorOpen] = useState(false);
    const hasSelectedDictionaries = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.type.trim()) || Boolean(filters.remarks.trim());

    const dictionaryQuery = useQuery({
        queryKey: ["dictionary", "page", query],
        queryFn: () => dictionaryService.page(query),
        retry: false
    });
    const dictionaryPage = dictionaryQuery.data;
    const dictionaries = useMemo(() => dictionaryPage?.records || [], [dictionaryPage?.records]);
    const totalCount = dictionaryPage?.count ?? dictionaryPage?.totalCount ?? 0;
    const currentPageNo = dictionaryPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = dictionaryPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const saveMutation = useMutation({
        mutationFn: (values: DictSaveCommand) =>
            values.id
                ? dictionaryService.changeDictionaryInfo(values)
                : dictionaryService.addDictionary(values),
        onSuccess: async () => {
            setEditorOpen(false);
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

    const openCreateEditor = () => {
        setEditingDictionary(null);
        setEditorOpen(true);
    };

    const openEditEditor = (dictionary: DictRecord) => {
        setEditingDictionary(dictionary);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingDictionary(null);
    };

    const saveDictionary = (request: DictSaveCommand) => {
        saveMutation.mutate(request);
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

    const columns: SandwishTableProps<DictRecord>["columns"] = [
        {
            title: "字典类型",
            dataIndex: "type",
            key: "type",
            width: DEFAULT_COLUMN_WIDTHS.type,
            render: (type: string) => <SandwishTag type="info">{type}</SandwishTag>
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
                    onClick: () => openEditEditor(dictionary)
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
            <SandwishListPage<DictRecord>
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
                onAdd={openCreateEditor}
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
                    <Button icon={<ReloadOutlined />} onClick={() => dictionaryQuery.refetch()}>
                        刷新
                    </Button>
                }
                batchClassName="dictionary-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditDictionary || !hasSelectedDictionaries}
                            loading={deleteMutation.isPending}
                            onClick={() => confirmDelete(selectedRowKeys.map(String))}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="dictionary-table"
                columns={columns}
                dataSource={dictionaries}
                loading={dictionaryQuery.isFetching}
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
                    emptyText: dictionaryQuery.isError
                        ? "字典列表加载失败，请确认权限和接口状态。"
                        : "暂无字典项"
                }}
            />

            <DictionaryEdit
                open={editorOpen}
                dictionary={editingDictionary}
                saving={saveMutation.isPending}
                onClose={closeEditor}
                onSave={saveDictionary}
            />
        </>
    );
};
