import { useQuery } from "@tanstack/react-query";
import { Descriptions, Typography } from "antd";
import { KuzhambuSpace } from "@/components";
import * as service from "@/pages/discovery/search-statistic/search-statistic-service";
import type { DiscoverySearchEventRecord } from "@/pages/discovery/search-statistic/search-statistic-types";

const { Text } = Typography;

interface SearchEventDetailProps {
    record: DiscoverySearchEventRecord;
}

export const SearchEventDetail = ({ record }: SearchEventDetailProps) => {
    const eventId = record.id ?? null;
    const detailQuery = useQuery({
        enabled: eventId !== null,
        queryFn: () => service.getSearchEventDetail({ id: eventId ?? "" }),
        queryKey: ["discovery-search-statistics", "event-detail", eventId],
        retry: false
    });
    const detail = detailQuery.data;

    if (detailQuery.isFetching) {
        return <Text type="secondary">详情加载中...</Text>;
    }
    if (detailQuery.isError) {
        return <Text type="danger">检索记录详情加载失败。</Text>;
    }

    return (
        <div className="search-statistics-record-detail">
            <Descriptions
                bordered
                column={2}
                items={[
                    { key: "id", label: "检索编号", children: detail?.id ?? record.id ?? "-" },
                    {
                        key: "queryText",
                        label: "搜索词",
                        children: detail?.queryText ?? record.queryText ?? "-"
                    },
                    {
                        key: "normalizedQueryText",
                        label: "清洗词",
                        children: detail?.normalizedQueryText ?? "-"
                    },
                    {
                        key: "displayQueryText",
                        label: "回显词",
                        children: detail?.displayQueryText ?? record.displayQueryText ?? "-"
                    },
                    {
                        key: "intentType",
                        label: "意图",
                        children: detail?.intentType ?? record.intentType ?? "-"
                    },
                    {
                        key: "status",
                        label: "状态",
                        children: detail?.searchStatus ?? record.searchStatus ?? "-"
                    },
                    {
                        key: "resultTotalCount",
                        label: "总结果",
                        children: detail?.resultTotalCount ?? record.resultTotalCount ?? "-"
                    },
                    {
                        key: "groupTotalCount",
                        label: "分组数",
                        children: detail?.groupTotalCount ?? record.groupTotalCount ?? "-"
                    },
                    { key: "requestId", label: "请求号", children: detail?.requestId ?? "-" },
                    { key: "traceId", label: "链路号", children: detail?.traceId ?? "-" }
                ]}
                size="small"
            />
            <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
                <Text strong>失败信息 / 检索范围</Text>
                <Text type="secondary">{detail?.failureCode ?? "-"}</Text>
                <Text>{detail?.failureMessage ?? "-"}</Text>
                <Text code style={{ whiteSpace: "pre-wrap" }}>
                    {detail?.searchScopesJson ?? "-"}
                </Text>
            </KuzhambuSpace>
        </div>
    );
};
