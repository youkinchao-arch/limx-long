import { useState } from "react";
import { Layout, Menu, Dropdown, Avatar, Button, Space, Typography } from "antd";
import {
  DashboardOutlined,
  TeamOutlined,
  ToolOutlined,
  CalendarOutlined,
  InboxOutlined,
  FileTextOutlined,
  CloudOutlined,
  ExperimentOutlined,
  ProfileOutlined,
  SettingOutlined,
  UserOutlined,
  GlobalOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../auth/AuthContext";

const { Header, Sider, Content } = Layout;

export default function MainLayout() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  const menuItems = [
    { key: "/dashboard", icon: <DashboardOutlined />, label: t("menu.dashboard") },
    { key: "/personnel", icon: <TeamOutlined />, label: t("menu.personnel") },
    { key: "/equipment", icon: <ToolOutlined />, label: t("menu.equipment") },
    { key: "/resources", icon: <CalendarOutlined />, label: t("menu.resource") },
    {
      key: "warehouse",
      icon: <InboxOutlined />,
      label: t("menu.warehouse"),
      children: [
        { key: "/warehouse/materials", label: t("menu.materials") },
        { key: "/warehouse/suppliers", label: t("menu.suppliers") },
      ],
    },
    { key: "/documents", icon: <FileTextOutlined />, label: t("menu.document") },
    { key: "/environment", icon: <CloudOutlined />, label: t("menu.environment") },
    { key: "/methods", icon: <ExperimentOutlined />, label: t("menu.method") },
    { key: "/reports", icon: <ProfileOutlined />, label: t("menu.report") },
    {
      key: "system",
      icon: <SettingOutlined />,
      label: t("menu.system"),
      children: [{ key: "/system/users", label: t("menu.users") }],
    },
  ];

  const toggleLang = () => {
    const next = i18n.language === "zh" ? "en" : "zh";
    i18n.changeLanguage(next);
    localStorage.setItem("lims_lang", next);
  };

  const selectedKey =
    menuItems
      .flatMap((m) => ("children" in m && m.children ? m.children : [m]))
      .map((m) => m.key)
      .filter((k) => location.pathname.startsWith(k))
      .sort((a, b) => b.length - a.length)[0] || location.pathname;

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed} theme="dark">
        <div
          style={{
            height: 56,
            margin: 12,
            color: "#fff",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontWeight: 600,
            fontSize: collapsed ? 14 : 16,
          }}
        >
          {collapsed ? "LIMS" : t("app.title")}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: "#fff",
            padding: "0 24px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <Typography.Text type="secondary">{t("app.subtitle")}</Typography.Text>
          <Space size="large">
            <Button type="text" icon={<GlobalOutlined />} onClick={toggleLang}>
              {i18n.language === "zh" ? "EN" : "中文"}
            </Button>
            <Dropdown
              menu={{
                items: [
                  {
                    key: "logout",
                    icon: <LogoutOutlined />,
                    label: t("common.logout"),
                    onClick: logout,
                  },
                ],
              }}
            >
              <Space style={{ cursor: "pointer" }}>
                <Avatar icon={<UserOutlined />} />
                {user?.full_name}
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content style={{ margin: 24 }}>
          <div style={{ background: "#fff", padding: 24, borderRadius: 8, minHeight: "100%" }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
