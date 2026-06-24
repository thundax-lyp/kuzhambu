export interface TextAreaAutoSizeConfig {
    maxRows?: number;
    minRows?: number;
}

export const resolveTextAreaAutoSize = (autoSize: TextAreaAutoSizeConfig) => {
    return process.env.NODE_ENV === "test" ? undefined : autoSize;
};
