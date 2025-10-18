# verum

[![Firebase](https://img.shields.io/badge/Firebase-%23039be5.svg?logo=firebase&logoColor=white)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)]()

A concise guide and project setup for hosting a website and mobile apps (Android / iOS) using Firebase.

- Quickstart — get running locally and deploy
- Mobile — Android & iOS setup notes
- Firebase features — Auth, Firestore, Storage
- CI/CD — GitHub Actions example
- Security & secrets — handling service accounts and envs

Table of contents
- Quickstart
- Local development (emulators)
- Deploying to Firebase
- Android setup
- iOS setup
- Firebase features (Auth / Firestore / Storage)
- CI/CD (GitHub Actions)
- Security & secrets
- Troubleshooting
- Contributing
- License

---

## Quickstart

Prerequisites
- Node.js 16+ (or LTS)
- Firebase CLI: npm i -g firebase-tools
- Git
- For mobile: Android Studio (Android), Xcode (iOS/macOS)

Clone and install
```bash
git clone git@github.com:Liamhigh/take2.git
cd take2
# only if the project contains node packages
npm ci
```

Set Firebase project
```bash
# login
firebase login

# pick or create a project
firebase projects:list
firebase use <PROJECT_ID>   # or firebase use --add to add an alias
```

Run locally with emulators (recommended)
```bash
# start all configured emulators
firebase emulators:start
```

Deploy hosting
```bash
# build if using a framework
npm run build

# deploy hosting only
firebase deploy --only hosting

# deploy everything (hosting, functions, firestore rules, etc.)
firebase deploy
```

---

## Local development (emulators)
Use the Firebase emulators to develop without touching production services.

- Start emulators: firebase emulators:start
- Test Firestore rules and Auth locally
- Connect the web/mobile app to the local emulator host with the SDK emulator methods (see Firebase docs)

Example: Firestore emulator in web
```javascript
import { connectFirestoreEmulator } from "firebase/firestore";
connectFirestoreEmulator(db, "localhost", 8080);
```

---

## Android setup (summary)

1. Add Android app in Firebase Console and download google-services.json
2. Place google-services.json in app/ (Android module)
3. Gradle (app/build.gradle):
```gradle
apply plugin: 'com.android.application'
apply plugin: 'com.google.gms.google-services'

dependencies {
  implementation platform('com.google.firebase:firebase-bom:32.7.0')
  implementation 'com.google.firebase:firebase-analytics'
  implementation 'com.google.firebase:firebase-auth'
  implementation 'com.google.firebase:firebase-firestore'
}
```
4. Add SHA-1 and SHA-256 in Firebase console (for Auth / Google Sign-In)
5. Initialize Firebase in your Application or Activity:
```kotlin
FirebaseApp.initializeApp(context)
val db = FirebaseFirestore.getInstance()
```
6. Build signed bundle for Play:
```bash
./gradlew bundleRelease
```

---

## iOS setup (summary)

1. Add iOS app in Firebase Console and download GoogleService-Info.plist
2. Add plist to Xcode project (Copy items if needed)
3. Podfile example:
```ruby
platform :ios, '13.0'
target 'YourApp' do
  use_frameworks!
  pod 'Firebase/Analytics'
  pod 'Firebase/Auth'
  pod 'Firebase/Firestore'
end
```
4. Install pods:
```bash
pod install
```
5. Initialize in AppDelegate:
```swift
import Firebase
FirebaseApp.configure()
```
6. Archive & upload via Xcode for App Store release.

---

## Firebase features

Authentication (web example)
```javascript
import { initializeApp } from 'firebase/app';
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
signInWithEmailAndPassword(auth, email, password);
```

Firestore (web example)
```javascript
import { getFirestore, collection, addDoc } from 'firebase/firestore';
const db = getFirestore(app);
const ref = await addDoc(collection(db, 'users'), { name: 'John', email: 'john@example.com' });
```

Storage (web example)
```javascript
import { getStorage, ref, uploadBytes } from 'firebase/storage';
const storage = getStorage(app);
await uploadBytes(ref(storage, 'images/profile.jpg'), file);
```

Security rules examples are available in this repo's docs (or in the Firebase console). Always test rules with the emulator before deploying.

---

## CI / CD (GitHub Actions)

Example workflow (high-level):
- Checkout
- Setup Node
- Install deps & build
- Deploy using Firebase service account stored in GitHub Secrets

Add a secret `FIREBASE_SERVICE_ACCOUNT` with the JSON service account key and use FirebaseExtended/action-hosting-deploy or firebase CLI in the workflow.

---

## Security & secrets

Never commit:
- google-services.json / GoogleService-Info.plist with production credentials
- Service account JSON keys

Recommended:
- Add .env.example (do not include real values)
- Use GitHub Secrets for CI
- Use .gitignore to exclude local/secret files

Example .firebaserc
```json
{
  "projects": {
    "default": "my-app-production",
    "staging": "my-app-staging"
  }
}
```

---

## Troubleshooting (quick tips)

- Build failures: check Firebase SDK versions and Gradle / Cocoapods versions
- Emulator issues: firebase emulators:start --inspect-functions
- Auth issues: check OAuth redirect URIs, authorized domains, and SHA keys for Android
- Deployment issues: firebase deploy --debug

Useful commands
```bash
firebase --version
firebase deploy --only hosting --debug
firebase hosting:channel:list
firebase emulators:start
```

---

## Contributing
Contributions welcome — please open issues for bugs / feature requests, and create small focused PRs.

Suggested repository helpers:
- .env.example
- .gitignore
- CONTRIBUTING.md
- PR template
- CODE_OF_CONDUCT.md

---

## License
MIT — see LICENSE file.