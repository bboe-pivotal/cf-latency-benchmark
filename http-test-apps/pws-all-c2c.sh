#!/bin/sh
#Deploy test apps where C2C networking is used between client->proxy and proxy->server
export PROXY_HOST=http-test-proxy
export PROXY_DOMAIN=apps.internal
export SERVER_HOST=http-test-server
export SERVER_DOMAIN=apps.internal
export CLIENT_SERVER_URL=http://http-test-proxy.apps.internal:8080
export PROXY_SERVER_URL=http://http-test-server.apps.internal:8080

./deploy-client-proxy-server.sh
