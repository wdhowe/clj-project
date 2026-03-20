#!/usr/bin/env bash
# Rename the project by replacing all occurrences of the current project name
# (detected from build.clj) with a new name, including directory renames.
# Usage: ./scripts/rename-project.sh <new-project-name> [new-github-username] [new-author]

set -euo pipefail

NEW_HYPHEN="${1:?Usage: $0 <new-project-name> [new-github-username] [new-author]}"
NEW_UNDER="${NEW_HYPHEN//-/_}"
NEW_GH_USER="${2:-}"
NEW_AUTHOR="${3:-}"

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

# Detect the current project name from build.clj :main entry
OLD_HYPHEN=$(grep ":main '" build.clj | head -1 | sed "s/.*:main '//;s/\.core.*//")
if [[ -z "$OLD_HYPHEN" ]]; then
  echo "Error: Could not detect current project name from build.clj"
  exit 1
fi
OLD_UNDER="${OLD_HYPHEN//-/_}"

# Detect current GitHub username from build.clj :git-repo entry
OLD_GH_USER=$(grep ":git-repo" build.clj | head -1 | sed 's/.*:git-repo "//;s/\/.*//')

# Detect current author from build.clj :author entry
OLD_AUTHOR=$(grep ":author" build.clj | head -1 | sed 's/.*:author "//;s/".*//')

if [[ "$NEW_HYPHEN" == "$OLD_HYPHEN" \
  && ( -z "$NEW_GH_USER" || "$NEW_GH_USER" == "$OLD_GH_USER" ) \
  && ( -z "$NEW_AUTHOR" || "$NEW_AUTHOR" == "$OLD_AUTHOR" ) ]]; then
  echo "Nothing to rename. Project: $OLD_HYPHEN, GitHub user: $OLD_GH_USER, Author: $OLD_AUTHOR"
  exit 0
fi

echo "Renaming project: $OLD_HYPHEN -> $NEW_HYPHEN"
echo "Directory form:   $OLD_UNDER -> $NEW_UNDER"
if [[ -n "$NEW_GH_USER" && "$NEW_GH_USER" != "$OLD_GH_USER" ]]; then
  echo "GitHub user:      $OLD_GH_USER -> $NEW_GH_USER"
fi
if [[ -n "$NEW_AUTHOR" && "$NEW_AUTHOR" != "$OLD_AUTHOR" ]]; then
  echo "Author:           $OLD_AUTHOR -> $NEW_AUTHOR"
fi
echo ""

# --- Replace in file contents (skip .git and binary files) ---
echo "Replacing references in file contents..."
SCRIPT_PATH="./scripts/rename-project.sh"
GH_SED_EXPR=()
if [[ -n "$NEW_GH_USER" && "$NEW_GH_USER" != "$OLD_GH_USER" ]]; then
  GH_SED_EXPR=(-e "s|$OLD_GH_USER|$NEW_GH_USER|g")
fi

grep -rl --exclude-dir=.git --exclude-dir=.cpcache --exclude-dir=.clj-kondo \
  --exclude-dir=target --exclude-dir=.lsp \
  -e "$OLD_HYPHEN" -e "$OLD_UNDER" ${OLD_GH_USER:+-e "$OLD_GH_USER"} \
  . | grep -v "$SCRIPT_PATH" | while read -r file; do
  if file "$file" | grep -q text; then
    if [[ "$(uname)" == "Darwin" ]]; then
      sed -i '' \
        -e "s|$OLD_HYPHEN|$NEW_HYPHEN|g" \
        -e "s|$OLD_UNDER|$NEW_UNDER|g" \
        "${GH_SED_EXPR[@]}" \
        "$file"
    else
      sed -i \
        -e "s|$OLD_HYPHEN|$NEW_HYPHEN|g" \
        -e "s|$OLD_UNDER|$NEW_UNDER|g" \
        "${GH_SED_EXPR[@]}" \
        "$file"
    fi
    echo "  Updated: $file"
  fi
done

# --- Replace author in build.clj ---
if [[ -n "$NEW_AUTHOR" && "$NEW_AUTHOR" != "$OLD_AUTHOR" ]]; then
  if [[ "$(uname)" == "Darwin" ]]; then
    sed -i '' "s|:author \"$OLD_AUTHOR\"|:author \"$NEW_AUTHOR\"|g" build.clj
  else
    sed -i "s|:author \"$OLD_AUTHOR\"|:author \"$NEW_AUTHOR\"|g" build.clj
  fi
  echo "  Updated author in: build.clj"
fi

# --- Rename directories ---
for old_dir in "src/$OLD_UNDER" "test/$OLD_UNDER"; do
  new_dir="${old_dir/$OLD_UNDER/$NEW_UNDER}"
  if [[ -d "$old_dir" ]]; then
    mv "$old_dir" "$new_dir"
    echo "  Renamed: $old_dir -> $new_dir"
  fi
done

echo ""
echo "Done! Project renamed to '$NEW_HYPHEN'."
echo "You may want to review the changes with 'git diff' and rename the root directory manually."
