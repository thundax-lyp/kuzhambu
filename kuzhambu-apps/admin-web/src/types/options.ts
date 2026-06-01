export interface OptionRecord {
    value: string;
    label: string;
}

export type OptionsRecord<TKey extends string = string> = Record<TKey, OptionRecord[]>;
