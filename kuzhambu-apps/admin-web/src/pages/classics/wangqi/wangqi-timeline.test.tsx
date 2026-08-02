import type { TimelineProps } from "antd";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { App as AntdApp } from "antd";
import { WangqiTimeline } from "./wangqi-timeline";
import type { WangqiDocumentRecord } from "./wangqi-types";

type MockTimelineProps = Pick<TimelineProps, "className" | "items" | "mode">;

const timelineMock = vi.hoisted(() =>
    vi.fn(({ className, items = [], mode }: MockTimelineProps) => (
        <div data-testid="mock-wangqi-timeline" data-mode={mode} className={className}>
            {items.map((item) => (
                <div key={item.key}>
                    {item.title}
                    {item.content}
                </div>
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

const records: WangqiDocumentRecord[] = [
    {
        id: "1",
        title: "天文卷",
        summary: "天象与历法条目。",
        documentTime: "2026-01-01T00:00:00.000+00:00",
        contentFormat: "MARKDOWN",
        content: "天文内容",
        events: [
            {
                title: "天文事件",
                occurredAt: "2026-01-01T00:00:00.000+00:00",
                occurredLabel: "万历元年"
            }
        ]
    },
    {
        id: "2",
        title: "地理卷",
        summary: "山川与舆图条目。",
        documentTime: "2026-02-01T00:00:00.000+00:00",
        contentFormat: "MARKDOWN",
        content: "地理内容"
    }
];

describe("WangqiTimeline", () => {
    it("opens a large alternating timeline drawer", async () => {
        const user = userEvent.setup();
        const onOpenDocument = vi.fn();

        render(
            <AntdApp>
                <WangqiTimeline dataSource={records} onOpenDocument={onOpenDocument} />
            </AntdApp>
        );

        await user.click(screen.getByRole("button", { name: "打开王圻文档时间线" }));

        expect(screen.getByTestId("classics-wangqi-wangqi-timeline-drawer")).toBeInTheDocument();
        expect(document.querySelector(".kuzhambu-drawer-large")).toBeTruthy();
        expect(timelineMock).toHaveBeenCalledWith(
            expect.objectContaining({
                className: "wangqi-timeline",
                mode: "alternate"
            }),
            expect.anything()
        );
        expect(screen.getByText("万历元年")).toBeInTheDocument();
        expect(screen.getByText("2026/02")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: /天文事件/ }));

        expect(onOpenDocument).toHaveBeenCalledWith(records[0]);
    });
});
