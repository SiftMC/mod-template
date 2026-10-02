# Picks the next version from the commits since the last v* tag, publishes it and creates the GitHub release.
last=$(git describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)
commits=$(git log --format='%s%n%b' ${last:+$last..}HEAD)
IFS=. read -r major minor patch <<< "${last:-v0.0.0}"
major=${major#v}
breaking='^[a-z]+(\(.+\))?!:|BREAKING CHANGE'
# Below 1.0.0 a breaking change is a minor step, as SemVer has it for 0.x.
if grep -qE "$breaking" <<< "$commits" && [ "$major" -gt 0 ]; then next=$((major + 1)).0.0
elif grep -qE "$breaking|^feat(\(.+\))?:" <<< "$commits"; then next=$major.$((minor + 1)).0
elif grep -qE '^(fix|perf)(\(.+\))?:' <<< "$commits"; then next=$major.$minor.$((patch + 1))
fi
next=${VERSION:-$next}
if [ -z "$next" ]; then echo "No feat, fix or perf commit since ${last:-the first commit}. Nothing to release."; exit 0; fi
export CHANGELOG=$(git log --format='- %s' ${last:+$last..}HEAD | grep -E '^- (feat|fix|perf)')
echo "Releasing $next"
[ -n "$DRY" ] && exit 0
./gradlew publishMods -Pmod_version=$next && gh release create "v$next" --title "$next" --notes "$CHANGELOG"
