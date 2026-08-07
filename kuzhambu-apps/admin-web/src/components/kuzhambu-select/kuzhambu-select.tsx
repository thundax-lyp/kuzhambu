import { Select } from "antd";
import type { SelectProps } from "antd";
import type { BaseOptionType, DefaultOptionType } from "antd/es/select";

export type KuzhambuSelectProps<
    ValueType = string,
    OptionType extends BaseOptionType | DefaultOptionType = DefaultOptionType
> = Omit<
    SelectProps<ValueType, OptionType>,
    | "bordered"
    | "dropdownClassName"
    | "dropdownMatchSelectWidth"
    | "dropdownRender"
    | "dropdownStyle"
    | "onDropdownVisibleChange"
    | "optionFilterProp"
    | "popupClassName"
    | "showArrow"
>;

// AI NOTE: This wrapper keeps searchable selects filtering by option label.
// Keep option construction, loading states, and domain validation in callers.
export const KuzhambuSelect = <
    ValueType = string,
    OptionType extends BaseOptionType | DefaultOptionType = DefaultOptionType
>({
    ...props
}: KuzhambuSelectProps<ValueType, OptionType>) => {
    return <Select<ValueType, OptionType> {...props} optionFilterProp="label" />;
};
