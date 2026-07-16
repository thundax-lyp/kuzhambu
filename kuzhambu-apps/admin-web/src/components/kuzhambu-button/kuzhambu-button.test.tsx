import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuButton } from "./kuzhambu-button";

describe("KuzhambuButton", () => {
    it("uses the required testId prop as the technical anchor", () => {
        render(<KuzhambuButton testId="kuzhambu-button-share-button">分享</KuzhambuButton>);

        expect(screen.getByTestId("kuzhambu-button-share-button")).toBeInTheDocument();
    });

    it("maps ariaLabel to the accessible button name when business semantics need it", () => {
        render(
            <KuzhambuButton ariaLabel="分享条目" testId="kuzhambu-button-share-entry-button">
                分享
            </KuzhambuButton>
        );

        expect(screen.getByRole("button", { name: "分享条目" })).toBeInTheDocument();
    });
});
