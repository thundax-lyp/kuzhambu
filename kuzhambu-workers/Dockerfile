ARG KUZHAMBU_WORKERS_BASE_IMAGE=python:3.10-slim

FROM ${KUZHAMBU_WORKERS_BASE_IMAGE}

ARG PIP_INDEX_URL=
ARG PIP_TRUSTED_HOST=

ENV PIP_INDEX_URL=${PIP_INDEX_URL}
ENV PIP_TRUSTED_HOST=${PIP_TRUSTED_HOST}
ENV PYTHONUNBUFFERED=1

WORKDIR /app
COPY kuzhambu-workers/pyproject.toml ./
COPY kuzhambu-workers/src ./src

RUN --mount=type=cache,target=/root/.cache/pip,sharing=locked python -m pip install --upgrade pip \
    && python -m pip install -e . \
    && python -m playwright install --with-deps chromium

EXPOSE 8000

CMD ["uvicorn", "kuzhambu_workers.main:app", "--host", "0.0.0.0", "--port", "8000"]
