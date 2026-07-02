# 👋🧩 Rushi's Patches
<p align="center">

![GitHub Release](https://img.shields.io/github/v/release/rushiranpise/morphe-patches?style=for-the-badge)
![License](https://img.shields.io/github/license/rushiranpise/morphe-patches?style=for-the-badge)
![GitHub Stars](https://img.shields.io/github/stars/rushiranpise/morphe-patches?style=for-the-badge)
![GitHub Forks](https://img.shields.io/github/forks/rushiranpise/morphe-patches?style=for-the-badge)
![GitHub Issues](https://img.shields.io/github/issues/rushiranpise/morphe-patches?style=for-the-badge)
![GitHub Discussions](https://img.shields.io/github/discussions/rushiranpise/morphe-patches?style=for-the-badge)

</p>
<br/>

> [!NOTE]
>
> *Just some patches I make in my free time, with a little(most :p) help from AI.*
>
> Patch requests are always welcome, **but please read the [new announcement](https://github.com/rushiranpise/morphe-patches/discussions/71) before opening an issue!**
>
> If you find this project useful and want to help support its development, consider [donating](#%EF%B8%8F-like-my-work-consider-donating). :pray:

<br/>

This is my personal collection of patches that I use with **Morphe**.  
Everything here is AI‑generated (mostly for fun and learning), then tested enough to not explode :p.

## ❓ About

## 🧠 Why AI‑generated?

Because I’m curious what LLMs can do with code diffs.  
No magic, no secrets — just a hobby.

## ⚠️ Very important disclaimer

I do this in my spare time.
Patches work *for me* on *my machine*.
Test before using on anything important.
Seriously...

## 📬 Feedback / ideas

Found something broken? Have a new patch idea?
Open an issue — I might get to it when I'm bored.

Bug reports must include logs, the patch source release used to create the patched APK (for example `stable v1.8.0` or `dev v1.8.0-dev.3`), and the APK source/type.

## ☕️ Like my work? Consider donating

I'm a **full‑time student** (read: perpetually broke).
These patches cost me AI subscription bills, late‑night debugging, and way too much coffee.

If you find any of this useful, a small donation would genuinely help me keep the lights on (and keep the AI subscriptions running).

**Reach out at:** `rushiranpise17@gmail.com`
(PayPal, Zelle, UPI)

Even a small amount makes a difference 🙏

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.8.0-dev.3](https://github.com/rushiranpise/morphe-patches/releases/tag/v1.8.0-dev.3)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;85 patches total
<details open>
<summary>📦 Waze - GPS, Maps & Traffic&nbsp;&nbsp;•&nbsp;&nbsp;12 patches&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.waze'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 5.19.0.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Alert Distances](#alert-distances) | Configures radar/camera and hazard alert announcement distances.<br>Credits: Waze CGE Mod.<br> | • Accident alert (m)<br>• General alert (m)<br>• Police / camera alert (m)<br>• Enforcement — freeways (m)<br>• Enforcement — highways (m)<br>• Enforcement — streets (m)<br>• Hazard alert (m)<br>• Heavy traffic alert (m)<br>• Min between alerts (m) |
| [AutoZoom](#autozoom) | Controls how aggressively the map zooms in/out based on driving speed.<br>Credits: Waze Chuppito Mod | • Speed factor<br>• Scale factor<br>• Max scale<br>• Gradient speed threshold (km/h) |
| [Disable Ads](#disable-ads) | Suppresses all Waze ad systems via bundled preferences file:<br>• AdMob SDK (Ad_.*)<br>• Google Ads (Google_Ads.*)<br>• Ads Inventory Prediction<br>• ExternalPOI pins, coupons, popups (ExternalPO_ + Extern__POI both key variants)<br>• Search autocomplete server ads<br>Credits: Waze CGE Mod (ExternalPOI keys), Waze Chuppito (dual-key coverage). |  |
| [Disable Advil Ad Requests](#disable-advil-ad-requests) | Stubs AdvilRequest.getPageUrl() → "" so the Advil ad server receives no page URL and returns no ad content.  |  |
| [Enlarged Speedometer](#enlarged-speedometer) | Increases speedometer digit size for better readability. | • Text size — speed below 100<br>• Text size — speed 100+ |
| [Map Skin (Vitamin C)](#map-skin-vitamin-c) | Applies Chuppito's 'Vitamin C' map skin. All visual values configurable.<br>• Night: true black AMOLED background (saves battery, prevents burn-in)<br>• Day: warm beige background<br>• Larger font labels across the board<br>• Wider navigation arrow head for better visibility<br>• Custom car 3D models: Batmobile, Riddler, race car, 3D arrow<br>Credits: ALEX02-GTT (skin design), Waze Chuppito Mod (integration). | • Night background color (hex)<br>• Day background color (hex)<br>• Font size — huge labels<br>• Font size — big labels<br>• Font size — medium labels<br>• Font size — small labels<br>• Nav arrow head width factor |
| [Navigation & Map](#navigation-map) | Configures navigation and map behaviour:<br>• Nearing destination distance (Credits: CGE Mod)<br>• Android Auto head-up alert distances<br>• Map turn mode (auto-zoom to upcoming turn)<br>• Traffic bar minimum time threshold<br>• GPS icon visibility<br>• Route notifications (hazard, school zone) both disabled by default<br>Credits: Waze CGE Mod (nearing destination), Waze Chuppito (remaining keys). | • Nearing destination distance (m)<br>• Android Auto head-up — normal roads (m)<br>• Android Auto head-up — freeways (m)<br>• Traffic bar min time in traffic (seconds)<br>• Show GPS icon on map<br>• Map turn mode (auto-zoom to turn)<br>• Permanent hazard route notification<br>• School zone route notification |
| [Popup Suppression](#popup-suppression) | Prevents promotional and ad popups from appearing while driving.<br>Raises the minimum trigger speed to a near-impossible value so popups never appear.<br>Credits: Waze Chuppito Mod. | • Min speed to show popups (MMSec)<br>• Fully stopped speed (MMSec)<br>• Min distance to show popup (m)<br>• Min reset scroll speed (MMSec)<br>• Delay after user interaction (seconds) |
| [Radar Sound (Any Speed)](#radar-sound-any-speed) | Plays radar/speed camera sound alerts regardless of current speed. Official Waze only alerts when over the speed limit.<br> |  |
| [Report Speed Limit](#report-speed-limit) | Adds a Report option when tapping the speedometer to report wrong or missing speed limits. Not available in the official version.<br> |  |
| [Speed Limit Sign](#speed-limit-sign) | Sets the speed limit sign style shown on the map. | • Sign style |
| [Uncensored Radar / Camera Display](#uncensored-radar-camera-display) | Shows exact fixed and mobile speed camera locations, including those not yet in the official Waze radar zone. Enables enforcement alerts via preferences keys:<br>Credits: Waze CGE Mod. |  |

</details>

<details open>
<summary>📦 Crime Radar&nbsp;&nbsp;•&nbsp;&nbsp;6 patches&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.newsbreak.crimeradar'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 26.23.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Bypass License Check](#bypass-license-check) | Bypasses PairIP DRM license verification to prevent forced app shutdown on non-Play-licensed installs. |  |
| [Bypass Subscription Paywall](#bypass-subscription-paywall) | Bypasses the subscription paywall in-app. |  |
| [Remove Item Limits](#remove-item-limits) | Removes all  item limits in-app. |  |
| [Suppress Premium Promotions](#suppress-premium-promotions) | Supress all premium promotions in-app. |  |
| [Unlock Followed Locations](#unlock-followed-locations) | Unlocks the Followed Locations premium feature. |  |
| [Unlock Premium](#unlock-premium) | Unlocks Premium Features In the App. |  |

</details>

<details open>
<summary>📦 MovieBox TV&nbsp;&nbsp;•&nbsp;&nbsp;7 patches&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.community.oneroom'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.0.15.0616.03 | 1.0.2.0526.03 |
| :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Bypass member rights check](#bypass-member-rights-check) | Forces MemberCheckResult.isPassed() to always return Boolean.TRUE, bypassing the server-side membership gate and preventing the 'Get Premium / 7-day trial' dialog. |  |
| [Clear VIP resolution tip](#clear-vip-resolution-tip) | Forces MemberResolutionBean.getVipResolutionTip() to always return Boolean.FALSE, suppressing any 'VIP only' badge shown on resolution options. |  |
| [Spoof member active status](#spoof-member-active-status) | Forces MemberInfo.isActive() to always return true, making the app believe the membership subscription is active. |  |
| [Spoof member days left](#spoof-member-days-left) | Forces MemberInfo.getDaysLeft() to always return 3650 (10 years), preventing the '0 days left' display on the premium screen. |  |
| [Spoof member provider flags](#spoof-member-provider-flags) | Forces MemberProvider.c() (kv_is_pay_enable_member) and MemberProvider.e() (kv_is_skip_ad) to always return true. |  |
| [Suppress newbie bonus dialog](#suppress-newbie-bonus-dialog) | Makes MemberProvider.w(F)V return immediately, suppressing the 'Claim 7-day trial / newbies bonus' ClaimMemberDialog popup. |  |
| [Unlock member resolution](#unlock-member-resolution) | Forces MemberResolutionBean.isUnlock() to always return Boolean.TRUE, making all locally-cached episodes appear as unlocked to the playback layer. |  |

</details>

<details open>
<summary>📦 Universal TV Remote Control&nbsp;&nbsp;•&nbsp;&nbsp;3 patches&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=sensustech.universal.tv.remote.control'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.8.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Ads](#disable-ads) | Prevents the AppOpen ad preloader from initialising. |  |
| [Suppress Paywall](#suppress-paywall) | Suppresses the in-app paywall. |  |
| [Unlock Premium](#unlock-premium) | Unlocks Premium Features In the App. |  |

</details>

<details open>
<summary>📦 1.1.1.1 + WARP&nbsp;&nbsp;•&nbsp;&nbsp;2 patches&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.cloudflare.onedotonedotonedotone'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 6.38.7 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Analytics / Telemetry](#disable-analytics-telemetry) | Disables App Analytics / Telemetry. |  |
| [Spoof WARP+ Unlimited UI](#spoof-warp-unlimited-ui) | Unlocks WARP+ UI locally. |  |

</details>

<details open>
<summary>📦 Case Tracker - Immigration&nbsp;&nbsp;•&nbsp;&nbsp;2 patches&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.saldous.casetracker'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 5.5.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove Ads](#remove-ads) | Removes interstitial, app-open and native ads. |  |
| [Unlock Premium](#unlock-premium) | Unlocks Premium Features In the App. |  |

</details>

<details open>
<summary>📦 m-Indicator&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.mobond.mindicator'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 18.0.352 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove Ads](#remove-ads) | Removes interstitial and exit native ads from m-Indicator. |  |

</details>

<details open>
<summary>📦 Psiphon Pro&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.psiphon3.subscription'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 476 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove Ads / Unlock Premium](#remove-ads-unlock-premium) | Forces SubscriptionStateImpl.getStatus() (LB2/c.h) to always return HAS_UNLIMITED_SUBSCRIPTION and getPurchase() (LB2/c.g) to return a well-formed fake Purchase, removing ads and the upgrade button/banner without crashing on the now-expected non-null Purchase object. |  |

</details>

<details open>
<summary>📦 Canva&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.canva.editor'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.365.o |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove Watermark](#remove-watermark) | Removes watermarks from Canva exports and previews. |  |

</details>

<details open>
<summary>📦 AIDA64&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.finalwire.aida64'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.20 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove ads](#remove-ads) | Stubs all ad loading paths: banner/interstitial loaders, billing callbacks, and ad SDK init. |  |

</details>

<details open>
<summary>📦 Speedtest by Ookla&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=org.zwanoo.android.speedtest'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 7.0.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Ad-Free](#unlock-ad-free) | Removes ads and unlocks ad-free status in Speedtest by Ookla. |  |

</details>

<details open>
<summary>📦 nzb360&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.kevinforeman.nzb360'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 23.5 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock All Access](#unlock-all-access) | Unlocks All access in Nzb360. |  |

</details>

<details open>
<summary>📦 Pialytic&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=verbosus.pialytic'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.2.8 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock All Features](#unlock-all-features) | Bypasses PairIP DRM license check, removes all paywalls, and unlocks all premium features including cloud sync and remote access. |  |

</details>

<details open>
<summary>📦 Flightradar24&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.flightradar24free'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 11.6.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Business Premium](#unlock-business-premium) | Unlocks Business/Gold premium features in Flightradar24: ad-free map, weather layers, ATC, 3D view, flight history, and unlimited saved locations. |  |

</details>

<details open>
<summary>📦 Greenify&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.oasisfeng.greenify'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 5.1.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Donation Features](#unlock-donation-features) | Unlocks all premium donation features in Greenify. |  |

</details>

<details open>
<summary>📦 BlockerHero&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.blockerhero'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.5.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Lifetime](#unlock-lifetime) | Unlocks lifetime subscription features in BlockerHero. |  |

</details>

<details open>
<summary>📦 Proxyman&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.proxyman.proxymanandroid'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.16.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Lifetime](#unlock-lifetime) | Unlocks all Lifetime features in Proxyman. |  |

</details>

<details open>
<summary>📦 SHAREit Premium&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=shareit.premium'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.1.98 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Lifetime](#unlock-lifetime) | Unlocks SHAREit lifetime premium. |  |

</details>

<details open>
<summary>📦 AdGuard Nightly&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.adguard.android'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 4.14.68 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Lifetime Premium](#unlock-lifetime-premium) | Unlocks all features locked behind the subscription paywall. |  |

</details>

<details open>
<summary>📦 3D Anatomy & Physiology&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=air.com.musclemotion.anatomy'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.2.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all premium content in Anatomy by Muscle Motion. |  |

</details>

<details open>
<summary>📦 Call Recorder - Automatic&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.catalinagroup.callrecorder'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.4.281 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks Premium Features In the App. |  |

</details>

<details open>
<summary>📦 CPU-Z&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.cpuid.cpu_z'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.57 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks Premium features in app. |  |

</details>

<details open>
<summary>📦 Hola VPN Proxy Plus&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=org.hola.play'>📥</a></summary>
<br>

**🎯 Supported versions:**

| AARCH64_1.248.400 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) |  |  |

</details>

<details open>
<summary>📦 Kyphosis Exercises - Hunchback&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=air.com.musclemotion.kyphosis'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.4.9 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all premium content. |  |

</details>

<details open>
<summary>📦 Beta Maniac&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=it.mirko.beta'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 0.9.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks premium in Beta by Mirko. |  |

</details>

<details open>
<summary>📦 NetMonster&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=cz.mroczis.netmonster'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.4.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all premium features. |  |

</details>

<details open>
<summary>📦 Ninja VPN&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=app.ninjavpn.android'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.4.6 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks Ninja VPN premium. |  |

</details>

<details open>
<summary>📦 Photo Editor&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.iudesk.android.photo.editor'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 13.3 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all Photo Editor premium features by bypassing license checks. |  |

</details>

<details open>
<summary>📦 Posture Correction Exercises&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=air.com.musclemotion.posture'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.2.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all premium content in Posture Correction by Muscle Motion. |  |

</details>

<details open>
<summary>📦 Proton VPN&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=ch.protonvpn.android'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 5.18.84.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks local Proton VPN premium  features. |  |

</details>

<details open>
<summary>📦 Rocket Money&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.truebill'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 13.15.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks Rocket Money Premium Features. |  |

</details>

<details open>
<summary>📦 SAI&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.mtv.sai'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.2.8 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Bypasses PairIP license check, paywall, ads, and pro upsell in SAI. |  |

</details>

<details open>
<summary>📦 Snipd: AI Podcast Player&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=ai.topicfinder.podcastdiscovery'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 4.1.14 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all Premium features in Snipd: AI Podcast Player by spoofing the RevenueCat CustomerInfo. |  |

</details>

<details open>
<summary>📦 Social Gamebox&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.app.social_gamebox'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.1.3 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks Social Gamebox premium features. |  |

</details>

<details open>
<summary>📦 Strength Training: Gym Workout&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=air.com.musclemotion.strength.mobile'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.5.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all premium content in Strength Training by Muscle Motion.  |  |

</details>

<details open>
<summary>📦 The Weather Channel&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.weather.Weather'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 16.10.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks The Weather Channel's Premium and Premium Pro subscription tiers. Enables the ad-free experience, extended 15-day hourly forecast, real-feel temperature, air quality index, minute-by-minute precipitation, severe weather notifications, and radar overlays gated behind the subscription paywall. |  |

</details>

<details open>
<summary>📦 TomTom GO Navigation&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.tomtom.gplay.navapp'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.6.316-beta |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks car and truck premium features. |  |

</details>

<details open>
<summary>📦 Windscribe VPN&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.windscribe.vpn'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 4.1.2274 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks Windscribe premium account. |  |

</details>

<details open>
<summary>📦 Windy: Weather Radar & Forecast&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.windyty.android'>📥</a></summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks Windy Pro features. |  |

</details>

<details open>
<summary>📦 Workout & Gym Training Plan&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=air.com.musclemotion.workout'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.2.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all premium content. |  |

</details>

<details open>
<summary>📦 Yoga - Poses & Classes&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=air.com.musclemotion.yoga'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.2.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all premium content in Yoga by Muscle Motion. |  |

</details>

<details open>
<summary>📦 Clickmate&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.inscode.autoclicker'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 7.1.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium ](#unlock-premium) | Unlocks premium in Clickmate |  |

</details>

<details open>
<summary>📦 AccuWeather: Weather Radar&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.accuweather.android'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 21.1.11-1-rc |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium+](#unlock-premium) | Unlocks AccuWeather's Premium+ subscription tier without a Play Store purchase. Enables the full 15-day and hourly forecast detail, MinuteCast extended precision, air quality and health indexes, real-feel temperature, severe weather notifications, and widget customisation. |  |

</details>

<details open>
<summary>📦 Cashew - Budget & Finance App&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.budget.tracker_app'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 6.5.9 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlock Pro Features in Cashew App |  |

</details>

<details open>
<summary>📦 Citizen - Safety Alert&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=sp0n.citizen'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 0.1298.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks all Citizen Plus/Protect features: Safety Network, Safety Center, Zones, Live Agent, Offender alerts, Clarity crime map, incident video, and more. |  |

</details>

<details open>
<summary>📦 Hibernator&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.tafayor.hibernator'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.56.10 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks all pro features in Hibernator. |  |

</details>

<details open>
<summary>📦 KillApps&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.tafayor.killall'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 1.57.9 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks all pro features in KillApps. |  |

</details>

<details open>
<summary>📦 ML Manager&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.javiersantos.mlmanager'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 5.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks all pro features in ML Manager. |  |

</details>

<details open>
<summary>📦 Permission Pilot&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=eu.darken.myperm'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.1.1-rc0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks all Pro features in app. |  |

</details>

<details open>
<summary>📦 NetGuard&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=eu.faircode.netguard'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.335 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks all pro features in NetGuard. |  |

</details>

<details open>
<summary>📦 NetShare - Hotspot & Wifi Direct&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=kha.prog.mikrotik'>📥</a></summary>
<br>

**🎯 Supported versions:**

| UI/link-274 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks Pro features in app. |  |

</details>

<details open>
<summary>📦 1Tap Cleaner&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.a0soft.gphone.acc.free'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 5.18 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks 1Tap Cleaner PRO: history export, app-group filters, unlimited cache targets, ad removal. |  |

</details>

<details open>
<summary>📦 RAR&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.rarlab.rar'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 7.20.build131 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Removes ads and unlocks the no-ads subscription in RAR. |  |

</details>

<details open>
<summary>📦 App Permission Manager&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.shexa.permissionmanager'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.4.6.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks all Pro features in app. |  |

</details>

<details open>
<summary>📦 Splitwise&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.Splitwise.SplitwiseMobile'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 26.5.5 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks Splitwise Pro features, removes ad banners, and suppresses all upgrade upsell prompts. |  |

</details>

<details open>
<summary>📦 Stargazing Hub&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.twtapp'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 3.2.1 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Subscription](#unlock-subscription) | Unlocks all subscription features in TWT App. |  |

</details>

<details open>
<summary>📦 TeraBox&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.dubox.drive'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 4.19.6 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock VIP](#unlock-vip) | Unlocks Dubox Drive VIP/SVIP (Premium+) |  |

</details>

<details open>
<summary>📦 Yatri&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.yatrirailways.yatri'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 5.0.3 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock VIP](#unlock-vip) | Unlocks Yatri VIP by forcing active plan status and spoofing active plan DB query. |  |

</details>

<details open>
<summary>📦 HTTP Sniffer&nbsp;&nbsp;•&nbsp;&nbsp;1 patch&nbsp;&nbsp;<a href='https://play.google.com/store/apps/details?id=com.anetcapture.mock'>📥</a></summary>
<br>

**🎯 Supported versions:**

| 2.11.0-ad_mob |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock VIP (Lifetime)](#unlock-vip-lifetime) | Forces permanent professional VIP tier, removes ads and upgrade popups, bypasses PairIP. |  |

</details>

<!-- PATCHES_END -->

#### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=rushiranpise/morphe-patches

Or manually add this repository url as a patch source in Morphe: https://github.com/rushiranpise/morphe-patches

### 📙 Contributing

Thank you for considering contributing to Rushi's Morphe Patches.  
You can find the contribution guidelines [here](CONTRIBUTING.md).

### 🛠️ Building

To build Rushi's Morphe Patches,
you can follow the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation).

> **Note:** Not all apps can be patched. Server-side features (AI, cloud sync, server-validated subscriptions) cannot be bypassed.

## FAQ

#### How do I use this?
Install [Morphe Manager](https://morphe.software), add this repo as a patch source, then select an app to patch.

#### Capture logs 

Refer to [Morphe Documentation](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-resources/questions.md#40-capture-logs). When opening a bug report, include the relevant logs, the patch source release you used (for example `stable v1.8.0` or `dev v1.8.0-dev.3`), and the APK source/type.

#### Help, why can't I log in with my Google account? Why doesn't Google Drive work?
MicroG integration is needed for features that require Google Play Services.
Patched apps are re-signed, which breaks Google Play Services authentication. This is a known limitation for all patched apps.

#### What APK version should I use?
Use the suggested versions. Download the correct format (XAPK/APKM/APK) and architecture (arm64 for modern phones).

#### What APK formats are supported?
Use clean APK, XAPK, or APKM files from a reputable source. Prefer `arm64-v8a` for modern phones, and avoid already-modded or repacked APKs when reporting bugs.

#### Can you make a patch for \[Super Cool App Goes Here\]?
Maybe? Each patch is different, and many features use server-side functionality that cannot be modified. Before submitting a request, please check the existing open (and closed!) issues to reduce duplication.

#### You haven't completed my patch request.. Did you not see it? / Did you forget? / Why do you hate me?
I did see it. I haven't forgotten. We're good, bro. I'm a one-person "team" who does this stuff in my free time. If it's within my technical capabilities, I'll get around to it at some point.

## 📩 Developers

If you're an app developer and have concerns about a patch, feel free to contact me or submit a takedown request.

I'm happy to discuss issues and will review reasonable requests in good faith.

## Disclaimer

> **⚠️ Legal Notice**
>
> This project is provided for **educational and research purposes only**. The patches in this repository modify third-party applications and may violate the terms of service of those applications.
>
> - This project is **not affiliated** with any of the app developers listed above.
> - Use these patches **at your own risk**. The author is not responsible for any consequences.
> - If you are a developer or rights holder and believe this project infringes on your rights, please [submit a takedown request](https://github.com/rushiranpise/morphe-patches/issues/new?template=takedown-request.yml) and the relevant patches will be **promptly removed**.
> - Users are encouraged to **support developers** by purchasing legitimate subscriptions if they find the apps useful.

## ❤️ Credits

Thanks to:

- Morphe developers
- Contributors
- Everyone who reports bugs and tests patches

## 📜 License

Rushi's Morphe Patches are licensed under the [GNU General Public License v3.0](LICENSE)
