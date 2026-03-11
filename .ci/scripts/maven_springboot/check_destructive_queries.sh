#!/bin/bash

####################### Colors #######################
color_red='\033[0;31m'
color_green='\033[0;32m'
color_yellow='\033[0;33m'
color_default='\033[0m'

CHANGELOG_DIR="src/main/resources/db/changelog"

WHITELIST_FILE="src/main/resources/db/changelog-whitelist.yml"

DESTRUCTIVE_KEYWORDS="drop\s+table|drop\s+column|delete|update|truncate\s+table"

set -e

check_destructive_queries() {
    local file=$1
    # Use grep to search for destructive SQL commands, returns non-zero if no matches found
    if ! output=$(grep -i -E "$DESTRUCTIVE_KEYWORDS" "$file"); then
        return 0
    else
        printf "${color_red}Destructive query found in $file:\n"
        printf "${color_red}%s${color_default}\n" "$output"
        printf "${color_yellow}If you are sure you want to keep this destructive migration, you must:\n"
        printf "1. Keep the changelog file in the master changelog directory: $CHANGELOG_DIR\n"
        printf "2. Add an entry to the whitelist file: $WHITELIST_FILE\n"
        return 1
    fi
}

for file in "$CHANGELOG_DIR"/*.sql; do
    relative_path="changelog/${file#$CHANGELOG_DIR/}"

    if ! whitelist_output=$(yq eval ".whitelisted_files[] | select(.file == \"$relative_path\")" "$WHITELIST_FILE"); then
        printf "${color_red}Error occurred while checking the whitelist for file: $file${color_default}\n"
        exit 1
    fi

    if echo "$whitelist_output" | grep -q "$relative_path"; then
        explanation=$(yq eval ".whitelisted_files[] | select(.file == \"$relative_path\") | .explanation" "$WHITELIST_FILE")
        printf "Skipping whitelisted file: $relative_path:\n - Explanation:${color_yellow} $explanation${color_default}\n"
        continue
    fi

    if ! check_destructive_queries "$file"; then
        exit 1
    fi
done


printf "${color_green}No destructive queries found in any changelog file.${color_default}\n"
exit 0
