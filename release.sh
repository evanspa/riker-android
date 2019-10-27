#!/bin/bash

readonly version="$1"
readonly tagLabel="${version}"

git add .
git commit -m "release: ${version}"

git tag -f -a $tagLabel -m "version $version"
git push -f --tags
