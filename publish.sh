#!/bin/bash

username=$1
password=$2

# check if the username is defined in gradle.properties
userFromGradleProperties=`./gradlew -q properties | grep mavenRepoUser`

if [[ "${username}" == "" && "${userFromGradleProperties}" == "" ]]; then
    read -p "Username: " username
fi

# check if the password is defined in gradle.properties
passwordFromGradleProperties=`./gradlew -q properties | grep mavenRepoPassword`
if [[ "${password}" == "" && "${passwordFromGradleProperties}" == "" ]]; then
    read -s -p "Password: " password
fi

echo ""

projectVersion=`./gradlew -q version`

echo "Publish ${projectVersion}"

userArg=""
if [[ "${username}" != "" ]]; then
    userArg="-PmavenRepoUser=${username}"
fi
passwordArg=""
if [[ "${password}" != "" ]]; then
    passwordArg="-PmavenRepoPassword=${password}"
fi

# tag the current branch
git tag ${projectVersion}
git push --tags

# publish the artifacts
gradle ${userArg} ${passwordArg} clean publish
