import { Card, Col, DatePicker, Form, Row, Select } from "antd";
import type { FormInstance } from "antd";
import type { Dayjs } from "dayjs";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { AiCallRecordPageQuery, AiInvocationSummaryQuery } from "../invocations-service";

const { RangePicker } = DatePicker;
const DATE_TIME_FORMAT = "YYYYMMDD HH:mm";

export type InvocationDateRangeValue = [Dayjs | null, Dayjs | null] | null;
export type InvocationSummaryFilterValues = AiInvocationSummaryQuery & {
    period?: InvocationDateRangeValue;
};
export type InvocationCallsFilterValues = AiCallRecordPageQuery & {
    requestedAt?: InvocationDateRangeValue;
};

const STATUS_OPTIONS = [
    { label: "成功", value: "SUCCEEDED" },
    { label: "失败", value: "FAILED" },
    { label: "待处理", value: "PENDING" },
    { label: "运行中", value: "RUNNING" }
];

interface InvocationFilterPanelProps {
    callsForm: FormInstance<InvocationCallsFilterValues>;
    capabilityOptions: Array<{ label: string; value: string }>;
    summaryForm: FormInstance<InvocationSummaryFilterValues>;
    summaryInitialValues: InvocationSummaryFilterValues;
    type: "calls" | "summary";
    onRefreshSummary: () => void;
    onResetCalls: () => void;
    onSearchCalls: () => void;
}

export const InvocationFilterPanel = ({
    callsForm,
    capabilityOptions,
    summaryForm,
    summaryInitialValues,
    type,
    onRefreshSummary,
    onResetCalls,
    onSearchCalls
}: InvocationFilterPanelProps) => {
    if (type === "summary") {
        return (
            <Card className="invocations-filter-card">
                <Form
                    form={summaryForm}
                    className="invocations-filter-form"
                    initialValues={summaryInitialValues}
                >
                    <Row gutter={12}>
                        <Col xs={24} md={12} lg={8}>
                            <Form.Item label="周期" name="period">
                                <RangePicker
                                    aria-label="周期"
                                    format={DATE_TIME_FORMAT}
                                    showTime
                                    style={{ width: "100%" }}
                                />
                            </Form.Item>
                        </Col>
                        <Col xs={24} sm={12} md={8} lg={6}>
                            <Form.Item label="统计粒度" name="bucketType">
                                <Select
                                    className="invocations-filter-control"
                                    options={[
                                        { label: "按天", value: "DAY" },
                                        { label: "按小时", value: "HOUR" }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col xs={24} sm={12} md={8} lg={6}>
                            <Form.Item label="能力" name="capability">
                                <Select
                                    allowClear
                                    className="invocations-filter-control"
                                    options={capabilityOptions}
                                />
                            </Form.Item>
                        </Col>
                        <Col xs={24} sm={12} md={8} lg={4}>
                            <Form.Item>
                                <KuzhambuButton
                                    testId="ai-invocations-invocations-refresh-button-2"
                                    type="primary"
                                    onClick={onRefreshSummary}
                                >
                                    刷新
                                </KuzhambuButton>
                            </Form.Item>
                        </Col>
                    </Row>
                </Form>
            </Card>
        );
    }

    return (
        <Form form={callsForm} className="invocations-filter-form">
            <Row gutter={12}>
                <Col xs={24} sm={12} md={8} lg={6}>
                    <Form.Item label="状态" name="status">
                        <Select
                            allowClear
                            className="invocations-filter-control"
                            options={STATUS_OPTIONS}
                        />
                    </Form.Item>
                </Col>
                <Col xs={24} md={12} lg={8}>
                    <Form.Item label="请求时间" name="requestedAt">
                        <RangePicker
                            aria-label="请求时间"
                            format={DATE_TIME_FORMAT}
                            showTime
                            style={{ width: "100%" }}
                        />
                    </Form.Item>
                </Col>
                <Col xs={24} sm={12} md={8} lg={6}>
                    <Form.Item>
                        <KuzhambuSpace>
                            <KuzhambuButton
                                testId="ai-invocations-invocations-query-button"
                                type="primary"
                                onClick={onSearchCalls}
                            >
                                查询
                            </KuzhambuButton>
                            <KuzhambuButton
                                testId="ai-invocations-invocations-reset-button"
                                onClick={onResetCalls}
                            >
                                重置
                            </KuzhambuButton>
                        </KuzhambuSpace>
                    </Form.Item>
                </Col>
            </Row>
        </Form>
    );
};
