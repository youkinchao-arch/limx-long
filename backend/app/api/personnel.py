from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import func, or_, select
from sqlalchemy.orm import Session

from app.api.deps import require_permission
from app.core.database import get_db
from app.models.personnel import Department, Personnel, TrainingRecord
from app.schemas.common import Page
from app.schemas.personnel import (
    DepartmentCreate,
    DepartmentOut,
    PersonnelCreate,
    PersonnelOut,
    PersonnelUpdate,
    TrainingRecordCreate,
    TrainingRecordOut,
)

router = APIRouter(prefix="/personnel", tags=["personnel"])


# --------------------------- 组织架构 ---------------------------
@router.get("/departments", response_model=list[DepartmentOut])
def list_departments(
    db: Session = Depends(get_db), _=Depends(require_permission("personnel:read"))
):
    return db.scalars(select(Department).order_by(Department.id)).all()


@router.post("/departments", response_model=DepartmentOut, status_code=status.HTTP_201_CREATED)
def create_department(
    payload: DepartmentCreate,
    db: Session = Depends(get_db),
    _=Depends(require_permission("personnel:write")),
):
    obj = Department(**payload.model_dump())
    db.add(obj)
    db.commit()
    db.refresh(obj)
    return obj


@router.delete("/departments/{dept_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_department(
    dept_id: int,
    db: Session = Depends(get_db),
    _=Depends(require_permission("personnel:write")),
):
    obj = db.get(Department, dept_id)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    db.delete(obj)
    db.commit()


# --------------------------- 人员台账 ---------------------------
@router.get("", response_model=Page[PersonnelOut])
def list_personnel(
    db: Session = Depends(get_db),
    _=Depends(require_permission("personnel:read")),
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=200),
    q: str | None = None,
    department_id: int | None = None,
    expiring_days: int | None = Query(None, description="资质将在 N 天内到期"),
):
    stmt = select(Personnel)
    if q:
        stmt = stmt.where(
            or_(
                Personnel.name.ilike(f"%{q}%"),
                Personnel.employee_no.ilike(f"%{q}%"),
                Personnel.position.ilike(f"%{q}%"),
            )
        )
    if department_id is not None:
        stmt = stmt.where(Personnel.department_id == department_id)
    if expiring_days is not None:
        limit_date = date.today() + timedelta(days=expiring_days)
        stmt = stmt.where(
            Personnel.qualification_expiry.is_not(None),
            Personnel.qualification_expiry <= limit_date,
        )
    total = db.scalar(select(func.count()).select_from(stmt.subquery())) or 0
    rows = db.scalars(
        stmt.order_by(Personnel.id.desc()).offset((page - 1) * page_size).limit(page_size)
    ).all()
    return Page(items=rows, total=total, page=page, page_size=page_size)


@router.post("", response_model=PersonnelOut, status_code=status.HTTP_201_CREATED)
def create_personnel(
    payload: PersonnelCreate,
    db: Session = Depends(get_db),
    _=Depends(require_permission("personnel:write")),
):
    if db.scalar(select(Personnel).where(Personnel.employee_no == payload.employee_no)):
        raise HTTPException(status_code=400, detail="工号已存在 / Employee no exists")
    obj = Personnel(**payload.model_dump())
    db.add(obj)
    db.commit()
    db.refresh(obj)
    return obj


@router.get("/{pid}", response_model=PersonnelOut)
def get_personnel(
    pid: int, db: Session = Depends(get_db), _=Depends(require_permission("personnel:read"))
):
    obj = db.get(Personnel, pid)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    return obj


@router.put("/{pid}", response_model=PersonnelOut)
def update_personnel(
    pid: int,
    payload: PersonnelUpdate,
    db: Session = Depends(get_db),
    _=Depends(require_permission("personnel:write")),
):
    obj = db.get(Personnel, pid)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    for key, value in payload.model_dump(exclude_unset=True).items():
        setattr(obj, key, value)
    db.commit()
    db.refresh(obj)
    return obj


@router.delete("/{pid}", status_code=status.HTTP_204_NO_CONTENT)
def delete_personnel(
    pid: int, db: Session = Depends(get_db), _=Depends(require_permission("personnel:write"))
):
    obj = db.get(Personnel, pid)
    if not obj:
        raise HTTPException(status_code=404, detail="Not found")
    db.delete(obj)
    db.commit()


# --------------------------- 培训/考核记录 ---------------------------
@router.get("/{pid}/trainings", response_model=list[TrainingRecordOut])
def list_trainings(
    pid: int, db: Session = Depends(get_db), _=Depends(require_permission("personnel:read"))
):
    return db.scalars(
        select(TrainingRecord)
        .where(TrainingRecord.personnel_id == pid)
        .order_by(TrainingRecord.id.desc())
    ).all()


@router.post("/trainings", response_model=TrainingRecordOut, status_code=status.HTTP_201_CREATED)
def create_training(
    payload: TrainingRecordCreate,
    db: Session = Depends(get_db),
    _=Depends(require_permission("personnel:write")),
):
    if not db.get(Personnel, payload.personnel_id):
        raise HTTPException(status_code=404, detail="Personnel not found")
    obj = TrainingRecord(**payload.model_dump())
    db.add(obj)
    db.commit()
    db.refresh(obj)
    return obj
