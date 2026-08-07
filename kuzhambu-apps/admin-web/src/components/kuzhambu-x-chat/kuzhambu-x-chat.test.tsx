import type { BubbleListProps, SenderProps } from "@ant-design/x";
import { render, screen } from "@testing-library/react";
import { KuzhambuXBubbleList, KuzhambuXSender } from "./index";

type MockBubbleListProps = Pick<BubbleListProps, "className" | "items"> & {
    "data-testid"?: string;
};
type MockSenderProps = Pick<SenderProps, "className" | "placeholder" | "value"> & {
    "data-testid"?: string;
};

const bubbleListMock = vi.hoisted(() =>
    vi.fn(({ className, items = [], "data-testid": testId }: MockBubbleListProps) => (
        <div data-testid={testId} className={className}>
            {items.map((item) => (
                <div key={item.key}>{item.content}</div>
            ))}
        </div>
    ))
);

const senderMock = vi.hoisted(() =>
    vi.fn(({ className, placeholder, value, "data-testid": testId }: MockSenderProps) => (
        <div data-testid={testId} data-placeholder={placeholder} className={className}>
            {value}
        </div>
    ))
);

vi.mock("@ant-design/x", () => ({
    Bubble: {
        List: bubbleListMock
    },
    Sender: senderMock
}));

describe("KuzhambuXChat", () => {
    it("renders bubble list with base class", () => {
        render(
            <KuzhambuXBubbleList
                testId="sample-bubble-list"
                className="sample-list"
                items={[{ key: "first", content: "第一条", role: "ai" }]}
            />
        );

        expect(screen.getByTestId("sample-bubble-list")).toHaveClass(
            "kuzhambu-x-bubble-list",
            "sample-list"
        );
        expect(screen.getByText("第一条")).toBeInTheDocument();
    });

    it("renders sender with base class", () => {
        render(
            <KuzhambuXSender
                testId="sample-sender"
                className="sample-sender"
                placeholder="发送消息"
                value="问题"
            />
        );

        expect(screen.getByTestId("sample-sender")).toHaveClass(
            "kuzhambu-x-sender",
            "sample-sender"
        );
        expect(screen.getByTestId("sample-sender")).toHaveAttribute("data-placeholder", "发送消息");
    });
});
