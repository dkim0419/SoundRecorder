# Easy Sound Recorder 2
[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16) [![Build Status](https://travis-ci.com/naXa777/SoundRecorder.svg?branch=master&style=flat)](https://travis-ci.com/naXa777/SoundRecorder) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/1b4c1e2546784537b6bef070769c34bb)](https://www.codacy.com/app/naXa777/SoundRecorder?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=naXa777/SoundRecorder&amp;utm_campaign=Badge_Grade)

A simple sound recording Android app implementing <a href="https://material.io/">Material Design</a>.

![Icon](/app/src/main/res/mipmap-hdpi/ic_launcher.png) [![Android app on Google Play](https://d.android.com/images/brand/en_app_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=by.naxa.soundrecorder)

## Contributing

Contributors are more than welcome. Feel free to report bugs, fix bugs, implement new features, improve translations, increase test coverage, or write documentation.

Please, read [Contributing guidelines](/CONTRIBUTING.md) before opening new [issues](https://github.com/naXa777/SoundRecorder/issues) or submitting [pull requests](https://github.com/naXa777/SoundRecorder/pulls) to this repository.

## Screenshots

![Imgur](https://i.imgur.com/wxCXesJl.png) ![Imgur](https://i.imgur.com/86sehcjl.png)
![Imgur](https://i.imgur.com/p9Pn9Qgl.png) ![Imgur](https://i.imgur.com/LthDOjHl.png)
![Imgur](https://i.imgur.com/KCODDi8l.png) ![Imgur](https://i.imgur.com/rxeQUDIl.png)
![Imgur](https://i.imgur.com/U6w7dnXl.png) ![Imgur](https://i.imgur.com/ZGRnroNl.png)

## Building

If you want to run the app locally, do the following:

1. Download or clone the repository
2. If you don't have a Fabric account -> Sign up at fabric.io
3. Declare environment variables of which the name starts with `ORG_GRADLE_PROJECT_` followed by the property name (include your own Fabric `apiKey` and `apiSecret`).
     `export ORG_GRADLE_PROJECT_soundrecorderCrashlyticsApiSecret=0cf7c9df6d057e7bb62b1427ab364e8115a75fcf7430873b6274bb094d1a8adb`   
     `export ORG_GRADLE_PROJECT_soundrecorderCrashlyticsApiKey=cc238b2a4866c96030`
   Then `fabric.properties` file will be created automatically by Gradle on the first build. Do not commit this file to a version control, keep it secret.
4. Import the project in your IDE (we use Gradle + Android Studio to build)
5. You should now be able to build and run the app.

See [Crashlytics Kit Setup Demo](https://github.com/plastiv/CrashlyticsDemo) if you need more help.

## Dev environment

- [Android Studio](https://d.android.com/studio/preview/) 3.3 (Canary) is used for development
- [Gradle](https://gradle.org/install/) 4.10 is used to build the project
- [Android SDK 9.0](https://d.android.com/studio/releases/platforms#9.0) (Pie), API level 28
- Java 1.6

## Permissions needed for the app are:

- record audio
- write to external storage (to store recordings)
- read from external storage (to playback recordings)
- internet access (for stats collection)

Since February 2017 Google enforces a strict privacy policy requirement for apps using sensitive permissions (the RECORD_AUDIO permission). See [Privacy Policy](https://soundrecorder.bitbucket.io/privacy_policy.html) of Easy Sound Recorder 2.

## Credits / Libraries used:

- [Java MP4 Parser](https://github.com/sannies/mp4parser)
- [Circular Progress Bar](https://github.com/yuriy-budiyev/circular-progress-bar)
- [Material Components for Android](https://github.com/material-components/material-components-android)
