import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuAlert } from "./kuzhambu-alert";

describe("KuzhambuAlert", () => {
    it("uses title as the visible alert message", () => {
        render(<KuzhambuAlert title="操作失败" type="error" />);

        expect(screen.getByText("操作失败")).toBeInTheDocument();
    });
});
