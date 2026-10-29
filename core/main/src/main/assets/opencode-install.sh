#!/system/bin/sh
set -e
apk add --no-cache nodejs npm
npm install -g @opencode/cli
exec opencode serve --port 4123 --hostname 127.0.0.1
