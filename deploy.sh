#!/bin/bash

echo 'VERSION=${project.version}' | mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate | grep '^VERSION' | grep -q '\-SNAPSHOT'

if [ $? -eq 0 ] && [ "$TRAVIS_BRANCH" == "master" ]; then
    mvn deploy --settings settings.xml -DskipTests=true
fi
