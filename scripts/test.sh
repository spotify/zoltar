#!/usr/bin/env bash

GOAL=test

if [ "${TRAVIS_SECURE_ENV_VARS}" = "true" ]; then
    GOAL=verify

    openssl aes-256-cbc \
      -K $encrypted_cfd4364d84ec_key \
      -iv $encrypted_cfd4364d84ec_iv \
      -in scripts/data-integration-test-a20d1bb2e128.json.enc \
      -out scripts/data-integration-test-a20d1bb2e128.json \
      -d
fi

mvn -Dbigquery.project.arg="-Dbigquery.project=dummy-project" \
  clean \
  spotless:check \
  checkstyle:checkstyle \
  findbugs:findbugs \
  $GOAL
