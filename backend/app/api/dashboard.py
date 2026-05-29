from datetime import date, timedelta

from fastapi import APIRouter, Depends
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.core.database import get_db
from app.models.equipment import Equipment
from app.models.modules import Document, EnvironmentRecord, Material, Report
from app.models.personnel import Personnel

router = APIRouter(prefix="/dashboard", tags=["dashboard"])


@router.get("/summary")
def summary(
    db: Session = Depends(get_db), _=Depends(get_current_user), reminder_days: int = 30
):
    """实验室运营可视化看板汇总数据。"""
    today = date.today()
    soon = today + timedelta(days=reminder_days)

    def count(model) -> int:
        return db.scalar(select(func.count()).select_from(model)) or 0

    personnel_total = count(Personnel)
    equipment_total = count(Equipment)
    material_total = count(Material)
    document_total = count(Document)
    report_total = count(Report)

    qualification_expiring = db.scalar(
        select(func.count()).select_from(Personnel).where(
            Personnel.qualification_expiry.is_not(None),
            Personnel.qualification_expiry <= soon,
        )
    ) or 0
    calibration_due = db.scalar(
        select(func.count()).select_from(Equipment).where(
            Equipment.calibration_due.is_not(None),
            Equipment.calibration_due <= soon,
        )
    ) or 0
    material_expiring = db.scalar(
        select(func.count()).select_from(Material).where(
            Material.expiry_date.is_not(None), Material.expiry_date <= soon
        )
    ) or 0
    env_alarms = db.scalar(
        select(func.count()).select_from(EnvironmentRecord).where(
            EnvironmentRecord.status != "normal"
        )
    ) or 0

    # 设备状态分布
    status_rows = db.execute(
        select(Equipment.status, func.count()).group_by(Equipment.status)
    ).all()
    equipment_status = {row[0]: row[1] for row in status_rows}

    return {
        "totals": {
            "personnel": personnel_total,
            "equipment": equipment_total,
            "material": material_total,
            "document": document_total,
            "report": report_total,
        },
        "reminders": {
            "qualification_expiring": qualification_expiring,
            "calibration_due": calibration_due,
            "material_expiring": material_expiring,
            "environment_alarms": env_alarms,
            "window_days": reminder_days,
        },
        "equipment_status": equipment_status,
    }
