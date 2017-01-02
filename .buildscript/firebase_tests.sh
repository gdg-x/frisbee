#!/bin/bash
# Use gcloud from Google to run Espresso tests on Firebase Test Lab

SLUG="gdg-x/frisbee"
BRANCH='develop';

if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping tests on Firebase: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping tests on Firebase: was pull request."
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  echo "Skipping tests on Firebase: wrong branch. Expected '$BRANCH' but was '$TRAVIS_BRANCH'."
else
  echo "Starting tests on Firebase"

  wget https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-138.0.0-linux-x86_64.tar.gz
  tar xf google-cloud-sdk-138.0.0-linux-x86_64.tar.gz
  echo "y" | ./google-cloud-sdk/bin/gcloud components update beta
  ./google-cloud-sdk/bin/gcloud auth activate-service-account --key-file settings/firebase-test-lab.json >/dev/null
  ./google-cloud-sdk/bin/gcloud beta test android run --async --type instrumentation --app ./app/build/outputs/apk/app-debug.apk --test ./app/build/outputs/apk/app-debug-androidTest.apk --device-ids Nexus5 --os-version-ids 22 --project api-project-429371117063

  echo "Test APK upload to Firebase complete"
fi
