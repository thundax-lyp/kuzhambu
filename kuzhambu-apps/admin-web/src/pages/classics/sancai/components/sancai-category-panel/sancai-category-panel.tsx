import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Tag } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import type { DictItem } from "@/types/dict";
import { SancaiCategoryEditDrawerModal } from "../sancai-category-edit-modal";
import type { SancaiCategoryFormValues } from "../sancai-form-values";
import * as categoryService from "@/pages/classics/sancai/sancai-category-service";
import type { SancaiCategoryRecord } from "@/pages/classics/sancai/sancai-types";
import "./sancai-category-panel.css";

interface SancaiCategoryPanelProps {
    categories: SancaiCategoryRecord[];
    defaultCreateOpen?: boolean;
    isLoading: boolean;
    onSelect: (category: SancaiCategoryRecord) => void;
    selectedCategory: SancaiCategoryRecord | null;
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const fallbackCategoryTypeOptions: DictItem[] = [
    { label: "正式门类", type: "SANCAI_CATEGORY_TYPE", value: "FORMAL" },
    { label: "辅助内容", type: "SANCAI_CATEGORY_TYPE", value: "AUXILIARY" }
];

const readCategoryTypeLabel = (category: SancaiCategoryRecord, options: DictItem[]) => {
    return (
        options.find((option) => option.value === category.categoryType)?.label ||
        category.categoryType ||
        "-"
    );
};

const readCategoryTypeColor = (category: SancaiCategoryRecord) => {
    return category.categoryType === "AUXILIARY" ? "gold" : "green";
};

export const SancaiCategoryPanel = ({
    categories,
    defaultCreateOpen = false,
    isLoading,
    onSelect,
    selectedCategory
}: SancaiCategoryPanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [editingCategory, setEditingCategory] = useState<SancaiCategoryRecord | null>(null);
    const [isModelOpen, setIsModelOpen] = useState(defaultCreateOpen);
    const typesQuery = useQuery<DictItem[]>({
        queryKey: ["classics", "sancai", "categories", "types"],
        queryFn: categoryService.listTypes,
        retry: false
    });
    const categoryTypeItems = typesQuery.data?.length
        ? typesQuery.data
        : fallbackCategoryTypeOptions;

    const closeModel = () => {
        setIsModelOpen(false);
        setEditingCategory(null);
    };

    const afterChanged = async (message: string) => {
        await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "categories"] });
        closeModel();
        messageApi.success(message);
    };

    const addMutation = useMutation({
        mutationFn: categoryService.add,
        onSuccess: () => afterChanged("三才图会门类已新增"),
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类新增失败");
        }
    });
    const updateMutation = useMutation({
        mutationFn: categoryService.update,
        onSuccess: () => afterChanged("三才图会门类已更新"),
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类更新失败");
        }
    });
    const deleteMutation = useMutation({
        mutationFn: categoryService.deleteById,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "categories"] });
            messageApi.success("三才图会门类已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类删除失败");
        }
    });
    const sortMutation = useMutation({
        mutationFn: categoryService.sort,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["classics", "sancai", "categories"] });
            messageApi.success("三才图会门类顺序已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "门类排序保存失败");
        }
    });

    const startEdit = (category: SancaiCategoryRecord) => {
        setEditingCategory(category);
        setIsModelOpen(true);
    };

    const submitCategory = (form: SancaiCategoryFormValues) => {
        const request = {
            id: editingCategory?.id,
            title: form.title,
            categoryType: form.categoryType
        };
        if (editingCategory) {
            updateMutation.mutate(request);
            return;
        }
        addMutation.mutate(request);
    };

    const confirmDelete = (category: SancaiCategoryRecord) => {
        confirm.danger({
            title: "删除三才图会门类",
            message: `确认删除 ${readTitle(category, "门类")}？`,
            description: "仅空门类可删除。若该门类下仍有关联卷，接口会拒绝删除。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(category.id)
        });
    };

    const submitSort = (
        sourceCategory: SancaiCategoryRecord,
        targetCategory: SancaiCategoryRecord,
        position: KuzhambuTableSortPosition
    ) => {
        if (sourceCategory.id === targetCategory.id) {
            return;
        }
        const remainingCategories = categories.filter(
            (category) => category.id !== sourceCategory.id
        );
        const targetIndex = remainingCategories.findIndex(
            (category) => category.id === targetCategory.id
        );
        if (targetIndex < 0) {
            return;
        }
        const insertIndex = position === "before" ? targetIndex : targetIndex + 1;
        const sortedCategories = [...remainingCategories];
        sortedCategories.splice(insertIndex, 0, sourceCategory);
        sortMutation.mutate({
            orderedIds: sortedCategories.map((category) => category.id),
            sortDirection: "ASC"
        });
    };

    const columns: KuzhambuTableProps<SancaiCategoryRecord>["columns"] = [
        {
            title: "门类",
            key: "title",
            width: 260,
            render: (_, category) => (
                <div className="sancai-category-title-cell">
                    <a
                        href="#"
                        aria-label={`打开门类 ${readTitle(category, "门类")}`}
                        onClick={(event) => {
                            event.preventDefault();
                            onSelect(category);
                        }}
                    >
                        {readTitle(category, "门类")}
                    </a>
                </div>
            )
        },
        {
            title: "类型",
            key: "categoryType",
            width: 160,
            render: (_, category) => (
                <Tag color={readCategoryTypeColor(category)}>
                    {readCategoryTypeLabel(category, categoryTypeItems)}
                </Tag>
            )
        },
        {
            key: "actions",
            options: (category) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑门类 ${readTitle(category, "门类")}`,
                    onClick: () => startEdit(category)
                },
                { type: "divider" },
                {
                    danger: true,
                    key: "delete",
                    text: "删除",
                    ariaLabel: `删除门类 ${readTitle(category, "门类")}`,
                    onClick: () => confirmDelete(category)
                }
            ]
        }
    ];

    return (
        <section className="sancai-panel-body">
            <div className="sancai-panel-scroll">
                <KuzhambuTable
                    className="sancai-category-table"
                    ariaLabel="三才图会门类表格"
                    columns={columns}
                    dataSource={categories}
                    loading={isLoading}
                    locale={{ emptyText: "暂无门类" }}
                    pagination={false}
                    rowKey="id"
                    size="middle"
                    scroll={{ x: 520 }}
                    sortable
                    onSort={submitSort}
                    onRow={(category) => ({
                        "aria-label": `选择门类 ${readTitle(category, "门类")}`,
                        className:
                            category.id === selectedCategory?.id
                                ? "sancai-category-table-row sancai-category-table-row-active"
                                : "sancai-category-table-row"
                    })}
                />
            </div>
            {isModelOpen ? (
                <SancaiCategoryEditDrawerModal
                    category={editingCategory}
                    categoryTypeOptions={categoryTypeItems}
                    isSubmitting={addMutation.isPending || updateMutation.isPending}
                    onCancel={closeModel}
                    onSubmit={submitCategory}
                />
            ) : null}
        </section>
    );
};
