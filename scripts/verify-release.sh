#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_root"

if [[ "${1:-}" != "--skip-build" ]]; then
  ./gradlew build
fi

version="$(sed -n 's/^version=//p' gradle.properties)"
jar_path="build/libs/eigenworks-${version}.jar"
sources_path="build/libs/eigenworks-${version}-sources.jar"

[[ -f "$jar_path" ]] || { echo "Missing installable JAR: $jar_path" >&2; exit 1; }
[[ -f "$sources_path" ]] || { echo "Missing sources JAR: $sources_path" >&2; exit 1; }

metadata="$(unzip -p "$jar_path" fabric.mod.json)"
grep -Fq "\"version\": \"${version}\"" <<<"$metadata"
grep -Fq '"minecraft": "~26.2"' <<<"$metadata"
grep -Fq '"java": ">=25"' <<<"$metadata"
grep -Fq '"fabric-api": ">=0.160.0+26.2"' <<<"$metadata"

archive_entries="$(unzip -Z1 "$jar_path")"
grep -Fxq 'dev/eigenworks/EigenWorks.class' <<<"$archive_entries"
grep -Fxq 'assets/eigenworks/lang/en_us.json' <<<"$archive_entries"
grep -Fxq 'data/eigenworks/recipe/robot_joint_module.json' <<<"$archive_entries"

echo "Verified EigenWorks ${version} for Minecraft 26.2 / Java 25"
sha256sum "$jar_path"
