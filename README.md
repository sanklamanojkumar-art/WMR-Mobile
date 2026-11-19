# WMR Mobile

WMR Mobile is a privacy-first Android photo studio built with Kotlin and Jetpack Compose. It takes inspiration from the broad editing experience of premium photo apps while maintaining its own design and implementation.

## Product direction

The goal is a polished, multi-tool photo studio—not a one-screen filter demo.

### Editing roadmap

1. **Foundation and reliability**
   - Stable image import, decoding, preview, reset, and export
   - Memory-safe editing pipeline and automated tests
   - Preserve original files; save edits as new files

2. **Core photo studio**
   - Brightness, contrast, saturation, warmth, tint, highlights, shadows
   - Curated color presets with adjustable strength
   - Crop, rotate, straighten, and flip
   - Before/after comparison and undo/redo

3. **Portrait tools with identity protection**
   - Optional, subtle skin texture/lighting adjustments
   - No face reshaping, eye/nose/lip resizing, face replacement, or identity reconstruction
   - Every portrait adjustment must be opt-in, adjustable, and reversible

4. **Creative studio**
   - Frames, borders, text, stickers, and overlays
   - Background blur and manual background tools
   - Collage layouts and multi-photo composition

5. **Polish and release**
   - Accessible premium UI, tool categories, presets, and onboarding
   - Full-resolution export, quality options, and metadata choices
   - Unit/instrumentation tests, crash checks, release signing, and store-ready privacy disclosures

## Privacy and image integrity

- Photos are selected through Android's system Photo Picker.
- The app does not upload selected photos to a server.
- Original files are never overwritten; edits are exported as new files.
- No AI-content/provenance removal or metadata stripping is provided.
- Face geometry and identity are protected by design. Identity-changing operations are out of scope.

## Current implementation

- Local image selection and preview
- Brightness, contrast, and saturation adjustments
- Natural, Vivid, Warm, Cool, Mono, and Soft color presets
- Before/after toggle and reset
- JPEG export to a new file

## Build APK

GitHub Actions runs unit tests, builds a debug APK, and uploads it as the `WMR-Mobile-debug-apk` artifact.

1. Open the **Actions** tab.
2. Select the latest **Android APK** workflow run.
3. Wait for the run to finish successfully.
4. Download the `WMR-Mobile-debug-apk` artifact and extract `app-debug.apk`.

## Important status note

A successful CI build confirms compilation and the configured automated tests only. It does not prove every editing interaction works on every physical phone. Device testing is tracked separately before a feature is called verified.
