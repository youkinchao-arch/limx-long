from typing import Any

from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel
from sqlalchemy import String, func, or_, select
from sqlalchemy.orm import Session

from app.api.deps import require_permission
from app.core.database import Base, get_db
from app.schemas.common import Page


def build_crud_router(
    *,
    model: type[Base],
    create_schema: type[BaseModel],
    update_schema: type[BaseModel],
    out_schema: type[BaseModel],
    prefix: str,
    tags: list[str],
    permission_prefix: str,
) -> APIRouter:
    """Generate a standard list/get/create/update/delete router for a model."""

    router = APIRouter(prefix=prefix, tags=tags)

    string_columns = [
        c for c in model.__table__.columns if isinstance(c.type, String)
    ]

    @router.get("", response_model=Page[out_schema])
    def list_items(
        db: Session = Depends(get_db),
        _: Any = Depends(require_permission(f"{permission_prefix}:read")),
        page: int = Query(1, ge=1),
        page_size: int = Query(20, ge=1, le=200),
        q: str | None = Query(None, description="Keyword search"),
    ):
        stmt = select(model)
        if q and string_columns:
            stmt = stmt.where(or_(*[col.ilike(f"%{q}%") for col in string_columns]))
        total = db.scalar(select(func.count()).select_from(stmt.subquery())) or 0
        rows = db.scalars(
            stmt.order_by(model.id.desc()).offset((page - 1) * page_size).limit(page_size)
        ).all()
        return Page(items=rows, total=total, page=page, page_size=page_size)

    @router.get("/{item_id}", response_model=out_schema)
    def get_item(
        item_id: int,
        db: Session = Depends(get_db),
        _: Any = Depends(require_permission(f"{permission_prefix}:read")),
    ):
        obj = db.get(model, item_id)
        if not obj:
            raise HTTPException(status_code=404, detail="Not found")
        return obj

    @router.post("", response_model=out_schema, status_code=status.HTTP_201_CREATED)
    def create_item(
        payload: create_schema,  # type: ignore[valid-type]
        db: Session = Depends(get_db),
        _: Any = Depends(require_permission(f"{permission_prefix}:write")),
    ):
        obj = model(**payload.model_dump())
        db.add(obj)
        db.commit()
        db.refresh(obj)
        return obj

    @router.put("/{item_id}", response_model=out_schema)
    def update_item(
        item_id: int,
        payload: update_schema,  # type: ignore[valid-type]
        db: Session = Depends(get_db),
        _: Any = Depends(require_permission(f"{permission_prefix}:write")),
    ):
        obj = db.get(model, item_id)
        if not obj:
            raise HTTPException(status_code=404, detail="Not found")
        for key, value in payload.model_dump(exclude_unset=True).items():
            setattr(obj, key, value)
        db.commit()
        db.refresh(obj)
        return obj

    @router.delete("/{item_id}", status_code=status.HTTP_204_NO_CONTENT)
    def delete_item(
        item_id: int,
        db: Session = Depends(get_db),
        _: Any = Depends(require_permission(f"{permission_prefix}:write")),
    ):
        obj = db.get(model, item_id)
        if not obj:
            raise HTTPException(status_code=404, detail="Not found")
        db.delete(obj)
        db.commit()

    return router
