#!/bin/sh
#Deploy test apps where tcp router is used client->proxy and C2C for proxy->server
#cf create-route <space> cf-tcpapps.io --random-port
export PROXY_PORT=3395
export PROXY_ADDRESS=cf-tcpapps.io
export SERVER_PORT=8080
export SERVER_INTERNAL_HOSTNAME=tcp-server
export SERVER_ADDRESS=tcp-server.apps.internal

./deploy-client-proxy-server.sh

cf map-route tcp-proxy $PROXY_ADDRESS --port $PROXY_PORT
cf map-route tcp-server apps.internal --hostname $SERVER_INTERNAL_HOSTNAME
