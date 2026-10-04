# Picks the next version from the commits since the last v* tag, uploads every jar and creates the GitHub release.
# VERSION=1.2.3 skips the pick. DRY=1 builds and runs the upload without sending anything.
set -euo pipefail
fail() { echo "$1" >&2; exit 1; }
semver='^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$'

last=$(git describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)
[ -z "$last" ] || [[ ${last#v} =~ $semver ]] || fail "The last tag $last is not a version like v1.2.3."
subjects=$(git log --format=%s ${last:+$last..}HEAD)
bodies=$(git log --format=%b ${last:+$last..}HEAD)
scope='(\([^)]*\))?'
bump=
if grep -qE "^[a-z]+$scope!:" <<< "$subjects" || grep -qE '^BREAKING[ -]CHANGE:' <<< "$bodies"; then bump=major
elif grep -qE "^feat$scope:" <<< "$subjects"; then bump=minor
elif grep -qE "^(fix|perf)$scope:" <<< "$subjects"; then bump=patch
fi

IFS=. read -r major minor patch <<< "${last#v}"
if [ -n "${VERSION:-}" ]; then next=${VERSION#v}
elif [ -z "$bump" ]; then echo "No feat, fix or perf commit since ${last:-the first commit}. Nothing to release."; exit 0
elif [ -z "$last" ]; then next=0.1.0
elif [ "$bump" = major ] && [ "$major" -gt 0 ]; then next=$((major + 1)).0.0
# Below 1.0.0 a breaking change is a minor step, as SemVer has it for 0.x.
elif [ "$bump" != patch ]; then next=$major.$((minor + 1)).0
else next=$major.$minor.$((patch + 1))
fi
[[ $next =~ $semver ]] || fail "The version must look like 1.2.3, not $next."
if git rev-parse -q --verify "refs/tags/v$next" > /dev/null; then fail "Tag v$next exists already."; fi

# Gradle uploads in parallel, so a missing token found halfway would leave some versions uploaded.
if [ -z "${DRY:-}" ]; then
    sites=$(grep -oE '^(modrinth|curseforge)_id[[:space:]]*=[[:space:]]*[^[:space:]]' gradle.properties | cut -d_ -f1 || true)
    [ -n "$sites" ] || fail "Set modrinth_id or curseforge_id in gradle.properties."
    for site in $sites; do
        token=${site^^}_TOKEN
        [ -n "${!token:-}" ] || fail "${site}_id is set, but the $token secret is missing."
    done
fi

export CHANGELOG=$(grep -E "^(feat|fix|perf)$scope!?:" <<< "$subjects" | sed 's/^/- /' || true)
echo "Releasing $next"
echo "$CHANGELOG"
./gradlew build -Pmod_version="$next"
# The dry run gets placeholder ids, so both upload tasks run and print what they would send.
./gradlew publishMods -Pmod_version="$next" ${DRY:+-PdryRun -Pmodrinth_id=dry-run -Pcurseforge_id=dry-run}
[ -n "${DRY:-}" ] || gh release create "v$next" --target "$(git rev-parse HEAD)" --title "$next" --notes "$CHANGELOG"
