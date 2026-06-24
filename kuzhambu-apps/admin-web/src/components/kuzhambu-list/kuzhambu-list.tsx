import { Empty, Spin } from "antd";
import { Fragment } from "react";
import type { Key, ReactNode } from "react";
import "./kuzhambu-list.css";

export interface KuzhambuListProps<ItemType> {
    ariaLabel?: string;
    as?: "ol" | "ul";
    bordered?: boolean;
    className?: string;
    dataSource: ItemType[];
    empty?: ReactNode;
    itemKey?: (item: ItemType, index: number) => Key;
    loading?: boolean;
    renderItem: (item: ItemType, index: number) => ReactNode;
    size?: "default" | "small";
}

export interface KuzhambuListItemProps {
    actions?: ReactNode[];
    children: ReactNode;
    className?: string;
    extra?: ReactNode;
}

export interface KuzhambuListMetaProps {
    description?: ReactNode;
    title?: ReactNode;
}

const joinClassNames = (...classNames: Array<string | false | null | undefined>) => {
    return classNames.filter(Boolean).join(" ");
};

const inferItemKey = <ItemType,>(item: ItemType, index: number): Key => {
    if (typeof item === "object" && item !== null) {
        if ("key" in item && (typeof item.key === "string" || typeof item.key === "number")) {
            return item.key;
        }
        if ("id" in item && (typeof item.id === "string" || typeof item.id === "number")) {
            return item.id;
        }
    }
    return index;
};

export const KuzhambuList = <ItemType,>({
    ariaLabel,
    as = "ul",
    bordered = false,
    className,
    dataSource,
    empty,
    itemKey,
    loading = false,
    renderItem,
    size = "default"
}: KuzhambuListProps<ItemType>) => {
    const ListTag = as;

    const content =
        dataSource.length > 0 ? (
            <ListTag
                aria-label={ariaLabel}
                className={joinClassNames(
                    "kuzhambu-list",
                    bordered && "kuzhambu-list--bordered",
                    size === "small" && "kuzhambu-list--small",
                    className
                )}
            >
                {dataSource.map((item, index) => (
                    <Fragment key={itemKey ? itemKey(item, index) : inferItemKey(item, index)}>
                        {renderItem(item, index)}
                    </Fragment>
                ))}
            </ListTag>
        ) : (
            (empty ?? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />)
        );

    return <Spin spinning={loading}>{content}</Spin>;
};

export const KuzhambuListItem = ({
    actions,
    children,
    className,
    extra
}: KuzhambuListItemProps) => {
    return (
        <li className={joinClassNames("kuzhambu-list-item", className)}>
            <div className="kuzhambu-list-item-main">{children}</div>
            {extra || actions?.length ? (
                <div className="kuzhambu-list-item-aside">
                    {extra}
                    {actions?.length ? (
                        <div className="kuzhambu-list-item-actions">{actions}</div>
                    ) : null}
                </div>
            ) : null}
        </li>
    );
};

export const KuzhambuListMeta = ({ description, title }: KuzhambuListMetaProps) => {
    return (
        <div className="kuzhambu-list-meta">
            {title ? <div className="kuzhambu-list-meta-title">{title}</div> : null}
            {description ? (
                <div className="kuzhambu-list-meta-description">{description}</div>
            ) : null}
        </div>
    );
};
