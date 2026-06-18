import { DeleteOutlined, EditOutlined } from "@ant-design/icons";
import { Button, Empty, Skeleton } from "antd";
import type { SancaiCategoryRecord } from "../sancai-types";

interface SancaiCategoryListProps {
    categories: SancaiCategoryRecord[];
    isLoading: boolean;
    onDelete: (category: SancaiCategoryRecord) => void;
    onEdit: (category: SancaiCategoryRecord) => void;
    onSelect: (category: SancaiCategoryRecord) => void;
    selectedCategory: SancaiCategoryRecord | null;
}

const readTitle = (value: { id: number; title?: string | null }, fallback: string) => {
    return value.title?.trim() || `${fallback} ${value.id}`;
};

const readCategoryTypeLabel = (category: SancaiCategoryRecord) => {
    return category.categoryType === "AUXILIARY" ? "辅助内容" : "正式门类";
};

export const SancaiCategoryList = ({
    categories,
    isLoading,
    onDelete,
    onEdit,
    onSelect,
    selectedCategory
}: SancaiCategoryListProps) => {
    if (isLoading) {
        return <Skeleton active paragraph={{ rows: 8 }} />;
    }

    if (!categories.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无门类" />;
    }

    return (
        <div className="sancai-category-list" aria-label="三才图会门类">
            {categories.map((category) => {
                const title = readTitle(category, "门类");
                return (
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
                            aria-label={`选择门类 ${title}`}
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
                                <span>{title}</span>
                            </span>
                        </button>
                        <div className="sancai-catalog-actions" aria-label={`${title} 操作`}>
                            <Button
                                aria-label={`编辑门类 ${title}`}
                                icon={<EditOutlined />}
                                size="small"
                                type="text"
                                onClick={() => onEdit(category)}
                            />
                            <Button
                                aria-label={`删除门类 ${title}`}
                                danger
                                icon={<DeleteOutlined />}
                                size="small"
                                type="text"
                                onClick={() => onDelete(category)}
                            />
                        </div>
                    </div>
                );
            })}
        </div>
    );
};
