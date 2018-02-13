#!/bin/bash

set -o nounset
set -o errexit
set -o pipefail

DEST_TABLE=${1:-"ml-sketchbook:model_serving.iris"}
TEMP_DIR=$(mktemp -d)

wget --directory-prefix=$TEMP_DIR https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data 
sed '$d' "$TEMP_DIR/iris.data" > "$TEMP_DIR/iris_clean.data"

bq load $DEST_TABLE "$TEMP_DIR/iris_clean.data" sepal_length_cm:float,sepal_width_cm:float,petal_length_cm:float,petal_width_cm:float,class:string
