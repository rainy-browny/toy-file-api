#!/bin/sh

CONTAINER_NAME="toy-file-api-mongo"
docker rm -f ${CONTAINER_NAME}
docker run -dt --name ${CONTAINER_NAME} \
    -p 27017:27017 \
    -v toyFileApiMongo:/data/db \
    mongo