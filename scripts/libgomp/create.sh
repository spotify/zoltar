#!/usr/bin/env bash

# Builds a debian docker image with libgomp1 installed and copies the binary to the output path.

set -o errexit
set -o nounset
set -o pipefail

DIR=$(dirname "$0")
OUTPUT_PATH=$DIR/../../zoltar-xgboost/src/main/resources/lib/libgomp.so.1
IMAGE_NAME=debian-libgomp1

docker build -t $IMAGE_NAME $DIR

docker cp -L $(docker create $IMAGE_NAME):/lib/x86_64-linux-gnu/libgomp.so.1 $OUTPUT_PATH
