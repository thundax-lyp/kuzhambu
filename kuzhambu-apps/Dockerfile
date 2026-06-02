FROM node:22-bookworm-slim AS build

ARG APP_NAME
ARG VITE_ADMIN_WEB_BASE=/
ARG VITE_ADMIN_API_BASE_URL=/admin-api/api
ARG VITE_PORTAL_WEB_BASE=/
ARG VITE_PORTAL_API_BASE_URL=/portal-api

ENV VITE_ADMIN_WEB_BASE=${VITE_ADMIN_WEB_BASE}
ENV VITE_ADMIN_API_BASE_URL=${VITE_ADMIN_API_BASE_URL}
ENV VITE_PORTAL_WEB_BASE=${VITE_PORTAL_WEB_BASE}
ENV VITE_PORTAL_API_BASE_URL=${VITE_PORTAL_API_BASE_URL}

WORKDIR /workspace/kuzhambu-apps
COPY kuzhambu-apps/package*.json ./
COPY kuzhambu-apps/admin-web/package*.json ./admin-web/
COPY kuzhambu-apps/portal-web/package*.json ./portal-web/
RUN npm ci

COPY kuzhambu-apps/ ./
RUN npm --workspace ${APP_NAME} run build

FROM nginx:1.27-alpine

COPY deploy/nginx/frontend.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/kuzhambu-apps/${APP_NAME}/dist /usr/share/nginx/html

EXPOSE 80
