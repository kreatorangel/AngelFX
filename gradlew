#!/usr/bin/env sh
if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle 9.2+ is required. Install Gradle and add it to PATH, then run: gradle build" >&2
  exit 1
fi
exec gradle "$@"
