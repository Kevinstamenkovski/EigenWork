#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
pack="$repository_root/resourcepacks/EigenWorks-Industrial-0.2.1"
archive="$repository_root/resourcepacks/eigenworks-industrial-resource-pack-0.2.1.zip"

test -f "$pack/pack.mcmeta"
test -f "$pack/pack.png"
test "$(find "$pack/assets/eigenworks/textures/block" -type f -name '*.png' | wc -l)" -eq 17
test "$(find "$pack/assets/eigenworks/textures/item" -type f -name '*.png' | wc -l)" -eq 2
test "$(find "$pack/assets/eigenworks/models/block" -type f -name '*.json' | wc -l)" -eq 17
test "$(find "$pack/assets/eigenworks/models/item" -type f -name '*.json' | wc -l)" -eq 2
grep -q '"pack_format": 88' "$pack/pack.mcmeta"
unzip -tq "$archive"
echo "Resource pack verified: 17 blocks, 2 tools, Minecraft 26.2 format 88"
