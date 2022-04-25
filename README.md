# Material Files

[本文中文版](README_zh-CN.md)

[![Android CI status](https://github.com/zhanghai/MaterialFiles/workflows/Android%20CI/badge.svg)](https://github.com/zhanghai/MaterialFiles/actions) [![GitHub release](https://img.shields.io/github/v/release/zhanghai/MaterialFiles)](https://github.com/zhanghai/MaterialFiles/releases) [![License](https://img.shields.io/github/license/zhanghai/MaterialFiles?color=blue)](LICENSE)

An open source Material Design file manager, for Android 5.0+.

[<img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="240">](https://play.google.com/store/apps/details?id=me.zhanghai.android.files) [<img alt="Get it on F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" width="240">](https://f-droid.org/packages/me.zhanghai.android.files)

[Get it on Coolapk](https://www.coolapk.com/apk/me.zhanghai.android.files) [Get the APK](https://github.com/zhanghai/MaterialFiles/releases/latest/download/app-release.apk)

[Help translation on Transifex](https://www.transifex.com/zhanghai/MaterialFiles/) ([Search Android & GNOME translations](https://translations.zhanghai.me/), [Search Microsoft translations](https://www.microsoft.com/en-us/language))

## Preview

<p><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="32%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="32%" /></p>

## Features

- Open source: Lightweight, clean and secure.
- Material Design: Follows Material Design guidelines, with attention into details.
- Breadcrumbs: Navigate in the filesystem with ease.
- Root support: View and manage files with root access.
- Archive support: View, extract and create common compressed files.
- NAS support: View and manage files on FTP, SFTP and SMB servers.
- Themes: Customizable UI colors, plus night mode with optional true black.
- Linux-aware: Like [Nautilus](https://wiki.gnome.org/action/show/Apps/Files), knows symbolic links, file permissions and SELinux context.
- Robust: Uses Linux system calls under the hood, not yet another [`ls` parser](https://news.ycombinator.com/item?id=7994720).
- Well-implemented: Built upon the right things, including [Java NIO2 File API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) and [LiveData](https://developer.android.com/topic/libraries/architecture/livedata).

## Why Material Files?

Because I like Material Design, and clean Material Design.

There are already a handful of powerful file managers, but most of them just isn't Material Design. And even among the ones with Material Design, they usually have various minor design flaws (layout, alignment, padding, icon, font, etc) across the app which makes me uncomfortable, while still being minor enough so that not everybody would care to fix it. So I had to create my own.

Because I want an open source file manager.

Most of the popular and reliable file managers are just closed source, and I sometimes use them to view and modify files that require root access. But deep down inside, I just feel uneasy with giving any closed source app the root access to my device. After all, that means giving literally full access to my device, which stays with me every day and stores my own information, and what apps do with such access merely depends on their good intent.

Because I want a file manager that is implemented the right way.

- This app implemented [Java NIO2 File API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) as its backend, instead of inventing a custom model for file information/operations, which often gets coupled with UI logic and grows into a mixture of everything ([example](https://github.com/TeamAmaze/AmazeFileManager/blob/master/app/src/main/java/com/amaze/filemanager/filesystem/HybridFile.java)). On the contrary, a decoupled backend allows cleaner code (which means less bugs), and easier addition of support for other file systems.

- This app doesn't use `java.io.File` or parse the output of `ls`, but built bindings to Linux syscalls to properly access the file system. `java.io.File` is an old API missing many features, and just can't handle things like symbolic links correctly, which is the reason why many people rather parse `ls` instead. However parsing the output `ls` is not only slow, but also [unreliable](https://news.ycombinator.com/item?id=7994720), which made [Cabinet](https://github.com/aminb/cabinet/blob/master/app/src/main/java/com/afollestad/cabinet/file/root/LsParser.java) broken on newer Android versions. By virtue of using Linux syscalls, this app is able to be fast and smooth, and handle advanced things like Linux permissions, symbolic links and even SELinux context. It can also handle file names with invalid UTF-8 encoding because paths are not naively stored as Java `String`s, which most file managers does and fails during file operation.

- This app built its frontend upon modern `ViewModel` and `LiveData` which enables a clear code structure and support for rotation. It also properly handles things like errors during file operation, file conflicts and foreground/background state.

In a word, this app tries to follow the best practices on Android and do the right thing, while keeping its source code clean and maintainable.

Because I know people can do it right.

[Nautilus](https://wiki.gnome.org/Apps/Files) is a beautifully-designed and user-friendly file manager on Linux desktop, and it's fully Linux-aware. [Phonograph](https://github.com/kabouzeid/Phonograph) is an open source Material Design music player app (which I've been using for years), and it has just the right Material Design and implementation.

So, it's time for yet another Android file manager.

## Inclusion in custom ROMs

Thank you if you choose to include Material Files in your custom ROM! However since I've received several user complaints due to improper inclusion, I'd like to offer some suggestions on including this app properly for the good of end users:

- Please don't replace the AOSP [DocumentsUI](https://android.googlesource.com/platform/packages/apps/DocumentsUI/) app with this app. This app is not designed to replace DocumentsUI and can't handle a number of functionalities in DocumentsUI - in fact, it relies on DocumentsUI to do things like granting external SD card access.

- Please make sure this app can be uninstalled or at least disabled. Some users may not want this app for a variety of reasons, and get very upset when they can't remove it.

- Please avoid conflict with the Play/F-Droid version of this app. App stores cannot update apps signed with a different certificate, so you can either ship an APK that's signed by me (or F-Droid) so that users will be able to update it on Play/F-Droid, or fork this project and rename the package name when you need to sign the APK with a different certificate and potentially making other changes.

## License

    Copyright (C) 2018 Hai Zhang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
