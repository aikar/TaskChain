#!/usr/bin/env bash
if [ $# -lt 1 ]; then
    echo "$0 <to>"
    exit 1
fi
ver=$1
function updateVersion {
    sed -i -e "s/<!--VERSION-->.*\?<!--VERSION-->/<!--VERSION-->$ver<!--VERSION-->/g" $1
}

files=(*/pom.xml pom.xml)
for f in "${files[@]}"; do
	updateVersion $f
done

if [[ "$ver" != *-SNAPSHOT ]]; then
	updateVersion README.md
fi
