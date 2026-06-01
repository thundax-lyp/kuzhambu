import { Tag } from "antd";
import type { TagProps } from "antd";
import "./kuzhambu-tag.css";

export type KuzhambuTagType = "neutral" | "info" | "accent" | "success" | "warning" | "danger";

export interface KuzhambuTagProps extends Omit<TagProps, "className" | "color"> {
    type?: KuzhambuTagType;
}

export const KuzhambuTag = ({ type = "neutral", ...props }: KuzhambuTagProps) => {
    return <Tag {...props} className={`kuzhambu-tag kuzhambu-tag-${type}`} />;
};
