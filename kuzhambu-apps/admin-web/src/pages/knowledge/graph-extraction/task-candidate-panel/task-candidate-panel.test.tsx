import { render, screen } from "@testing-library/react";
import type { GraphCandidatePreviewRecord } from "@/pages/knowledge/graph-extraction/graph-extraction-types";
import { TaskCandidatePanel } from "./task-candidate-panel";

const createCandidate = (): GraphCandidatePreviewRecord => ({
    candidateId: "candidate-9001",
    diff: [
        {
            candidateObjectId: "node-001",
            changeType: "ADD",
            changedFields: ["name", "nodeType"],
            objectType: "NODE"
        },
        {
            candidateObjectId: "node-002",
            changeType: "UPDATE",
            changedFields: ["alias"],
            draftObjectId: "draft-node-002",
            objectType: "NODE"
        },
        {
            candidateObjectId: "edge-001",
            changeType: "REMOVE",
            draftObjectId: "draft-edge-001",
            objectType: "EDGE"
        },
        {
            candidateObjectId: "edge-002",
            changeType: "CONFLICT",
            issues: [
                {
                    code: "DUPLICATE_RELATION",
                    message: "关系与现有草稿冲突",
                    objectId: "edge-002",
                    objectType: "EDGE",
                    severity: "BLOCKING"
                }
            ],
            objectType: "EDGE"
        }
    ],
    edges: [
        {
            candidateObjectId: "edge-002",
            qualifiers: {
                evidence: "卷一"
            },
            relationType: "师承",
            sourceCandidateNodeId: "node-001",
            targetCandidateNodeId: "node-002"
        }
    ],
    issues: [
        {
            code: "DUPLICATE_RELATION",
            field: "relationType",
            message: "关系与现有草稿冲突",
            objectId: "edge-002",
            objectType: "EDGE",
            severity: "BLOCKING"
        },
        {
            code: "LOW_CONFIDENCE",
            message: "证据置信度偏低",
            objectId: "node-002",
            objectType: "NODE",
            severity: "WARNING"
        }
    ],
    nodes: [
        {
            candidateObjectId: "node-001",
            name: "张三",
            nodeType: "PERSON",
            properties: {
                dynasty: "明"
            }
        },
        {
            candidateObjectId: "node-002",
            name: "李四",
            nodeType: "PERSON",
            properties: {}
        }
    ]
});

describe("TaskCandidatePanel", () => {
    it("shows unavailable empty state when candidate is null", () => {
        render(<TaskCandidatePanel candidate={null} />);

        expect(
            screen.getByTestId("knowledge-graph-extraction-task-detail-candidate-section")
        ).toBeInTheDocument();
        expect(screen.getByText("候选不可用")).toBeInTheDocument();
    });

    it("renders candidate nodes, edges, issues and all diff change types", () => {
        render(<TaskCandidatePanel candidate={createCandidate()} />);

        expect(screen.getByText("candidate-9001")).toBeInTheDocument();
        expect(screen.getByText("张三")).toBeInTheDocument();
        expect(screen.getByText("师承")).toBeInTheDocument();
        expect(screen.getAllByText("关系与现有草稿冲突").length).toBeGreaterThan(0);

        expect(screen.getByText("新增")).toBeInTheDocument();
        expect(screen.getByText("更新")).toBeInTheDocument();
        expect(screen.getByText("移除")).toBeInTheDocument();
        expect(screen.getByText("冲突")).toBeInTheDocument();
    });
});
