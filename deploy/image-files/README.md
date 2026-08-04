# Image Files

This directory stores local Docker image archives produced by `docker save`.

The archives are intentionally ignored by Git because they are large binary delivery files. Keep only the directory contract in the repository.

Use one archive per Docker image so later stages can replace or patch images independently.

## Contract

Image files are release artifacts, not source files. They are handled in two steps:

1. A build host builds, imports, or retags images to the standard compose references, then saves one tar per image into this directory.
2. A deploy host receives this directory, loads every tar with Docker, then starts or smokes the compose stack without depending on Docker registry access.

The source used to obtain an image can change with network conditions. The saved image tag should still match the standard compose reference unless the deploy environment intentionally overrides it in `deploy/.env`.

## Make

From the repository root on a build host, first build, pull, import, or retag the images required by `deploy/docker-compose.yml`. Then save the standard references into this directory:

```sh
mkdir -p deploy/image-files
for image in admin-web portal-web admin-starter portal-starter workers; do
  docker save "kuzhambu/${image}:dev" -o "deploy/image-files/kuzhambu-${image}-dev.tar"
done
docker save nginx:1.27-alpine -o deploy/image-files/foundation-nginx-1.27-alpine.tar
docker save mysql:8.4 -o deploy/image-files/foundation-mysql-8.4.tar
docker save redis:7.2 -o deploy/image-files/foundation-redis-7.2.tar
docker save kuzhambu/elasticsearch:8.18.8 -o deploy/image-files/foundation-elasticsearch-8.18.8.tar
docker save apache/rocketmq:5.4.0 -o deploy/image-files/foundation-rocketmq-5.4.0.tar
```

Typical files:

```sh
kuzhambu-admin-web-dev.tar
kuzhambu-portal-web-dev.tar
kuzhambu-admin-starter-dev.tar
kuzhambu-portal-starter-dev.tar
kuzhambu-workers-dev.tar
foundation-nginx-1.27-alpine.tar
foundation-mysql-8.4.tar
foundation-redis-7.2.tar
foundation-elasticsearch-8.18.8.tar
foundation-rocketmq-5.4.0.tar
```

## Load

From the repository root on a deploy host:

```sh
scripts/smoke/load-image-files.sh deploy/image-files
```

The script fails if the directory is missing or contains no `*.tar` archives.

## Verify

```sh
for image in \
  kuzhambu/admin-web:dev \
  kuzhambu/portal-web:dev \
  kuzhambu/admin-starter:dev \
  kuzhambu/portal-starter:dev \
  kuzhambu/workers:dev \
  nginx:1.27-alpine \
  mysql:8.4 \
  redis:7.2 \
  kuzhambu/elasticsearch:8.18.8 \
  apache/rocketmq:5.4.0; do
  docker image inspect "$image" >/dev/null && echo "OK $image"
done
```
