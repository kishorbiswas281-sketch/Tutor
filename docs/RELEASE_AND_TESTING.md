# Testing & APK Build Instructions

## 1. Running Tests

### Android Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```
This runs:
- `TutorEnginesUnitTest`: Grammar correction rules, AI teacher transcript parser, pronunciation evaluator, and 20-lesson / 64-word curriculum validation.
- `ExampleRobolectricTest`: Room in-memory persistence, `TutorRepository` progress & conversation lifecycle, and Compose UI rendering tests.

### Backend Tests
```bash
cd backend
npm install
npm test
```

---

## 2. Building the Debug APK

```bash
gradle :app:assembleDebug
```
The debug APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

Install on a connected device or emulator:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Building a Signed Release APK

1. Generate an upload keystore (if you do not already have one):
```bash
keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```
2. Export signing environment variables (never commit keystore passwords to Git):
```bash
export KEYSTORE_PATH=/path/to/my-upload-key.jks
export STORE_PASSWORD=your_store_password
export KEY_PASSWORD=your_key_password
```
3. Build the signed release APK (or Android App Bundle for Google Play):
```bash
gradle :app:assembleRelease
# Or for Play Store AAB:
gradle :app:bundleRelease
```
Outputs:
- Signed APK: `app/build/outputs/apk/release/app-release.apk`
- Signed AAB: `app/build/outputs/bundle/release/app-release.aab`
