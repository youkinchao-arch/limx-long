from datetime import date

from sqlalchemy import Date, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base
from app.models.base import TimestampMixin


class Department(Base, TimestampMixin):
    """组织架构 / Organization structure (self-referential tree)."""

    __tablename__ = "departments"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(128))
    code: Mapped[str | None] = mapped_column(String(64), nullable=True)
    parent_id: Mapped[int | None] = mapped_column(
        ForeignKey("departments.id", ondelete="SET NULL"), nullable=True
    )
    children: Mapped[list["Department"]] = relationship(
        backref="parent", remote_side=lambda: [Department.id]
    )


class Personnel(Base, TimestampMixin):
    """人员台账 / Personnel ledger."""

    __tablename__ = "personnel"

    id: Mapped[int] = mapped_column(primary_key=True)
    employee_no: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    name: Mapped[str] = mapped_column(String(128))
    gender: Mapped[str | None] = mapped_column(String(16), nullable=True)
    department_id: Mapped[int | None] = mapped_column(
        ForeignKey("departments.id", ondelete="SET NULL"), nullable=True
    )
    position: Mapped[str | None] = mapped_column(String(128), nullable=True)
    phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    email: Mapped[str | None] = mapped_column(String(128), nullable=True)
    hire_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    # 资质到期日 used for expiry reminders.
    qualification: Mapped[str | None] = mapped_column(String(255), nullable=True)
    qualification_expiry: Mapped[date | None] = mapped_column(Date, nullable=True)
    status: Mapped[str] = mapped_column(String(32), default="active")
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)

    department: Mapped[Department | None] = relationship(lazy="selectin")
    training_records: Mapped[list["TrainingRecord"]] = relationship(
        back_populates="personnel", cascade="all, delete-orphan"
    )


class TrainingRecord(Base, TimestampMixin):
    """培训 / 考核记录 (Training & assessment record)."""

    __tablename__ = "training_records"

    id: Mapped[int] = mapped_column(primary_key=True)
    personnel_id: Mapped[int] = mapped_column(
        ForeignKey("personnel.id", ondelete="CASCADE")
    )
    title: Mapped[str] = mapped_column(String(255))
    category: Mapped[str | None] = mapped_column(String(64), nullable=True)
    train_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    result: Mapped[str | None] = mapped_column(String(64), nullable=True)
    score: Mapped[str | None] = mapped_column(String(32), nullable=True)
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)

    personnel: Mapped[Personnel] = relationship(back_populates="training_records")
