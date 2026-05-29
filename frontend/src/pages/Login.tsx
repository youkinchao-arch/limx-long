import { useState } from "react";
import { Button, Card, Form, Input, Typography, message } from "antd";
import { LockOutlined, UserOutlined, GlobalOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../auth/AuthContext";

export default function Login() {
  const { t, i18n } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      await login(values.username, values.password);
      navigate("/dashboard");
    } catch (e: any) {
      message.error(e?.response?.data?.detail || t("common.failed"));
    } finally {
      setLoading(false);
    }
  };

  const toggleLang = () => {
    const next = i18n.language === "zh" ? "en" : "zh";
    i18n.changeLanguage(next);
    localStorage.setItem("lims_lang", next);
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "linear-gradient(135deg, #1677ff 0%, #0958d9 100%)",
      }}
    >
      <Card style={{ width: 380 }}>
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <Typography.Title level={3} style={{ marginBottom: 4 }}>
            {t("app.title")}
          </Typography.Title>
          <Typography.Text type="secondary">{t("app.subtitle")}</Typography.Text>
        </div>
        <Form onFinish={onFinish} initialValues={{ username: "admin", password: "admin123" }}>
          <Form.Item name="username" rules={[{ required: true }]}>
            <Input prefix={<UserOutlined />} placeholder={t("common.username")} size="large" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true }]}>
            <Input.Password
              prefix={<LockOutlined />}
              placeholder={t("common.password")}
              size="large"
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large" loading={loading}>
              {t("common.login")}
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: "center" }}>
          <Button type="link" icon={<GlobalOutlined />} onClick={toggleLang}>
            {i18n.language === "zh" ? "English" : "中文"}
          </Button>
        </div>
      </Card>
    </div>
  );
}
