#!/usr/bin/env bash

RED='\033[0;31m' # Red color
GREEN='\033[0;32m' # Green color
NC='\033[0m' # No Color

echo -e "${GREEN}>>> Run the test server ${NC}"
cd Adjust
./gradlew connectedAndroidTest


