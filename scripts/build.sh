#!/bin/bash

TAG=`git describe --tags`

echo "Remove old release build"
rm -v "./app/build/outputs/apk/release/auction-app-release-$TAG.apk"

echo "Create release build"
./gradlew assembleRelease

echo "Remove old aligned build"
rm -v "auction-app-release-unsigned-aligned-$TAG.apk"

zipalign -v -p 4 \
  "./app/build/outputs/apk/release/auction-app-release-$TAG.apk" \
  "auction-app-release-unsigned-aligned-$TAG.apk"

echo "Remove old signed build"
rm -v "auction-app-release-signed-$TAG.apk"

echo "Create signed build"
./sign-release-apk/apksigner sign --ks \
  ./sign-release-apk/auction-app-release-keystore --out \
  "auction-app-release-signed-$TAG.apk" \
  "auction-app-release-unsigned-aligned-$TAG.apk"
