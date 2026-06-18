import {
    DeleteOutlined,
    EditOutlined,
    MenuOutlined,
    PlusOutlined
} from "@ant-design/icons";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { App, Button, Empty, Skeleton, Typography } from "antd";
import { useState } from "react";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { SancaiCategoryModel } from "./sancai-category-model";
import { SancaiCategorySortModel } from "./sancai-category-sort-model";
import * as service from "../sancai-service";
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

const readCategoryTypeLabel = (category: SancaiCategoryRecord) => {
    return category.categoryType === "AUXILIARY" ? "辅助内容" : "正式门类";
};

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

    const closeModel = () => {
        setIsModelOpen(false);
        setEditingCategory(null);
    };

    const deleteMutation = useMutation({
        mutationFn: service.removeCategory,
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

    const confirmDelete = (category: SancaiCategoryRecord) => {
        confirm.danger({
            title: "删除三才图会门类",
            message: `确认删除 ${readTitle(category, "门类")}？`,
            description: "仅空门类可删除。若该门类下仍有关联卷，接口会拒绝删除。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync({ id: category.id })
        });
    };

    const openSort = () => {
        setIsSortOpen(true);
    };

    const closeSort = () => {
        setIsSortOpen(false);
    };

    const categoryContent = (() => {
        if (isLoading) {
            return <Skeleton active paragraph={{ rows: 8 }} />;
        }
        if (!categories.length) {
            return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无门类" />;
        }
        return (
            <div className="sancai-category-list" aria-label="三才图会门类">
                {categories.map((category) => (
                    <div
                        className={
                            category.id === selectedCategory?.id
                                ? "sancai-catalog-row sancai-catalog-row-active"
                                : "sancai-catalog-row"
                        }
                        key={category.id}
                    >
                        <button
                            className="sancai-catalog-item"
                            type="button"
                            aria-label={`选择门类 ${readTitle(category, "门类")}`}
                            aria-pressed={category.id === selectedCategory?.id}
                            onClick={() => onSelect(category)}
                        >
                            <span className="sancai-category-main">
                                <span
                                    className={
                                        category.categoryType === "AUXILIARY"
                                            ? "sancai-category-type-dot sancai-category-type-dot-auxiliary"
                                            : "sancai-category-type-dot sancai-category-type-dot-formal"
                                    }
                                    aria-label={`门类类型 ${readCategoryTypeLabel(category)}`}
                                />
                                <span>{readTitle(category, "门类")}</span>
                            </span>
                        </button>
                        <div
                            className="sancai-catalog-actions"
                            aria-label={`${readTitle(category, "门类")} 操作`}
                        >
                            <Button
                                aria-label={`编辑门类 ${readTitle(category, "门类")}`}
                                icon={<EditOutlined />}
                                size="small"
                                type="text"
                                onClick={() => startEdit(category)}
                            />
                            <Button
                                aria-label={`删除门类 ${readTitle(category, "门类")}`}
                                danger
                                icon={<DeleteOutlined />}
                                size="small"
                                type="text"
                                onClick={() => confirmDelete(category)}
                            />
                        </div>
                    </div>
                ))}
            </div>
        );
    })();

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
            <div className="sancai-catalog-scroll">{categoryContent}</div>
            {isModelOpen ? (
                <SancaiCategoryModel category={editingCategory} onCancel={closeModel} />
            ) : null}
            {isSortOpen ? (
                <SancaiCategorySortModel categories={categories} onCancel={closeSort} />
            ) : null}
        </section>
    );
};
