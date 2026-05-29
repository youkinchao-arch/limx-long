from datetime import date, datetime

from pydantic import BaseModel


# --------- 仓库物资 ---------
class SupplierIn(BaseModel):
    name: str
    contact: str | None = None
    phone: str | None = None
    remark: str | None = None


class SupplierOut(SupplierIn):
    id: int
    model_config = {"from_attributes": True}


class MaterialIn(BaseModel):
    code: str
    name: str
    category: str | None = None
    unit: str | None = None
    warehouse: str | None = None
    quantity: float = 0
    safety_stock: float = 0
    expiry_date: date | None = None
    remark: str | None = None


class MaterialOut(MaterialIn):
    id: int
    model_config = {"from_attributes": True}


# --------- 文件管理 ---------
class DocumentIn(BaseModel):
    doc_no: str
    title: str
    category: str | None = None
    version: str | None = None
    status: str = "draft"
    effective_date: date | None = None
    remark: str | None = None


class DocumentOut(DocumentIn):
    id: int
    model_config = {"from_attributes": True}


# --------- 环境管理 ---------
class EnvironmentRecordIn(BaseModel):
    location: str
    temperature: float | None = None
    humidity: float | None = None
    recorded_at: datetime
    status: str = "normal"
    remark: str | None = None


class EnvironmentRecordOut(EnvironmentRecordIn):
    id: int
    model_config = {"from_attributes": True}


# --------- 方法管理 ---------
class TestMethodIn(BaseModel):
    code: str
    name: str
    standard: str | None = None
    category: str | None = None
    limit_value: str | None = None
    status: str = "draft"
    remark: str | None = None


class TestMethodOut(TestMethodIn):
    id: int
    model_config = {"from_attributes": True}


# --------- 报告管理 ---------
class ReportIn(BaseModel):
    report_no: str
    title: str
    customer: str | None = None
    template: str | None = None
    status: str = "draft"
    issue_date: date | None = None
    remark: str | None = None


class ReportOut(ReportIn):
    id: int
    model_config = {"from_attributes": True}


# --------- 资源排程 ---------
class ResourceBookingIn(BaseModel):
    title: str
    resource_type: str
    resource_name: str | None = None
    start_time: datetime
    end_time: datetime
    priority: int = 0
    status: str = "pending"
    remark: str | None = None


class ResourceBookingOut(ResourceBookingIn):
    id: int
    model_config = {"from_attributes": True}
