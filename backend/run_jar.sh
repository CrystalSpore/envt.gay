#!/usr/bin/env bash
ENV_ARGS=""

while read IN; do
  echo "$ENV_ARGS"
  ENV_ARGS="-D$IN $ENV_ARGS"
done < .env

java -jar "$1 $ENV_ARGS"