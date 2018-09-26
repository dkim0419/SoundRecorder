# Contributing

Contributors are more than welcome. How can you contribute?

### Report bugs

We want our app to be as stable as possible thus your bug reports are immensely valuable. File [GitHub Issues](https://github.com/naXa777/SoundRecorder/issues) for anything that is unexpectedly broken.

* App version
* Device model
* Android version
* Steps to reproduce the bug
* Expected behavior
* Actual behavior (a screenshot and/or log file may be helpful)

### Contribute Code [![Open Source Helpers](https://www.codetriage.com/naxa777/soundrecorder/badges/users.svg)](https://www.codetriage.com/naxa777/soundrecorder)

We have labeled tasks you can help with as [![GitHub issues by-label](https://img.shields.io/github/issues/naXa777/SoundRecorder/help%20wanted.svg)](https://github.com/naXa777/SoundRecorder/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22) and [![GitHub issues by-label](https://img.shields.io/github/issues/naXa777/SoundRecorder/good%20first%20issue.svg)](https://github.com/naXa777/SoundRecorder/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22). Just pick up an issue that you're interested in and start coding. If you have a great idea you really want to implement, start by logging an issue for us. We'll let you know if it fits with our product direction and then you can start development. When you're ready open a Pull Request with a description of your changes.

See [Git Essentials](#git-essentials) for a simple bugfix workflow.

### Translate

You don't have to be a programmer if you want to translate this application in your own language or improve existing translations.
You can translate SoundRecorder using [POEditor](https://poeditor.com/join/project/IuPsne4VcJ) - a collaborative translation platform.

[![POEditor](https://poeditor.com/public/images/logo_small.png)](https://poeditor.com/join/project/IuPsne4VcJ)

### Automate Testing

Testing is imperative to the health of the project. There's a configured CI pipeline ([Travis CI](https://travis-ci.com/naXa777/SoundRecorder)) intended for running unit tests and instrumented tests on every commit to the repository, but unfortunately, there're very few tests at the moment.

Please follow standard guidelines if you want to contribute a test:

1. Android Developers - [Test apps on Android](https://d.android.com/training/testing/)
2. Android Studio - [Test your app](https://d.android.com/studio/test/)
3. GitHub - [Android testing samples](https://github.com/googlesamples/android-testing)


## Git Essentials

Workflows can vary, but here is a very simple workflow for contributing a bug fix:

1. [Fork](https://help.github.com/articles/fork-a-repo/) the repository.

2. Clone the fork:

       $ git clone git@github.com:YOUR_USERNAME/SoundRecorder.git
       $ git remote add upstream https://github.com/naXa777/SoundRecorder.git

    Read why do you need to [Configure a remote](https://help.github.com/articles/configuring-a-remote-for-a-fork/) if you're interested.

3. Prepare a feature branch:

       $ git checkout -b issue-123-keyword master

4. Do development and then commit your changes:

       $ git commit -m "fix #123 - Description of what I had changed"
       $ git push

    A quick note: See Chris Beams' guide to writing good commit messages - [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/).

5. Open a pull request.

   Read [Creating a pull request from a fork](https://help.github.com/articles/creating-a-pull-request-from-a-fork/) for details.
