import { Tag } from "antd";
import type { TagProps } from "antd";
import "./kuzhambu-tag.css";

export type KuzhambuTagType = "neutral" | "info" | "accent" | "success" | "warning" | "danger";

export interface KuzhambuTagProps extends Omit<TagProps, "bordered" | "className" | "color"> {
    type?: KuzhambuTagType;
}

// AI NOTE: This tag wrapper maps semantic tag types to the local visual palette.
// Pages choose the semantic type; this component should not infer domain status rules.
export const KuzhambuTag = ({ type = "neutral", ...props }: KuzhambuTagProps) => {
    return <Tag {...props} className={`kuzhambu-tag kuzhambu-tag-${type}`} />;
};
