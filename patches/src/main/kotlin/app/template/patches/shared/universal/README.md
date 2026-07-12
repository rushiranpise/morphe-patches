# Universal Patches

Ports and Morphe-style equivalents inspired by ReVanced Patches universal/all patches.

Credit: ReVanced Patches, GPL-3.0, https://github.com/ReVanced/revanced-patches

Implemented here:
- Export all activities
- Enable Android debugging
- Hide app icon
- Predictive back gesture
- Remove screen capture restriction
- Change version code
- Set target SDK 34
- Remove share targets
- Remove screenshot restriction
- Hide mock location
- Disable Play Integrity
- Hide ADB status
- Spoof build info
- Spoof SIM provider
- Spoof Wi-Fi connection
- Custom certificates / network security config
- Override certificate pinning
- Export internal data DocumentsProvider
- Add resource file
- Hex patch
- Change package name
- Enable ROM signature spoofing
- Spoof keystore security level
- Spoof root of trust
- Spoof Play age signals
- Remove runtime audio capture policy

Existing Morphe equivalents:
- Spoof Pixel device / build info: `shared.pixel.spoofPixelDevicePatch`
- Spoof install source: `shared.installer.spoofInstallSourcePatch`
- Spoof DRM/Widevine: `shared.drm.spoofDrmPatch`
- Signature/GMS/Firebase spoof helpers under `shared.signature`, `shared.gms`, `shared.firebase`
