import { Input } from "antd";
import { render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
    KuzhambuForm,
    KuzhambuFormHiddenItem,
    KuzhambuFormItem,
    KuzhambuFormPlaceholderItem
} from "./kuzhambu-form";
import { KUZHAMBU_FORM_ITEM_LAYOUTS, readKuzhambuFormLayoutTier } from "./kuzhambu-form-layout";

class ResizeObserverMock {
    observe = vi.fn();
    disconnect = vi.fn();
}

const mockContainerWidth = (width: number) => {
    vi.spyOn(HTMLElement.prototype, "getBoundingClientRect").mockReturnValue({
        bottom: 0,
        height: 0,
        left: 0,
        right: width,
        top: 0,
        width,
        x: 0,
        y: 0,
        toJSON: () => ({})
    });
};

describe("KuzhambuForm", () => {
    beforeEach(() => {
        if (!("ResizeObserver" in globalThis)) {
            vi.stubGlobal("ResizeObserver", ResizeObserverMock);
        }
        mockContainerWidth(1200);
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("maps layoutSize to mobile, tablet, and desktop item columns", () => {
        expect(KUZHAMBU_FORM_ITEM_LAYOUTS.small.col).toEqual({ xs: 24, md: 12, lg: 8 });
        expect(KUZHAMBU_FORM_ITEM_LAYOUTS.middle.col).toEqual({ xs: 24, md: 12, lg: 12 });
        expect(KUZHAMBU_FORM_ITEM_LAYOUTS.large.col).toEqual({ xs: 24, md: 24, lg: 24 });
    });

    it("maps container width to the active layout tier", () => {
        expect(readKuzhambuFormLayoutTier(700)).toBe("xs");
        expect(readKuzhambuFormLayoutTier(900)).toBe("md");
        expect(readKuzhambuFormLayoutTier(1200)).toBe("lg");
    });

    it("renders form items in calculated rows from desktop container width", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormItem label="短项" layoutSize="small">
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="中项" layoutSize="middle">
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="长项" layoutSize="large">
                    <Input />
                </KuzhambuFormItem>
            </KuzhambuForm>
        );

        expect(screen.getByText("短项")).toBeInTheDocument();
        expect(screen.getByText("中项")).toBeInTheDocument();
        expect(screen.getByText("长项")).toBeInTheDocument();
        expect(container.querySelectorAll(".kuzhambu-form-row")).toHaveLength(2);
        expect(container.querySelector(".kuzhambu-form-col.ant-col-8")).toBeInTheDocument();
        expect(container.querySelector(".kuzhambu-form-col.ant-col-12")).toBeInTheDocument();
        expect(container.querySelector(".kuzhambu-form-col.ant-col-24")).toBeInTheDocument();
    });

    it("uses container width rather than viewport width for compact layout", () => {
        mockContainerWidth(700);

        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormItem label="短项" layoutSize="small">
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="中项" layoutSize="middle">
                    <Input />
                </KuzhambuFormItem>
            </KuzhambuForm>
        );

        expect(container.querySelectorAll(".kuzhambu-form-row")).toHaveLength(2);
        expect(container.querySelectorAll(".kuzhambu-form-col.ant-col-24")).toHaveLength(2);
    });

    it("uses placeholder items to reserve grid slots", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormPlaceholderItem layoutSize="small" />
                <KuzhambuFormItem label="偏移项" layoutSize="small">
                    <Input />
                </KuzhambuFormItem>
            </KuzhambuForm>
        );

        expect(screen.getByText("偏移项")).toBeInTheDocument();
        expect(
            container.querySelector("[aria-hidden='true'].kuzhambu-form-col.ant-col-8")
        ).toBeInTheDocument();
        expect(container.querySelectorAll(".kuzhambu-form-col.ant-col-8")).toHaveLength(2);
    });

    it("uses fillLine placeholders to close the current calculated row", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormItem label="短项" layoutSize="small">
                    <Input />
                </KuzhambuFormItem>
                <KuzhambuFormPlaceholderItem fillLine />
                <KuzhambuFormItem label="下一行" layoutSize="small">
                    <Input />
                </KuzhambuFormItem>
            </KuzhambuForm>
        );

        expect(screen.getByText("短项")).toBeInTheDocument();
        expect(screen.getByText("下一行")).toBeInTheDocument();
        expect(container.querySelectorAll(".kuzhambu-form-row")).toHaveLength(2);
        expect(
            container.querySelector("[aria-hidden='true'].kuzhambu-form-col.ant-col-16")
        ).toBeInTheDocument();
    });

    it("does not render an empty full-line placeholder row", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormPlaceholderItem layoutSize="large" />
                <KuzhambuFormItem label="字段" layoutSize="small">
                    <Input />
                </KuzhambuFormItem>
            </KuzhambuForm>
        );

        expect(screen.getByText("字段")).toBeInTheDocument();
        expect(container.querySelectorAll(".kuzhambu-form-row")).toHaveLength(1);
        expect(
            container.querySelector("[aria-hidden='true'].kuzhambu-form-col")
        ).not.toBeInTheDocument();
    });

    it("renders placeholder children when a full-line placeholder has content", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormPlaceholderItem layoutSize="large">
                    <div>分隔内容</div>
                </KuzhambuFormPlaceholderItem>
            </KuzhambuForm>
        );

        expect(screen.getByText("分隔内容")).toBeInTheDocument();
        expect(container.querySelectorAll(".kuzhambu-form-row")).toHaveLength(1);
        expect(container.querySelector(".kuzhambu-form-col.ant-col-24")).toBeInTheDocument();
    });

    it("renders hidden fields without allocating grid columns", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormHiddenItem name="id">
                    <Input />
                </KuzhambuFormHiddenItem>
            </KuzhambuForm>
        );

        expect(container.querySelector(".kuzhambu-form-col.ant-col-12")).not.toBeInTheDocument();
    });
});
