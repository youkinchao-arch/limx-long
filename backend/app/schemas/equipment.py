from datetime import date

from pydantic import BaseModel


class EquipmentBase(BaseModel):
    asset_no: str
    name: str
    category: str | None = None
    model: str | None = None
    manufacturer: str | None = None
    serial_no: str | None = None
    location: str | None = None
    status: str = "idle"
    purchase_date: date | None = None
    calibration_date: date | None = None
    calibration_due: date | None = None
    calibration_cycle_days: int | None = None
    remark: str | None = None


class EquipmentCreate(EquipmentBase):
    pass


class EquipmentUpdate(BaseModel):
    name: str | None = None
    category: str | None = None
    model: str | None = None
    manufacturer: str | None = None
    serial_no: str | None = None
    location: str | None = None
    status: str | None = None
    purchase_date: date | None = None
    calibration_date: date | None = None
    calibration_due: date | None = None
    calibration_cycle_days: int | None = None
    remark: str | None = None


class EquipmentOut(EquipmentBase):
    id: int

    model_config = {"from_attributes": True}
