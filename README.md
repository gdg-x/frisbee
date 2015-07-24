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

Development
-----------

Check out project and import it to Android Studio. 

###Checkstyle Integration

This project enforces a checkstyle. The checkstyle configuration is available in `settings/checkstyle.xml` file. Please respect to the configuration. 

**Note:** Because of the line ending difference between Unix systems and Windows, you may get checkstyle errors about line endings. If you do, please check this [help page](https://www.jetbrains.com/idea/help/handling-lf-and-crlf-line-endings.html).
If you cannot resolve the issue, you can simply comment out the following line in the `checkstyle.xml` file.
```
<module name="NewlineAtEndOfFile" />
```

###Image Optimization

Image optimization is encouraged for new images. You can use the [image_optim](https://github.com/toy/image_optim) tool to do it easily. Use the following command to install it.
```
gem install image_optim image_optim_pack
```

###API Keys
`local.properties` file is used for storing API keys since it is already ignored in the `.gitignore` file. 

The file looks like:
```
android_simple_api_access_key=
android_backup_key=
play_app_id=
gcm_sender_id=
hub_client_id=
```

The first one (`android_simple_api_access_key`) is a regular API access key which can be gotten by creating a new project at [Google Developer Console](https://console.developers.google.com/project). `android_backup_key` is not necessary but can be obtained at this [page](http://developer.android.com/google/backup/signup.html). `play_app_id` is the Google Developer Console project id and lastly `hub_client_id` is the client id used for oAuth with Google.


###Contributors
* [Sebastian Mauer](https://github.com/mauimauer)
* [Friedger Müffke](https://github.com/friedger)
* [Henrique Rocha](https://github.com/HenriqueRocha)
* [Said Tahsin Dane](https://github.com/tasomaniac)
* [Stefan Hoth](https://github.com/stefanhoth)
* [Bartek](https://github.com/przybylski)
* [Jerrell Mardis](https://github.com/jerrellmardis)
* [Paresh Mayani](https://github.com/PareshMayani)

###Translation

There are GDG communities all over the world, so we want Frisbee to be available in every language there is.
You can help us out. Head over to Crowdin and start translating Frisbee to your language.
https://crowdin.com/project/gdgx-frisbee

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
