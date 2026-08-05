import { Typography } from "antd";
import "./kuzhambu-expandable-text.css";

export interface KuzhambuExpandableTextProps {
    className?: string;
    collapsedRows?: number;
    collapseText?: string;
    content?: string | null;
    emptyText?: string;
    expandText?: string;
    testId?: string;
}

export const KuzhambuExpandableText = ({
    className,
    collapsedRows = 3,
    collapseText = "收起",
    content,
    emptyText = "-",
    expandText = "展开",
    testId
}: KuzhambuExpandableTextProps) => {
    const displayContent = content?.trim() || emptyText;
    const safeCollapsedRows = Math.max(1, Math.floor(collapsedRows));

    return (
        <Typography.Paragraph
            className={["kuzhambu-expandable-text", className].filter(Boolean).join(" ")}
            data-testid={testId}
            ellipsis={{
                rows: safeCollapsedRows,
                expandable: "collapsible",
                symbol: (expanded) => (expanded ? collapseText : expandText)
            }}
        >
            {displayContent}
        </Typography.Paragraph>
    );
};
