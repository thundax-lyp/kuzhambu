import { Form, Input, Pagination } from "antd";
import { useEffect } from "react";
import {
    KuzhambuButton,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuFormPlaceholderItem,
    KuzhambuSelect,
    KuzhambuSpace
} from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from "@/types/page";
import type { GraphExtractionTaskPageQuery } from "../graph-extraction-service";
import type {
    GraphContentRefRecord,
    GraphContentType,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "../graph-extraction-types";

interface TaskFiltersProps {
    loading?: boolean;
    query: GraphExtractionTaskPageQuery;
    total: number;
    onChange: (nextQuery: GraphExtractionTaskPageQuery) => void;
}

interface TaskFilterFormValues {
    batchId?: string;
    categoryCode?: string;
    contentRefsText?: string;
    contentType?: GraphContentType;
    disposition?: GraphTaskDisposition;
    executionStatus?: GraphTaskExecutionStatus;
    keyword?: string;
    volumeCode?: string;
}

const CONTENT_TYPE_OPTIONS = [
    { label: "三才图会", value: "SANCAI_ENTRY" },
    { label: "王圻文献", value: "WANGQI_DOCUMENT" },
    { label: "明代民俗", value: "MING_CUSTOMS" }
];

const EXECUTION_STATUS_OPTIONS = [
    { label: "待执行", value: "PENDING" },
    { label: "运行中", value: "RUNNING" },
    { label: "已成功", value: "SUCCEEDED" },
    { label: "已失败", value: "FAILED" },
    { label: "已取消", value: "CANCELLED" }
];

const DISPOSITION_OPTIONS = [
    { label: "待采纳", value: "PENDING" },
    { label: "合并采纳", value: "ADOPTED_MERGE" },
    { label: "替换采纳", value: "ADOPTED_REPLACE" },
    { label: "已丢弃", value: "DISCARDED" },
    { label: "已替代", value: "SUPERSEDED" }
];

const normalizeText = (value?: string) => {
    const text = value?.trim();
    return text || undefined;
};

const toContentRefsText = (contentRefs?: GraphContentRefRecord[]) =>
    contentRefs?.map((ref) => `${ref.contentType}:${ref.contentRefId}`).join(", ") || "";

const parseContentRefsText = (value?: string): GraphContentRefRecord[] | undefined => {
    const refs = normalizeText(value)
        ?.split(/[\n,，]+/u)
        .map((item) => item.trim())
        .filter(Boolean)
        .map((item) => {
            const [contentType, ...idParts] = item.split(":");
            return {
                contentType: contentType?.trim(),
                contentRefId: idParts.join(":").trim()
            };
        })
        .filter((ref) => ref.contentType && ref.contentRefId);

    return refs?.length ? (refs as GraphContentRefRecord[]) : undefined;
};

const compactQuery = (query: GraphExtractionTaskPageQuery): GraphExtractionTaskPageQuery =>
    Object.fromEntries(
        Object.entries(query).filter(([, value]) => value !== undefined)
    ) as GraphExtractionTaskPageQuery;

const toFormValues = (query: GraphExtractionTaskPageQuery): TaskFilterFormValues => ({
    batchId: query.batchId,
    categoryCode: query.categoryCode,
    contentRefsText: toContentRefsText(query.contentRefs),
    contentType: query.contentType,
    disposition: query.disposition,
    executionStatus: query.executionStatus,
    keyword: query.keyword,
    volumeCode: query.volumeCode
});

const toFilterQuery = (
    values: TaskFilterFormValues,
    query: GraphExtractionTaskPageQuery
): GraphExtractionTaskPageQuery =>
    compactQuery({
        ...query,
        batchId: normalizeText(values.batchId),
        categoryCode: normalizeText(values.categoryCode),
        contentRefs: parseContentRefsText(values.contentRefsText),
        contentType: values.contentType,
        disposition: values.disposition,
        executionStatus: values.executionStatus,
        keyword: normalizeText(values.keyword),
        pageNo: DEFAULT_PAGE_NO,
        volumeCode: normalizeText(values.volumeCode)
    });

export const TaskFilters = ({ loading = false, query, total, onChange }: TaskFiltersProps) => {
    const [form] = Form.useForm<TaskFilterFormValues>();
    const currentPageNo = query.pageNo ?? DEFAULT_PAGE_NO;
    const currentPageSize = query.pageSize ?? DEFAULT_PAGE_SIZE;

    useEffect(() => {
        form.setFieldsValue(toFormValues(query));
    }, [form, query]);

    const resetFilters = () => {
        form.resetFields();
        onChange(
            compactQuery({
                groupBy: query.groupBy,
                pageNo: DEFAULT_PAGE_NO,
                pageSize: currentPageSize
            })
        );
    };

    return (
        <KuzhambuForm<TaskFilterFormValues>
            form={form}
            initialValues={toFormValues(query)}
            itemGap="compact"
            onFinish={(values) => onChange(toFilterQuery(values, query))}
        >
            <KuzhambuFormItem label="关键字" name="keyword" layoutSize="small">
                <Input allowClear placeholder="标题、摘要或任务号" />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="来源类型" name="contentType" layoutSize="small">
                <KuzhambuSelect allowClear options={CONTENT_TYPE_OPTIONS} />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="分类" name="categoryCode" layoutSize="small">
                <Input allowClear placeholder="分类编码" />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="卷目" name="volumeCode" layoutSize="small">
                <Input allowClear placeholder="卷目编码" />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="素材引用" name="contentRefsText" layoutSize="middle">
                <Input allowClear placeholder="SANCAI_ENTRY:1001, SANCAI_ENTRY:1002" />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="批次号" name="batchId" layoutSize="small">
                <Input allowClear placeholder="batch-001" />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="运行状态" name="executionStatus" layoutSize="small">
                <KuzhambuSelect allowClear options={EXECUTION_STATUS_OPTIONS} />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="采纳状态" name="disposition" layoutSize="small">
                <KuzhambuSelect allowClear options={DISPOSITION_OPTIONS} />
            </KuzhambuFormItem>
            <KuzhambuFormPlaceholderItem fillLine>
                <KuzhambuSpace>
                    <KuzhambuButton
                        testId="knowledge-graph-extraction-task-filter-submit-button"
                        htmlType="submit"
                        loading={loading}
                        type="primary"
                    >
                        查询
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-graph-extraction-task-filter-reset-button"
                        onClick={resetFilters}
                    >
                        重置
                    </KuzhambuButton>
                </KuzhambuSpace>
            </KuzhambuFormPlaceholderItem>
            <KuzhambuFormPlaceholderItem fillLine>
                <Pagination
                    current={currentPageNo}
                    pageSize={currentPageSize}
                    pageSizeOptions={PAGE_SIZE_OPTIONS}
                    showSizeChanger
                    showTotal={(count) => `共 ${count} 个任务`}
                    total={total}
                    onChange={(pageNo, pageSize) =>
                        onChange(
                            compactQuery({
                                ...query,
                                pageNo,
                                pageSize
                            })
                        )
                    }
                />
            </KuzhambuFormPlaceholderItem>
        </KuzhambuForm>
    );
};
