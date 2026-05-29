from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.security import hash_password
from app.models.user import Role, User

# Permission catalogue (module:action). "*" grants everything.
MODULE_PERMISSIONS = [
    "user",
    "personnel",
    "equipment",
    "warehouse",
    "document",
    "environment",
    "method",
    "report",
    "resource",
]


def _all_permissions() -> str:
    perms: list[str] = []
    for module in MODULE_PERMISSIONS:
        perms.append(f"{module}:read")
        perms.append(f"{module}:write")
    return ",".join(perms)


def _readonly_permissions() -> str:
    return ",".join(f"{m}:read" for m in MODULE_PERMISSIONS)


def init_roles(db: Session) -> dict[str, Role]:
    roles_spec = {
        "admin": ("系统管理员 / Administrator", "*"),
        "manager": ("实验室管理员 / Lab Manager", _all_permissions()),
        "operator": ("实验员 / Operator", _readonly_permissions()),
    }
    result: dict[str, Role] = {}
    for code, (name, perms) in roles_spec.items():
        role = db.scalar(select(Role).where(Role.code == code))
        if not role:
            role = Role(code=code, name=name, permissions=perms)
            db.add(role)
        else:
            role.permissions = perms
            role.name = name
        result[code] = role
    db.commit()
    for r in result.values():
        db.refresh(r)
    return result


def init_admin(db: Session, admin_role: Role) -> None:
    existing = db.scalar(select(User).where(User.username == settings.first_admin_username))
    if existing:
        return
    user = User(
        username=settings.first_admin_username,
        full_name=settings.first_admin_name,
        hashed_password=hash_password(settings.first_admin_password),
        is_superuser=True,
        is_active=True,
    )
    user.roles = [admin_role]
    db.add(user)
    db.commit()


def bootstrap(db: Session) -> None:
    roles = init_roles(db)
    init_admin(db, roles["admin"])
