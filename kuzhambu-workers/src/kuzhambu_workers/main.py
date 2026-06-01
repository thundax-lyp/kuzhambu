from fastapi import FastAPI

from kuzhambu_workers import __version__
from kuzhambu_workers.api.health_routes import router as health_router


def create_app() -> FastAPI:
    app = FastAPI(
        title="Kuzhambu Workers",
        version=__version__,
        docs_url=None,
        redoc_url=None,
    )

    app.include_router(health_router)

    return app


app = create_app()
