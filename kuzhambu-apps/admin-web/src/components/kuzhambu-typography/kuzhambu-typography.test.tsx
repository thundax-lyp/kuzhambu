import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import {
    KuzhambuParagraph,
    KuzhambuText,
    KuzhambuTitle,
    KuzhambuTypographyLink
} from "./kuzhambu-typography";

describe("KuzhambuTypography", () => {
    it("keeps stable wrapper classes and caller class names", () => {
        render(
            <>
                <KuzhambuText className="custom-text">text</KuzhambuText>
                <KuzhambuTitle className="custom-title" level={3}>
                    title
                </KuzhambuTitle>
                <KuzhambuParagraph className="custom-paragraph">paragraph</KuzhambuParagraph>
                <KuzhambuTypographyLink className="custom-link">link</KuzhambuTypographyLink>
            </>
        );

        expect(screen.getByText("text")).toHaveClass("kuzhambu-typography-text", "custom-text");
        expect(screen.getByText("title")).toHaveClass("kuzhambu-typography-title", "custom-title");
        expect(screen.getByText("paragraph")).toHaveClass(
            "kuzhambu-typography-paragraph",
            "custom-paragraph"
        );
        expect(screen.getByText("link")).toHaveClass("kuzhambu-typography-link", "custom-link");
    });
});
