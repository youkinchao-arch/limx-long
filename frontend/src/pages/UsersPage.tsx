import { useCallback, useEffect, useState } from "react";
import {
  Button,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Typography,
  message,
} from "antd";
import { PlusOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import api from "../api/client";
import { useAuth, type CurrentUser, type Role } from "../auth/AuthContext";

export default function UsersPage() {
  const { t } = useTranslation();
  const { hasPermission } = useAuth();
  const [users, setUsers] = useState<CurrentUser[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();
  const canWrite = hasPermission("user:write");

  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const [u, r] = await Promise.all([
        api.get<CurrentUser[]>("/auth/users"),
        api.get<Role[]>("/auth/roles"),
      ]);
      setUsers(u.data);
      setRoles(r.data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const submit = async () => {
    const values = await form.validateFields();
    try {
      await api.post("/auth/users", values);
      message.success(t("common.success"));
      setOpen(false);
      form.resetFields();
      fetchAll();
    } catch (e: any) {
      message.error(e?.response?.data?.detail || t("common.failed"));
    }
  };

  const columns = [
    { title: t("common.username"), dataIndex: "username", key: "username" },
    { title: t("user.fullName"), dataIndex: "full_name", key: "full_name" },
    { title: "Email", dataIndex: "email", key: "email", render: (v: string) => v || "-" },
    {
      title: t("user.roles"),
      dataIndex: "roles",
      key: "roles",
      render: (rs: Role[]) => rs.map((r) => <Tag key={r.id} color="blue">{r.name}</Tag>),
    },
    {
      title: t("user.active"),
      dataIndex: "is_active",
      key: "is_active",
      render: (v: boolean) => (
        <Tag color={v ? "green" : "red"}>{v ? t("common.yes") : t("common.no")}</Tag>
      ),
    },
  ];

  return (
    <div>
      <Typography.Title level={4}>{t("menu.users")}</Typography.Title>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} disabled={!canWrite} onClick={() => setOpen(true)}>
          {t("common.create")}
        </Button>
      </Space>
      <Table rowKey="id" loading={loading} dataSource={users} columns={columns} />

      <Modal
        title={t("common.create")}
        open={open}
        onOk={submit}
        onCancel={() => setOpen(false)}
        okText={t("common.save")}
        cancelText={t("common.cancel")}
        destroyOnClose
      >
        <Form form={form} layout="vertical" initialValues={{ is_superuser: false, role_ids: [] }}>
          <Form.Item name="username" label={t("common.username")} rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="full_name" label={t("user.fullName")} rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label={t("common.password")} rules={[{ required: true }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="email" label="Email">
            <Input />
          </Form.Item>
          <Form.Item name="role_ids" label={t("user.roles")}>
            <Select
              mode="multiple"
              options={roles.map((r) => ({ value: r.id, label: r.name }))}
            />
          </Form.Item>
          <Form.Item name="is_superuser" label="Superuser" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
