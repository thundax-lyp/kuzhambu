import { MenuOutlined, PlusOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Typography } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import type { DictItem } from "@/types/dict";
import { SancaiCategoryList } from "./sancai-category-list";
import { SancaiCategoryModel } from "./sancai-category-model";
import { SancaiCategorySortModel } from "./sancai-category-sort-model";
import type { SancaiCategoryFormValues } from "./sancai-form-values";
import * as categoryService from "../services/sancai-category-service";
import type { SancaiCategoryRecord } from "../sancai-types";

const { Text, Title } = Typography;

interface SancaiCategoryPanelProps {
    categories: SancaiCategoryRecord[];
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

export const SancaiCategoryPanel = ({
    categories,
    isLoading,
    onSelect,
    selectedCategory
}: SancaiCategoryPanelProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [editingCategory, setEditingCategory] = useState<SancaiCategoryRecord | null>(null);
    const [isModelOpen, setIsModelOpen] = useState(false);
    const [isSortOpen, setIsSortOpen] = useState(false);
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

    const startCreate = () => {
        setEditingCategory(null);
        setIsModelOpen(true);
    };

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

    const openSort = () => {
        setIsSortOpen(true);
    };

    const closeSort = () => {
        setIsSortOpen(false);
    };

    return (
        <section className="sancai-catalog-column">
            <div className="sancai-panel-heading">
                <Title level={3}>门类</Title>
                <div className="sancai-heading-actions">
                    <Text type="secondary">{categories.length} 项</Text>
                    <Button
                        aria-label="调整三才图会门类顺序"
                        title="调整门类顺序"
                        icon={<MenuOutlined />}
                        size="small"
                        onClick={openSort}
                    />
                    <Button
                        aria-label="新增三才图会门类"
                        title="新增门类"
                        icon={<PlusOutlined />}
                        size="small"
                        onClick={startCreate}
                    />
                </div>
            </div>
            <div className="sancai-catalog-scroll">
                <SancaiCategoryList
                    categories={categories}
                    isLoading={isLoading}
                    selectedCategory={selectedCategory}
                    onDelete={confirmDelete}
                    onEdit={startEdit}
                    onSelect={onSelect}
                />
            </div>
            {isModelOpen ? (
                <SancaiCategoryModel
                    category={editingCategory}
                    categoryTypeOptions={categoryTypeItems}
                    isSubmitting={addMutation.isPending || updateMutation.isPending}
                    onCancel={closeModel}
                    onSubmit={submitCategory}
                />
            ) : null}
            {isSortOpen ? (
                <SancaiCategorySortModel categories={categories} onCancel={closeSort} />
            ) : null}
        </section>
    );
};
