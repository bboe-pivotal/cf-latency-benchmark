#!/bin/sh
#Deploy test apps where gorouter is used between client->proxy and proxy->server
export PROXY_HOST=http-test-proxy
export PROXY_DOMAIN=cfapps.io
export SERVER_HOST=http-test-server
export SERVER_DOMAIN=cfapps.io
export CLIENT_SERVER_URL=http://http-test-proxy.cfapps.io
export PROXY_SERVER_URL=http://http-test-server.cfapps.io

./deploy-client-proxy-server.sh