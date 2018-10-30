#!/bin/bash

cd /scb-engine

if [ ! -f keystore.p12 ]; then
    keytool -genkey -alias scb-engine -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650 \
    -dname "CN=SecureCodeboxEngine, OU=secure-codebox, O=SecureCodebox.io, C=DE, ST=HH, L=Hamburg" \
    -storepass "$ENGINE_KEYSTORE_PW"
fi

java -Dloader.path="./lib/,./plugins/" -jar ./app.jar
