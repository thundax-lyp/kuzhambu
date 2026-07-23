import { Input } from "antd";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuForm, KuzhambuFormItem } from "./kuzhambu-form";
import { KUZHAMBU_FORM_ITEM_LAYOUTS, KUZHAMBU_FORM_ITEM_OFFSETS } from "./kuzhambu-form-layout";

describe("KuzhambuForm", () => {
    it("maps layoutSize to responsive mobile, tablet, and desktop item columns", () => {
        expect(KUZHAMBU_FORM_ITEM_LAYOUTS.small.col).toEqual({ xs: 24, md: 12, lg: 8 });
        expect(KUZHAMBU_FORM_ITEM_LAYOUTS.middle.col).toEqual({ xs: 24, md: 12, lg: 12 });
        expect(KUZHAMBU_FORM_ITEM_LAYOUTS.large.col).toEqual({ xs: 24, md: 24, lg: 24 });
    });

    it("renders form items inside the shared row container", () => {
        render(
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
    });

    it("keeps labels aligned to four grid columns across item sizes", () => {
        const smallTabletLabelSpan = KUZHAMBU_FORM_ITEM_LAYOUTS.small.col.md * 8;
        const middleTabletLabelSpan = KUZHAMBU_FORM_ITEM_LAYOUTS.middle.col.md * 8;
        const largeTabletLabelSpan = KUZHAMBU_FORM_ITEM_LAYOUTS.large.col.md * 4;
        const smallDesktopLabelSpan = KUZHAMBU_FORM_ITEM_LAYOUTS.small.col.lg * 12;
        const middleDesktopLabelSpan = KUZHAMBU_FORM_ITEM_LAYOUTS.middle.col.lg * 8;
        const largeDesktopLabelSpan = KUZHAMBU_FORM_ITEM_LAYOUTS.large.col.lg * 4;

        expect(smallTabletLabelSpan).toBe(middleTabletLabelSpan);
        expect(middleTabletLabelSpan).toBe(largeTabletLabelSpan);
        expect(smallDesktopLabelSpan).toBe(middleDesktopLabelSpan);
        expect(middleDesktopLabelSpan).toBe(largeDesktopLabelSpan);
    });

    it("keeps endOfLine as a row break after the current item", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormItem endOfLine label="备注" layoutSize="small">
                    <Input />
                </KuzhambuFormItem>
            </KuzhambuForm>
        );

        expect(screen.getByText("备注")).toBeInTheDocument();
        expect(container.querySelector(".ant-col-md-24")).toBeInTheDocument();
        expect(container.querySelector(".ant-form-item-label.ant-col-lg-12")).toBeInTheDocument();
    });

    it("maps offsetSize to outer grid offsets without changing item label layout", () => {
        const { container } = render(
            <KuzhambuForm>
                <KuzhambuFormItem label="偏移项" layoutSize="small" offsetSize="small">
                    <Input />
                </KuzhambuFormItem>
            </KuzhambuForm>
        );

        expect(KUZHAMBU_FORM_ITEM_OFFSETS.small).toEqual({ md: 12, lg: 8 });
        expect(KUZHAMBU_FORM_ITEM_OFFSETS.middle).toEqual({ md: 12, lg: 12 });
        expect(screen.getByText("偏移项")).toBeInTheDocument();
        expect(container.querySelector(".ant-col-md-offset-12")).toBeInTheDocument();
        expect(container.querySelector(".ant-col-lg-offset-8")).toBeInTheDocument();
        expect(container.querySelector(".ant-form-item-label.ant-col-lg-12")).toBeInTheDocument();
    });
});
