# LockDPC - Android Device Owner Remote Lock System

Two Android apps for managing devices using Android Enterprise Device Owner mode.

- **User App** (`com.semdev.dpc.user`) — Installed as Device Owner on managed phone
- **Admin App** (`com.semdev.dpc.admin`) — Remote management console

## Architecture

```
Admin App → Firestore (commands/) → Cloud Function → FCM → User App (DPC) → lockNow()
```

## Setup

### 1. Firebase Project

1. Create a Firebase project at https://console.firebase.google.com
2. Enable **Authentication** → Sign-in method: Email/Password
3. Enable **Cloud Firestore** → Create database (start in test mode, then apply rules from `firebase/firestore.rules`)
4. Enable **Cloud Messaging** (FCM)
5. Register two Android apps in Firebase:
   - Package: `com.semdev.dpc.user` → download `google-services.json` → save as `user-app/app/google-services.json`
   - Package: `com.semdev.dpc.admin` → download `google-services.json` → save as `admin-app/app/google-services.json`

### 2. Create Admin User

In Firebase Console → Authentication → Add user with email/password.
Then in Firestore, create document: `admins/{uid}` with `{ "email": "admin@example.com", "name": "Admin" }`

### 3. Deploy Cloud Functions

```bash
cd functions
npm install
firebase deploy --only functions
```

### 4. Build APKs

```bash
# User app
cd user-app
./gradlew assembleDebug

# Admin app
cd admin-app
./gradlew assembleDebug
```

APKs will be at `*/app/build/outputs/apk/debug/*.apk`

### 5. Install on Devices

**Admin App:** Install on admin's phone (normal install)

**User App (Device Owner):** Install on SM-A075F, then run:

```bash
adb shell dpm set-device-owner com.semdev.dpc.user/.DeviceAdminReceiver
```

> Note: This requires a factory reset device or a device where no account is set up. On Samsung, you may need to remove your Google account first.

## Commands

| Command | Action |
|---------|--------|
| `LOCK` | Locks device with full-screen overlay |
| `UNLOCK` | Restores normal access |
| `REBOOT` | Forces device reboot |
| `WIPE` | Factory reset |

## GitHub CI

Three workflows are provided:

- `build-user-app.yml` — Builds user APK on push to `main`
- `build-admin-app.yml` — Builds admin APK on push to `main`
- `firebase-deploy.yml` — Deploys Cloud Functions

### GitHub Secrets Required

| Secret | Value |
|--------|-------|
| `GOOGLE_SERVICES_USER_JSON` | base64 of `user-app/google-services.json` |
| `GOOGLE_SERVICES_ADMIN_JSON` | base64 of `admin-app/google-services.json` |
| `KEYSTORE_JKS` | base64 of release keystore (optional) |
| `KEY_ALIAS` | Keystore key alias |
| `KEY_PASSWORD` | Keystore key password |
| `STORE_PASSWORD` | Keystore store password |
| `FIREBASE_SERVICE_ACCOUNT` | Firebase service account JSON (for functions deploy) |
