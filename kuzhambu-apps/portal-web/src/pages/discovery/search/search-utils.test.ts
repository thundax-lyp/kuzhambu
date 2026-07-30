import { describe, expect, it } from "vitest";
import { createClickCommand } from "./search-utils";
import type { DiscoverySearchGroupResponse, DiscoverySearchItemResponse } from "./search-types";

const group: DiscoverySearchGroupResponse = {
    count: 1,
    groupKey: "SANCAI_ENTRY",
    groupTitle: "三才图会",
    items: []
};

const item: DiscoverySearchItemResponse = {
    contentDomain: "CLASSICS",
    contentId: "1001",
    contentType: "SANCAI_ENTRY",
    groupRank: 1,
    resultRank: 1,
    title: "礼器"
};

describe("search utils", () => {
    it("rejects non-positive search event ids when creating click commands", () => {
        expect(createClickCommand("0", group, item)).toBeNull();
        expect(createClickCommand("-1", group, item)).toBeNull();
        expect(createClickCommand(0, group, item)).toBeNull();
    });

    it("normalizes positive search event ids when creating click commands", () => {
        expect(createClickCommand(" 9001 ", group, item)).toMatchObject({
            contentId: "1001",
            searchEventId: "9001"
        });
        expect(createClickCommand(9002, group, item)).toMatchObject({
            contentId: "1001",
            searchEventId: "9002"
        });
    });
});
