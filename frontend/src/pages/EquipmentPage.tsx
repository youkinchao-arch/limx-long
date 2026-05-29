import { useState } from "react";
import { Button, Modal, Typography } from "antd";
import { QrcodeOutlined } from "@ant-design/icons";
import { useTranslation } from "react-i18next";
import CrudTable, { type CrudField } from "../components/CrudTable";
import { TOKEN_KEY } from "../api/client";

export default function EquipmentPage() {
  const { t } = useTranslation();
  const [qrId, setQrId] = useState<number | null>(null);
  const [qrUrl, setQrUrl] = useState<string>("");

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

  const fields: CrudField[] = [
    { name: "asset_no", label: t("equipment.assetNo"), required: true },
    { name: "name", label: t("equipment.name"), required: true },
    { name: "category", label: t("equipment.category") },
    { name: "model", label: t("equipment.model") },
    { name: "manufacturer", label: t("equipment.manufacturer"), hideInTable: true },
    { name: "serial_no", label: t("equipment.serialNo"), hideInTable: true },
    { name: "location", label: t("equipment.location") },
    { name: "status", label: t("common.status"), type: "select", options: statusOptions, tag: statusMap },
    { name: "purchase_date", label: t("equipment.purchaseDate"), type: "date", hideInTable: true },
    { name: "calibration_date", label: t("equipment.calibrationDate"), type: "date", hideInTable: true },
    { name: "calibration_due", label: t("equipment.calibrationDue"), type: "date" },
    { name: "calibration_cycle_days", label: t("equipment.calibrationCycle"), type: "number", hideInTable: true },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];

  const showQr = async (record: { id: number }) => {
    const token = localStorage.getItem(TOKEN_KEY);
    const resp = await fetch(`/api/v1/equipment/${record.id}/qrcode`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const blob = await resp.blob();
    setQrUrl(URL.createObjectURL(blob));
    setQrId(record.id);
  };

  return (
    <div>
      <Typography.Title level={4}>{t("menu.equipment")}</Typography.Title>
      <CrudTable
        resource="/equipment"
        permissionPrefix="equipment"
        fields={fields}
        extraRowActions={(record) => (
          <Button size="small" icon={<QrcodeOutlined />} onClick={() => showQr(record as { id: number })}>
            {t("equipment.qrcode")}
          </Button>
        )}
      />
      <Modal open={qrId !== null} footer={null} onCancel={() => setQrId(null)} title={t("equipment.qrcode")}>
        <div style={{ display: "flex", justifyContent: "center", padding: 16 }}>
          {qrUrl && <img src={qrUrl} alt="QR code" style={{ width: 220, height: 220 }} />}
        </div>
      </Modal>
    </div>
  );
}
