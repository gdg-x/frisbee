# CastCompanionLibrary-android

CastCompanionLibrary-android is a library project to enable developers integrate Cast capabilities into their applications faster and easier.

## Dependencies
* google-play-services_lib library from the Android SDK (at least version 4.3)
* android-support-v7-appcompat (version 19.0.1 or above)
* android-support-v7-mediarouter (version 19.0.1 or above)

## Setup Instructions
* Set up the project dependencies

## Documentation
See the "CastCompanionLibray.pdf" inside the project for a more extensive documentation.

## References and How to report bugs
* [Cast Developer Documentation](http://developers.google.com/cast/)
* [Design Checklist](http://developers.google.com/cast/docs/design_checklist)
* If you find any issues with this library, please open a bug here on GitHub
* Question are answered on [StackOverflow](http://stackoverflow.com/questions/tagged/google-cast)

## How to make contributions?
Please read and follow the steps in the CONTRIBUTING.md

## License
See LICENSE

## Google+
Google Cast Developers Community on Google+ [http://goo.gl/TPLDxj](http://goo.gl/TPLDxj)

## Change List
1.4 -> 1.5
 * Fixed the issue where VideoCastNotificationService was not setting up data namespace if one was configured
 * Fixed issue 50
 * Added aversion number that will be printed in the log statements for tracking purposes
 * Correcting the typo in the name of method checkGooglePlaySevices() by introducing a new method and deprecating the old one (issue 48)
 * Fixing many typos in comments and some resources
 * Updating documentation to reflect the correct name of callbacks for the custom namespace for VideoCastManager

1.3 -> 1.4
 * Added support for MediaRouteButton
 * Added "alias" resources for Mini Controller play/pause/stop buttons so clients can customize them easily
 * Added a color resource to control thw color of the title of the custom VideoMediaRouteControllerDialog
 * Fixed some typos in JavaDoc

1.2 -> 1.3
 * Fixing issue 32
 * Fixing issue 33
 * Adding a better BaseCastManager.clearContext() variation
 * Implementing enhancement 30
 * Making sure play/pause button is hidden when ProgressBar is shown in VideoMediaRouteControllerDialog
 * probably some more adjustments and bug fixes

1.1 -> 1.2
 * Improving thread-safety in calling various ConsumerImpl callbacks
 * (backward incompatible) Changing the signature of IMediaAuthListener.onResult
 * Adding an API to BaseCastManager so clients can clear the "context" to avoid any leaks
 * Various bug fixes

1.0 -> 1.1
 * Added gradle build scripts (make sure you have Android Support Repository)
 * For live media, the "pause" button at various places is replaced with a "stop" button
 * Refactored the VideoCastControllerActivity to enable configuration changes without losing any running process
 * Added new capabilities for clients to hook in an authorization process prior to casting a video
 * A number of bug fixes, style fixes, etc
 * Updated documentation
