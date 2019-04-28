#!/bin/sh
#Deploy test apps where C2C networking is used between client->proxy and proxy->server
export PROXY_PORT=8080
export PROXY_ADDRESS=tcp-proxy.apps.internal
export PROXY_INTERNAL_HOSTNAME=tcp-proxy
export SERVER_PORT=8080
export SERVER_INTERNAL_HOSTNAME=tcp-server
export SERVER_ADDRESS=tcp-server.apps.internal

./deploy-client-proxy-server.sh

cf map-route tcp-proxy apps.internal --hostname $PROXY_INTERNAL_HOSTNAME
cf map-route tcp-server apps.internal --hostname $SERVER_INTERNAL_HOSTNAME
