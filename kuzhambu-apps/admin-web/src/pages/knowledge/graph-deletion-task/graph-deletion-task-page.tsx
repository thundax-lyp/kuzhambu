import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuPage,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import { graphDeletionTaskMockData } from "./__mocks__/graph-mock-data";
import { DeletionTaskDetailDrawer } from "./deletion-task-detail-drawer";
import type { GraphDeletionTaskRecord } from "./graph-deletion-task-types";
import "./graph-deletion-task-page.css";

export const GraphDeletionTaskPage = () => {
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canApplyGraph = hasPermission("knowledge:graph:apply");
    const [isMockEmpty, setIsMockEmpty] = useState(false);
    const [tasks, setTasks] = useState<GraphDeletionTaskRecord[]>([
        ...graphDeletionTaskMockData.tasks
    ] as GraphDeletionTaskRecord[]);
    const [detailTask, setDetailTask] = useState<GraphDeletionTaskRecord | null>(null);

    if (!canViewGraph)
        return (
            <KuzhambuPage
                className="graph-deletion-task-page"
                description="需要知识图谱查看权限。"
                title="图谱删除任务"
            >
                <KuzhambuAlert title="无权查看图谱删除任务" type="warning" showIcon />
            </KuzhambuPage>
        );
    return (
        <KuzhambuPage
            className="graph-deletion-task-page"
            description="跟踪删除任务、失败原因和重试结果。"
            title="图谱删除任务"
        >
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <KuzhambuButton
                    testId="knowledge-graph-deletion-task-toggle-empty-button"
                    onClick={() => setIsMockEmpty((value) => !value)}
                >
                    模拟空态
                </KuzhambuButton>
                {isMockEmpty ? <KuzhambuAlert title="暂无删除任务" type="info" showIcon /> : null}
                {!isMockEmpty
                    ? tasks.map((task) => (
                          <KuzhambuCard key={task.id} title={task.id}>
                              <KuzhambuSpace>
                                  <KuzhambuTag
                                      type={
                                          task.status === "FAILED"
                                              ? "danger"
                                              : task.status === "SUCCEEDED"
                                                ? "success"
                                                : "info"
                                      }
                                  >
                                      {task.status}
                                  </KuzhambuTag>
                                  {task.failureReason ? <span>{task.failureReason}</span> : null}
                                  <KuzhambuButton
                                      testId={`knowledge-graph-deletion-task-detail-${task.id}-button`}
                                      onClick={() => setDetailTask(task)}
                                  >
                                      查看详情
                                  </KuzhambuButton>
                                  {task.status === "FAILED" ? (
                                      <KuzhambuButton
                                          disabled={!canApplyGraph}
                                          testId="knowledge-graph-deletion-task-retry-button"
                                          onClick={() =>
                                              setTasks((current) => {
                                                  const nextTask = {
                                                      ...task,
                                                      status: "SUCCEEDED" as const,
                                                      failureReason: undefined
                                                  };
                                                  setDetailTask(nextTask);
                                                  return current.map((item) =>
                                                      item.id === task.id ? nextTask : item
                                                  );
                                              })
                                          }
                                      >
                                          重试
                                      </KuzhambuButton>
                                  ) : null}
                              </KuzhambuSpace>
                          </KuzhambuCard>
                      ))
                    : null}
            </KuzhambuSpace>
            <DeletionTaskDetailDrawer
                task={detailTask}
                open={detailTask !== null}
                onClose={() => setDetailTask(null)}
            />
        </KuzhambuPage>
    );
};
