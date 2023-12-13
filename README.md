<div align="center">
  <img style="border-radius: 50%" src="./androidApp/src/androidMain/ic_launcher-playstore.png" width="100px">
  <h1>Rockabye</h1>
</div>

Swipe with your partner to find the perfect baby name

## Download 📦

Rockabye is available for Android and iOS

Rockabye is an app designed for expecting couples struggling to agree on a name for their baby.
This app offers a fun and interactive solution by allowing both partners to swipe through a curated list of baby names.
Much like a dating app, both partners can start swiping on names they like.
When both partners swipe right on the same name, it's a match!
This feature not only simplifies the process of choosing a baby name but also ensures that both parents are equally involved in the decision-making.
Rockabye is the perfect tool for parents-to-be looking for a harmonious way to find the perfect name for their little one.

<div align="center"><a href="https://apps.apple.com/us/app/rockabye/id6474103446"><img src="./assets/app_store_download.svg" width="200px"/></a></div>
<div align="center"><a href="https://play.google.com/store/apps/details?id=com.joetr.bundle"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" width="230px"/></a></div>

## Publishing

### iOS
iOS is published on release via fastlane by creating a tag (release/x.y.z)

### Android
Android is published on release via fastlane by creating a tag (release/x.y.z)

## On A Fresh Clone
1. Add `.env.default` under `fastlane` based on the `.env.default.sample` for building and using fastlane locally (only needed for deploying)

2. Add `keystore.jks` under `androidApp` with a `key.properties` file that looks like:
    1. ```
        keyAlias=<alias>
        keyPassword=<key password>
        storePassword=<stote password>
        storeFile=keystore.jks
       ```

## Mockups
https://studio.app-mockup.com/
