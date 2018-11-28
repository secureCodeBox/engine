#!/bin/sh

cd /scb-engine

create_self_signed_certificate()
{
    echo "Creating self signed certificate..."
    keytool -genkey -alias scb-engine -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650 \
    -dname "CN=secureCodeBoxEngine, OU=secureCodeBox.io, O=secureCodeBox.io, C=DE, ST=HH, L=Hamburg" \
    -storepass "${SERVER_SSL_KEY_STORE_PASSWORD}"
}

create_certificate_if_not_available()
{
    echo "Check if keystore already exists"
    if [ ! -f ./keystore.p12 ]
    then
        echo "Keystore not found."
        create_self_signed_certificate
    else
        echo "Keystore already exists"
    fi
}

echo "Check if HTTPS is enabled..."
if [ "${SERVER_SSL_ENABLED}" == "true" ]
then
    echo "Https enabled"
    create_certificate_if_not_available
else
    echo "No HTTPS enabled. You can use environment variables to enable HTTPS."
fi

# required for using taskrole in aws fargate service, because default its only available for pid 1
export AWS_CONTAINER_CREDENTIALS_RELATIVE_URI

echo "Starting secureCodeBox engine..."
java -Dloader.path="./lib/,./plugins/" -jar ./app.jar
