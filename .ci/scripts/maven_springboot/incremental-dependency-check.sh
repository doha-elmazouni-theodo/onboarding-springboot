#!/usr/bin/bash

set -e

function getDependencyListFromPOM {
    if [ "$#" -ne 2 ]; then
        echo "Error: Function ${FUNCNAME[0]} requires 2 arguments. Got $# arguments"
        exit 22;
    fi
    ./mvnw -Dmaven.repo.local=${HOME}/.m2/repository -q -Djava.awt.headless=true -f "${1}" -B dependency:list -DincludeScope=compile -Dsilent=true -Dsort=true -DoutputFile="${2}" -DoutputAbsoluteArtifactFilename > /dev/null 2>&1
}

# Variable Definitions
DEPENDENCY_CHECK_SCRIPT=${1}
DEPENDENCY_CHECK_DATABASE=${2}
FORK_BRANCH=${3}

SOURCE_BRANCH_POM="pom.xml"
TARGET_BRANCH_POM="main_pom.xml"
DEP_DIFF_FILE="deps.diff.txt"

echo "determining forking point between HEAD and $FORK_BRANCH ..."
FORK_HASH=$(git merge-base $FORK_BRANCH HEAD)
echo retrieving pom.xml from branch $FORK_BRANCH forking point $FORK_HASH ...
git show $FORK_HASH:./pom.xml > ${TARGET_BRANCH_POM}
echo

echo diffing current pom.xml version with commit $FORK_HASH ...
diff ${SOURCE_BRANCH_POM} ${TARGET_BRANCH_POM} && { echo "POM files didn't change! exiting"; exit 0; }
echo

echo "getting dependency list from source ..."
getDependencyListFromPOM "${SOURCE_BRANCH_POM}" "${SOURCE_BRANCH_POM}.txt"
cat ${SOURCE_BRANCH_POM}.txt
echo

echo "getting dependency list from target ..."
getDependencyListFromPOM "${TARGET_BRANCH_POM}" "${TARGET_BRANCH_POM}.txt"
cat ${TARGET_BRANCH_POM}.txt
echo


echo diffing dependency list from source and target ...
git diff --no-index -U0 ${TARGET_BRANCH_POM}.txt ${SOURCE_BRANCH_POM}.txt | grep "^\+" > $DEP_DIFF_FILE || true
cat $DEP_DIFF_FILE
echo

if [ ! -s $DEP_DIFF_FILE ]; then
    echo -e "POM changed but no dependencies added!\n"
    exit 0;
fi

NEW_DEPENDENCIES_FOLDER=/tmp/new_dependencies
echo new dependencies are:
mkdir -p $NEW_DEPENDENCIES_FOLDER
grep -Eo '\/.*\.jar' $DEP_DIFF_FILE | xargs -I '{}' cp -v '{}' $NEW_DEPENDENCIES_FOLDER
echo

# TODO: investigate why apt install takes almost one minute
which xmllint >/dev/null 2>&1 || { sudo apt update -y > /dev/null && sudo apt install -y libxml2-utils > /dev/null; }
FAIL_BUILD_ON_CVSS=$(xmllint --xpath "//*[local-name()='failBuildOnCVSS']/text()" pom.xml | xargs)
SUPPRESSION_FILE=$(xmllint --xpath "//*[local-name()='properties']/*[local-name()='org.owasp.dependency-check-maven.suppressionFile']/text()" pom.xml | xargs)

echo running Dependency Check ...
echo FAIL_BUILD_ON_CVSS=$FAIL_BUILD_ON_CVSS
echo SUPPRESSION_FILE=$SUPPRESSION_FILE
bash $DEPENDENCY_CHECK_SCRIPT -s "$NEW_DEPENDENCIES_FOLDER/**/*.jar" \
    --format JSON --format HTML --prettyPrint  --failOnCVSS $FAIL_BUILD_ON_CVSS --noupdate --data $DEPENDENCY_CHECK_DATABASE \
    --disableAssembly --disableNugetconf --disableNuspec --disableRetireJS --suppression $SUPPRESSION_FILE
