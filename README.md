# Material Files (Beta)

[本文中文版](README_zh-CN.md)

An open source Material Design file manager, for Android 5.0+.

Download: [Google Play](https://play.google.com/store/apps/details?id=me.zhanghai.android.files), [Coolapk](https://www.coolapk.com/apk/me.zhanghai.android.files), [APK](https://github.com/zhanghai/MaterialFiles/releases/download/v1.0.0-beta.7/app-release.apk)

## Preview

<p><img src="screenshots/main.png" width="32%" /> <img src="screenshots/drawer.png" width="32%" /> <img src="screenshots/properties-basic.png" width="32%" />
<img src="screenshots/settings.png" width="32%" /> <img src="screenshots/standard-directories.png" width="32%" /> <img src="screenshots/about.png" width="32%" /></p>

## Features

- Open source: Lightweight, clean and secure.
- Material Design: Like the good old [Cabinet](https://github.com/aminb/cabinet), with attention into details.
- Breadcrumbs: Navigate in the filesystem with ease.
- Root support: View and manage files with root access.
- Themes: Customizable UI colors and night mode.
- Linux-aware: Like [Nautilus](https://wiki.gnome.org/action/show/Apps/Files), knows symbolic links, file permissions and SELinux context.
- Robust: Uses Linux system calls under the hood, not yet another [`ls` parser](https://news.ycombinator.com/item?id=7994720).
- Well-implemented: Built upon the right things, including [Java NIO2 File API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) and [LiveData](https://developer.android.com/topic/libraries/architecture/livedata).

## Why Material Files?

Because I like Material Design, and clean Material Design.

- [Cabinet](https://www.ghacks.net/2015/04/27/cabinet-is-a-feature-rich-file-explorer-for-android/) is dead, and broken on newer Android versions,
- [Amaze File Manager](https://play.google.com/store/apps/details?id=com.amaze.filemanager) doesn't have breadcrumb navigation.
- [Solid Explorer](https://play.google.com/store/apps/details?id=pl.solidexplorer2) has a strange dark ripple effect.
- [Root Explorer](https://play.google.com/store/apps/details?id=com.speedsoftware.rootexplorer) feels like a hybrid of Holo and Material Design.
- [MiXplorer](https://play.google.com/store/apps/details?id=com.mixplorer.silver), while being super powerful, just isn't really Material Design.

Even among the apps with Material Design, they (more or less) have various minor design issues (about layout, alignment, padding, icon, font, etc) across the app which makes me uncomfortable, while still being minor enough so that not everybody would care to fix it. So I had to create my own.

Because I want an open source file manager.

[Solid Explorer](https://play.google.com/store/apps/details?id=pl.solidexplorer2), [Root Explorer](https://play.google.com/store/apps/details?id=com.speedsoftware.rootexplorer) and [MiXplorer](https://play.google.com/store/apps/details?id=com.mixplorer.silver) are all powerful and feature-rich file managers, but just, closed source.

I sometimes use file managers to view and modify files that require root access, but deep down inside, I just feel uneasy with giving any closed source app root access to my device. After all, that means giving literally full access to my device, which stays with me every day and stores my own information, and what apps do with such access merely depends on their good intent.

Because I want a file manager that is implemented the right way.

Before I started working on this project, I investigated the existing open source apps, mainly [source code](https://github.com/aminb/cabinet) of the abandoned [Cabinet](https://www.ghacks.net/2015/04/27/cabinet-is-a-feature-rich-file-explorer-for-android/) and [source code](https://github.com/TeamAmaze/AmazeFileManager) of [Amaze File Manager](https://play.google.com/store/apps/details?id=com.amaze.filemanager).

- They both built their custom models for file information ([cabinet/File.java](https://github.com/aminb/cabinet/blob/master/app/src/main/java/com/afollestad/cabinet/file/base/File.java), [AmazeFileManager/HybridFile.java](https://github.com/TeamAmaze/AmazeFileManager/blob/master/app/src/main/java/com/amaze/filemanager/filesystem/HybridFile.java)), and mixed the path of a file with the information about a file together. Such way of abstraction might be in good shape in the beginning, but then it grows over time and eventually becomes a terrible mixture of everything.

    On the contrary, Java 8 has came with the [NIO2 file API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html), a (comparatively) well designed abstraction for files, which is able to accommodate the similarities and differences of filesystems across Linux, Windows and macOS, and clearly separates the concept of a `Path` and how to get information about the file for that path (`FileSystemProvider`).

- They are both parsing the output of `ls` ([cabinet/LsParser.java](https://github.com/aminb/cabinet/blob/master/app/src/main/java/com/afollestad/cabinet/file/root/LsParser.java), [AmazeFileManager/RootHelper.java](https://github.com/TeamAmaze/AmazeFileManager/blob/818e6f70b68f1d8df4d615b9f629ed5bc69e791d/app/src/main/java/com/amaze/filemanager/filesystem/RootHelper.java#L296)). A proper file manager should [never parse the output of `ls`](https://news.ycombinator.com/item?id=7994720), because there is just no reliable way to determine which part of that output is a file name, and if there's ever a file with an unexpected name, the app might crash or surprise user in even more unexpected ways. Moreover, parsing `ls` requires launching a whole new process every time, which noticeably slows down the loading time. And even if the app uses the old Java `File` API when possible, its symbolic link handling just won't let a file manager implement file operations correctly.

    The proper solution to this is to use the Linux system calls, because Android is built upon Linux, uses the file system mechanism of Linux, and file managers should be Linux-aware. Only by using the system calls directly instead of a fragile or limited intermediate, will file managers be able to handle file names, symbolic links, ownership and permissions correctly.

- Their source code, organization or quality, just isn't in my own personal flavor to work on to build the best file manager for Android.

Because I know people can do it right.

[Nautilus](https://wiki.gnome.org/Apps/Files) is a beautifully-designed and user-friendly file manager on Linux desktop, and it's fully Linux-aware. [Phonograph](https://github.com/kabouzeid/Phonograph) is an open source Material Design music player app (which I've been using for years), and it has just the right Material Design and implementation.

So, it's time for yet another Android file manager.

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
