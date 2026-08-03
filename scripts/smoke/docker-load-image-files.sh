#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
IMAGE_FILES_DIR="${1:-${KUZHAMBU_IMAGE_FILES_DIR:-${REPO_ROOT}/deploy/image-files}}"

if [[ ! -d "${IMAGE_FILES_DIR}" ]]; then
    echo "Missing image files directory: ${IMAGE_FILES_DIR}" >&2
    exit 1
fi

shopt -s nullglob
archives=("${IMAGE_FILES_DIR}"/*.tar)
shopt -u nullglob

if (( ${#archives[@]} == 0 )); then
    echo "No Docker image archives found in ${IMAGE_FILES_DIR}" >&2
    exit 1
fi

for archive in "${archives[@]}"; do
    echo "[docker-load-image-files] loading $(basename "${archive}")"
    docker load -i "${archive}" >/dev/null
done

echo "Docker image files loaded"
