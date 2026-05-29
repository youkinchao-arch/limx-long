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
import { DownloadOutlined, HistoryOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import dayjs from "dayjs";
import CrudTable, { type CrudField, type Row } from "../components/CrudTable";
import { useAuth } from "../auth/AuthContext";
import api from "../api/client";

interface SignatureRow {
  id: number;
  action: string;
  from_status?: string;
  to_status?: string;
  actor_username?: string;
  meaning?: string;
  signature_hash?: string;
  comment?: string;
  created_at: string;
}

const STATUS_KEYS = ["draft", "under_review", "approved", "rejected", "signed", "issued"];

type DecisionType = "approve" | "reject" | "sign" | "issue";

export default function ReportsPage() {
  const { t } = useTranslation();
  const { hasPermission } = useAuth();
  const canWrite = hasPermission("report:write");
  const canApprove = hasPermission("report:approve");
  const canSign = hasPermission("report:sign");

  const [historyId, setHistoryId] = useState<number | null>(null);
  const [signatures, setSignatures] = useState<SignatureRow[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const [decision, setDecision] = useState<{ id: number; type: DecisionType } | null>(null);
  const [form] = Form.useForm();
  const refreshRef = useRef<() => void>(() => {});

  const statusMap: Record<string, string> = Object.fromEntries(
    STATUS_KEYS.map((s) => [s, t(`report.statusMap.${s}`)]),
  );

  const fields: CrudField[] = [
    { name: "report_no", label: t("report.reportNo"), required: true },
    { name: "title", label: t("report.title"), required: true },
    { name: "customer", label: t("report.customer") },
    { name: "template", label: t("report.template") },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: statusMap,
      hideInForm: true,
      options: STATUS_KEYS.map((s) => ({ value: s, label: statusMap[s] })),
    },
    { name: "issue_date", label: t("report.issueDate"), type: "date", hideInForm: true },
    { name: "conclusion", label: t("report.conclusion"), type: "textarea", hideInTable: true },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];

  const runAction = async (
    id: number,
    action: string,
    refresh: () => void,
    payload?: Record<string, unknown>,
  ) => {
    try {
      await api.post(`/reports/${id}/${action}`, payload ?? {});
      message.success(t("report.actionSuccess"));
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
      const { data } = await api.get<SignatureRow[]>(`/reports/${id}/signatures`);
      setSignatures(data);
    } finally {
      setLoadingHistory(false);
    }
  };

  const downloadPdf = async (record: Row) => {
    try {
      const { data } = await api.get(`/reports/${record.id}/pdf`, { responseType: "blob" });
      const url = window.URL.createObjectURL(data as Blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `report-${record.report_no}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      message.error(t("common.failed"));
    }
  };

  const submitDecision = async (refresh: () => void) => {
    if (!decision) return;
    const values = await form.validateFields();
    const payload: Record<string, unknown> = { comment: values.comment };
    if (decision.type === "sign") {
      payload.password = values.password;
      payload.meaning = values.meaning;
    }
    if (decision.type === "issue" && values.issue_date) {
      payload.issue_date = (values.issue_date as dayjs.Dayjs).format("YYYY-MM-DD");
    }
    await runAction(decision.id, decision.type, refresh, payload);
    setDecision(null);
    form.resetFields();
  };

  const openDecision = (id: number, type: DecisionType) => {
    form.resetFields();
    setDecision({ id, type });
  };

  const rowActions = (record: Row, refresh: () => void) => {
    const status = record.status as string;
    return (
      <>
        {canWrite && (status === "draft" || status === "rejected") && (
          <Popconfirm
            title={t("report.submitConfirm")}
            onConfirm={() => runAction(record.id, "submit", refresh)}
          >
            <Button size="small" type="primary" ghost>
              {t("report.submit")}
            </Button>
          </Popconfirm>
        )}
        {canApprove && status === "under_review" && (
          <>
            <Button size="small" type="primary" onClick={() => openDecision(record.id, "approve")}>
              {t("report.approve")}
            </Button>
            <Button size="small" danger onClick={() => openDecision(record.id, "reject")}>
              {t("report.reject")}
            </Button>
          </>
        )}
        {canSign && status === "approved" && (
          <Button size="small" type="primary" onClick={() => openDecision(record.id, "sign")}>
            {t("report.sign")}
          </Button>
        )}
        {canApprove && status === "signed" && (
          <Button size="small" type="primary" onClick={() => openDecision(record.id, "issue")}>
            {t("report.issue")}
          </Button>
        )}
        <Button size="small" icon={<HistoryOutlined />} onClick={() => openHistory(record.id)}>
          {t("report.history")}
        </Button>
        <Button size="small" icon={<DownloadOutlined />} onClick={() => downloadPdf(record)}>
          {t("report.downloadPdf")}
        </Button>
      </>
    );
  };

  const modalTitle: Record<DecisionType, string> = {
    approve: t("report.approveTitle"),
    reject: t("report.rejectTitle"),
    sign: t("report.signTitle"),
    issue: t("report.issueTitle"),
  };

  const signatureColumns = [
    {
      title: t("report.action"),
      dataIndex: "action",
      key: "action",
      render: (a: string) => <Tag>{t(`report.actionMap.${a}`, a)}</Tag>,
    },
    {
      title: t("report.transition"),
      key: "transition",
      render: (_: unknown, r: SignatureRow) =>
        `${statusMap[r.from_status ?? ""] ?? r.from_status ?? "-"} → ${statusMap[r.to_status ?? ""] ?? r.to_status ?? "-"}`,
    },
    { title: t("report.operator"), dataIndex: "actor_username", key: "actor_username" },
    {
      title: t("report.time"),
      dataIndex: "created_at",
      key: "created_at",
      render: (v: string) => (v ? dayjs(v).format("YYYY-MM-DD HH:mm") : "-"),
    },
    { title: t("report.meaning"), dataIndex: "meaning", key: "meaning", render: (v: string) => v || "-" },
    {
      title: t("report.signatureHash"),
      dataIndex: "signature_hash",
      key: "signature_hash",
      render: (v: string) => (v ? <code>{v.slice(0, 12)}…</code> : "-"),
    },
  ];

  return (
    <div>
      <Typography.Title level={4}>{t("menu.report")}</Typography.Title>
      <CrudTable
        resource="/reports"
        permissionPrefix="report"
        fields={fields}
        extraRowActions={(record, refresh) => {
          refreshRef.current = refresh;
          return <Space wrap>{rowActions(record, refresh)}</Space>;
        }}
      />

      <Modal
        open={decision !== null}
        title={decision ? modalTitle[decision.type] : ""}
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
          {decision?.type === "sign" && (
            <>
              <Form.Item
                name="password"
                label={t("common.password")}
                rules={[{ required: true, message: t("report.passwordRequired") }]}
              >
                <Input.Password autoComplete="off" />
              </Form.Item>
              <Form.Item name="meaning" label={t("report.meaning")}>
                <Input placeholder={t("report.meaningPlaceholder")} />
              </Form.Item>
            </>
          )}
          {decision?.type === "issue" && (
            <Form.Item name="issue_date" label={t("report.issueDate")}>
              <DatePicker style={{ width: "100%" }} />
            </Form.Item>
          )}
          <Form.Item
            name="comment"
            label={t("report.comment")}
            rules={
              decision?.type === "reject"
                ? [{ required: true, message: t("report.rejectReasonRequired") }]
                : undefined
            }
          >
            <Input.TextArea rows={3} placeholder={t("report.commentPlaceholder")} />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        open={historyId !== null}
        title={t("report.signatureHistory")}
        width={760}
        onClose={() => setHistoryId(null)}
      >
        <Table
          rowKey="id"
          size="small"
          loading={loadingHistory}
          dataSource={signatures}
          columns={signatureColumns}
          pagination={false}
          locale={{ emptyText: t("report.noHistory") }}
        />
      </Drawer>
    </div>
  );
}
