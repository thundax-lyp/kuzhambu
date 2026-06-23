import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { GraphLineageNodeRecord } from "../graph-results-types";

interface GraphLineageNodeDetailProps {
    loading?: boolean;
    node?: GraphLineageNodeRecord | null;
    onClose: () => void;
    open: boolean;
}

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

export const GraphLineageNodeDetail = ({
    loading = false,
    node,
    onClose,
    open
}: GraphLineageNodeDetailProps) => {
    return (
        <KuzhambuDrawer
            title="正式世系节点详情"
            open={open}
            size="middle"
            loading={loading}
            onClose={onClose}
        >
            <Descriptions column={1} bordered size="small">
                <Descriptions.Item label="节点号">{node?.nodeId || "-"}</Descriptions.Item>
                <Descriptions.Item label="业务键">{node?.nodeKey || "-"}</Descriptions.Item>
                <Descriptions.Item label="名称">{node?.name || "-"}</Descriptions.Item>
                <Descriptions.Item label="类型">{node?.nodeType || "-"}</Descriptions.Item>
                <Descriptions.Item label="世代">{node?.generation ?? "-"}</Descriptions.Item>
                <Descriptions.Item label="性别">{node?.gender || "-"}</Descriptions.Item>
                <Descriptions.Item label="确认状态">
                    {node?.confirmationStatus || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="版本号">{node?.latestVersionId || "-"}</Descriptions.Item>
                <Descriptions.Item label="来源引用">
                    {node?.sourceRefsJson || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="首次抽取">
                    {formatTimestamp(node?.firstExtractedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="最近抽取">
                    {formatTimestamp(node?.lastExtractedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="确认时间">
                    {formatTimestamp(node?.confirmedAt)}
                </Descriptions.Item>
            </Descriptions>
        </KuzhambuDrawer>
    );
};
