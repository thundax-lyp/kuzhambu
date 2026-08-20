import { Network } from "lucide-react";
import { AtlasWorkbenchCanvas } from "./atlas-workbench-canvas";
import { useAtlasProjection } from "./use-atlas-projection";

import "./atlas-page.css";

export const KnowledgeAtlasPage = () => {
    const { autoExpansionComplete, expandedNodeIds, expandNode, graph, graphState, overview } =
        useAtlasProjection();
    const metrics = [
        ["正式节点", overview?.publishedNodeCount],
        ["关系", overview?.publishedEdgeCount],
        ["覆盖素材", overview?.coveredMaterialCount]
    ];
    return (
        <main className="knowledge-atlas-shell">
            <header className="knowledge-atlas-header">
                <div>
                    <h1>三才图会总谱</h1>
                    <p>从核心主题出发，探索三才图会中的人物、事物与观念关联。</p>
                </div>
                <Network aria-hidden="true" size={30} />
            </header>
            <section className="knowledge-atlas-preview">
                {graphState === "error" ? (
                    <p className="knowledge-atlas-error">总谱暂不可用，请稍后重试。</p>
                ) : (
                    <AtlasWorkbenchCanvas
                        finalRelaxation={autoExpansionComplete}
                        expandedNodeIds={expandedNodeIds}
                        graph={graph}
                        loading={graphState === "loading"}
                        onNodeExpand={expandNode}
                    />
                )}
            </section>
            <dl aria-live="polite" className="knowledge-atlas-metrics">
                {metrics.map(([label, value]) => (
                    <div key={label}>
                        <dt>{label}</dt>
                        <dd>{value ?? "-"}</dd>
                    </div>
                ))}
            </dl>
        </main>
    );
};
