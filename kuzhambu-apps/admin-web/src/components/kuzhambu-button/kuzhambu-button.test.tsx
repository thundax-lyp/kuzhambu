import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuButton } from "./kuzhambu-button";

describe("KuzhambuButton", () => {
    it("uses the required name prop as the accessible button name", () => {
        render(<KuzhambuButton name="分享">分 享</KuzhambuButton>);

        expect(screen.getByRole("button", { name: "分享" })).toBeInTheDocument();
    });
});
