from datetime import date

from pydantic import BaseModel


class DepartmentBase(BaseModel):
    name: str
    code: str | None = None
    parent_id: int | None = None


class DepartmentCreate(DepartmentBase):
    pass


class DepartmentOut(DepartmentBase):
    id: int

    model_config = {"from_attributes": True}


class PersonnelBase(BaseModel):
    employee_no: str
    name: str
    gender: str | None = None
    department_id: int | None = None
    position: str | None = None
    phone: str | None = None
    email: str | None = None
    hire_date: date | None = None
    qualification: str | None = None
    qualification_expiry: date | None = None
    status: str = "active"
    remark: str | None = None


class PersonnelCreate(PersonnelBase):
    pass


class PersonnelUpdate(BaseModel):
    name: str | None = None
    gender: str | None = None
    department_id: int | None = None
    position: str | None = None
    phone: str | None = None
    email: str | None = None
    hire_date: date | None = None
    qualification: str | None = None
    qualification_expiry: date | None = None
    status: str | None = None
    remark: str | None = None


class PersonnelOut(PersonnelBase):
    id: int

    model_config = {"from_attributes": True}


class TrainingRecordBase(BaseModel):
    personnel_id: int
    title: str
    category: str | None = None
    train_date: date | None = None
    result: str | None = None
    score: str | None = None
    remark: str | None = None


class TrainingRecordCreate(TrainingRecordBase):
    pass


class TrainingRecordOut(TrainingRecordBase):
    id: int

    model_config = {"from_attributes": True}
