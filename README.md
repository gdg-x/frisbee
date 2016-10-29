Project Frisbee
===============
### Powered by GDG[x]

[![Join the chat at https://gitter.im/gdg-x/frisbee](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/gdg-x/frisbee?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/gdg-x/frisbee.png?branch=develop)](https://travis-ci.org/gdg-x/frisbee)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/gdgx-frisbee/localized.png)](https://crowdin.com/project/gdgx-frisbee)

All GDG content in one place. Project Frisbee is the result of GDG[x]'s coordinated efforts to build an applications that makes it easier to discover Google Developer Group content while being on the go.
The GDG App features the Google+ news feed, Upcoming Events and general information on every
active chapter listed in the Google Developer Group Directory

###[Download the application](https://play.google.com/store/apps/details?id=org.gdg.frisbee.android)

###Features:
* GDG Pulse
* Google Developer Experts Directory
* Chapter News and Events
* Upcoming Event Dashclock extension and Widget
* Featured Special Event Series Section
* Arrow (Find you fellow organizers worldwide and earn points.)

Frisbee is a community effort and we appreciate the help of everyone who wants to help improve the App:

Become a tester and VIP user by joining the [G+ Frisbee community] (https://plus.google.com/communities/100423211916386801761).

Check http://github.com/gdg-x for more information about all development activities around GDGs.

###For event organizers
Please read https://github.com/gdg-x/frisbee/wiki/FAQ-for-organizers

Development
-----------
Read the [Contribution Guidelines](https://github.com/gdg-x/frisbee/blob/develop/CONTRIBUTING.md).

Read the [Development Guide](https://github.com/gdg-x/frisbee/wiki/Developer-Documentation).

When sending pull requests please make sure to enable EditorConfig in Android Studio -> Settings -> Editor -> Code & Style -> EditorConfig.

####Speeding up debug builds

The project uses multidex. To speed up the builds you need to set `minSdkLevel` to 21 and above. Our project uses 
`minSdk` property to override `minSdkLevel`. To do that, you should open Android Studio Compiler Settings and add a 
command line property like below: `-PminSdk=21`

![Android Studio Compiler Settings]
(https://cloud.githubusercontent.com/assets/763339/13549170/1f9fa1c8-e2f8-11e5-846d-fcd37616692c.png)

###Contributors
See [list of contributors](https://github.com/gdg-x/frisbee/graphs/contributors)

Maintainers are listed in [MAINTAINERS](https://github.com/gdg-x/frisbee/blob/develop/MAINTAINERS) file.
 
At least 1 approval is needed by the maintainers for PRs to be merged.

###Translation

There are GDG communities all over the world, so we want Frisbee to be available in every language there is.
You can help us out. Head over to Crowdin and start translating Frisbee to your language.
https://crowdin.com/project/gdgx-frisbee

Release
-------

The project has continuous distribution triggered by Travis CI. 

- Pushing to `master` branch -> alpha release
- Pushing to `release` branch -> beta release

_Note:_ Alpha release is a special build that **should not** be promoted to beta or production.

`release` branch is meant to be for production release. It is by default deployed to Beta. After a brief test there, it can be safely promoted to production.

Push triggers a release. 

When something is pushed to `master` or `release`, after a successful release, it always creates another commit increasing the version number and git tag. It's not necessary to tag manually.

####Best Practices

- Alpha release
  - Just merge `develop` into `master` and push to trigger an internal alpha release.
- Beta/Production release
  - Update `release` branch to latest `develop`
  - Increase version number in root `build.gradle` file
  - Make `versionBuild = 0`
  - Update `app/src/main/play/en-US/whatsnew` file for change log in Google Play Store
  - Commit the changes and push
  - After a successful release, merge `release` branch back into `develop`

######The GDG App, GDG[x] are not endorsed and/or supported by Google, the corporation.

License
--------

    © 2013-2015 GDG[x]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
