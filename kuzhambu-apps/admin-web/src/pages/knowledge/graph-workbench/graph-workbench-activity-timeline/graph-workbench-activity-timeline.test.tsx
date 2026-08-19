import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { GraphWorkbenchActivityTimeline } from "./graph-workbench-activity-timeline";

describe("GraphWorkbenchActivityTimeline", () => {
    it("renders the action and material title as text, with a localized status tag", () => {
        render(
            <GraphWorkbenchActivityTimeline
                activities={[
                    {
                        occurredAt: "not-a-date",
                        summary: "发布素材 毕宿 SUCCEEDED",
                        type: "PUBLISH"
                    }
                ]}
            />
        );

        expect(screen.getByText("发布素材")).toBeInTheDocument();
        expect(screen.getByText("毕宿")).toBeInTheDocument();
        expect(screen.getByText("成功")).toBeInTheDocument();
        expect(screen.queryByText("Invalid Date")).not.toBeInTheDocument();
    });
});
