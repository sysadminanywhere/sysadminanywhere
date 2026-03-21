#!/bin/bash
set -e

for db in incident inventory n8n; do
  echo "Creating database '$db'"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<EOSQL
CREATE DATABASE $db;
EOSQL
done
