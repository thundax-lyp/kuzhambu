export type KuzhambuFormItemLayoutSize = "small" | "middle" | "large";
export type KuzhambuFormLayoutTier = "xs" | "md" | "lg";

export const KUZHAMBU_FORM_ITEM_LAYOUTS = {
    small: {
        col: { xs: 24, md: 12, lg: 8 },
        labelCol: { xs: { span: 24 }, md: { span: 8 }, lg: { span: 12 } },
        wrapperCol: { xs: { span: 24 }, md: { span: 16 }, lg: { span: 12 } }
    },
    middle: {
        col: { xs: 24, md: 12, lg: 12 },
        labelCol: { xs: { span: 24 }, md: { span: 8 }, lg: { span: 8 } },
        wrapperCol: { xs: { span: 24 }, md: { span: 16 }, lg: { span: 16 } }
    },
    large: {
        col: { xs: 24, md: 24, lg: 24 },
        labelCol: { xs: { span: 24 }, md: { span: 4 }, lg: { span: 4 } },
        wrapperCol: { xs: { span: 24 }, md: { span: 20 }, lg: { span: 20 } }
    }
} as const;

export const readKuzhambuFormLayoutTier = (containerWidth: number): KuzhambuFormLayoutTier => {
    if (containerWidth < 768) {
        return "xs";
    }
    if (containerWidth < 992) {
        return "md";
    }
    return "lg";
};
