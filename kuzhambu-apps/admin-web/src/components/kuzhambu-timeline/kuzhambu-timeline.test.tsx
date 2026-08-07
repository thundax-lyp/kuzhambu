import type { TimelineProps } from "antd";
import { render, screen } from "@testing-library/react";
import { KuzhambuTimeline } from "./kuzhambu-timeline";

type MockTimelineProps = Pick<TimelineProps, "className" | "items" | "mode"> & {
    "data-testid"?: string;
};

const timelineMock = vi.hoisted(() =>
    vi.fn(({ className, items = [], mode, "data-testid": testId }: MockTimelineProps) => (
        <div data-testid={testId} data-mode={mode} className={className}>
            {items.map((item) => (
                <div key={item.key}>{item.children}</div>
            ))}
        </div>
    ))
);

vi.mock("antd", async (importOriginal) => {
    const actual = await importOriginal<typeof import("antd")>();
    return {
        ...actual,
        Timeline: timelineMock
    };
});

describe("KuzhambuTimeline", () => {
    it("renders timeline with test id and base class", () => {
        render(
            <KuzhambuTimeline
                testId="sample-timeline"
                className="sample-class"
                mode="alternate"
                items={[{ key: "first", children: "第一项" }]}
            />
        );

        expect(screen.getByTestId("sample-timeline")).toHaveClass(
            "kuzhambu-timeline",
            "sample-class"
        );
        expect(screen.getByTestId("sample-timeline")).toHaveAttribute("data-mode", "alternate");
        expect(screen.getByText("第一项")).toBeInTheDocument();
    });
});
