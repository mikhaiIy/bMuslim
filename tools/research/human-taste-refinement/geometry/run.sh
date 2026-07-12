#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p output
node geometry-matrix.mjs | tee output/run-summary.txt
