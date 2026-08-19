import { KuzhambuTag } from "@/components";
import type { KuzhambuTagType } from "@/components/kuzhambu-tag/kuzhambu-tag";
import type { GraphWorkbenchOverviewRecord } from "../graph-workbench-types";
import "./graph-workbench-activity-timeline.css";

export interface GraphWorkbenchActivityTimelineProps {
    activities: GraphWorkbenchOverviewRecord["recentActivities"];
}

const ACTIVITY_STATUS_PATTERN = /^(.*?)\s+(.+?)\s+(SUCCEEDED|FAILED|PENDING|RUNNING)$/;

const statusTagType = (status: string): KuzhambuTagType => {
    if (status === "SUCCEEDED") return "success";
    if (status === "FAILED") return "danger";
    if (status === "PENDING" || status === "RUNNING") return "warning";
    return "neutral";
};

const statusLabel = (status: string) => {
    const labels: Record<string, string> = {
        FAILED: "失败",
        PENDING: "待执行",
        RUNNING: "执行中",
        SUCCEEDED: "成功"
    };
    return labels[status] ?? status;
};

const formatOccurredAt = (occurredAt: string | null) => {
    if (!occurredAt) return null;
    const date = new Date(occurredAt);
    if (Number.isNaN(date.getTime())) return null;
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    return `${date.getFullYear()}-${day}-${month}`;
};

const readActivity = (activity: GraphWorkbenchOverviewRecord["recentActivities"][number]) => {
    const matched = activity.summary.match(ACTIVITY_STATUS_PATTERN);
    if (matched) {
        return { action: matched[1], status: matched[3], subject: matched[2] };
    }
    return { action: activity.type, status: null, subject: activity.summary };
};

export const GraphWorkbenchActivityTimeline = ({
    activities
}: GraphWorkbenchActivityTimelineProps) => {
    if (!activities.length) return null;
    return (
        <ol className="graph-workbench-activity">
            {activities.slice(0, 10).map((activity, index) => {
                const { action, status, subject } = readActivity(activity);
                const occurredAt = formatOccurredAt(activity.occurredAt);
                return (
                    <li key={`${activity.occurredAt}-${index}`}>
                        <div className="graph-workbench-activity-content">
                            <span className="graph-workbench-activity-action">{action}</span>
                            <strong>{subject}</strong>
                            {status ? (
                                <KuzhambuTag type={statusTagType(status)}>
                                    {statusLabel(status)}
                                </KuzhambuTag>
                            ) : null}
                        </div>
                        {occurredAt ? (
                            <time dateTime={activity.occurredAt ?? undefined}>{occurredAt}</time>
                        ) : null}
                    </li>
                );
            })}
        </ol>
    );
};
