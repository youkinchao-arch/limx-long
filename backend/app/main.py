from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# Import models so they are registered with the metadata.
import app.models  # noqa: F401
from app.api import auth, dashboard, equipment, modules, personnel
from app.core.config import settings
from app.core.database import Base, SessionLocal, engine
from app.core.init_data import bootstrap


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        bootstrap(db)
    finally:
        db.close()
    yield


app = FastAPI(title=settings.project_name, lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origin_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/api/health")
def health():
    return {"status": "ok", "service": settings.project_name}


api_prefix = settings.api_v1_prefix
app.include_router(auth.router, prefix=api_prefix)
app.include_router(personnel.router, prefix=api_prefix)
app.include_router(equipment.router, prefix=api_prefix)
app.include_router(dashboard.router, prefix=api_prefix)
app.include_router(modules.router, prefix=api_prefix)
