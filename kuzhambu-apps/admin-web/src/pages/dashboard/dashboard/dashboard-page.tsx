import {
    AppstoreOutlined,
    AuditOutlined,
    CloudServerOutlined,
    TeamOutlined
} from "@ant-design/icons";
import { Card, Typography } from "antd";
import { KuzhambuPage } from "@/components";
import "./dashboard-page.css";

const { Title, Text } = Typography;

const metricItems = [
    {
        label: "会话",
        value: "22",
        delta: "15%",
        tone: "green",
        icon: <TeamOutlined />,
        line: "M8 54 L34 30 L58 36 L84 16 L112 20"
    },
    {
        label: "审计",
        value: "320",
        delta: "4%",
        tone: "orange",
        icon: <AuditOutlined />,
        line: "M8 58 L30 24 L56 18 L84 36 L112 10"
    },
    {
        label: "对象",
        value: "1,080",
        delta: "8%",
        tone: "violet",
        icon: <CloudServerOutlined />,
        line: "M8 62 L36 48 L62 42 L88 28 L112 12"
    }
];

const workColumns = [
    {
        title: "待办",
        count: 2,
        items: [
            ["登录巡检", "检查验证码和访问令牌状态"],
            ["菜单整理", "同步菜单层级与展示顺序"]
        ]
    },
    {
        title: "进行中",
        count: 2,
        items: [
            ["消息队列", "查看日志消费和重试情况"],
            ["对象存储", "确认上传、预览和访问状态"]
        ]
    },
    {
        title: "完成",
        count: 2,
        items: [
            ["权限同步", "刷新用户、角色和菜单权限"],
            ["审计检索", "整理操作记录和对象变更"]
        ]
    }
];

export const DashboardPage = () => {
    return (
        <KuzhambuPage
            className="dashboard-page"
            title="仪表盘"
            description="查看会话、审计、存储和今日事项。"
            actions={<Text className="dashboard-brand">Kuzhambu Workspace</Text>}
        >
            <div className="dashboard-content">
                <section className="dashboard-metrics" aria-label="核心指标">
                    {metricItems.map((metric) => (
                        <Card
                            className={`dashboard-metric-card dashboard-metric-card-${metric.tone}`}
                            key={metric.label}
                        >
                            <div className="dashboard-metric-card-heading">
                                <Text>{metric.label}</Text>
                                <span>{metric.icon}</span>
                            </div>
                            <div className="dashboard-metric-card-body">
                                <div>
                                    <strong>{metric.value}</strong>
                                    <span className="dashboard-metric-delta">{metric.delta}</span>
                                    <p>较上周</p>
                                </div>
                                <svg
                                    viewBox="0 0 120 72"
                                    role="img"
                                    aria-label={`${metric.label}趋势`}
                                >
                                    <path
                                        className="dashboard-metric-chart-fill"
                                        d={`${metric.line} L112 72 L8 72 Z`}
                                    />
                                    <path className="dashboard-metric-chart-line" d={metric.line} />
                                </svg>
                            </div>
                        </Card>
                    ))}
                </section>

                <section className="dashboard-campaign-board">
                    <div className="dashboard-section-title-row">
                        <Title level={3}>今日治理事项</Title>
                        <button type="button">任务台</button>
                    </div>
                    <div className="dashboard-operation-columns">
                        {workColumns.map((column) => (
                            <div className="dashboard-operation-column" key={column.title}>
                                <Text>
                                    {column.title} <span>{column.count}</span>
                                </Text>
                                {column.items.map(([title, description]) => (
                                    <article className="dashboard-operation-card" key={title}>
                                        <div className="dashboard-operation-card-icon">
                                            <AppstoreOutlined />
                                        </div>
                                        <strong>{title}</strong>
                                        <p>{description}</p>
                                        <div className="dashboard-operation-progress">
                                            <span />
                                        </div>
                                    </article>
                                ))}
                                <button className="dashboard-add-operation" type="button">
                                    + 新建
                                </button>
                            </div>
                        ))}
                    </div>
                </section>
            </div>
        </KuzhambuPage>
    );
};
