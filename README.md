> [!NOTE]
> **This is a personal fork of [Feeder](https://github.com/spacecowboy/Feeder)**, not the
> official app. Everything below the line is upstream's README. For the official app, use the
> upstream links further down. This fork is built and published separately.
>
> **What's different in this fork**
> - **Boost-style article cards:** bold title, one "feed · time" line, snippet, full-width
>   image and a row of quick actions (save, share, open in browser, more), with tight corners
>   and spacing.
> - **Full-width search:** while searching, the search field takes the whole top bar, with a
>   back arrow to close it.
> - **Icons throughout:** in the article long-press menu and every settings row.
> - **Redesigned sidebar:** app name header, the open feed or tag highlighted, titles and
>   unread counts aligned, bold tags with an expand arrow, indented feeds inside tags, and
>   read feeds dimmed.
> - **More feed icons:** feeds that declare no icon fall back to their site's `favicon.ico`.
> - **New app icon:** an F whose arms bend into RSS arcs, white on forest green.
> - **Boost-style colours:** neutral grey backgrounds and cards with one accent colour
>   (green by default). *Settings → Accent color* replaces *Dynamic colors*: preset colour
>   tiles, a custom colour (hue, shade or hex), or *Wallpaper* to keep Android 12+ dynamic
>   colours.
> - **Boost-style reader:** feed and author line, bold title, edge-to-edge images and an
>   icon bar (save, share, open in browser, read aloud, read/unread). Fetch full article and
>   *Reader appearance* are in the ⋮ menu; the appearance panel has title size, text size,
>   line spacing, justify, edge-to-edge images and font.
> - **Friendlier empty list:** "All caught up" with buttons to open another feed or add one.
> - **Boost-style pull to refresh:** the circle only follows your finger; loading shows as a
>   thin accent-coloured bar under the top bar.
> - **Settings → Data:** importing and exporting feeds (OPML) and saved articles moved here
>   from the feed screen's ⋮ menu.
> - **Separate app id** (`com.nononsenseapps.feeder.fork`), so it installs next to the
>   official Feeder. Move feeds over with OPML export/import (*Settings → Data* in this fork).
> - **Versioning:** `<upstream version>.<fork build>`, e.g. `2.25.0.1`.
> - **Releases:** signed APKs on [GitHub Releases](https://github.com/sudo-panda/Feeder/releases)
>   and in this fork's own F-Droid repository (below). Pull requests can be tried as betas
>   of the same app: turn on *Unstable updates* in F-Droid, see [FORK.md](FORK.md).
>
> **Get the fork** from its F-Droid repository. Open this link on your phone and tap the
> repository link (or scan the QR code) to add it to F-Droid with its signing fingerprint:
>
> <a href="https://sudo-panda.github.io/Feeder/repo/"><img src="https://f-droid.org/badge/get-it-on.png" alt="Get the fork from its F-Droid repository" height="80"/></a>
>
> See [FORK.md](FORK.md) for how the fork is built, signed, versioned and kept in sync with upstream.

---

Feeder
=====
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/Y8Y44OYQL)

<a href="https://f-droid.org/repository/browse/?fdid=com.nononsenseapps.feeder" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>

<a href='https://play.google.com/store/apps/details?id=com.nononsenseapps.feeder.play'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height="80"/></a>

<a href="https://hosted.weblate.org/engage/feeder/">
<img src="https://hosted.weblate.org/widgets/feeder/-/android-strings/svg-badge.svg" alt="Translation status" />
</a>

### Description

Feeder is an open source feed reader (RSS/Atom/JSONFeed) for Android created in 2014.

With Feeder you can read the latest news and posts from your favorite sites.

Feeder does NOT sync with usual remote backends so no account registration of any kind is necessary.

Feeder is free to use and runs locally on your device. Your data is 100% private.

### License

**THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND**

**GPLv3**, for more info see *LICENSE*.

### Translations are welcome!

If you want to translate Feeder into your native language the easiest way is to go to [Weblate](https://hosted.weblate.org/engage/feeder/) but making a merge request is of course fine if that is something you are comfortable with.

### Quick install

Clone the project:

    git clone https://github.com/spacecowboy/Feeder.git

Then build and install the app to your phone which is connected via USB:

    ./gradlew installFdroidDebug

### Features

* Offline reading
* Notification support
* OPML import/export
* Material Design

### Screenshots

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US_phone_portrait_light_list.png" width=50%/><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2_en-US_phone_portrait_dark_list.png" width=50%/>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3_en-US_phone_portrait_light_article.png" width=50%/><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4_en-US_phone_portrait_dark_article.png" width=50%/>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5_en-US_phone_portrait_light_settings.png" width=50%/><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6_en-US_phone_portrait_dark_settings.png" width=50%/>

### Download

Feeder is available on [F-Droid](https://f-droid.org/en/packages/com.nononsenseapps.feeder/) and [Google Play](https://play.google.com/store/apps/details?id=com.nononsenseapps.feeder.play) and there are APKs available for download on [GitHub Releases](https://github.com/spacecowboy/Feeder/releases).

The GitHub releases are signed with a key matching this signature:

```
SHA-256: C5:EE:FF:22:48:81:35:FF:C2:58:3C:3A:43:B0:53:A1:61:CA:86:98:62:96:1A:B8:53:4F:44:C7:5F:D5:7D:97
```
