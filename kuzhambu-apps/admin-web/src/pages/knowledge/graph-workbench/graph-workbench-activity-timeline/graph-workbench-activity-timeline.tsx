import type { GraphWorkbenchOverviewRecord } from "../graph-workbench-types";
import "./graph-workbench-activity-timeline.css";

export interface GraphWorkbenchActivityTimelineProps {
    activities: GraphWorkbenchOverviewRecord["recentActivities"];
}

export const GraphWorkbenchActivityTimeline = ({
    activities
}: GraphWorkbenchActivityTimelineProps) => {
    if (!activities.length) return null;
    return (
        <ol className="graph-workbench-activity">
            {activities.slice(0, 10).map((activity, index) => (
                <li key={`${activity.occurredAt}-${index}`}>{activity.summary}</li>
            ))}
        </ol>
    );
};
