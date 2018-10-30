#!/bin/sh

cd /scb-engine

echo "Check if keystore already exists"
if [ ! -f ./keystore.p12 ]
then
    echo "Keystore not found. Creating self signed certificate..."
    keytool -genkey -alias scb-engine -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650 \
    -dname "CN=SecureCodeboxEngine, OU=secure-codebox, O=SecureCodebox.io, C=DE, ST=HH, L=Hamburg" \
    -storepass "$ENGINE_KEYSTORE_PW"
else
    echo "Keystore already exists"
fi

echo "Starting scb engine..."
java -Dloader.path="./lib/,./plugins/" -jar ./app.jar
