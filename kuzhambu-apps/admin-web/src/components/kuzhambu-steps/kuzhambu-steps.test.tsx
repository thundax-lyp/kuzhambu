import { render, screen } from "@testing-library/react";
import { KuzhambuSteps } from "./kuzhambu-steps";

describe("KuzhambuSteps", () => {
    it("renders steps with test id", () => {
        render(
            <KuzhambuSteps
                testId="sample-steps"
                current={1}
                items={[{ title: "选择图片" }, { title: "图片理解" }, { title: "生图" }]}
            />
        );

        expect(screen.getByTestId("sample-steps")).toBeInTheDocument();
        expect(screen.getByText("选择图片")).toBeInTheDocument();
        expect(screen.getByText("图片理解")).toBeInTheDocument();
        expect(screen.getByText("生图")).toBeInTheDocument();
    });
});
