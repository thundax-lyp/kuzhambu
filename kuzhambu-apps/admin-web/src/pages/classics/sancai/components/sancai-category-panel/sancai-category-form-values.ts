import type { SancaiCategoryRecord } from "@/pages/classics/sancai/sancai-types";

export interface SancaiCategoryFormValues {
    categoryType: string;
    title: string;
}

export const toCategoryFormValues = (category?: SancaiCategoryRecord): SancaiCategoryFormValues => {
    return {
        categoryType: category?.categoryType || "FORMAL",
        title: category?.title || ""
    };
};
