import { Tag } from "antd";
import type { TagProps } from "antd";
import "./sandwish-tag.css";

export type SandwishTagType = "neutral" | "info" | "accent" | "success" | "warning" | "danger";

export interface SandwishTagProps extends Omit<TagProps, "className" | "color"> {
    type?: SandwishTagType;
}

export const SandwishTag = ({ type = "neutral", ...props }: SandwishTagProps) => {
    return <Tag {...props} className={`sandwish-tag sandwish-tag-${type}`} />;
};
