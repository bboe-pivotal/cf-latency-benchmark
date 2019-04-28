#!/bin/sh
#Deploy test apps where gorouter is used client->proxy and C2C for proxy->server
export PROXY_HOST=http-test-proxy
export PROXY_DOMAIN=cfapps.io
export SERVER_HOST=http-test-server
export SERVER_DOMAIN=apps.internal
export CLIENT_SERVER_URL=http://http-test-proxy.cfapps.io
export PROXY_SERVER_URL=http://http-test-server.apps.internal:8080

./deploy-client-proxy-server.sh
