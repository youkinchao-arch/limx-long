import { useEffect, useState } from "react";
import { Card, Col, Row, Statistic, Tag, Typography, Spin } from "antd";
import {
  TeamOutlined,
  ToolOutlined,
  InboxOutlined,
  FileTextOutlined,
  ProfileOutlined,
  WarningOutlined,
} from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import api from "../api/client";

interface Summary {
  totals: Record<string, number>;
  reminders: Record<string, number> & { window_days: number };
  equipment_status: Record<string, number>;
}

export default function Dashboard() {
  const { t } = useTranslation();
  const [data, setData] = useState<Summary | null>(null);

  useEffect(() => {
    api.get<Summary>("/dashboard/summary").then((r) => setData(r.data));
  }, []);

  if (!data) return <Spin />;
  const days = data.reminders.window_days;

  const totalCards = [
    { key: "personnel", icon: <TeamOutlined />, color: "#1677ff", value: data.totals.personnel },
    { key: "equipment", icon: <ToolOutlined />, color: "#52c41a", value: data.totals.equipment },
    { key: "material", icon: <InboxOutlined />, color: "#faad14", value: data.totals.material },
    { key: "document", icon: <FileTextOutlined />, color: "#722ed1", value: data.totals.document },
    { key: "report", icon: <ProfileOutlined />, color: "#13c2c2", value: data.totals.report },
  ];

  const reminderCards = [
    { key: "qualificationExpiring", value: data.reminders.qualification_expiring },
    { key: "calibrationDue", value: data.reminders.calibration_due },
    { key: "materialExpiring", value: data.reminders.material_expiring },
    { key: "environmentAlarms", value: data.reminders.environment_alarms },
  ];

  return (
    <div>
      <Typography.Title level={4}>{t("menu.dashboard")}</Typography.Title>
      <Row gutter={[16, 16]}>
        {totalCards.map((c) => (
          <Col xs={12} sm={8} md={8} lg={4} key={c.key} style={{ flex: 1 }}>
            <Card>
              <Statistic
                title={t(`dashboard.${c.key}`)}
                value={c.value}
                prefix={<span style={{ color: c.color }}>{c.icon}</span>}
              />
            </Card>
          </Col>
        ))}
      </Row>

      <Typography.Title level={5} style={{ marginTop: 24 }}>
        <WarningOutlined style={{ color: "#faad14", marginRight: 8 }} />
        {t("dashboard.reminders")}
        <Typography.Text type="secondary" style={{ fontSize: 13, marginLeft: 8 }}>
          {t("dashboard.windowHint", { days })}
        </Typography.Text>
      </Typography.Title>
      <Row gutter={[16, 16]}>
        {reminderCards.map((c) => (
          <Col xs={12} md={6} key={c.key}>
            <Card>
              <Statistic
                title={t(`dashboard.${c.key}`)}
                value={c.value}
                valueStyle={{ color: c.value > 0 ? "#cf1322" : "#3f8600" }}
              />
            </Card>
          </Col>
        ))}
      </Row>

      <Typography.Title level={5} style={{ marginTop: 24 }}>
        {t("dashboard.equipmentStatus")}
      </Typography.Title>
      <Card>
        {Object.keys(data.equipment_status).length === 0
          ? "-"
          : Object.entries(data.equipment_status).map(([status, count]) => (
              <Tag key={status} color="blue" style={{ marginBottom: 8, fontSize: 14, padding: "4px 10px" }}>
                {t(`equipment.statusMap.${status}`, status)}: {count}
              </Tag>
            ))}
      </Card>
    </div>
  );
}
