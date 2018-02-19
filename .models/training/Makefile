SHELL := /bin/bash
ID := $(shell date +%Y-%m-%d--%H-%M-%S)
USERNAME ?= $(shell whoami)
JOB_DIR ?= gs://ml-sketchbook-temp/iris/trained/$(USERNAME)/$(ID)
TRAINING_SET ?= gs://ml-sketchbook-temp/iris
EXTRA_FLAGS ?=
IRIS_PYTHON_PKG := iris/model_serving_model_train.egg-info


.PHONY: help clean iris-tensorflow iris-xgboost
.DEFAULT_GOAL := help

help:
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[32m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

$(IRIS_PYTHON_PKG):
	cd iris; pip install -r requirements.txt

clean:
	@rm -r $(IRIS_PYTHON_PKG)

iris-tensorflow: $(IRIS_PYTHON_PKG) ## Train iris tensorflow model
	iris-tensorflow \
		--job_dir=$(JOB_DIR) \
		--training_set=$(TRAINING_SET) \
		--batch_size=12 \
		$(EXTRA_FLAGS)

iris-xgboost: $(IRIS_PYTHON_PKG) ## Train iris xgboost model
	iris-xgboost \
		--job_dir=$(JOB_DIR) \
		--training_set=$(TRAINING_SET) \
		--batch_size=12 \
		$(EXTRA_FLAGS)
