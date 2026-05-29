import { useRef, useState } from "react";
import {
  Button,
  DatePicker,
  Drawer,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from "antd";
import { HistoryOutlined, QrcodeOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import dayjs from "dayjs";
import CrudTable, { type CrudField, type Row } from "../components/CrudTable";
import { useAuth } from "../auth/AuthContext";
import api, { TOKEN_KEY } from "../api/client";

interface EquipmentRecordRow {
  id: number;
  record_type: string;
  result?: string;
  performed_date?: string;
  performed_by_name?: string;
  provider?: string;
  certificate_no?: string;
  next_due_date?: string;
  cycle_days?: number;
  signature_hash?: string;
  notes?: string;
  created_at: string;
}

export default function EquipmentPage() {
  const { t } = useTranslation();
  const { hasPermission } = useAuth();
  const canCalibrate = hasPermission("equipment:calibrate");
  const canMaintain = hasPermission("equipment:maintain");

  const [qrId, setQrId] = useState<number | null>(null);
  const [qrUrl, setQrUrl] = useState<string>("");

  const [historyId, setHistoryId] = useState<number | null>(null);
  const [records, setRecords] = useState<EquipmentRecordRow[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const [record, setRecord] = useState<{ id: number; type: "calibration" | "maintenance" } | null>(null);
  const [form] = Form.useForm();
  const refreshRef = useRef<() => void>(() => {});

  const statusMap: Record<string, string> = {
    in_use: t("equipment.statusMap.in_use"),
    idle: t("equipment.statusMap.idle"),
    repair: t("equipment.statusMap.repair"),
    maintenance: t("equipment.statusMap.maintenance"),
    sealed: t("equipment.statusMap.sealed"),
    scrapped: t("equipment.statusMap.scrapped"),
    borrowed: t("equipment.statusMap.borrowed"),
  };
  const statusOptions = Object.entries(statusMap).map(([value, label]) => ({ value, label }));

  const calStatusMap: Record<string, string> = {
    overdue: t("equipment.calStatusMap.overdue"),
    due_soon: t("equipment.calStatusMap.due_soon"),
    valid: t("equipment.calStatusMap.valid"),
    unknown: t("equipment.calStatusMap.unknown"),
  };

  const fields: CrudField[] = [
    { name: "asset_no", label: t("equipment.assetNo"), required: true },
    { name: "name", label: t("equipment.name"), required: true },
    { name: "category", label: t("equipment.category"), hideInTable: true },
    { name: "model", label: t("equipment.model"), hideInTable: true },
    { name: "manufacturer", label: t("equipment.manufacturer"), hideInTable: true },
    { name: "serial_no", label: t("equipment.serialNo"), hideInTable: true },
    { name: "location", label: t("equipment.location") },
    { name: "status", label: t("common.status"), type: "select", options: statusOptions, tag: statusMap },
    { name: "calibration_due", label: t("equipment.calibrationDue"), type: "date" },
    { name: "calibration_status", label: t("equipment.calibrationState"), tag: calStatusMap, hideInForm: true },
    { name: "maintenance_due", label: t("equipment.maintenanceDue"), type: "date" },
    { name: "maintenance_status", label: t("equipment.maintenanceState"), tag: calStatusMap, hideInForm: true },
    { name: "purchase_date", label: t("equipment.purchaseDate"), type: "date", hideInTable: true },
    { name: "calibration_date", label: t("equipment.calibrationDate"), type: "date", hideInTable: true },
    { name: "calibration_cycle_days", label: t("equipment.calibrationCycle"), type: "number", hideInTable: true },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];

  const showQr = async (row: { id: number }) => {
    const token = localStorage.getItem(TOKEN_KEY);
    const resp = await fetch(`/api/v1/equipment/${row.id}/qrcode`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const blob = await resp.blob();
    setQrUrl(URL.createObjectURL(blob));
    setQrId(row.id);
  };

  const openHistory = async (id: number) => {
    setHistoryId(id);
    setLoadingHistory(true);
    try {
      const { data } = await api.get<EquipmentRecordRow[]>(`/equipment/${id}/records`);
      setRecords(data);
    } finally {
      setLoadingHistory(false);
    }
  };

  const openRecord = (id: number, type: "calibration" | "maintenance") => {
    form.resetFields();
    setRecord({ id, type });
  };

  const submitRecord = async (refresh: () => void) => {
    if (!record) return;
    const values = await form.validateFields();
    const payload: Record<string, unknown> = {
      record_type: record.type,
      result: values.result,
      provider: values.provider,
      certificate_no: values.certificate_no,
      cycle_days: values.cycle_days,
      notes: values.notes,
      password: values.password,
    };
    if (values.performed_date) {
      payload.performed_date = (values.performed_date as dayjs.Dayjs).format("YYYY-MM-DD");
    }
    if (values.next_due_date) {
      payload.next_due_date = (values.next_due_date as dayjs.Dayjs).format("YYYY-MM-DD");
    }
    try {
      await api.post(`/equipment/${record.id}/records`, payload);
      message.success(t("equipment.recordSuccess"));
      setRecord(null);
      form.resetFields();
      refresh();
    } catch (e) {
      const err = e as { response?: { data?: { detail?: string } } };
      message.error(err?.response?.data?.detail || t("common.failed"));
    }
  };

  const resultOptions =
    record?.type === "maintenance"
      ? [{ value: "completed", label: t("equipment.resultMap.completed") }]
      : [
          { value: "pass", label: t("equipment.resultMap.pass") },
          { value: "limited", label: t("equipment.resultMap.limited") },
          { value: "fail", label: t("equipment.resultMap.fail") },
        ];

  const rowActions = (row: Row) => (
    <>
      {canCalibrate && (
        <Button size="small" type="primary" ghost onClick={() => openRecord(row.id, "calibration")}>
          {t("equipment.recordCalibration")}
        </Button>
      )}
      {canMaintain && (
        <Button size="small" onClick={() => openRecord(row.id, "maintenance")}>
          {t("equipment.recordMaintenance")}
        </Button>
      )}
      <Button size="small" icon={<HistoryOutlined />} onClick={() => openHistory(row.id)}>
        {t("equipment.records")}
      </Button>
      <Button size="small" icon={<QrcodeOutlined />} onClick={() => showQr(row as { id: number })}>
        {t("equipment.qrcode")}
      </Button>
    </>
  );

  const recordColumns = [
    {
      title: t("equipment.recordType"),
      dataIndex: "record_type",
      key: "record_type",
      render: (v: string) => (
        <Tag color={v === "calibration" ? "blue" : "geekblue"}>{t(`equipment.recordTypeMap.${v}`, v)}</Tag>
      ),
    },
    {
      title: t("equipment.result"),
      dataIndex: "result",
      key: "result",
      render: (v: string) => (v ? t(`equipment.resultMap.${v}`, v) : "-"),
    },
    { title: t("equipment.performedDate"), dataIndex: "performed_date", key: "performed_date", render: (v: string) => v || "-" },
    { title: t("equipment.nextDueDate"), dataIndex: "next_due_date", key: "next_due_date", render: (v: string) => v || "-" },
    { title: t("equipment.provider"), dataIndex: "provider", key: "provider", render: (v: string) => v || "-" },
    { title: t("equipment.certificateNo"), dataIndex: "certificate_no", key: "certificate_no", render: (v: string) => v || "-" },
    { title: t("equipment.operator"), dataIndex: "performed_by_name", key: "performed_by_name", render: (v: string) => v || "-" },
    {
      title: t("equipment.signatureHash"),
      dataIndex: "signature_hash",
      key: "signature_hash",
      render: (v: string) => (v ? <code>{v.slice(0, 12)}…</code> : "-"),
    },
  ];

  return (
    <div>
      <Typography.Title level={4}>{t("menu.equipment")}</Typography.Title>
      <CrudTable
        resource="/equipment"
        permissionPrefix="equipment"
        fields={fields}
        extraRowActions={(row, refresh) => {
          refreshRef.current = refresh;
          return <Space wrap>{rowActions(row)}</Space>;
        }}
      />

      <Modal open={qrId !== null} footer={null} onCancel={() => setQrId(null)} title={t("equipment.qrcode")}>
        <div style={{ display: "flex", justifyContent: "center", padding: 16 }}>
          {qrUrl && <img src={qrUrl} alt="QR code" style={{ width: 220, height: 220 }} />}
        </div>
      </Modal>

      <Modal
        open={record !== null}
        title={
          record?.type === "maintenance"
            ? t("equipment.recordMaintenance")
            : t("equipment.recordCalibration")
        }
        onOk={() => submitRecord(refreshRef.current)}
        onCancel={() => {
          setRecord(null);
          form.resetFields();
        }}
        okText={t("common.save")}
        cancelText={t("common.cancel")}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="result" label={t("equipment.result")}>
            <Select options={resultOptions} allowClear />
          </Form.Item>
          <Form.Item name="performed_date" label={t("equipment.performedDate")}>
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="cycle_days" label={t("equipment.cycleDays")}>
            <InputNumber style={{ width: "100%" }} min={1} />
          </Form.Item>
          <Form.Item name="next_due_date" label={t("equipment.nextDueDate")} help={t("equipment.nextDueHint")}>
            <DatePicker style={{ width: "100%" }} />
          </Form.Item>
          <Form.Item name="provider" label={t("equipment.provider")}>
            <Input />
          </Form.Item>
          <Form.Item name="certificate_no" label={t("equipment.certificateNo")}>
            <Input />
          </Form.Item>
          <Form.Item name="notes" label={t("equipment.notes")}>
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item
            name="password"
            label={t("common.password")}
            rules={[{ required: true, message: t("equipment.passwordRequired") }]}
          >
            <Input.Password autoComplete="off" />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        open={historyId !== null}
        title={t("equipment.recordHistory")}
        width={860}
        onClose={() => setHistoryId(null)}
      >
        <Table
          rowKey="id"
          size="small"
          loading={loadingHistory}
          dataSource={records}
          columns={recordColumns}
          pagination={false}
          locale={{ emptyText: t("equipment.noRecords") }}
        />
      </Drawer>
    </div>
  );
}
