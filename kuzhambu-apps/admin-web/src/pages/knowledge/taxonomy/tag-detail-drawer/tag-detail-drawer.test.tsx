import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { TagDetailDrawer } from "./tag-detail-drawer";

describe("TagDetailDrawer", () => {
    afterEach(() => {
        cleanup();
    });

    it("delegates deprecate action when enabled", async () => {
        const onDeprecate = vi.fn();

        render(
            <TagDetailDrawer
                canDeprecateTag
                open
                tagDetail={{
                    tag: {
                        id: "1001",
                        name: "礼制",
                        status: "ENABLED",
                        reviewStatus: "APPROVED",
                        source: "MANUAL",
                        contentRefCount: 2
                    },
                    aliases: [],
                    contentRefs: []
                }}
                onClose={() => {}}
                onDeprecate={onDeprecate}
            />
        );

        await userEvent.click(screen.getByRole("button", { name: "废弃标签" }));
        expect(onDeprecate).toHaveBeenCalledWith({ id: "1001" });
    });
});
