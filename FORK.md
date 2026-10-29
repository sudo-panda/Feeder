# Personal fork of Feeder

This fork changes Feeder's UI (Boost-style cards, icons everywhere, new app icon) and
builds a signed APK for every change. Upstream: <https://github.com/spacecowboy/Feeder>.

## How changes reach the phone

1. Ask Claude (Claude Code, from the Claude app) for a change. Claude works on a
   `claude/*` branch, bumps the fork build number, and opens a pull request.
2. The **Fork build** workflow lints, tests and builds every branch push.
3. Optional: add the `beta` label to the pull request to try it on the phone first
   (see [Beta channel](#beta-channel)).
4. Merge the pull request in the GitHub app.
5. The workflow builds the release APK on `master`, signs it in a separate job,
   publishes it as a GitHub release `v<version>`, and updates a self-hosted F-Droid
   repository on GitHub Pages: `https://sudo-panda.github.io/Feeder/repo`.
6. The F-Droid app on the phone sees the new version and offers the update.

## Versioning

Version = `<upstream version>.<forkBuild>`, with `forkBuild` taken from
[`fork.properties`](fork.properties). Every fork change increases it by 1. Merging a
new upstream release does not, so `2.25.0.2` becomes `2.25.1.2`.
The version code is `<upstream versionCode> * 1000 + forkBuild`, so it always increases.

The release app id is `com.nononsenseapps.feeder.fork`, so it can be installed next to
the official Feeder. Move feeds over with OPML export/import.

A push to `master` whose version already has a release publishes nothing
(for example a docs-only change).

## Beta channel

To try a pull request on the phone before merging it, add the `beta` label to it. While
the label is on, every push to the pull request publishes a new beta, and the workflow
comments on the pull request when it's ready.

- Betas are a separate app, **Feeder beta** (`com.nononsenseapps.feeder.fork.beta`, orange
  icon), with its own data. A broken beta can't affect the production app.
- Betas are signed with a separate beta key, so they can never update the production app.
- Betas come from a separate F-Droid repository on the `fdroid-beta` branch:
  `https://raw.githubusercontent.com/sudo-panda/Feeder/fdroid-beta/repo`. The first beta
  comment on a pull request includes the link with its fingerprint to add in F-Droid.
- Version `<upstream version>.<forkBuild>-beta.<n>`. Only the last published beta is kept.
- Pull requests from other people's forks are never built as betas.

## One-time setup

1. **Actions tab:** enable workflows for the fork. Then open the *Continuous Integration*
   workflow (upstream's Google Play pipeline) and choose *Disable workflow*.
2. **Settings → Pages**: under *Build and deployment*, set *Source* to *GitHub Actions*.
3. **Settings → Environments → New environment** named `release`:
   - *Deployment branches and tags*: *Selected branches and tags*, add `master`.
   - *Environment secrets*: add `SIGNING_PASSWORD`, a random password of at least
     32 characters from a password manager. Keep it in the password manager: without it,
     no further updates can be signed.
4. **Settings → Environments → New environment** named `beta`, with *Environment
   secrets*: `BETA_SIGNING_PASSWORD`, another random password of at least 32 characters.
   Leave its deployment branches unrestricted: betas are built from pull requests.
   Then, in the pull requests tab, create a label named `beta`.
5. Merge the first pull request. The first release run creates the signing key
   (`signing/release.p12`, encrypted with `SIGNING_PASSWORD`) and commits it.
6. On the phone, open `https://sudo-panda.github.io/Feeder/repo/` (the F-Droid badge in the
   README) and tap the repository link to add it to F-Droid. Or, in the F-Droid app,
   *Settings → Repositories → +*, and enter
   `https://sudo-panda.github.io/Feeder/repo?fingerprint=<fingerprint>`, where
   `<fingerprint>` is the "Signing certificate SHA-256" from the release notes. F-Droid
   refuses the repo if the fingerprint doesn't match. Then install "Feeder" from it.
7. After the first release, add **Settings → Rules → Rulesets → New branch ruleset**
   targeting `master`: *Restrict deletions*, *Block force pushes*, *Require a pull
   request before merging*, with no bypass list.
8. Make sure two-factor authentication is on for your GitHub account.

## Cloud environment for Claude (local builds)

So Claude can build and test before pushing, the Claude Code cloud environment needs the
Android SDK. Open the environment's settings (the environment menu in the session's title
bar, then *Edit*):

- *Network access*: add `dl.google.com`, `maven.google.com`, `jitpack.io` and
  `cowboyprogrammer.org` (one upstream unit test fetches its feed) to the allowed domains,
  and keep *Allow package managers* ticked.
- *Setup script*:

  ```bash
  #!/bin/bash
  set -euo pipefail
  export ANDROID_HOME=/opt/android-sdk
  if [ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    curl -fsSL -o /tmp/cmdline-tools.zip \
      https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
    unzip -q /tmp/cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    rm /tmp/cmdline-tools.zip
  fi
  yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null
  echo "export ANDROID_HOME=$ANDROID_HOME" >> ~/.bashrc
  ```

  Gradle downloads the exact platform and build tools it needs on the first build.

## Security model

- Only someone with push access to this repo can change what gets built. Keep 2FA on.
  Every change goes through a pull request that you merge yourself.
- The signing password is an environment secret available only to jobs on `master`.
  Branch builds never see it.
- Jobs that can read a signing password only run the `ci/fork-*` scripts with the JDK
  (`keytool`, `jar`, `jarsigner`), the runner's Android build tools, `git` and `gh`.
  Gradle, fdroidserver and every other third-party tool run in jobs without secrets.
  fdroidserver builds the F-Droid repo with an unsigned index, and the signing job signs
  it with `jarsigner`.
- The beta password lives in the separate `beta` environment and only signs the beta app
  and beta repo. Any branch push could get at it (GitHub can't restrict it to labelled
  pull requests), which is why betas are a separate app with a separate key: a leaked
  beta key can't touch the production app.
- The committed keystore is encrypted with the password (PKCS#12, AES-256). A random
  password of 32+ characters makes it infeasible to brute-force.
- Third-party GitHub Actions are pinned to full commit SHAs, setup-gradle validates the
  Gradle wrapper checksum, and fdroidserver with all its Python dependencies is pinned by
  hash in `ci/fdroidserver-requirements.txt`.
- Android only installs updates signed with the same key, so a different APK cannot
  replace the installed app. The F-Droid repo is pinned by that same fingerprint, so
  F-Droid rejects an index or APK signed by anyone else.

## Syncing with upstream

Ask Claude to "sync with the latest upstream Feeder release". Claude merges the newest
upstream release tag (not upstream `master`), resolves conflicts and opens a PR. Fork
changes are kept small and marked with `Fork:` comments to keep conflicts rare.
