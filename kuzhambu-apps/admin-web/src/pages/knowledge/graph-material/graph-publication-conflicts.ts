import type {
    GraphPublicationConflictDecisionRecord,
    GraphPublicationPreviewObjectRecord,
    GraphPublicationPreviewRecord
} from "./graph-material-types";

const reuseConflictDecisions = (
    objectType: "NODE" | "EDGE",
    objects: GraphPublicationPreviewObjectRecord[]
): GraphPublicationConflictDecisionRecord[] =>
    objects.flatMap((object) =>
        object.matchType === "CONFLICT" && object.matchedObjectId
            ? [
                  {
                      action: "REUSE_MATCH" as const,
                      matchedObjectId: object.matchedObjectId,
                      materialObjectId: object.materialObjectId,
                      objectType
                  }
              ]
            : []
    );

/**
 * Re-publishing a withdrawn material restores its links to the governed graph.
 * Preview conflicts therefore reuse their exact preview match by default.
 */
export const buildReuseConflictDecisions = (
    preview: GraphPublicationPreviewRecord
): GraphPublicationConflictDecisionRecord[] => [
    ...reuseConflictDecisions("NODE", preview.nodes),
    ...reuseConflictDecisions("EDGE", preview.edges)
];
