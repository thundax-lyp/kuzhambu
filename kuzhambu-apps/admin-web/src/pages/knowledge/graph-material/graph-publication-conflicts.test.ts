import { describe, expect, it } from "vitest";
import { buildReuseConflictDecisions } from "./graph-publication-conflicts";

describe("buildReuseConflictDecisions", () => {
    it("reuses every conflict match and ignores non-conflict rows", () => {
        expect(
            buildReuseConflictDecisions({
                edges: [
                    { matchType: "CONFLICT", matchedObjectId: "21", materialObjectId: "11" },
                    { matchType: "CREATE", materialObjectId: "12" }
                ],
                issues: [],
                materialLockVersion: "3",
                materialRef: { contentRefId: "1", contentType: "SANCAI_ENTRY" },
                nodes: [
                    { matchType: "CONFLICT", matchedObjectId: "20", materialObjectId: "10" },
                    { matchType: "CONFLICT", materialObjectId: "13" }
                ],
                previewToken: "preview-token",
                publishable: true
            })
        ).toEqual([
            {
                action: "REUSE_MATCH",
                matchedObjectId: "20",
                materialObjectId: "10",
                objectType: "NODE"
            },
            {
                action: "REUSE_MATCH",
                matchedObjectId: "21",
                materialObjectId: "11",
                objectType: "EDGE"
            }
        ]);
    });
});
