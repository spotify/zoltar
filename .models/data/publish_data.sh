#!/bin/bash

set -o nounset
set -o errexit
set -o pipefail

DEST_TABLE=${1:-"ml-sketchbook:zoltar.iris"}
TEMP_DIR=$(mktemp -d)

wget --directory-prefix=$TEMP_DIR https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data 
sed '$d' "$TEMP_DIR/iris.data" > "$TEMP_DIR/iris_clean.data"

bq load $DEST_TABLE "$TEMP_DIR/iris_clean.data" sepal_length:float,sepal_width:float,petal_length:float,petal_width:float,class_name:string
