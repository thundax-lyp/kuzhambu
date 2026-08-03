# Image Files

This directory stores local Docker image archives produced by `docker save`.

The archives are intentionally ignored by Git because they are large binary delivery files. Keep only the directory contract in the repository.

Use one archive per Docker image so later stages can replace or patch images independently.

Typical files:

```sh
kuzhambu-admin-web-dev.tar
kuzhambu-portal-web-dev.tar
kuzhambu-admin-starter-dev.tar
kuzhambu-portal-starter-dev.tar
kuzhambu-workers-dev.tar
```
