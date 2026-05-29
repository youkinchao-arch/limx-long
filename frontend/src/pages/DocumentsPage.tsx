import { useRef, useState } from "react";
import {
  Button,
  DatePicker,
  Drawer,
  Form,
  Input,
  Modal,
  Popconfirm,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from "antd";
import { HistoryOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import dayjs from "dayjs";
import CrudTable, { type CrudField, type Row } from "../components/CrudTable";
import { useAuth } from "../auth/AuthContext";
import api from "../api/client";

interface ReviewRow {
  id: number;
  action: string;
  from_status?: string;
  to_status?: string;
  actor_username?: string;
  comment?: string;
  created_at: string;
}

const STATUS_KEYS = ["draft", "under_review", "approved", "rejected", "obsolete"];

export default function DocumentsPage() {
  const { t } = useTranslation();
  const { hasPermission } = useAuth();
  const canWrite = hasPermission("document:write");
  const canApprove = hasPermission("document:approve");

  const [historyId, setHistoryId] = useState<number | null>(null);
  const [reviews, setReviews] = useState<ReviewRow[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const [decision, setDecision] = useState<{ id: number; type: "approve" | "reject" } | null>(null);
  const [form] = Form.useForm();
  const refreshRef = useRef<() => void>(() => {});

  const statusMap: Record<string, string> = Object.fromEntries(
    STATUS_KEYS.map((s) => [s, t(`document.statusMap.${s}`)]),
  );

  const fields: CrudField[] = [
    { name: "doc_no", label: t("document.docNo"), required: true },
    { name: "title", label: t("document.title"), required: true },
    { name: "category", label: t("document.category") },
    { name: "version", label: t("document.version") },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: statusMap,
      hideInForm: true,
      options: STATUS_KEYS.map((s) => ({ value: s, label: statusMap[s] })),
    },
    { name: "effective_date", label: t("document.effectiveDate"), type: "date" },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];

  const runAction = async (
    id: number,
    action: string,
    refresh: () => void,
    payload?: Record<string, unknown>,
  ) => {
    try {
      await api.post(`/documents/${id}/${action}`, payload ?? {});
      message.success(t("document.actionSuccess"));
      refresh();
    } catch (e) {
      const err = e as { response?: { data?: { detail?: string } } };
      message.error(err?.response?.data?.detail || t("common.failed"));
    }
  };

  const openHistory = async (id: number) => {
    setHistoryId(id);
    setLoadingHistory(true);
    try {
      const { data } = await api.get<ReviewRow[]>(`/documents/${id}/reviews`);
      setReviews(data);
    } finally {
      setLoadingHistory(false);
    }
  };

  const submitDecision = async (refresh: () => void) => {
    if (!decision) return;
    const values = await form.validateFields();
    const payload: Record<string, unknown> = { comment: values.comment };
    if (decision.type === "approve" && values.effective_date) {
      payload.effective_date = (values.effective_date as dayjs.Dayjs).format("YYYY-MM-DD");
    }
    await runAction(decision.id, decision.type, refresh, payload);
    setDecision(null);
    form.resetFields();
  };

  const rowActions = (record: Row, refresh: () => void) => {
    const status = record.status as string;
    return (
      <>
        {canWrite && (status === "draft" || status === "rejected") && (
          <Popconfirm
            title={t("document.submitConfirm")}
            onConfirm={() => runAction(record.id, "submit", refresh)}
          >
            <Button size="small" type="primary" ghost>
              {t("document.submit")}
            </Button>
          </Popconfirm>
        )}
        {canApprove && status === "under_review" && (
          <>
            <Button
              size="small"
              type="primary"
              onClick={() => {
                form.resetFields();
                setDecision({ id: record.id, type: "approve" });
              }}
            >
              {t("document.approve")}
            </Button>
            <Button
              size="small"
              danger
              onClick={() => {
                form.resetFields();
                setDecision({ id: record.id, type: "reject" });
              }}
            >
              {t("document.reject")}
            </Button>
          </>
        )}
        {canApprove && status === "approved" && (
          <Popconfirm
            title={t("document.obsoleteConfirm")}
            onConfirm={() => runAction(record.id, "obsolete", refresh)}
          >
            <Button size="small">{t("document.obsolete")}</Button>
          </Popconfirm>
        )}
        <Button size="small" icon={<HistoryOutlined />} onClick={() => openHistory(record.id)}>
          {t("document.history")}
        </Button>
      </>
    );
  };

  const reviewColumns = [
    {
      title: t("document.action"),
      dataIndex: "action",
      key: "action",
      render: (a: string) => <Tag>{t(`document.actionMap.${a}`, a)}</Tag>,
    },
    {
      title: t("document.transition"),
      key: "transition",
      render: (_: unknown, r: ReviewRow) =>
        `${statusMap[r.from_status ?? ""] ?? r.from_status ?? "-"} → ${statusMap[r.to_status ?? ""] ?? r.to_status ?? "-"}`,
    },
    { title: t("document.operator"), dataIndex: "actor_username", key: "actor_username" },
    {
      title: t("document.time"),
      dataIndex: "created_at",
      key: "created_at",
      render: (v: string) => (v ? dayjs(v).format("YYYY-MM-DD HH:mm") : "-"),
    },
    { title: t("document.comment"), dataIndex: "comment", key: "comment", render: (v: string) => v || "-" },
  ];

  return (
    <div>
      <Typography.Title level={4}>{t("menu.document")}</Typography.Title>
      <CrudTable
        resource="/documents"
        permissionPrefix="document"
        fields={fields}
        extraRowActions={(record, refresh) => {
          refreshRef.current = refresh;
          return <Space wrap>{rowActions(record, refresh)}</Space>;
        }}
      />

      <Modal
        open={decision !== null}
        title={decision?.type === "approve" ? t("document.approveTitle") : t("document.rejectTitle")}
        onOk={() => submitDecision(refreshRef.current)}
        onCancel={() => {
          setDecision(null);
          form.resetFields();
        }}
        okText={t("common.save")}
        cancelText={t("common.cancel")}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          {decision?.type === "approve" && (
            <Form.Item name="effective_date" label={t("document.effectiveDate")}>
              <DatePicker style={{ width: "100%" }} />
            </Form.Item>
          )}
          <Form.Item
            name="comment"
            label={t("document.comment")}
            rules={
              decision?.type === "reject"
                ? [{ required: true, message: t("document.rejectReasonRequired") }]
                : undefined
            }
          >
            <Input.TextArea rows={3} placeholder={t("document.commentPlaceholder")} />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        open={historyId !== null}
        title={t("document.reviewHistory")}
        width={640}
        onClose={() => setHistoryId(null)}
      >
        <Table
          rowKey="id"
          size="small"
          loading={loadingHistory}
          dataSource={reviews}
          columns={reviewColumns}
          pagination={false}
          locale={{ emptyText: t("document.noHistory") }}
        />
      </Drawer>
    </div>
  );
}
