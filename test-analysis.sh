#!/usr/bin/env bash

RED='\033[0;31m' # Red color
GREEN='\033[0;32m' # Green color
NC='\033[0m' # No Color
ADID=9166f13cb531a27021b7d0442463f89e
APP_TOKEN=2fm9gkqubvpc

echo -e "${GREEN}>>> Running adb uninstall ${NC}"
adb uninstall com.adjust.example

echo -e "${GREEN}>>> Forgetting device (in case the device was used elsewhere${NC}"
curl http://app.adjust.com/forget_device\?app_token\=${APP_TOKEN}\&adid\=${ADID}; echo 

echo -e "${GREEN}>>> Run the test server ${NC}"
cd ~/Dev/go
./bin/analyzer_rest_server-example &

cd ~/Dev/android_sdk/Adjust
./gradlew example:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.adjust.example.BasicTimeoutTest

echo -e "${GREEN}>>> Terminate server test ${NC}"
fg
curl -X POST http://0.0.0.0:8081/terminate
