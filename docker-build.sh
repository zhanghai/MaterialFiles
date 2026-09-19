#!/bin/bash

# Default build type is debug
BUILD_TYPE=${1:-debug}
IMAGE_NAME="material-files-builder"

if ! docker info > /dev/null 2>&1; then
    echo "Docker daemon is not running."
    exit 1
fi

echo "Building Docker image ${IMAGE_NAME}..."
docker build --platform linux/amd64 -t ${IMAGE_NAME} .

echo "Running build for ${BUILD_TYPE}..."
# Mount current directory to /app in container.
# Output files will be written back to the host system.
docker run --rm \
    --platform linux/amd64 \
    -v "$(pwd):/app" \
    -w /app \
    -e STORE_FILE="${STORE_FILE}" \
    -e STORE_PASSWORD="${STORE_PASSWORD}" \
    -e KEY_ALIAS="${KEY_ALIAS}" \
    -e KEY_PASSWORD="${KEY_PASSWORD}" \
    ${IMAGE_NAME} \
    ./gradlew assemble$(echo ${BUILD_TYPE} | awk '{print toupper(substr($0,1,1)) substr($0,2)}')

if [ $? -eq 0 ]; then
    echo "Build successful!"
else
    echo "Build failed!"
    exit 1
fi
