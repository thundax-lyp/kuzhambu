import asyncio
from contextlib import asynccontextmanager

from fastapi import FastAPI

from kuzhambu_workers import __version__
from kuzhambu_workers.api.ai_routes import router as ai_router
from kuzhambu_workers.api.artifact_routes import router as artifact_router
from kuzhambu_workers.api.health_routes import router as health_router
from kuzhambu_workers.api.openai_routes import router as openai_router
from kuzhambu_workers.api.render_routes import router as render_router
from kuzhambu_workers.core.config import load_settings
from kuzhambu_workers.render.artifact_store import cleanup_expired_artifacts


async def _artifact_cleanup_loop() -> None:
    settings = load_settings()
    while True:
        cleanup_expired_artifacts(settings.temp_dir)
        await asyncio.sleep(settings.artifact_cleanup_interval_seconds)


@asynccontextmanager
async def lifespan(_: FastAPI):
    task = asyncio.create_task(_artifact_cleanup_loop())
    try:
        yield
    finally:
        task.cancel()
        try:
            await task
        except asyncio.CancelledError:
            pass


def create_app() -> FastAPI:
    app = FastAPI(
        title="Kuzhambu Workers",
        version=__version__,
        lifespan=lifespan,
        openapi_url="/internal/openapi.json",
        docs_url="/internal/docs",
        redoc_url="/internal/redoc",
        openapi_tags=[
            {"name": "AI", "description": "Unified internal AI execution interfaces."},
            {
                "name": "OpenAI Compatible",
                "description": "Internal OpenAI-compatible facade interfaces.",
            },
            {
                "name": "Artifacts",
                "description": "Internal temporary artifact download interfaces.",
            },
            {"name": "Render", "description": "Render usecase interfaces."},
            {"name": "Health", "description": "Health and capability probes."},
        ],
    )

    app.include_router(ai_router)
    app.include_router(artifact_router)
    app.include_router(health_router)
    app.include_router(openai_router)
    app.include_router(render_router)

    return app


app = create_app()
