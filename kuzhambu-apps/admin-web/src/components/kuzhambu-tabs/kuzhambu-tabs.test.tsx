import { render, screen } from "@testing-library/react";
import { KuzhambuTabs } from "./kuzhambu-tabs";

describe("KuzhambuTabs", () => {
    it("renders tabs with test id", () => {
        render(
            <KuzhambuTabs
                testId="sample-tabs"
                items={[
                    {
                        key: "first",
                        label: "第一个",
                        children: <div>第一个内容</div>
                    }
                ]}
            />
        );

        expect(screen.getByTestId("sample-tabs")).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "第一个" })).toBeInTheDocument();
        expect(screen.getByText("第一个内容")).toBeInTheDocument();
    });
});
