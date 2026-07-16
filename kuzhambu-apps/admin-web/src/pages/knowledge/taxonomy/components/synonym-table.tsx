import { ReloadOutlined } from "@ant-design/icons";
import { Typography } from "antd";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import type { SynonymRemoveCommand, SynonymStatusCommand } from "../taxonomy-service";
import type { SynonymPageQuery, SynonymRecord } from "../taxonomy-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    term: 220,
    synonym: 220,
    status: 140,
    actions: 180
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readStatusValue = (record: SynonymRecord): "ENABLED" | "DISABLED" => {
    return record.status === "DISABLED" ? "DISABLED" : "ENABLED";
};

const readStatusLabel = (status: "ENABLED" | "DISABLED") => {
    return status === "ENABLED" ? "启用" : "禁用";
};

interface SynonymTableProps {
    canEditSynonym: boolean;
    loading: boolean;
    query: SynonymPageQuery;
    removing?: boolean;
    synonyms: SynonymRecord[];
    totalCount: number;
    onAdd: () => void;
    onChange: (values: SynonymPageQuery) => void;
    onEdit: (record: SynonymRecord) => void;
    onRefresh: () => void;
    onRemove: (request: SynonymRemoveCommand) => void;
    onStatusChange: (request: SynonymStatusCommand) => void;
}

export const SynonymTable = ({
    canEditSynonym,
    loading,
    query,
    removing = false,
    synonyms,
    totalCount,
    onAdd,
    onChange,
    onEdit,
    onRefresh,
    onRemove,
    onStatusChange
}: SynonymTableProps) => {
    const currentPageNo = query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = query.pageSize || DEFAULT_PAGE_SIZE;
    const hasSearch = Boolean(normalizeSearch(query.term));

    const updateQuery = (nextQuery: SynonymPageQuery) => {
        onChange({
            ...query,
            ...nextQuery,
            pageNo: nextQuery.pageNo || DEFAULT_PAGE_NO,
            pageSize: nextQuery.pageSize || currentPageSize
        });
    };

    const columns: KuzhambuTableProps<SynonymRecord>["columns"] = [
        {
            title: "术语",
            dataIndex: "term",
            key: "term",
            width: DEFAULT_COLUMN_WIDTHS.term,
            ellipsis: true,
            render: (term: string | null | undefined, record) => <Text>{term || record.id}</Text>
        },
        {
            title: "同义词",
            dataIndex: "synonym",
            key: "synonym",
            width: DEFAULT_COLUMN_WIDTHS.synonym,
            ellipsis: true,
            render: (synonym?: string | null) => synonym || "-"
        },
        {
            title: "状态",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, record) => {
                const statusValue = readStatusValue(record);
                return (
                    <KuzhambuSwitch
                        checked={statusValue === "ENABLED"}
                        checkedChildren={readStatusLabel("ENABLED")}
                        unCheckedChildren={readStatusLabel("DISABLED")}
                        disabled={!canEditSynonym}
                        aria-label={`切换 ${record.term || record.id} 状态`}
                        onChange={(checked) => {
                            onStatusChange({
                                id: record.id,
                                status: checked ? "ENABLED" : "DISABLED"
                            });
                        }}
                    />
                );
            }
        },
        {
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            options: (record) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑同义词 ${record.term || record.id}`,
                    disabled: !canEditSynonym || removing,
                    onClick: onEdit
                },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除同义词 ${record.term || record.id}`,
                    disabled: !canEditSynonym || removing,
                    onClick: () => onRemove({ id: record.id })
                }
            ]
        }
    ];

    return (
        <KuzhambuListPage<SynonymRecord>
            pageClassName="knowledge-taxonomy-synonym-page"
            title="同义词管理"
            description="维护术语与同义词映射，支持分页、创建、更新、启用、禁用与删除。"
            subjectName="同义词"
            enableAdd={canEditSynonym}
            enableSearch
            searchShortcut="⌘K"
            searchValue={query.term || ""}
            onSearchChange={(value) =>
                updateQuery({
                    term: normalizeSearch(value),
                    pageNo: DEFAULT_PAGE_NO
                })
            }
            onAdd={onAdd}
            pageActions={
                <KuzhambuButton
                    testId="knowledge-taxonomy-synonym-refresh-button"
                    icon={<ReloadOutlined />}
                    onClick={onRefresh}
                >
                    刷新
                </KuzhambuButton>
            }
            filterActive={hasSearch}
            rowKey="id"
            columns={columns}
            dataSource={synonyms}
            loading={loading}
            pagination={{
                current: currentPageNo,
                pageSize: currentPageSize,
                total: totalCount,
                showTotal: (total) => `共 ${total} 个同义词`,
                onChange: (pageNo, pageSize) => {
                    updateQuery({
                        pageNo,
                        pageSize
                    });
                }
            }}
            locale={{
                emptyText: loading ? "同义词加载中..." : "暂无同义词"
            }}
        />
    );
};
