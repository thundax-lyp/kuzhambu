from fastapi import FastAPI

from kuzhambu_workers import __version__
from kuzhambu_workers.api.ai_routes import router as ai_router
from kuzhambu_workers.api.health_routes import router as health_router
from kuzhambu_workers.api.render_routes import router as render_router


def create_app() -> FastAPI:
    app = FastAPI(
        title="Kuzhambu Workers",
        version=__version__,
        openapi_url="/internal/openapi.json",
        docs_url="/internal/docs",
        redoc_url="/internal/redoc",
    )

    app.include_router(ai_router)
    app.include_router(health_router)
    app.include_router(render_router)

    return app


app = create_app()
