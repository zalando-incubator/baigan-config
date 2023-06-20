#!/usr/bin/env bash

set -euxo pipefail

: "${1?"Usage: $0 <[pre]major|[pre]minor|[pre]patch|prerelease>"}"

./mvnw scm:check-local-modification

[ "$1" == "prerelease" ] && versionsuffix="" || versionsuffix="-"
current=$({ echo 0.0.0; git -c "versionsort.suffix=${versionsuffix}" tag --list --sort=version:refname; } | tail -n1)
release=$(semver "${current}" -i "$1" --preid RC)
next=$(semver "${release}" -i minor)

./mvnw versions:set -D newVersion="${release}"
git commit -am "Release ${release}"

./mvnw clean deploy scm:tag -P release -D tag="${release}" -D pushChanges=false

./mvnw versions:set -D newVersion="${next}-SNAPSHOT"

git push --atomic origin main "${release}"

git commit -am "Development ${next}-SNAPSHOT"

git push
