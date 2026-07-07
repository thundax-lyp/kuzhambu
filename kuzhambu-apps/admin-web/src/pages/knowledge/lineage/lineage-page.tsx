import { Empty, Typography } from "antd";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import "./lineage-page.css";

const { Text, Title } = Typography;

export const LineagePage = () => {
    const canViewLineage = hasPermission("knowledge:graph:view");

    return (
        <KuzhambuPage
            className="lineage-page knowledge-lineage-page"
            description="以正式世系版本为入口浏览节点、关系和来源线索。"
            eyebrow="Knowledge / Lineage"
            title="世系图浏览"
        >
            {canViewLineage ? (
                <KuzhambuSpace
                    orientation="vertical"
                    size={16}
                    className="knowledge-lineage-layout"
                >
                    <section className="knowledge-lineage-toolbar" aria-label="世系图筛选">
                        <div>
                            <Title level={4}>画布筛选</Title>
                            <Text type="secondary">选择版本后查看正式世系图。</Text>
                        </div>
                    </section>
                    <section className="knowledge-lineage-workspace" aria-label="世系图画布">
                        <div className="knowledge-lineage-canvas-shell">
                            <Empty
                                description="请选择世系版本"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        </div>
                        <aside
                            className="knowledge-lineage-detail-shell"
                            aria-label="节点和关系详情"
                        >
                            <Empty
                                description="尚未选中节点或关系"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        </aside>
                    </section>
                </KuzhambuSpace>
            ) : (
                <Empty
                    description="当前账号暂无知识图谱查看权限。"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            )}
        </KuzhambuPage>
    );
};
