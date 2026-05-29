"""Lightweight models for the remaining LIMS modules.

These provide functional ledgers/records that the generic CRUD layer exposes.
They are intentionally simple and meant to be extended module-by-module.
"""

from datetime import date, datetime

from sqlalchemy import Date, DateTime, Numeric, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base
from app.models.base import TimestampMixin


# ----------------------------- 仓库物资 / Warehouse -----------------------------
class Supplier(Base, TimestampMixin):
    __tablename__ = "suppliers"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(128))
    contact: Mapped[str | None] = mapped_column(String(128), nullable=True)
    phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)


class Material(Base, TimestampMixin):
    """物资 / 消耗品台账 (multi-level warehouse via `warehouse` field)."""

    __tablename__ = "materials"

    id: Mapped[int] = mapped_column(primary_key=True)
    code: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    name: Mapped[str] = mapped_column(String(128))
    category: Mapped[str | None] = mapped_column(String(64), nullable=True)
    unit: Mapped[str | None] = mapped_column(String(32), nullable=True)
    warehouse: Mapped[str | None] = mapped_column(String(128), nullable=True)
    quantity: Mapped[float] = mapped_column(Numeric(14, 3), default=0)
    safety_stock: Mapped[float] = mapped_column(Numeric(14, 3), default=0)
    expiry_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)


# ----------------------------- 文件管理 / Document -----------------------------
class Document(Base, TimestampMixin):
    __tablename__ = "documents"

    id: Mapped[int] = mapped_column(primary_key=True)
    doc_no: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    title: Mapped[str] = mapped_column(String(255))
    category: Mapped[str | None] = mapped_column(String(64), nullable=True)
    version: Mapped[str | None] = mapped_column(String(32), nullable=True)
    # draft / approved / published / changed / obsolete
    status: Mapped[str] = mapped_column(String(32), default="draft")
    effective_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)


# ----------------------------- 环境管理 / Environment -----------------------------
class EnvironmentRecord(Base, TimestampMixin):
    """温湿度等环境监控记录."""

    __tablename__ = "environment_records"

    id: Mapped[int] = mapped_column(primary_key=True)
    location: Mapped[str] = mapped_column(String(128))
    temperature: Mapped[float | None] = mapped_column(Numeric(6, 2), nullable=True)
    humidity: Mapped[float | None] = mapped_column(Numeric(6, 2), nullable=True)
    recorded_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    # normal / warning / alarm
    status: Mapped[str] = mapped_column(String(32), default="normal")
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)


# ----------------------------- 方法管理 / Method -----------------------------
class TestMethod(Base, TimestampMixin):
    __tablename__ = "test_methods"

    id: Mapped[int] = mapped_column(primary_key=True)
    code: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    name: Mapped[str] = mapped_column(String(255))
    standard: Mapped[str | None] = mapped_column(String(255), nullable=True)
    category: Mapped[str | None] = mapped_column(String(64), nullable=True)
    limit_value: Mapped[str | None] = mapped_column(String(255), nullable=True)
    # draft / validated / active / obsolete
    status: Mapped[str] = mapped_column(String(32), default="draft")
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)


# ----------------------------- 报告管理 / Report -----------------------------
class Report(Base, TimestampMixin):
    __tablename__ = "reports"

    id: Mapped[int] = mapped_column(primary_key=True)
    report_no: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    title: Mapped[str] = mapped_column(String(255))
    customer: Mapped[str | None] = mapped_column(String(128), nullable=True)
    template: Mapped[str | None] = mapped_column(String(64), nullable=True)
    # draft / submitted / approved / archived
    status: Mapped[str] = mapped_column(String(32), default="draft")
    issue_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)


# ----------------------------- 资源排程 / Resource scheduling -----------------------------
class ResourceBooking(Base, TimestampMixin):
    __tablename__ = "resource_bookings"

    id: Mapped[int] = mapped_column(primary_key=True)
    title: Mapped[str] = mapped_column(String(255))
    resource_type: Mapped[str] = mapped_column(String(32))  # personnel / equipment / material
    resource_name: Mapped[str | None] = mapped_column(String(128), nullable=True)
    start_time: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    end_time: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    priority: Mapped[int] = mapped_column(default=0)
    # pending / confirmed / conflict / done / cancelled
    status: Mapped[str] = mapped_column(String(32), default="pending")
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)
