#!/bin/bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype's snapshot repo.
#
# Adapted from https://coderwall.com/p/9b_lfq and
# http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

SLUG="gdg-x/frisbee"
ALPHA_BRANCH='master';
BETA_BRANCH='release';

incrementVersion () {
  echo "Incrementing version number..."
  perl -pi -e 's/(versionBuild\s=\s)(\d+)/$1.($2+1)/ge' build.gradle
}

getVersionName () {
  echo $(perl -ne 'print "$1." if /version(?:Major|Minor|Patch|Build)\s=\s(\d+)/' build.gradle | sed 's/\.$//')
}

updateWhatsNewFile () {
  if [ "$TRACK" == "alpha" ]; then
    echo -e "Automated alpha build from CI\n\nLatest git commit:\n" > app/src/alpha/play/en-US/whatsnew
    git log -1 --oneline >> app/src/alpha/play/en-US/whatsnew
  fi
}

commitAndPushToGit () {
  local version=$1
  git config user.name "GDG-X"
  git config user.email "support@gdgx.io"
  git add build.gradle
  git commit -m "Prepare for release $version"
  git tag -a $version -m "Version $version"
  # Make sure to make the output quiet, or else the API token will leak!
  # This works because the API key can replace your password.
  git push -q https://gdg-x:$GITHUB_API_KEY@github.com/gdg-x/frisbee 2>/dev/null
  git push -q --tags https://gdg-x:$GITHUB_API_KEY@github.com/gdg-x/frisbee 2>/dev/null
}

isAlreadyDeployed () {
  #Check if the branch has a tag
  #If we don't have a tag, increase the build number, push a commit with a tag and release it.
  git describe --exact-match >/dev/null 2>/dev/null
  [ $? -eq 0 ]
}

if [ "$TRAVIS_BRANCH" == "$ALPHA_BRANCH" ]; then
  BRANCH=$ALPHA_BRANCH
  TRACK='alpha'
  GRADLE_TASK='publishApkProdAlpha'
elif [ "$TRAVIS_BRANCH" == "$BETA_BRANCH" ]; then
  BRANCH=$BETA_BRANCH
  TRACK='beta'
  GRADLE_TASK='publishApkProdRelease'
else
  BRANCH=''
fi

if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping $TRACK deployment: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping $TRACK deployment: was pull request."
elif [ "$BRANCH" == '' ]; then
  echo "Skipping $TRACK deployment: wrong branch. Expected '$ALPHA_BRANCH' or '$BETA_BRANCH' but was '$TRAVIS_BRANCH'."
elif isAlreadyDeployed; then
  echo "Skipping $TRACK deployment: commit already has a tag."
else
  echo "Checking out branch: $BRANCH"
  git checkout $BRANCH
  incrementVersion
  version=$(getVersionName);
  updateWhatsNewFile
  echo "Deploying $TRACK APK version $version"
  ./gradlew $GRADLE_TASK -Dtrack=$TRACK
  if [ $? -eq 0 ]; then
    echo "$TRACK APK successfully deployed!"
    commitAndPushToGit $version
  fi
fi
