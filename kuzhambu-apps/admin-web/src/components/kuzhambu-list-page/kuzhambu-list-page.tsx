import type { ReactNode } from "react";
import { useState } from "react";
import { FilterOutlined, PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input } from "antd";
import { KuzhambuFilterPanel } from "@/components/kuzhambu-filter-panel";
import type { KuzhambuFilterPanelField } from "@/components/kuzhambu-filter-panel";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type {
    KuzhambuTableBatchActionBarProps,
    KuzhambuTableProps
} from "@/components/kuzhambu-table";
import "./kuzhambu-list-page.css";

export type KuzhambuListPageFilterField = KuzhambuFilterPanelField;

export interface KuzhambuListPageProps<RecordType extends object = object> extends Omit<
    KuzhambuTableProps<RecordType>,
    "title"
> {
    batchActions?: ReactNode;
    batchClassName?: string;
    addText?: ReactNode;
    content?: ReactNode;
    enableAdd?: boolean;
    description?: ReactNode;
    enableFilter?: boolean;
    enableSearch?: boolean;
    filterDefaultOpen?: boolean;
    filterActive?: boolean;
    filterFields?: KuzhambuListPageFilterField[];
    filterText?: ReactNode;
    onAdd?: () => void;
    onFilterApply?: () => void;
    onFilterReset?: () => void;
    onSearchChange?: (value: string) => void;
    pageActions?: ReactNode;
    pageClassName?: string;
    searchPlaceholder?: string;
    searchShortcut?: ReactNode;
    searchValue?: string;
    selectedCount?: number;
    subjectName?: string;
    title: ReactNode;
}

// AI NOTE: This is the default admin list-page scaffold.
// Use it when a page is "header + search/filter + batch actions + table".
// Do not hide page-specific data fetching, permissions, or mutation behavior in this component.
// Pages should pass semantic actions and table columns; this component owns only layout composition.
export const KuzhambuListPage = <RecordType extends object = object>({
    batchActions,
    batchClassName,
    addText,
    content,
    enableAdd = false,
    description,
    enableFilter = false,
    enableSearch = false,
    filterDefaultOpen = false,
    filterActive = false,
    filterFields,
    filterText = "筛选",
    onAdd,
    onFilterApply,
    onFilterReset,
    onSearchChange,
    pageActions,
    pageClassName,
    searchPlaceholder,
    searchShortcut,
    searchValue = "",
    selectedCount = 0,
    subjectName,
    title,
    ...tableProps
}: KuzhambuListPageProps<RecordType>) => {
    const [internalFilterOpen, setInternalFilterOpen] = useState(filterDefaultOpen);
    const actualFilterOpen = enableFilter && internalFilterOpen;
    const resolvedSearchPlaceholder =
        searchPlaceholder ?? (subjectName ? `搜索${subjectName}...` : "搜索...");
    const resolvedSearchAriaLabel = subjectName ? `搜索${subjectName}` : "搜索列表";
    const resolvedAddText = addText ?? (subjectName ? `新增${subjectName}` : undefined);
    const resolvedTableAriaLabel = tableProps.ariaLabel ?? `${subjectName ?? "数据"}列表`;
    const tableBatchActionBar: KuzhambuTableBatchActionBarProps | undefined = batchActions
        ? {
              actions: batchActions,
              className: batchClassName,
              selectedCount
          }
        : undefined;
    const headerActions = (
        <KuzhambuSpace className="kuzhambu-list-page-actions">
            {enableSearch ? (
                <Input
                    allowClear
                    aria-label={resolvedSearchAriaLabel}
                    className={[
                        "kuzhambu-list-page-search",
                        actualFilterOpen ? "kuzhambu-list-page-search-hidden" : ""
                    ]
                        .filter(Boolean)
                        .join(" ")}
                    placeholder={resolvedSearchPlaceholder}
                    prefix={<SearchOutlined />}
                    suffix={
                        searchShortcut ? (
                            <span className="kuzhambu-list-page-search-shortcut">
                                {searchShortcut}
                            </span>
                        ) : null
                    }
                    value={searchValue}
                    onChange={(event) => onSearchChange?.(event.target.value)}
                />
            ) : null}
            {enableFilter ? (
                <Button
                    className={
                        actualFilterOpen || filterActive
                            ? "kuzhambu-list-page-filter-toggle-active"
                            : undefined
                    }
                    icon={<FilterOutlined />}
                    aria-expanded={actualFilterOpen}
                    onClick={() => setInternalFilterOpen(!actualFilterOpen)}
                >
                    {filterText}
                </Button>
            ) : null}
            {pageActions}
            {enableAdd && resolvedAddText ? (
                <Button type="primary" icon={<PlusOutlined />} onClick={onAdd}>
                    {resolvedAddText}
                </Button>
            ) : null}
        </KuzhambuSpace>
    );
    const body = content ?? (
        <KuzhambuTable<RecordType>
            {...tableProps}
            ariaLabel={resolvedTableAriaLabel}
            batchActionBar={tableBatchActionBar}
        />
    );

    return (
        <KuzhambuPage
            actions={headerActions}
            className={pageClassName}
            description={description}
            title={title}
        >
            {enableFilter && filterFields?.length ? (
                <KuzhambuFilterPanel
                    open={actualFilterOpen}
                    fields={filterFields}
                    resetDisabled={!filterActive}
                    onApply={() => {
                        onFilterApply?.();
                        setInternalFilterOpen(false);
                    }}
                    onReset={onFilterReset}
                />
            ) : null}

            {body}
        </KuzhambuPage>
    );
};
