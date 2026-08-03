ARG KUZHAMBU_WEB_BUILD_IMAGE=node:22-bookworm-slim
ARG KUZHAMBU_WEB_RUNTIME_IMAGE=nginx:1.27-alpine

FROM ${KUZHAMBU_WEB_BUILD_IMAGE} AS build

ARG APP_NAME
ARG COREPACK_NPM_REGISTRY=
ARG NPM_CONFIG_REGISTRY=
ARG VITE_ADMIN_WEB_BASE=/
ARG VITE_ADMIN_API_BASE_URL=/kuzhambu-admin-api/api
ARG VITE_PORTAL_WEB_BASE=/
ARG VITE_PORTAL_API_BASE_URL=/kuzhambu-api/api

ENV COREPACK_NPM_REGISTRY=${COREPACK_NPM_REGISTRY}
ENV NPM_CONFIG_REGISTRY=${NPM_CONFIG_REGISTRY}
ENV PNPM_HOME=/pnpm
ENV PNPM_STORE_DIR=/pnpm/store
ENV PATH=${PNPM_HOME}:${PATH}
ENV VITE_ADMIN_WEB_BASE=${VITE_ADMIN_WEB_BASE}
ENV VITE_ADMIN_API_BASE_URL=${VITE_ADMIN_API_BASE_URL}
ENV VITE_PORTAL_WEB_BASE=${VITE_PORTAL_WEB_BASE}
ENV VITE_PORTAL_API_BASE_URL=${VITE_PORTAL_API_BASE_URL}

USER root

WORKDIR /workspace/kuzhambu-apps
COPY kuzhambu-apps/package.json kuzhambu-apps/pnpm-lock.yaml kuzhambu-apps/pnpm-workspace.yaml ./
COPY kuzhambu-apps/admin-web/package.json ./admin-web/
COPY kuzhambu-apps/portal-web/package.json ./portal-web/
RUN --mount=type=cache,target=/pnpm/store,sharing=locked corepack enable \
    && pnpm config set store-dir ${PNPM_STORE_DIR} \
    && pnpm install --frozen-lockfile

COPY kuzhambu-apps/ ./
RUN pnpm --filter ./${APP_NAME} run build

FROM ${KUZHAMBU_WEB_RUNTIME_IMAGE}

ARG APP_NAME

COPY deploy/nginx/frontend.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/kuzhambu-apps/${APP_NAME}/dist /usr/share/nginx/html

EXPOSE 80
