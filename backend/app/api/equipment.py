from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException, Query, Response, status
from sqlalchemy import func, or_, select
from sqlalchemy.orm import Session

from app.api.deps import require_permission
from app.core.database import get_db
from app.models.equipment import Equipment
from app.schemas.common import Page
from app.schemas.equipment import EquipmentCreate, EquipmentOut, EquipmentUpdate

router = APIRouter(prefix="/equipment", tags=["equipment"])


@router.get("", response_model=Page[EquipmentOut])
def list_equipment(
    db: Session = Depends(get_db),
    _=Depends(require_permission("equipment:read")),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=200),
    q: str | None = None,
    status_filter: str | None = Query(None, alias="status"),
    calibration_due_days: int | None = Query(None, description="校准将在 N 天内到期"),
):
    stmt = select(Equipment)
    if q:
        stmt = stmt.where(
            or_(
                Equipment.name.ilike(f"%{q}%"),
                Equipment.asset_no.ilike(f"%{q}%"),
                Equipment.model.ilike(f"%{q}%"),
            )
        )
    if status_filter:
        stmt = stmt.where(Equipment.status == status_filter)
    if calibration_due_days is not None:
        limit_date = date.today() + timedelta(days=calibration_due_days)
        stmt = stmt.where(
            Equipment.calibration_due.is_not(None),
            Equipment.calibration_due <= limit_date,
        )
    total = db.scalar(select(func.count()).select_from(stmt.subquery())) or 0
    rows = db.scalars(
        stmt.order_by(Equipment.id.desc()).offset((page - 1) * page_size).limit(page_size)
    ).all()
    return Page(items=rows, total=total, page=page, page_size=page_size)


@router.post("", response_model=EquipmentOut, status_code=status.HTTP_201_CREATED)
def create_equipment(
    payload: EquipmentCreate,
    db: Session = Depends(get_db),
    _=Depends(require_permission("equipment:write")),
):
    if db.scalar(select(Equipment).where(Equipment.asset_no == payload.asset_no)):
        raise HTTPException(status_code=400, detail="资产编号已存在 / Asset no exists")
    obj = Equipment(**payload.model_dump())
    db.add(obj)
    db.commit()
    db.refresh(obj)
    return obj


@router.get("/{eid}", response_model=EquipmentOut)
def get_equipment(
    eid: int, db: Session = Depends(get_db), _=Depends(require_permission("equipment:read"))
):
    obj = db.get(Equipment, eid)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    return obj


@router.put("/{eid}", response_model=EquipmentOut)
def update_equipment(
    eid: int,
    payload: EquipmentUpdate,
    db: Session = Depends(get_db),
    _=Depends(require_permission("equipment:write")),
):
    obj = db.get(Equipment, eid)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    for key, value in payload.model_dump(exclude_unset=True).items():
        setattr(obj, key, value)
    db.commit()
    db.refresh(obj)
    return obj


@router.delete("/{eid}", status_code=status.HTTP_204_NO_CONTENT)
def delete_equipment(
    eid: int, db: Session = Depends(get_db), _=Depends(require_permission("equipment:write"))
):
    obj = db.get(Equipment, eid)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    db.delete(obj)
    db.commit()


@router.get("/{eid}/qrcode")
def equipment_qrcode(
    eid: int, db: Session = Depends(get_db), _=Depends(require_permission("equipment:read"))
):
    """生成设备唯一标识二维码 (PNG). 扫码可查询设备。"""
    from io import BytesIO

    import qrcode

    obj = db.get(Equipment, eid)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    payload = f"LIMS-EQUIP:{obj.asset_no}"
    img = qrcode.make(payload, box_size=8, border=2)
    buf = BytesIO()
    img.save(buf, format="PNG")
    return Response(content=buf.getvalue(), media_type="image/png")
