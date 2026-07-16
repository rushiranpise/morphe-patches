# Universal Patches

Ports and Morphe-style equivalents inspired by ReVanced Patches universal/all patches.

Credit: ReVanced Patches, GPL-3.0, https://github.com/ReVanced/revanced-patches

Implemented here:
- Export all activities
- Enable Android debugging
- Hide app icon
- Predictive back gesture
- Change version code
- Set target SDK 34
- Remove share targets
- Force dark theme
- Hide mock location
- Hide ADB status
- Spoof build info
- Spoof SIM provider
- Spoof Wi-Fi connection
- Override certificate pinning
- Export internal data DocumentsProvider
- Enable ROM signature spoofing
- Spoof keystore security level
- Spoof root of trust
- Spoof Play age signals
- Remove ad manifest entries
- Disable ad SDK calls
- Disable shake ads
- Disable clipboard access
- Hide VPN and proxy

Existing Morphe equivalents:
- Spoof Pixel device / build info: `shared.pixel.spoofPixelDevicePatch`
- Spoof install source: `shared.installer.spoofInstallSourcePatch`
- Spoof DRM/Widevine: `shared.drm.spoofDrmPatch`
- Signature/GMS/Firebase spoof helpers under `shared.signature`, `shared.gms`, `shared.firebase`
