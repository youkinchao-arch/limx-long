import { Typography } from "antd";
import { useTranslation } from "react-i18next";
import CrudTable, { type CrudField } from "../components/CrudTable";

export function MaterialsPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "code", label: t("material.code"), required: true },
    { name: "name", label: t("material.name"), required: true },
    { name: "category", label: t("material.category") },
    { name: "unit", label: t("material.unit") },
    { name: "warehouse", label: t("material.warehouse") },
    { name: "quantity", label: t("material.quantity"), type: "number" },
    { name: "safety_stock", label: t("material.safetyStock"), type: "number", hideInTable: true },
    { name: "expiry_date", label: t("material.expiryDate"), type: "date" },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];
  return (
    <div>
      <Typography.Title level={4}>{t("menu.materials")}</Typography.Title>
      <CrudTable resource="/warehouse/materials" permissionPrefix="warehouse" fields={fields} />
    </div>
  );
}

export function SuppliersPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "name", label: t("material.name"), required: true },
    { name: "contact", label: t("personnel.name") },
    { name: "phone", label: t("personnel.phone") },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];
  return (
    <div>
      <Typography.Title level={4}>{t("menu.suppliers")}</Typography.Title>
      <CrudTable resource="/warehouse/suppliers" permissionPrefix="warehouse" fields={fields} />
    </div>
  );
}
