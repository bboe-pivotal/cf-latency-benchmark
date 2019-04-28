#!/bin/sh
echo PROXY_PORT: $PROXY_PORT
echo PROXY_ADDRESS: $PROXY_ADDRESS
echo SERVER_PORT: $SERVER_PORT
echo SERVER_ADDRESS: $SERVER_ADDRESS

./mvnw clean package -DskipTests

cf d -f tcp-client
cf d -f tcp-proxy
cf d -f tcp-server
cf ds -f tcp-client-server
cf ds -f tcp-proxy-server

cf create-service metrics-forwarder unlimited latency-test-metric-forwarder

CLIENT_SERVER_JSON="{\"address\":\"$PROXY_ADDRESS\", \"port\":\"$PROXY_PORT\"}"
cf create-user-provided-service tcp-client-server -p "$CLIENT_SERVER_JSON"

PROXY_SERVER_JSON="{\"address\":\"$SERVER_ADDRESS\", \"port\":\"$SERVER_PORT\"}"
cf create-user-provided-service tcp-proxy-server -p "$PROXY_SERVER_JSON"

cf p -f tcp-test-proxy-manifest.yml --no-start

cf add-network-policy tcp-client --destination-app tcp-proxy --protocol tcp --port 8080
cf add-network-policy tcp-proxy --destination-app tcp-server --protocol tcp --port 8080

cf bs tcp-client latency-test-metric-forwarder

cf start tcp-client &
cf start tcp-proxy &
cf start tcp-server
