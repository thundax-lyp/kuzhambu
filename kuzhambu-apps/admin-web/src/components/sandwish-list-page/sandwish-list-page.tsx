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
import "./sandwish-list-page.css";

export interface SandwishListPageFilterState {
    closeFilter: () => void;
    filterOpen: boolean;
    openFilter: () => void;
    toggleFilter: () => void;
}

export type SandwishListPageFilterField = KuzhambuFilterPanelField;

export interface SandwishListPageProps<RecordType extends object = object> extends Omit<
    KuzhambuTableProps<RecordType>,
    "title"
> {
    batchActions?: ReactNode;
    batchClassName?: string;
    addText?: ReactNode;
    defaultFilterOpen?: boolean;
    enableAdd?: boolean;
    description?: ReactNode;
    enableFilter?: boolean;
    enableSearch?: boolean;
    eyebrow?: ReactNode;
    filterActive?: boolean;
    filter?: ReactNode | ((filterState: SandwishListPageFilterState) => ReactNode);
    filterClassName?: string;
    filterFields?: SandwishListPageFilterField[];
    filterText?: ReactNode;
    filterOpen?: boolean;
    onAdd?: () => void;
    onFilterApply?: () => void;
    onFilterOpenChange?: (open: boolean) => void;
    onFilterReset?: () => void;
    onSearchChange?: (value: string) => void;
    pageActions?: ReactNode | ((filterState: SandwishListPageFilterState) => ReactNode);
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

export const SandwishListPage = <RecordType extends object = object>({
    batchActions,
    batchClassName,
    addText,
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
}: SandwishListPageProps<RecordType>) => {
    const [internalFilterOpen, setInternalFilterOpen] = useState(defaultFilterOpen);
    const actualFilterOpen = enableFilter ? (filterOpen ?? internalFilterOpen) : false;
    const setFilterOpen = (open: boolean) => {
        if (filterOpen === undefined) {
            setInternalFilterOpen(open);
        }
        onFilterOpenChange?.(open);
    };
    const filterState: SandwishListPageFilterState = {
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
    const resolvedAddText = addText ?? (subjectName ? `新增${subjectName}` : undefined);
    const headerActions = (
        <Space className="sandwish-list-page-actions">
            {enableSearch ? (
                <Input
                    allowClear
                    className={[
                        "sandwish-list-page-search",
                        actualFilterOpen ? "sandwish-list-page-search-hidden" : ""
                    ]
                        .filter(Boolean)
                        .join(" ")}
                    placeholder={resolvedSearchPlaceholder}
                    prefix={<SearchOutlined />}
                    suffix={
                        searchShortcut ? (
                            <span className="sandwish-list-page-search-shortcut">
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
                            ? "sandwish-list-page-filter-toggle-active"
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

            {tableAside ? (
                <div
                    className={[
                        "sandwish-list-page-table-area",
                        `sandwish-list-page-table-area-aside-${tableAsidePlacement}`,
                        tableAreaClassName
                    ].join(" ")}
                >
                    <div className="sandwish-list-page-table-main">
                        <KuzhambuTable<RecordType> {...tableProps} />
                    </div>
                    <aside
                        className={["sandwish-list-page-table-aside", tableAsideClassName]
                            .filter(Boolean)
                            .join(" ")}
                    >
                        {tableAside}
                    </aside>
                </div>
            ) : (
                <KuzhambuTable<RecordType> {...tableProps} />
            )}
        </KuzhambuPage>
    );
};
