import type { Dayjs } from "dayjs";
import type { AiInvocationLogPageQuery, AiInvocationSummaryQuery } from "./invocations-service";

export const INVOCATION_DATE_TIME_FORMAT = "YYYYMMDD HH:mm";

export type InvocationDateRangeValue = [Dayjs | null, Dayjs | null] | null;

export type InvocationSummaryFilterValues = AiInvocationSummaryQuery & {
    period?: InvocationDateRangeValue;
};

export type InvocationLogFilterValues = AiInvocationLogPageQuery & {
    requestedAt?: InvocationDateRangeValue;
};
