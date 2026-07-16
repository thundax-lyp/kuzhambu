import { LockOutlined, ReloadOutlined, UserOutlined } from "@ant-design/icons";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Alert, App, Card, Form, Input, Typography } from "antd";
import { sm2 } from "sm-crypto";
import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { createLoginForm, getCaptchaUrl, refreshCaptcha } from "@/auth/auth-service";
import { loginWithPermissions } from "@/auth/auth-session-service";
import { getAccessToken } from "@/auth/token-storage";
import { KuzhambuLogo } from "@/components/kuzhambu-logo";
import { KuzhambuSpace, KuzhambuSpaceCompact } from "@/components/kuzhambu-space";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./login-page.css";

const { Title, Text, Paragraph } = Typography;

interface LoginFormValues {
    userName: string;
    password: string;
    captcha: string;
}

interface LoginLocationState {
    from?: {
        pathname?: string;
    };
}

const resolveRedirectPath = (state: unknown) => {
    const from = (state as LoginLocationState | null)?.from;
    return from?.pathname && from.pathname !== "/login" ? from.pathname : "/dashboard";
};

export const LoginPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { message: messageApi } = App.useApp();
    const [captchaVersion, setCaptchaVersion] = useState(() => Date.now());

    const loginFormQuery = useQuery({
        queryKey: ["auth", "login-form"],
        queryFn: createLoginForm
    });

    const loginForm = loginFormQuery.data;
    const captchaUrl = loginForm?.loginToken
        ? getCaptchaUrl(loginForm.loginToken, captchaVersion)
        : "";

    const refreshCaptchaMutation = useMutation({
        mutationFn: async () => {
            if (!loginForm?.loginToken) {
                return;
            }
            await refreshCaptcha(loginForm.loginToken);
        },
        onSettled: () => setCaptchaVersion(Date.now())
    });

    const loginMutation = useMutation({
        mutationFn: async (values: LoginFormValues) => {
            if (!loginForm) {
                throw new Error("登录表单未初始化");
            }

            const encryptedPassword = sm2.doEncrypt(values.password, loginForm.publicKey, 0);

            return loginWithPermissions({
                loginToken: loginForm.loginToken,
                userName: values.userName,
                password: encryptedPassword,
                captcha: values.captcha
            });
        },
        onSuccess: () => {
            messageApi.success("登录成功");
            navigate(resolveRedirectPath(location.state), { replace: true });
        },
        onError: (error) => {
            const description = error instanceof Error ? error.message : "请检查账号、密码和验证码";
            messageApi.error(description);
            refreshCaptchaMutation.mutate();
        }
    });

    if (getAccessToken()) {
        return <Navigate to={resolveRedirectPath(location.state)} replace />;
    }

    return (
        <>
            <main className="login-page">
                <section className="login-hero">
                    <KuzhambuLogo className="login-logo" />
                    <Title level={1}>KUZHAMBU WORKSPACE</Title>
                    <Paragraph>使用后台账号和验证码进入工作区。</Paragraph>
                </section>

                <Card className="login-card">
                    <KuzhambuSpace orientation="vertical" size={24} className="login-card-content">
                        <div>
                            <Title level={2}>登录</Title>
                            <Text type="secondary">请输入后台账号信息</Text>
                        </div>

                        {loginFormQuery.isError ? (
                            <Alert
                                type="error"
                                showIcon
                                title="登录表单初始化失败"
                                description="请确认后台接口服务已启动，并检查 /kuzhambu-admin-api 代理配置。"
                                action={
                                    <KuzhambuButton
                                        name="重试"
                                        size="small"
                                        onClick={() => loginFormQuery.refetch()}
                                    >
                                        重试
                                    </KuzhambuButton>
                                }
                            />
                        ) : null}

                        <Form<LoginFormValues>
                            layout="vertical"
                            requiredMark={false}
                            onFinish={(values) => loginMutation.mutate(values)}
                        >
                            <Form.Item
                                label="账号"
                                name="userName"
                                rules={[{ required: true, message: "请输入账号" }]}
                            >
                                <Input
                                    size="large"
                                    prefix={<UserOutlined />}
                                    placeholder="请输入后台账号"
                                    autoComplete="username"
                                />
                            </Form.Item>

                            <Form.Item
                                label="密码"
                                name="password"
                                rules={[{ required: true, message: "请输入密码" }]}
                            >
                                <Input.Password
                                    size="large"
                                    prefix={<LockOutlined />}
                                    placeholder="请输入密码"
                                    autoComplete="current-password"
                                />
                            </Form.Item>

                            <Form.Item
                                label="验证码"
                                name="captcha"
                                rules={[{ required: true, message: "请输入验证码" }]}
                            >
                                <KuzhambuSpaceCompact className="login-captcha-row">
                                    <Input size="large" placeholder="验证码" autoComplete="off" />
                                    <button
                                        className="login-captcha-image-button"
                                        type="button"
                                        onClick={() => refreshCaptchaMutation.mutate()}
                                        disabled={!captchaUrl || refreshCaptchaMutation.isPending}
                                        aria-label="刷新验证码"
                                    >
                                        {captchaUrl ? (
                                            <img src={captchaUrl} alt="图形验证码" />
                                        ) : (
                                            <ReloadOutlined />
                                        )}
                                    </button>
                                </KuzhambuSpaceCompact>
                            </Form.Item>

                            <KuzhambuButton
                                name="登录"
                                block
                                size="large"
                                type="primary"
                                htmlType="submit"
                                loading={loginMutation.isPending || loginFormQuery.isLoading}
                                disabled={!loginForm}
                            >
                                登录
                            </KuzhambuButton>
                        </Form>
                    </KuzhambuSpace>
                </Card>
            </main>
        </>
    );
};
