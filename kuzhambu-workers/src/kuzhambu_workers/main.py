from fastapi import FastAPI

from kuzhambu_workers import __version__


def create_app() -> FastAPI:
    app = FastAPI(
        title="Kuzhambu Workers",
        version=__version__,
        docs_url=None,
        redoc_url=None,
    )

    @app.get("/internal/health")
    def health() -> dict[str, str]:
        return {
            "status": "UP",
            "service": "kuzhambu-workers",
            "version": __version__,
        }

    return app


app = create_app()
