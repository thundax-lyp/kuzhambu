export type IdValue = string | number | null | undefined;

const POSITIVE_DECIMAL_ID_PATTERN = /^[1-9]\d*$/;

export const normalizeId = (id: IdValue) => (id === null || id === undefined ? "" : String(id));

export const normalizeNullableId = (id: IdValue) => {
    const normalized = normalizeId(id);
    return normalized || null;
};

export const isSameId = (left: IdValue, right: IdValue) => normalizeId(left) === normalizeId(right);

export const isPositiveDecimalId = (id: IdValue) => {
    return POSITIVE_DECIMAL_ID_PATTERN.test(normalizeId(id).trim());
};
