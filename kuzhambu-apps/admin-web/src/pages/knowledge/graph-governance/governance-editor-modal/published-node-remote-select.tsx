import { useInfiniteQuery } from "@tanstack/react-query";
import type { InfiniteData } from "@tanstack/react-query";
import { Spin } from "antd";
import type { UIEvent } from "react";
import { useEffect, useMemo, useState } from "react";
import { KuzhambuSelect } from "@/components";
import type { Page } from "@/types/page";
import * as service from "../graph-governance-service";
import type { GraphGovernanceNodeRecord } from "../graph-governance-types";

const SEARCH_PAGE_SIZE = 20;
const SEARCH_DELAY_MS = 300;

interface PublishedNodeRemoteSelectProps {
    ariaLabel: string;
    initialNodes?: GraphGovernanceNodeRecord[];
    onChange?: (value: string) => void;
    onSelectNode?: (node: GraphGovernanceNodeRecord) => void;
    value?: string;
}

const toOption = (node: GraphGovernanceNodeRecord) => ({
    label: `${node.name || node.id}${node.nodeType ? `（${node.nodeType}）` : ""}`,
    value: node.id
});

export const PublishedNodeRemoteSelect = ({
    ariaLabel,
    initialNodes = [],
    onChange,
    onSelectNode,
    value
}: PublishedNodeRemoteSelectProps) => {
    const [keyword, setKeyword] = useState("");
    const [searchKeyword, setSearchKeyword] = useState("");
    useEffect(() => {
        const timer = window.setTimeout(() => setSearchKeyword(keyword.trim()), SEARCH_DELAY_MS);
        return () => window.clearTimeout(timer);
    }, [keyword]);
    const nodeQuery = useInfiniteQuery<
        Page<GraphGovernanceNodeRecord>,
        Error,
        InfiniteData<Page<GraphGovernanceNodeRecord>>,
        readonly string[],
        number
    >({
        enabled: searchKeyword.length > 0,
        getNextPageParam: (page) => (page.pageNo < page.totalPage ? page.pageNo + 1 : undefined),
        initialPageParam: 1,
        queryFn: ({ pageParam }) =>
            service.pagePublishedNodes({
                keyword: searchKeyword,
                pageNo: pageParam,
                pageSize: SEARCH_PAGE_SIZE,
                status: "ACTIVE"
            }),
        queryKey: ["knowledge", "graph-governance", "node-picker", searchKeyword]
    });
    const options = useMemo(() => {
        const nodes = new Map(initialNodes.map((node) => [node.id, node]));
        nodeQuery.data?.pages
            .flatMap((page) => page.records)
            .forEach((node) => nodes.set(node.id, node));
        return Array.from(nodes.values()).map(toOption);
    }, [initialNodes, nodeQuery.data]);
    const nodesById = useMemo(() => {
        const nodes = new Map(initialNodes.map((node) => [node.id, node]));
        nodeQuery.data?.pages
            .flatMap((page) => page.records)
            .forEach((node) => nodes.set(node.id, node));
        return nodes;
    }, [initialNodes, nodeQuery.data]);
    const loadNextPage = (event: UIEvent<HTMLDivElement>) => {
        const target = event.currentTarget;
        if (
            nodeQuery.hasNextPage &&
            !nodeQuery.isFetchingNextPage &&
            target.scrollTop + target.clientHeight >= target.scrollHeight - 24
        ) {
            void nodeQuery.fetchNextPage();
        }
    };

    return (
        <KuzhambuSelect
            aria-label={ariaLabel}
            filterOption={false}
            loading={nodeQuery.isLoading || nodeQuery.isFetchingNextPage}
            notFoundContent={
                nodeQuery.isLoading ? (
                    <Spin size="small" />
                ) : keyword.trim() ? (
                    "未找到节点"
                ) : (
                    "输入名称搜索节点"
                )
            }
            options={options}
            showSearch
            style={{ width: "100%" }}
            value={value}
            onChange={(nextValue) => {
                onChange?.(nextValue);
                const selectedNode = nodesById.get(nextValue);
                if (selectedNode) {
                    onSelectNode?.(selectedNode);
                }
            }}
            onPopupScroll={loadNextPage}
            onSearch={setKeyword}
        />
    );
};
