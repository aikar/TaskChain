#!/usr/bin/env bash
if [ $# -lt 1 ]; then
    echo "$0 <release>"
    exit 1
fi
version=$1


a=( ${version//./ } )
((a[2]++))
files=(*/pom.xml pom.xml README.md CHANGELOG.md)


next="${a[0]}.${a[1]}.${a[2]}-SNAPSHOT"

./version.sh "$version"
mvn -T 4 clean javadoc:jar deploy || (echo "Something went wrong with compile, aborting" ; exit 1)

git add -A docs
git commit -m "Javadocs $version" docs
git commit -m "Release $version" ${files[@]}
git tag -a -m "$version" $version

./version.sh "$next"
git commit -m "Prepare for development $next" ${files[@]}
git push origin master
git push --tags origin master

