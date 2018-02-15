SHELL := /bin/bash
ID := $(shell date +%Y-%m-%d--%H-%M-%S)
USERNAME ?= $(shell whoami)
JOB_DIR ?= gs://ml-sketchbook-temp/iris/trained/$(USERNAME)/$(ID)
TRAINING_SET ?= gs://ml-sketchbook-temp/iris
EXTRA_FLAGS ?=


.PHONY: help

help:
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[32m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help

train: ## Train TF model
	./iris.py --job_dir=$(JOB_DIR) --training_set=$(TRAINING_SET) --batch_size=12 $(EXTRA_FLAGS)
