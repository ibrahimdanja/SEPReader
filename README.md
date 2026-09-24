# SEP Reader

A lightweight Android reader/client for the Stanford Encyclopedia of Philosophy.

Features in v1:
- Dedicated SEP Reader interface
- Search directly against SEP
- Official SEP pages in an in-app WebView
- Dark/light mode
- Local recent-entry tracking, with a real "Recently read" list you can reopen
- Bookmarks — tap ☆ while reading an article to save it, tap again to remove it; view/clear them from "Bookmarks" on the home screen
- Back/forward article navigation
- Adjustable architecture for future notes and offline reading
- Android 6.0+ compatibility

## Build

This repository includes a GitHub Actions workflow. Upload the project to a GitHub repository and run **Build SEP Reader APK** from Actions. The resulting APK is uploaded as a workflow artifact.

The app does not copy the SEP database into the APK. It accesses the official Stanford Encyclopedia of Philosophy website, keeping the encyclopedia content maintained by Stanford.
