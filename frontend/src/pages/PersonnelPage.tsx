import { Typography } from "antd";
import { useTranslation } from "react-i18next";
import CrudTable, { type CrudField } from "../components/CrudTable";

export default function PersonnelPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "employee_no", label: t("personnel.employeeNo"), required: true },
    { name: "name", label: t("personnel.name"), required: true },
    { name: "gender", label: t("personnel.gender") },
    { name: "position", label: t("personnel.position") },
    { name: "phone", label: t("personnel.phone") },
    { name: "email", label: t("personnel.email") },
    { name: "hire_date", label: t("personnel.hireDate"), type: "date" },
    { name: "qualification", label: t("personnel.qualification") },
    { name: "qualification_expiry", label: t("personnel.qualificationExpiry"), type: "date" },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: { active: t("equipment.statusMap.in_use") },
      options: [{ value: "active", label: t("equipment.statusMap.in_use") }],
    },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];

  return (
    <div>
      <Typography.Title level={4}>{t("menu.personnel")}</Typography.Title>
      <CrudTable resource="/personnel" permissionPrefix="personnel" fields={fields} />
    </div>
  );
}
