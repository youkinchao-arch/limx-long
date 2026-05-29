import { useCallback, useEffect, useState } from "react";
import {
  Button,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  message,
} from "antd";
import { DeleteOutlined, EditOutlined, PlusOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import dayjs from "dayjs";
import api, { type Page } from "../api/client";
import { useAuth } from "../auth/AuthContext";

export type FieldType = "text" | "number" | "date" | "datetime" | "textarea" | "select";

export interface CrudField {
  name: string;
  label: string;
  type?: FieldType;
  required?: boolean;
  options?: { value: string; label: string }[];
  // status-style colored tag in the table
  tag?: Record<string, string>;
  hideInTable?: boolean;
  hideInForm?: boolean;
}

interface CrudTableProps {
  resource: string; // e.g. "/documents"
  permissionPrefix: string; // e.g. "document"
  fields: CrudField[];
  rowKey?: string;
  extraRowActions?: (record: Row, refresh: () => void) => React.ReactNode;
}

export interface Row {
  id: number;
  [k: string]: unknown;
}

const DATE_FIELDS: FieldType[] = ["date", "datetime"];

export default function CrudTable({
  resource,
  permissionPrefix,
  fields,
  rowKey = "id",
  extraRowActions,
}: CrudTableProps) {
  const { t } = useTranslation();
  const { hasPermission } = useAuth();
  const [data, setData] = useState<Row[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Row | null>(null);
  const [form] = Form.useForm();

  const canWrite = hasPermission(`${permissionPrefix}:write`);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const { data: resp } = await api.get<Page<Row>>(resource, {
        params: { page, page_size: pageSize, q: q || undefined },
      });
      setData(resp.items);
      setTotal(resp.total);
    } finally {
      setLoading(false);
    }
  }, [resource, page, pageSize, q]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record: Row) => {
    setEditing(record);
    const values: Record<string, unknown> = { ...record };
    fields.forEach((f) => {
      if (DATE_FIELDS.includes(f.type ?? "text") && record[f.name]) {
        values[f.name] = dayjs(record[f.name] as string);
      }
    });
    form.setFieldsValue(values);
    setModalOpen(true);
  };

  const submit = async () => {
    const values = await form.validateFields();
    fields.forEach((f) => {
      if (DATE_FIELDS.includes(f.type ?? "text") && values[f.name]) {
        const d = values[f.name] as dayjs.Dayjs;
        values[f.name] =
          f.type === "datetime" ? d.toISOString() : d.format("YYYY-MM-DD");
      }
    });
    try {
      if (editing) {
        await api.put(`${resource}/${editing.id}`, values);
      } else {
        await api.post(resource, values);
      }
      message.success(t("common.success"));
      setModalOpen(false);
      fetchData();
    } catch (e: any) {
      message.error(e?.response?.data?.detail || t("common.failed"));
    }
  };

  const remove = async (record: Row) => {
    await api.delete(`${resource}/${record.id}`);
    message.success(t("common.success"));
    fetchData();
  };

  const columns = [
    ...fields
      .filter((f) => !f.hideInTable)
      .map((f) => ({
        title: f.label,
        dataIndex: f.name,
        key: f.name,
        render: (value: unknown) => {
          if (value == null || value === "") return "-";
          if (f.tag) {
            const color =
              { in_use: "green", active: "green", normal: "green", approved: "green", repair: "orange", warning: "orange", under_review: "orange", alarm: "red", rejected: "red", scrapped: "red", sealed: "default", obsolete: "default" }[
                value as string
              ] || "blue";
            return <Tag color={color}>{f.tag[value as string] || String(value)}</Tag>;
          }
          if (f.type === "datetime") return dayjs(value as string).format("YYYY-MM-DD HH:mm");
          return String(value);
        },
      })),
    {
      title: t("common.actions"),
      key: "_actions",
      width: 220,
      render: (_: unknown, record: Row) => (
        <Space>
          {extraRowActions?.(record, fetchData)}
          <Button
            size="small"
            icon={<EditOutlined />}
            disabled={!canWrite}
            onClick={() => openEdit(record)}
          >
            {t("common.edit")}
          </Button>
          <Popconfirm
            title={t("common.confirmDelete")}
            onConfirm={() => remove(record)}
            disabled={!canWrite}
          >
            <Button size="small" danger icon={<DeleteOutlined />} disabled={!canWrite}>
              {t("common.delete")}
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder={t("common.keyword")}
          allowClear
          onSearch={(v) => {
            setPage(1);
            setQ(v);
          }}
          style={{ width: 240 }}
        />
        <Button
          type="primary"
          icon={<PlusOutlined />}
          disabled={!canWrite}
          onClick={openCreate}
        >
          {t("common.create")}
        </Button>
      </Space>

      <Table
        rowKey={rowKey}
        loading={loading}
        dataSource={data}
        columns={columns}
        scroll={{ x: "max-content" }}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (n) => t("common.total", { count: n }),
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
        }}
      />

      <Modal
        title={editing ? t("common.edit") : t("common.create")}
        open={modalOpen}
        onOk={submit}
        onCancel={() => setModalOpen(false)}
        okText={t("common.save")}
        cancelText={t("common.cancel")}
        destroyOnClose
        width={640}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          {fields
            .filter((f) => !f.hideInForm)
            .map((f) => (
              <Form.Item
                key={f.name}
                name={f.name}
                label={f.label}
                rules={f.required ? [{ required: true }] : undefined}
              >
                {renderInput(f)}
              </Form.Item>
            ))}
        </Form>
      </Modal>
    </>
  );
}

function renderInput(f: CrudField) {
  switch (f.type) {
    case "number":
      return <InputNumber style={{ width: "100%" }} />;
    case "date":
      return <DatePicker style={{ width: "100%" }} />;
    case "datetime":
      return <DatePicker showTime style={{ width: "100%" }} />;
    case "textarea":
      return <Input.TextArea rows={3} />;
    case "select":
      return <Select options={f.options} allowClear />;
    default:
      return <Input />;
  }
}
