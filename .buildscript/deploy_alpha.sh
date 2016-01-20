#!/bin/bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype's snapshot repo.
#
# Adapted from https://coderwall.com/p/9b_lfq and
# http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

SLUG="gdg-x/frisbee"
JDK="oraclejdk8"
BRANCH="develop"


if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping alpha deployment: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
  echo "Skipping alpha deployment: wrong JDK. Expected '$JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping alpha deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  echo "Skipping alpha deployment: wrong branch. Expected '$BRANCH' but was '$TRAVIS_BRANCH'."
else
  #Check if the branch has a tag
    #If we don't have a tag, increase the build number, push a commit with a tag and release it.
    git describe --exact-match >/dev/null 2>/dev/null
    if [ $? -eq 0 ]; then
      echo "Skipping alpha deployment: commit already has a tag."
      exit 0
    fi

    echo "Incrementing version number..."
    perl -pi -e 's/(versionBuild\s=\s)(\d+)/$1.($2+1)/ge' build.gradle
    version=$(perl -ne 'print "$1." if /version(?:Major|Minor|Patch|Build)\s=\s(\d+)/' build.gradle | sed 's/\.$//')
    echo "Automated alpha build from CI" > app/src/alpha/play/en-US/whatsnew
    echo "Deploying alpha APK version $version"
    ./gradlew clean publishApkProdAlpha -Dtrack=alpha
    if [ $? -eq 0 ]; then
      echo "Alpha APK successfully deployed!"
      git add build.gradle
      git -c user.name='GDG-X' -c user.email='support@gdgx.io' commit -m "Prepare for release $version"
      git tag -a $version -m "Version $version"
      # Make sure to make the output quiet, or else the API token will leak!
      # This works because the API key can replace your password.
      git push -f -q https://gdg-x:$GITHUB_API_KEY@github.com/gdg-x/frisbee 2>/dev/null
      git push -f -q --tags https://gdg-x:$GITHUB_API_KEY@github.com/gdg-x/frisbee 2>/dev/null
    fi
fi
