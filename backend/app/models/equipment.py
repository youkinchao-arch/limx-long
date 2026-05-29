from datetime import date

from sqlalchemy import Date, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base
from app.models.base import TimestampMixin


class Equipment(Base, TimestampMixin):
    """设备台账 / Equipment ledger."""

    __tablename__ = "equipment"

    id: Mapped[int] = mapped_column(primary_key=True)
    # 设备唯一标识 used for QR/barcode generation.
    asset_no: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    name: Mapped[str] = mapped_column(String(128))
    category: Mapped[str | None] = mapped_column(String(64), nullable=True)
    model: Mapped[str | None] = mapped_column(String(128), nullable=True)
    manufacturer: Mapped[str | None] = mapped_column(String(128), nullable=True)
    serial_no: Mapped[str | None] = mapped_column(String(128), nullable=True)
    location: Mapped[str | None] = mapped_column(String(128), nullable=True)
    # 状态: in_use / idle / repair / maintenance / sealed / scrapped / borrowed
    status: Mapped[str] = mapped_column(String(32), default="idle")
    purchase_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    # 计量溯源 / 检定校准
    calibration_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    calibration_due: Mapped[date | None] = mapped_column(Date, nullable=True)
    calibration_cycle_days: Mapped[int | None] = mapped_column(nullable=True)
    remark: Mapped[str | None] = mapped_column(Text, nullable=True)
