import { Select } from "antd";
import type { SelectProps } from "antd";
import type { BaseOptionType, DefaultOptionType } from "antd/es/select";

import "./kuzhambu-select.css";

const joinClassNames = (...classNames: Array<string | false | null | undefined>) => {
    return classNames.filter(Boolean).join(" ");
};

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
> & {
    controlClassName?: string;
};

// AI NOTE: This wrapper keeps searchable selects filtering by option label.
// Keep option construction, loading states, and domain validation in callers.
export const KuzhambuSelect = <
    ValueType = string,
    OptionType extends BaseOptionType | DefaultOptionType = DefaultOptionType
>({
    className,
    controlClassName,
    ...props
}: KuzhambuSelectProps<ValueType, OptionType>) => {
    return (
        <Select<ValueType, OptionType>
            className={joinClassNames(
                className,
                controlClassName && "kuzhambu-select-control-surface",
                controlClassName
            )}
            {...props}
            optionFilterProp="label"
        />
    );
};
