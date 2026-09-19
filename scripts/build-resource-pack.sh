#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
atlas="$repository_root/art/resource_pack/eigenworks_industrial_atlas.png"
pack="$repository_root/resourcepacks/EigenWorks-Industrial-0.2.1"
tool_classes="$repository_root/build/resource-pack-tools"
output="$repository_root/resourcepacks/eigenworks-industrial-resource-pack-0.2.1.zip"

mkdir -p "$tool_classes"
javac -d "$tool_classes" "$repository_root/tools/ResourcePackTextureBuilder.java"
java -Djava.awt.headless=true -cp "$tool_classes" ResourcePackTextureBuilder "$atlas" "$pack"
rm -f "$output"
(cd "$pack" && zip -q -r "$output" .)
echo "Built $output"
