import { useMutation } from "@tanstack/react-query";
import { Alert, App, Card, Empty, Space, Typography } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { GraphExtractionCreate } from "./components/graph-extraction-create";
import * as service from "./graph-extraction-service";
import type {
    GraphExtractionCreateCommand,
    GraphExtractionTaskRecord
} from "./graph-extraction-types";
import "./graph-extraction-page.css";

const { Paragraph, Text, Title } = Typography;

export const GraphExtractionPage = () => {
    const { message: messageApi } = App.useApp();
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const [latestCreatedTask, setLatestCreatedTask] = useState<GraphExtractionTaskRecord | null>(
        null
    );
    const createTaskMutation = useMutation({
        mutationFn: (request: GraphExtractionCreateCommand) => service.addTask(request),
        onSuccess: (task) => {
            setLatestCreatedTask(task);
            messageApi.success("抽取任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "抽取任务创建失败");
        }
    });

    return (
        <KuzhambuPage
            className="graph-extraction-page knowledge-graph-extraction-page"
            description="统一管理 Knowledge 抽取任务、候选结果和正式应用动作。"
            eyebrow="Knowledge / Graph Extraction"
            title="知识抽取任务"
        >
            <Space orientation="vertical" size={16} className="knowledge-graph-extraction-layout">
                <Alert
                    banner
                    className="knowledge-graph-extraction-banner"
                    title="本页先提供任务骨架，下一步会接通创建动作、任务列表和候选应用。"
                    type="info"
                />

                <section aria-labelledby="graph-extraction-create-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-create-section" level={4}>
                            创建抽取任务
                        </Title>
                        <Text type="secondary">三类抽取任务共用统一任务台账和候选应用链路。</Text>
                    </div>
                    <Paragraph className="knowledge-graph-extraction-helper">
                        当前先接通任务创建动作，详情抽屉与候选应用会在下一步补齐。
                    </Paragraph>
                    <GraphExtractionCreate
                        canEdit={canEditGraph}
                        creatingTaskType={createTaskMutation.variables?.taskType || null}
                        latestCreatedTask={latestCreatedTask}
                        onCreate={createTaskMutation.mutate}
                    />
                </section>

                <section aria-labelledby="graph-extraction-task-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-task-section" level={4}>
                            任务列表
                        </Title>
                        <Text type="secondary">
                            将展示任务状态、AI 候选关联、失败原因和应用时间线。
                        </Text>
                    </div>
                    <Card className="knowledge-graph-extraction-placeholder" variant="borderless">
                        <Empty
                            description="任务列表与详情抽屉将在下一步接入。"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    </Card>
                </section>
            </Space>
        </KuzhambuPage>
    );
};
