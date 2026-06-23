import { Button, Form, Input, Select, Space } from "antd";
import type { RefinementTaskPageQuery } from "../refinement-types";

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
    return (
        <Form
            layout="inline"
            initialValues={value}
            onFinish={(values) =>
                onChange({
                    ...value,
                    ...values,
                    pageNo: 1
                })
            }
        >
            <Form.Item label="任务类型" name="taskType">
                <Select
                    allowClear
                    style={{ width: 140 }}
                    options={[
                        { label: "图谱", value: "GRAPH" },
                        { label: "关系", value: "RELATION" },
                        { label: "世系", value: "LINEAGE" }
                    ]}
                />
            </Form.Item>
            <Form.Item label="门类编码" name="sourceCategoryCode">
                <Input allowClear placeholder="如 myth" />
            </Form.Item>
            <Form.Item label="来源类型" name="sourceContentType">
                <Input allowClear placeholder="如 SANCAI_ENTRY" />
            </Form.Item>
            <Form.Item label="状态" name="status">
                <Select
                    allowClear
                    style={{ width: 140 }}
                    options={[
                        { label: "草稿", value: "DRAFT" },
                        { label: "已应用", value: "APPLIED" }
                    ]}
                />
            </Form.Item>
            <Form.Item>
                <Space>
                    <Button htmlType="submit" loading={loading} type="primary">
                        筛选
                    </Button>
                    <Button
                        onClick={() =>
                            onChange({
                                pageNo: 1,
                                pageSize: value.pageSize
                            })
                        }
                    >
                        重置
                    </Button>
                </Space>
            </Form.Item>
        </Form>
    );
};
