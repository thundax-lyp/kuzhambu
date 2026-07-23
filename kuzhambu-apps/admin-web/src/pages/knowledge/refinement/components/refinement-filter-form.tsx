import { Form, Input, Select } from "antd";
import { useEffect } from "react";
import { KuzhambuForm, KuzhambuFormItem } from "@/components/kuzhambu-form";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { RefinementTaskPageQuery } from "../refinement-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface RefinementFilterFormProps {
    loading?: boolean;
    value: RefinementTaskPageQuery;
    onChange: (nextValue: RefinementTaskPageQuery) => void;
}

export const RefinementFilterForm = ({
    loading = false,
    value,
    onChange
}: RefinementFilterFormProps) => {
    const [form] = Form.useForm<RefinementTaskPageQuery>();
    const resetValues: RefinementTaskPageQuery = {
        pageNo: 1,
        pageSize: value.pageSize
    };

    useEffect(() => {
        form.setFieldsValue(value);
    }, [form, value]);

    return (
        <KuzhambuForm
            form={form}
            initialValues={value}
            onFinish={(values) =>
                onChange({
                    ...value,
                    ...values,
                    pageNo: 1
                })
            }
        >
            <KuzhambuFormItem label="任务类型" name="taskType" layoutSize="small">
                <Select
                    allowClear
                    options={[
                        { label: "图谱", value: "GRAPH" },
                        { label: "关系", value: "RELATION" },
                        { label: "世系", value: "LINEAGE" }
                    ]}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="门类编码" name="sourceCategoryCode" layoutSize="small">
                <Input allowClear placeholder="如 myth" />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="来源类型" name="sourceContentType" layoutSize="small">
                <Input allowClear placeholder="如 SANCAI_ENTRY" />
            </KuzhambuFormItem>
            <KuzhambuFormItem label="状态" name="status" layoutSize="small">
                <Select
                    allowClear
                    options={[
                        { label: "草稿", value: "DRAFT" },
                        { label: "已应用", value: "APPLIED" }
                    ]}
                />
            </KuzhambuFormItem>
            <KuzhambuFormItem layoutSize="large">
                <KuzhambuSpace>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-filter-filter-button"
                        htmlType="submit"
                        loading={loading}
                        type="primary"
                    >
                        筛选
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-refinement-refinement-filter-reset-button"
                        onClick={() => {
                            form.setFieldsValue({
                                taskType: undefined,
                                sourceCategoryCode: undefined,
                                sourceContentType: undefined,
                                status: undefined
                            });
                            onChange(resetValues);
                        }}
                    >
                        重置
                    </KuzhambuButton>
                </KuzhambuSpace>
            </KuzhambuFormItem>
        </KuzhambuForm>
    );
};
