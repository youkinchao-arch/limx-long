from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    project_name: str = "Hongqiao LIMS"
    api_v1_prefix: str = "/api/v1"

    # Default to local Postgres; override with DATABASE_URL in production.
    database_url: str = "postgresql+psycopg://lims:lims@localhost:5432/lims"

    secret_key: str = "change-me-in-production-please-use-a-long-random-string"
    access_token_expire_minutes: int = 60 * 24
    algorithm: str = "HS256"

    # Comma-separated list of allowed CORS origins.
    cors_origins: str = "http://localhost:5173,http://localhost:3000"

    # Default super admin bootstrapped on first run.
    first_admin_username: str = "admin"
    first_admin_password: str = "admin123"
    first_admin_name: str = "系统管理员"

    @property
    def cors_origin_list(self) -> list[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
