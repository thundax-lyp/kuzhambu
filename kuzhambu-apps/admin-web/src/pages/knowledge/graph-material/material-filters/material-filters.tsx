import { Form, Input, Pagination } from "antd";
import { useEffect } from "react";
import {
    KuzhambuButton,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSelect,
    KuzhambuSpace
} from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE, PAGE_SIZE_OPTIONS } from "@/types/page";
import type { GraphMaterialPageQuery } from "../graph-material-service";

interface MaterialFiltersProps {
    loading?: boolean;
    totalCount?: number;
    value: GraphMaterialPageQuery;
    onChange: (nextValue: GraphMaterialPageQuery) => void;
}

type MaterialFilterValues = Omit<GraphMaterialPageQuery, "pageNo" | "pageSize">;

const SOURCE_TYPE_OPTIONS = [
    { label: "三才图会条目", value: "SANCAI_ENTRY" },
    { label: "王祺文档", value: "WANGQI_DOCUMENT" },
    { label: "明代风俗", value: "MING_CUSTOMS" }
];

const MATERIAL_STATUS_OPTIONS = [
    { label: "草稿", value: "DRAFT" },
    { label: "发布中", value: "PUBLISHING" },
    { label: "已发布", value: "PUBLISHED" },
    { label: "撤回中", value: "WITHDRAWING" },
    { label: "失败", value: "FAILED" }
];

const TASK_EXECUTION_STATUS_OPTIONS = [
    { label: "待执行", value: "PENDING" },
    { label: "运行中", value: "RUNNING" },
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "已取消", value: "CANCELLED" }
];

const TASK_DISPOSITION_OPTIONS = [
    { label: "待采纳", value: "PENDING" },
    { label: "已合并采纳", value: "ADOPTED_MERGE" },
    { label: "已替换采纳", value: "ADOPTED_REPLACE" },
    { label: "已丢弃", value: "DISCARDED" },
    { label: "已被替代", value: "SUPERSEDED" }
];

const normalizeText = (value?: string) => {
    const trimmedValue = value?.trim();
    return trimmedValue ? trimmedValue : undefined;
};

const normalizeFilters = (values: MaterialFilterValues): MaterialFilterValues => {
    const filters: MaterialFilterValues = {
        categoryCode: normalizeText(values.categoryCode),
        contentType: values.contentType,
        keyword: normalizeText(values.keyword),
        status: values.status,
        taskDisposition: values.taskDisposition,
        taskExecutionStatus: values.taskExecutionStatus,
        volumeCode: normalizeText(values.volumeCode)
    };
    return Object.fromEntries(
        Object.entries(filters).filter(([, filterValue]) => filterValue !== undefined)
    ) as MaterialFilterValues;
};

export const MaterialFilters = ({
    loading = false,
    totalCount = 0,
    value,
    onChange
}: MaterialFiltersProps) => {
    const [form] = Form.useForm<MaterialFilterValues>();
    const currentPageNo = value.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = value.pageSize || DEFAULT_PAGE_SIZE;

    useEffect(() => {
        form.setFieldsValue(value);
    }, [form, value]);

    const updateFilters = (values: MaterialFilterValues) => {
        onChange({
            ...value,
            ...normalizeFilters(values),
            pageNo: DEFAULT_PAGE_NO,
            pageSize: currentPageSize
        });
    };

    const resetFilters = () => {
        const resetValue: GraphMaterialPageQuery = {
            pageNo: DEFAULT_PAGE_NO,
            pageSize: currentPageSize
        };
        form.resetFields();
        onChange(resetValue);
    };

    return (
        <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
            <KuzhambuForm form={form} initialValues={value} onFinish={updateFilters}>
                <KuzhambuFormItem label="关键字" name="keyword" layoutSize="middle">
                    <Input allowClear placeholder="素材标题或正文关键字" />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="来源类型" name="contentType" layoutSize="small">
                    <KuzhambuSelect allowClear options={SOURCE_TYPE_OPTIONS} />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="分类" name="categoryCode" layoutSize="small">
                    <Input allowClear placeholder="分类编码" />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="卷目" name="volumeCode" layoutSize="small">
                    <Input allowClear placeholder="卷目编码" />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="素材状态" name="status" layoutSize="small">
                    <KuzhambuSelect allowClear options={MATERIAL_STATUS_OPTIONS} />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    label="任务运行状态"
                    name="taskExecutionStatus"
                    layoutSize="small"
                >
                    <KuzhambuSelect allowClear options={TASK_EXECUTION_STATUS_OPTIONS} />
                </KuzhambuFormItem>
                <KuzhambuFormItem label="任务采纳状态" name="taskDisposition" layoutSize="small">
                    <KuzhambuSelect allowClear options={TASK_DISPOSITION_OPTIONS} />
                </KuzhambuFormItem>
                <KuzhambuFormItem layoutSize="middle">
                    <KuzhambuSpace>
                        <KuzhambuButton
                            testId="knowledge-graph-material-filter-submit-button"
                            htmlType="submit"
                            loading={loading}
                            type="primary"
                        >
                            筛选
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="knowledge-graph-material-filter-reset-button"
                            onClick={resetFilters}
                        >
                            重置
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </KuzhambuFormItem>
            </KuzhambuForm>
            <Pagination
                aria-label="图谱素材分页"
                current={currentPageNo}
                pageSize={currentPageSize}
                pageSizeOptions={PAGE_SIZE_OPTIONS}
                showSizeChanger
                total={totalCount}
                onChange={(pageNo, pageSize) =>
                    onChange({
                        ...value,
                        pageNo,
                        pageSize
                    })
                }
            />
        </KuzhambuSpace>
    );
};
