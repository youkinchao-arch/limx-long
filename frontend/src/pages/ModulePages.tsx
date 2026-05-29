import { Typography } from "antd";
import { useTranslation } from "react-i18next";
import CrudTable, { type CrudField } from "../components/CrudTable";

const docStatus = { draft: "draft", approved: "approved", published: "published", changed: "changed", obsolete: "obsolete" };

export function DocumentsPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "doc_no", label: t("document.docNo"), required: true },
    { name: "title", label: t("document.title"), required: true },
    { name: "category", label: t("document.category") },
    { name: "version", label: t("document.version") },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: docStatus,
      options: Object.keys(docStatus).map((s) => ({ value: s, label: s })),
    },
    { name: "effective_date", label: t("document.effectiveDate"), type: "date" },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];
  return (
    <div>
      <Typography.Title level={4}>{t("menu.document")}</Typography.Title>
      <CrudTable resource="/documents" permissionPrefix="document" fields={fields} />
    </div>
  );
}

export function EnvironmentPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "location", label: t("environment.location"), required: true },
    { name: "temperature", label: t("environment.temperature"), type: "number" },
    { name: "humidity", label: t("environment.humidity"), type: "number" },
    { name: "recorded_at", label: t("environment.recordedAt"), type: "datetime", required: true },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: { normal: "normal", warning: "warning", alarm: "alarm" },
      options: ["normal", "warning", "alarm"].map((s) => ({ value: s, label: s })),
    },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];
  return (
    <div>
      <Typography.Title level={4}>{t("menu.environment")}</Typography.Title>
      <CrudTable resource="/environment/records" permissionPrefix="environment" fields={fields} />
    </div>
  );
}

export function MethodsPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "code", label: t("method.code"), required: true },
    { name: "name", label: t("method.name"), required: true },
    { name: "standard", label: t("method.standard") },
    { name: "category", label: t("method.category") },
    { name: "limit_value", label: t("method.limitValue"), hideInTable: true },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: { draft: "draft", validated: "validated", active: "active", obsolete: "obsolete" },
      options: ["draft", "validated", "active", "obsolete"].map((s) => ({ value: s, label: s })),
    },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];
  return (
    <div>
      <Typography.Title level={4}>{t("menu.method")}</Typography.Title>
      <CrudTable resource="/methods" permissionPrefix="method" fields={fields} />
    </div>
  );
}

export function ReportsPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "report_no", label: t("report.reportNo"), required: true },
    { name: "title", label: t("report.title"), required: true },
    { name: "customer", label: t("report.customer") },
    { name: "template", label: t("report.template") },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: { draft: "draft", submitted: "submitted", approved: "approved", archived: "archived" },
      options: ["draft", "submitted", "approved", "archived"].map((s) => ({ value: s, label: s })),
    },
    { name: "issue_date", label: t("report.issueDate"), type: "date" },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];
  return (
    <div>
      <Typography.Title level={4}>{t("menu.report")}</Typography.Title>
      <CrudTable resource="/reports" permissionPrefix="report" fields={fields} />
    </div>
  );
}

export function ResourcesPage() {
  const { t } = useTranslation();
  const fields: CrudField[] = [
    { name: "title", label: t("resource.title"), required: true },
    {
      name: "resource_type",
      label: t("resource.resourceType"),
      type: "select",
      required: true,
      options: [
        { value: "personnel", label: t("menu.personnel") },
        { value: "equipment", label: t("menu.equipment") },
        { value: "material", label: t("menu.materials") },
      ],
    },
    { name: "resource_name", label: t("resource.resourceName") },
    { name: "start_time", label: t("resource.startTime"), type: "datetime", required: true },
    { name: "end_time", label: t("resource.endTime"), type: "datetime", required: true },
    { name: "priority", label: t("resource.priority"), type: "number" },
    {
      name: "status",
      label: t("common.status"),
      type: "select",
      tag: { pending: "pending", confirmed: "confirmed", conflict: "conflict", done: "done", cancelled: "cancelled" },
      options: ["pending", "confirmed", "conflict", "done", "cancelled"].map((s) => ({ value: s, label: s })),
    },
    { name: "remark", label: t("common.remark"), type: "textarea", hideInTable: true },
  ];
  return (
    <div>
      <Typography.Title level={4}>{t("menu.resource")}</Typography.Title>
      <CrudTable resource="/resources/bookings" permissionPrefix="resource" fields={fields} />
    </div>
  );
}
