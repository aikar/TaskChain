#!/usr/bin/env bash
if [ $# -lt 1 ]; then
    echo "$0 <to>"
    exit 1
fi
ver=$1


files=(*/pom.xml pom.xml README.md)
for f in "${files[@]}"; do
    sed -i -e "s/<!--VERSION-->.*\?<!--VERSION-->/<!--VERSION-->$ver<!--VERSION-->/g" $f
done
