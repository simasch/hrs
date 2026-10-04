#!/bin/bash
set -e
BRANCH="workshop/${GITHUB_USER}"

git fetch origin
if git ls-remote --exit-code --heads origin "$BRANCH" > /dev/null 2>&1; then
  echo "Branch $BRANCH exists, checking it out"
  git checkout "$BRANCH"
else
  echo "Creating branch $BRANCH"
  git checkout -b "$BRANCH"
  git push -u origin "$BRANCH"
fi
