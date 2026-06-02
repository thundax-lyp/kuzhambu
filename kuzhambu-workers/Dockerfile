FROM python:3.10-slim

ENV PYTHONUNBUFFERED=1

WORKDIR /app
COPY kuzhambu-workers/pyproject.toml ./
COPY kuzhambu-workers/src ./src

RUN python -m pip install --no-cache-dir --upgrade pip \
    && python -m pip install --no-cache-dir -e . \
    && python -m playwright install --with-deps chromium

EXPOSE 8000

CMD ["uvicorn", "kuzhambu_workers.main:app", "--host", "0.0.0.0", "--port", "8000"]
