import type { ReactNode } from "react";
import { useState } from "react";
import { FilterOutlined, PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input, Space } from "antd";
import { KuzhambuBatchActionBar } from "@/components/kuzhambu-batch-action-bar";
import { KuzhambuFilterPanel } from "@/components/kuzhambu-filter-panel";
import type { KuzhambuFilterPanelField } from "@/components/kuzhambu-filter-panel";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuTable } from "@/components/kuzhambu-table";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { KuzhambuListPageTableArea } from "./kuzhambu-list-page-table-area";
import "./kuzhambu-list-page.css";

export interface KuzhambuListPageFilterState {
    closeFilter: () => void;
    filterOpen: boolean;
    openFilter: () => void;
    toggleFilter: () => void;
}

export type KuzhambuListPageFilterField = KuzhambuFilterPanelField;

export interface KuzhambuListPageProps<RecordType extends object = object> extends Omit<
    KuzhambuTableProps<RecordType>,
    "title"
> {
    batchActions?: ReactNode;
    batchClassName?: string;
    addText?: ReactNode;
    content?: ReactNode;
    defaultFilterOpen?: boolean;
    enableAdd?: boolean;
    description?: ReactNode;
    enableFilter?: boolean;
    enableSearch?: boolean;
    eyebrow?: ReactNode;
    filterActive?: boolean;
    filter?: ReactNode | ((filterState: KuzhambuListPageFilterState) => ReactNode);
    filterClassName?: string;
    filterFields?: KuzhambuListPageFilterField[];
    filterText?: ReactNode;
    filterOpen?: boolean;
    onAdd?: () => void;
    onFilterApply?: () => void;
    onFilterOpenChange?: (open: boolean) => void;
    onFilterReset?: () => void;
    onSearchChange?: (value: string) => void;
    pageActions?: ReactNode | ((filterState: KuzhambuListPageFilterState) => ReactNode);
    pageClassName?: string;
    searchPlaceholder?: string;
    searchShortcut?: ReactNode;
    searchValue?: string;
    selectedCount?: number;
    subjectName?: string;
    tableAside?: ReactNode;
    tableAsideClassName?: string;
    tableAreaClassName?: string;
    tableAsidePlacement?: "left" | "right";
    title: ReactNode;
}

export const KuzhambuListPage = <RecordType extends object = object>({
    batchActions,
    batchClassName,
    addText,
    content,
    defaultFilterOpen = false,
    enableAdd = false,
    description,
    enableFilter = false,
    enableSearch = false,
    eyebrow,
    filterActive = false,
    filter,
    filterClassName,
    filterFields,
    filterText = "筛选",
    filterOpen,
    onAdd,
    onFilterApply,
    onFilterOpenChange,
    onFilterReset,
    onSearchChange,
    pageActions,
    pageClassName,
    searchPlaceholder,
    searchShortcut,
    searchValue = "",
    selectedCount = 0,
    subjectName,
    tableAside,
    tableAsideClassName,
    tableAreaClassName,
    tableAsidePlacement = "right",
    title,
    ...tableProps
}: KuzhambuListPageProps<RecordType>) => {
    const [internalFilterOpen, setInternalFilterOpen] = useState(defaultFilterOpen);
    const actualFilterOpen = enableFilter ? (filterOpen ?? internalFilterOpen) : false;
    const setFilterOpen = (open: boolean) => {
        if (filterOpen === undefined) {
            setInternalFilterOpen(open);
        }
        onFilterOpenChange?.(open);
    };
    const filterState: KuzhambuListPageFilterState = {
        closeFilter: () => setFilterOpen(false),
        filterOpen: actualFilterOpen,
        openFilter: () => setFilterOpen(true),
        toggleFilter: () => setFilterOpen(!actualFilterOpen)
    };
    const resolvedPageActions =
        typeof pageActions === "function" ? pageActions(filterState) : pageActions;
    const resolvedFilter = typeof filter === "function" ? filter(filterState) : filter;
    const resolvedSearchPlaceholder =
        searchPlaceholder ?? (subjectName ? `搜索${subjectName}...` : "搜索...");
    const resolvedSearchAriaLabel = subjectName ? `搜索${subjectName}` : "搜索列表";
    const resolvedAddText = addText ?? (subjectName ? `新增${subjectName}` : undefined);
    const resolvedTableAriaLabel = tableProps.ariaLabel ?? `${subjectName ?? "数据"}列表`;
    const headerActions = (
        <Space className="kuzhambu-list-page-actions">
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
                    onClick={filterState.toggleFilter}
                >
                    {filterText}
                </Button>
            ) : null}
            {resolvedPageActions}
            {enableAdd && resolvedAddText ? (
                <Button type="primary" icon={<PlusOutlined />} onClick={onAdd}>
                    {resolvedAddText}
                </Button>
            ) : null}
        </Space>
    );
    const renderBody = () => {
        if (content && tableAside) {
            return (
                <KuzhambuListPageTableArea
                    aside={tableAside}
                    asideClassName={tableAsideClassName}
                    areaClassName={tableAreaClassName}
                    placement={tableAsidePlacement}
                >
                    {content}
                </KuzhambuListPageTableArea>
            );
        }

        if (content) {
            return content;
        }

        if (tableAside) {
            return (
                <KuzhambuListPageTableArea
                    aside={tableAside}
                    asideClassName={tableAsideClassName}
                    areaClassName={tableAreaClassName}
                    placement={tableAsidePlacement}
                >
                    <KuzhambuTable<RecordType> {...tableProps} ariaLabel={resolvedTableAriaLabel} />
                </KuzhambuListPageTableArea>
            );
        }

        return <KuzhambuTable<RecordType> {...tableProps} ariaLabel={resolvedTableAriaLabel} />;
    };

    return (
        <KuzhambuPage
            actions={headerActions}
            className={pageClassName}
            description={description}
            eyebrow={eyebrow}
            title={title}
        >
            {enableFilter && (resolvedFilter || filterFields?.length) ? (
                <KuzhambuFilterPanel
                    open={actualFilterOpen}
                    className={filterClassName}
                    fields={filterFields}
                    resetDisabled={!filterActive}
                    onApply={() => {
                        onFilterApply?.();
                        filterState.closeFilter();
                    }}
                    onReset={onFilterReset}
                >
                    {resolvedFilter}
                </KuzhambuFilterPanel>
            ) : null}

            {batchActions ? (
                <KuzhambuBatchActionBar
                    actions={batchActions}
                    className={batchClassName}
                    selectedCount={selectedCount}
                />
            ) : null}

            {renderBody()}
        </KuzhambuPage>
    );
};
