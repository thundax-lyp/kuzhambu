import { Button } from "antd";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import {
    KuzhambuSpace,
    KuzhambuSpaceCompact,
    type KuzhambuSpaceCompactProps,
    type KuzhambuSpaceProps
} from "./kuzhambu-space";

const acceptsKuzhambuSpaceProps: (_props: KuzhambuSpaceProps) => void = () => undefined;
const acceptsKuzhambuSpaceCompactProps: (_props: KuzhambuSpaceCompactProps) => void = () =>
    undefined;

// @ts-expect-error `direction` is deprecated in antd and forbidden in KuzhambuSpace.
acceptsKuzhambuSpaceProps({ direction: "vertical" });
// @ts-expect-error `direction` is deprecated in antd and forbidden in KuzhambuSpaceCompact.
acceptsKuzhambuSpaceCompactProps({ direction: "vertical" });

describe("KuzhambuSpace", () => {
    it("renders space with orientation props", () => {
        const { container } = render(
            <KuzhambuSpace orientation="vertical" size={12}>
                <span>甲</span>
                <span>乙</span>
            </KuzhambuSpace>
        );

        expect(screen.getByText("甲")).toBeInTheDocument();
        expect(screen.getByText("乙")).toBeInTheDocument();
        expect(container.querySelector(".ant-space-vertical")).toBeInTheDocument();
    });

    it("renders compact space for grouped actions", () => {
        const { container } = render(
            <KuzhambuSpaceCompact block>
                <Button type="primary">保存</Button>
                <Button>取消</Button>
            </KuzhambuSpaceCompact>
        );

        expect(screen.getByRole("button", { name: /保\s*存/u })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /取\s*消/u })).toBeInTheDocument();
        expect(container.querySelector(".ant-space-compact")).toBeInTheDocument();
    });
});
