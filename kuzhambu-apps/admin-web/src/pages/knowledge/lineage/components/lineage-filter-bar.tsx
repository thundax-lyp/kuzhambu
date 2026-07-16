import { ReloadOutlined } from "@ant-design/icons";
import { Input, Select } from "antd";
import type { LineageCanvasQuery } from "../lineage-service";
import type { LineageAvailableFiltersRecord, LineageVersionRecord } from "../lineage-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface LineageFilterBarProps {
    filters: LineageAvailableFiltersRecord;
    loading?: boolean;
    query: LineageCanvasQuery;
    onChange: (nextQuery: LineageCanvasQuery) => void;
    onRefresh: () => void;
    onReset: () => void;
}

const readVersionLabel = (version: LineageVersionRecord) => {
    const versionText = version.versionNo == null ? version.versionId : version.versionNo;
    const sourceText = version.sourceCategoryName || version.sourceContentType || "未标注来源";
    return `版本 ${versionText} / ${sourceText}`;
};

const readOptions = (values: string[]) => {
    return values.map((value) => ({
        label: value,
        value
    }));
};

export const LineageFilterBar = ({
    filters,
    loading = false,
    query,
    onChange,
    onRefresh,
    onReset
}: LineageFilterBarProps) => {
    const changeQuery = (patch: Partial<LineageCanvasQuery>) => {
        onChange({
            ...query,
            ...patch
        });
    };

    return (
        <div className="knowledge-lineage-filter-bar">
            <Select
                aria-label="图谱版本"
                className="knowledge-lineage-filter-bar__version"
                allowClear
                loading={loading}
                placeholder="图谱版本"
                value={query.versionId ?? undefined}
                options={filters.versions.map((version) => ({
                    label: readVersionLabel(version),
                    value: version.versionId
                }))}
                onChange={(value) =>
                    changeQuery({
                        versionId: value ?? null,
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            />
            <Input.Search
                aria-label="搜索世系节点或关系"
                className="knowledge-lineage-filter-bar__search"
                allowClear
                key={query.keyword || "empty-keyword"}
                placeholder="搜索世系节点或关系"
                defaultValue={query.keyword ?? undefined}
                onSearch={(value) =>
                    changeQuery({
                        keyword: value.trim() || null,
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            />
            <Select
                aria-label="节点类型"
                allowClear
                placeholder="节点类型"
                value={query.nodeType ?? undefined}
                options={readOptions(filters.nodeTypes)}
                onChange={(value) =>
                    changeQuery({
                        nodeType: value ?? null,
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            />
            <Select
                aria-label="关系类型"
                allowClear
                placeholder="关系类型"
                value={query.relationType ?? undefined}
                options={readOptions(filters.relationTypes)}
                onChange={(value) =>
                    changeQuery({
                        relationType: value ?? null,
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            />
            <Select
                aria-label="确认状态"
                allowClear
                placeholder="确认状态"
                value={query.confirmationStatus ?? undefined}
                options={readOptions(filters.confirmationStatuses)}
                onChange={(value) =>
                    changeQuery({
                        confirmationStatus: value ?? null,
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            />
            <Select
                aria-label="深度"
                className="knowledge-lineage-filter-bar__depth"
                value={query.depth ?? 2}
                options={[1, 2, 3, 4].map((value) => ({
                    label: `${value} 层`,
                    value
                }))}
                onChange={(value) =>
                    changeQuery({
                        depth: value,
                        focusNodeId: null,
                        focusRelationId: null
                    })
                }
            />
            <KuzhambuButton
                testId="knowledge-lineage-lineage-filter-bar-reset-button"
                onClick={onReset}
            >
                重置
            </KuzhambuButton>
            <KuzhambuButton
                testId="knowledge-lineage-lineage-filter-bar-refresh-button"
                icon={<ReloadOutlined />}
                loading={loading}
                onClick={onRefresh}
            >
                刷新
            </KuzhambuButton>
        </div>
    );
};
