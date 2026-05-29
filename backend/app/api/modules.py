"""Routers for the remaining modules, built from the generic CRUD factory."""

from fastapi import APIRouter

from app.api.crud_factory import build_crud_router
from app.models.modules import (
    Document,
    EnvironmentRecord,
    Material,
    Report,
    ResourceBooking,
    Supplier,
    TestMethod,
)
from app.schemas.modules import (
    DocumentIn,
    DocumentOut,
    EnvironmentRecordIn,
    EnvironmentRecordOut,
    MaterialIn,
    MaterialOut,
    ReportIn,
    ReportOut,
    ResourceBookingIn,
    ResourceBookingOut,
    SupplierIn,
    SupplierOut,
    TestMethodIn,
    TestMethodOut,
)

router = APIRouter()

# 仓库物资
router.include_router(
    build_crud_router(
        model=Supplier, create_schema=SupplierIn, update_schema=SupplierIn,
        out_schema=SupplierOut, prefix="/warehouse/suppliers", tags=["warehouse"],
        permission_prefix="warehouse",
    )
)
router.include_router(
    build_crud_router(
        model=Material, create_schema=MaterialIn, update_schema=MaterialIn,
        out_schema=MaterialOut, prefix="/warehouse/materials", tags=["warehouse"],
        permission_prefix="warehouse",
    )
)

# 文件管理
router.include_router(
    build_crud_router(
        model=Document, create_schema=DocumentIn, update_schema=DocumentIn,
        out_schema=DocumentOut, prefix="/documents", tags=["document"],
        permission_prefix="document",
    )
)

# 环境管理
router.include_router(
    build_crud_router(
        model=EnvironmentRecord, create_schema=EnvironmentRecordIn,
        update_schema=EnvironmentRecordIn, out_schema=EnvironmentRecordOut,
        prefix="/environment/records", tags=["environment"],
        permission_prefix="environment",
    )
)

# 方法管理
router.include_router(
    build_crud_router(
        model=TestMethod, create_schema=TestMethodIn, update_schema=TestMethodIn,
        out_schema=TestMethodOut, prefix="/methods", tags=["method"],
        permission_prefix="method",
    )
)

# 报告管理
router.include_router(
    build_crud_router(
        model=Report, create_schema=ReportIn, update_schema=ReportIn,
        out_schema=ReportOut, prefix="/reports", tags=["report"],
        permission_prefix="report",
    )
)

# 资源排程
router.include_router(
    build_crud_router(
        model=ResourceBooking, create_schema=ResourceBookingIn,
        update_schema=ResourceBookingIn, out_schema=ResourceBookingOut,
        prefix="/resources/bookings", tags=["resource"],
        permission_prefix="resource",
    )
)
