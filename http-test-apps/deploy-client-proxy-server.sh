#!/bin/sh
echo PROXY_HOST: $PROXY_HOST
echo PROXY_DOMAIN: $PROXY_DOMAIN
echo SERVER_HOST: $SERVER_HOST
echo SERVER_DOMAIN: $SERVER_DOMAIN
echo CLIENT_SERVER_URL: $CLIENT_SERVER_URL
echo PROXY_SERVER_URL: $PROXY_SERVER_URL

./mvnw clean package -DskipTests

cf d -f http-client
cf d -f http-proxy
cf d -f http-server
cf ds -f http-client-server
cf ds -f http-proxy-server

CLIENT_SERVER_JSON={\"url\":\"$CLIENT_SERVER_URL\"}
cf create-user-provided-service http-client-server -p $CLIENT_SERVER_JSON

PROXY_SERVER_JSON={\"url\":\"$PROXY_SERVER_URL\"}
cf create-user-provided-service http-proxy-server -p $PROXY_SERVER_JSON

cf p -f http-test-proxy-manifest.yml --no-start

cf map-route http-proxy $PROXY_DOMAIN --hostname $PROXY_HOST
cf map-route http-server $SERVER_DOMAIN --hostname $SERVER_HOST

cf add-network-policy http-client --destination-app http-proxy --protocol tcp --port 8080
cf add-network-policy http-proxy --destination-app http-server --protocol tcp --port 8080

cf start http-client &
cf start http-proxy &
cf start http-server
