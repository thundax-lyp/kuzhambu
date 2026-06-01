export const DEFAULT_PAGE_NO = 1;
export const DEFAULT_PAGE_SIZE = 20;
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

export type PageQuery<TQuery extends object = Record<string, never>> = TQuery & {
    pageNo?: number;
    pageSize?: number;
};

export interface Page<TRecord> {
    pageNo: number;
    pageSize: number;
    totalPage: number;
    count: number;
    totalCount?: number;
    records: TRecord[];
}
