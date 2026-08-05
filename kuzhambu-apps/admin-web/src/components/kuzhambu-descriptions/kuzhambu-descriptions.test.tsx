import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuDescriptions } from "./kuzhambu-descriptions";

describe("KuzhambuDescriptions", () => {
    it("renders label value items through the Ant Design descriptions wrapper", () => {
        render(
            <KuzhambuDescriptions
                ariaLabel="基础信息"
                items={[
                    {
                        key: "category",
                        label: "门类",
                        children: "天文"
                    }
                ]}
            />
        );

        expect(screen.getByLabelText("基础信息")).toBeInTheDocument();
        expect(screen.getByText("门类")).toBeInTheDocument();
        expect(screen.getByText("天文")).toBeInTheDocument();
    });
});
