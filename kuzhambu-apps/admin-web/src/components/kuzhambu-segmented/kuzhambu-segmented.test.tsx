import type { SegmentedProps } from "antd";
import { render, screen } from "@testing-library/react";
import { KuzhambuSegmented } from "./kuzhambu-segmented";

type MockSegmentedProps = Pick<SegmentedProps<string>, "className" | "options" | "value"> & {
    "data-testid"?: string;
};

const segmentedMock = vi.hoisted(() =>
    vi.fn(({ className, options = [], value, "data-testid": testId }: MockSegmentedProps) => (
        <div data-testid={testId} data-value={value} className={className}>
            {options.map((option) => {
                const optionRecord = typeof option === "object" ? option : { label: option };
                return <span key={String(optionRecord.label)}>{optionRecord.label}</span>;
            })}
        </div>
    ))
);

vi.mock("antd", async (importOriginal) => {
    const actual = await importOriginal<typeof import("antd")>();
    return {
        ...actual,
        Segmented: segmentedMock
    };
});

describe("KuzhambuSegmented", () => {
    it("renders segmented with test id and base class", () => {
        render(
            <KuzhambuSegmented
                testId="sample-segmented"
                className="sample-class"
                options={[{ label: "统计摘要", value: "summary" }]}
                value="summary"
            />
        );

        expect(screen.getByTestId("sample-segmented")).toHaveClass(
            "kuzhambu-segmented",
            "sample-class"
        );
        expect(screen.getByTestId("sample-segmented")).toHaveAttribute("data-value", "summary");
        expect(screen.getByText("统计摘要")).toBeInTheDocument();
    });
});
