import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { GraphVersionRecord } from "../graph-results-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface GraphVersionDetailProps {
    loading?: boolean;
    open: boolean;
    version?: GraphVersionRecord | null;
    onOpenResults?: (version: GraphVersionRecord) => void;
    onClose: () => void;
}

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

export const GraphVersionDetail = ({
    loading = false,
    open,
    version,
    onOpenResults,
    onClose
}: GraphVersionDetailProps) => {
    return (
        <KuzhambuDrawer
            title="图谱版本详情"
            open={open}
            size="middle"
            loading={loading}
            onClose={onClose}
            footer={
                <KuzhambuButton
                    name="查看此版本正式结果"
                    type="primary"
                    disabled={!version}
                    onClick={() => {
                        if (version && onOpenResults) {
                            onOpenResults(version);
                        }
                    }}
                >
                    查看此版本正式结果
                </KuzhambuButton>
            }
        >
            <Descriptions column={1} bordered size="small">
                <Descriptions.Item label="版本号">{version?.versionId || "-"}</Descriptions.Item>
                <Descriptions.Item label="任务号">{version?.taskId || "-"}</Descriptions.Item>
                <Descriptions.Item label="候选 ID">{version?.candidateId || "-"}</Descriptions.Item>
                <Descriptions.Item label="任务类型">{version?.taskType || "-"}</Descriptions.Item>
                <Descriptions.Item label="来源类型">
                    {version?.sourceContentType || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="来源内容 ID">
                    {version?.sourceContentId || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="版本序号">{version?.versionNo || "-"}</Descriptions.Item>
                <Descriptions.Item label="状态">{version?.status || "-"}</Descriptions.Item>
                <Descriptions.Item label="精修状态">
                    {version?.refinementApplied ? "已精修" : "未精修"}
                </Descriptions.Item>
                <Descriptions.Item label="最新精修任务">
                    {version?.lastRefinementTaskId || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="最新精修时间">
                    {formatTimestamp(version?.lastRefinementAppliedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="应用时间">
                    {formatTimestamp(version?.appliedAt)}
                </Descriptions.Item>
            </Descriptions>
        </KuzhambuDrawer>
    );
};
