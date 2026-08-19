import { KuzhambuAlert, KuzhambuPage } from "@/components";
import { hasPermission } from "@/auth/permission-storage";
import { GraphWorkbenchActivityTimeline } from "./graph-workbench-activity-timeline";
import { GraphWorkbenchCanvas } from "./graph-workbench-canvas";
import { GraphWorkbenchLegend } from "./graph-workbench-legend";
import { GraphWorkbenchOverview } from "./graph-workbench-overview";
import { useGraphWorkbenchAtlas } from "./hooks/use-graph-workbench-atlas";
import "./graph-workbench-page.css";

export const GraphWorkbenchPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const atlas = useGraphWorkbenchAtlas(canViewGraph);
    const reducedMotion =
        typeof window !== "undefined" &&
        typeof window.matchMedia === "function" &&
        window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    if (!canViewGraph) {
        return <KuzhambuAlert showIcon title="无权查看图谱工作台" type="warning" />;
    }
    return (
        <KuzhambuPage
            className="graph-workbench-page"
            description="正式知识图的只读动态态势展示。"
            title="图谱工作台"
        >
            <section className="graph-workbench-stage">
                <GraphWorkbenchOverview overview={atlas.overview} state={atlas.overviewState} />
                <GraphWorkbenchCanvas graph={atlas.graph} motion={!reducedMotion} />
                <GraphWorkbenchLegend graph={atlas.graph} />
            </section>
            {atlas.overviewState === "ready" && atlas.overview?.recentActivities.length ? (
                <GraphWorkbenchActivityTimeline activities={atlas.overview.recentActivities} />
            ) : null}
            {atlas.graphState === "error" ? (
                <KuzhambuAlert showIcon title="正式图画布暂不可用" type="warning" />
            ) : null}
        </KuzhambuPage>
    );
};
