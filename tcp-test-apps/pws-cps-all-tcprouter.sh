#!/bin/sh
#Deploy test apps where tcp router is used between client->proxy and proxy->server
#cf create-route <space> cf-tcpapps.io --random-port
export PROXY_PORT=3395
export PROXY_ADDRESS=cf-tcpapps.io
export SERVER_PORT=3336
export SERVER_ADDRESS=cf-tcpapps.io

./deploy-client-proxy-server.sh

cf map-route tcp-proxy $PROXY_ADDRESS --port $PROXY_PORT
cf map-route tcp-server $SERVER_ADDRESS --port $SERVER_PORT
