# WMR-Mobile

WMR Mobile is a privacy-conscious Android photo editor starter app built with Kotlin and Jetpack Compose.

## Current features

- Select a photo using Android's system Photo Picker.
- Preview a locally selected image.
- Adjust brightness, contrast, and saturation.
- Toggle between the original and edited preview.
- Reset all adjustments.
- Export the edited result as a new JPEG using Android's document picker.
- Keep the original selected file unchanged; the app does not upload photos.

## Build APK

The GitHub Actions workflow runs unit tests, builds a debug APK, and uploads it as the `WMR-Mobile-debug-apk` artifact.

1. Open the **Actions** tab.
2. Select the latest **Android APK** workflow run.
3. Wait for the run to finish successfully.
4. Download the `WMR-Mobile-debug-apk` artifact and extract `app-debug.apk`.

## Important limitations

This is an early photo-editor version, not a complete professional editor. Adjustments are rendered into a new export; the original file is not overwritten. The app does not remove watermarks, strip provenance metadata, or attempt to evade AI-content labels. No facial identity reconstruction or face reshaping is implemented.

## Technical details

- Kotlin 2.2.20
- Android Gradle Plugin 8.13.2
- Jetpack Compose + Material 3
- Minimum Android API 26
- Java 17
