#!/bin/bash

./gradlew -Pprod clean bootJar
chmod a+x scripts/*

java -jar build/libs/cronmanager-0.0.1-SNAPSHOT.jar