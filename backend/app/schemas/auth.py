from pydantic import BaseModel


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"


class LoginRequest(BaseModel):
    username: str
    password: str


class RoleOut(BaseModel):
    id: int
    code: str
    name: str
    permissions: list[str]

    model_config = {"from_attributes": True}


class UserOut(BaseModel):
    id: int
    username: str
    full_name: str
    email: str | None = None
    is_active: bool
    is_superuser: bool
    roles: list[RoleOut] = []
    permissions: list[str] = []

    model_config = {"from_attributes": True}


class UserCreate(BaseModel):
    username: str
    full_name: str
    password: str
    email: str | None = None
    role_ids: list[int] = []
    is_superuser: bool = False


class UserUpdate(BaseModel):
    full_name: str | None = None
    email: str | None = None
    password: str | None = None
    is_active: bool | None = None
    role_ids: list[int] | None = None
