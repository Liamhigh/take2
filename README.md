Verum Omnis Forensic Engine

A stateless, offline-first Android forensic evidence sealing application with cryptographic integrity guarantees.

## 🛡️ Constitutional Governance Layer

This project implements the **Verum Omnis Constitutional Governance Layer** - a set of immutable principles ensuring ethical, secure, and legally-admissible forensic evidence processing.

### Core Principles
- **Stateless**: No persistent user sessions or tracking
- **Offline-First**: All forensic operations work without network connectivity
- **Cryptographically Sealed**: SHA-512 hashing for evidence integrity
- **Zero Telemetry**: No data leaves the device
- **Airgap Ready**: Full functionality in isolated environments

### Forensic Standards
- **Hash Algorithm**: SHA-512
- **PDF Standard**: PDF 1.7
- **Seal Required**: Yes
- **Tamper Detection**: Mandatory
- **Admissibility Standard**: Legal-grade, contradiction-free

---

## 📱 Android Forensic App

The Android app (`android/`) provides:
- Evidence file selection and processing
- SHA-512 cryptographic sealing
- Chain of custody tracking
- Offline evidence verification
- PDF export with watermarks and QR codes

### Building the Android App

```bash
cd android
./gradlew assembleDebug
```

### Key Components

| Component | Description |
|-----------|-------------|
| `VerumForensicEngine` | Core forensic processing engine |
| `CryptoSealService` | SHA-512 hashing and sealing |
| `EvidenceProcessingService` | Background evidence processing |
| `OfflineModeManager` | Offline-first compliance |

---

## 🌐 Firebase Hosting & Mobile App Setup Guide

This comprehensive guide covers setting up Firebase Hosting for a website and Firebase for Android/iOS apps from start to finish.

Table of Contents

· Prerequisites
· Initial Firebase Setup
· Website Hosting
· Android App Setup
· iOS App Setup
· Firebase Features Configuration
· Deployment
· CI/CD Setup
· Troubleshooting

Prerequisites

Required Accounts & Tools

· Firebase Account: console.firebase.google.com
· Node.js (v16 or higher)
· Firebase CLI: npm install -g firebase-tools
· Git for version control
· Android Studio (for Android apps)
· Xcode (for iOS apps, macOS only)

For Mobile Apps

· Google Play Console account (Android)
· Apple Developer Account (iOS, $99/year)

Initial Firebase Setup

1. Create Firebase Project

```bash
# Login to Firebase
firebase login

# Create new project
firebase projects:create
```

Or via Firebase Console:

1. Go to Firebase Console
2. Click "Add project"
3. Enter project name (e.g., my-app-2024)
4. Enable Google Analytics (recommended)
5. Choose Analytics location

2. Initialize Firebase in Your Project

```bash
# Navigate to your project directory
cd my-project

# Initialize Firebase
firebase init

# Select features (Hosting, Firestore, Authentication, etc.)
# Choose your Firebase project
# Configure as needed
```

Website Hosting

1. Project Structure

```
my-project/
├── public/ # Website files
│ ├── index.html
│ ├── css/
│ ├── js/
│ └── images/
├── functions/ # Cloud Functions (optional)
├── firebase.json
└── .firebaserc
```

2. Basic Firebase Configuration

firebase.json

```json
{
  "hosting": {
    "public": "public",
    "ignore": [
      "firebase.json",
      "**/.*",
      "**/node_modules/**"
    ],
    "rewrites": [
      {
        "source": "**",
        "destination": "/index.html"
      }
    ],
    "headers": [
      {
        "source": "**/*.@(eot|otf|ttf|ttc|woff|font.css)",
        "headers": [
          {
            "key": "Access-Control-Allow-Origin",
            "value": "*"
          }
        ]
      },
      {
        "source": "**/*.@(js|css)",
        "headers": [
          {
            "key": "Cache-Control",
            "value": "max-age=604800"
          }
        ]
      }
    ]
  }
}
```

3. Deploy Website

```bash
# Build your website (if using a framework)
npm run build

# Deploy to Firebase
firebase deploy --only hosting

# Deploy everything
firebase deploy
```

Android App Setup

1. Add Android App to Firebase

1. Firebase Console → Project Overview → Android icon
2. Enter Android package name (e.g., com.yourapp.android)
3. Enter app nickname (optional)
4. Download google-services.json

2. Android Project Configuration

app/build.gradle

```gradle
apply plugin: 'com.android.application'
apply plugin: 'com.google.gms.google-services' // Add this line

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-analytics'
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    // Add other Firebase services as needed
}
```

project/build.gradle

```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0' // Google services plugin
    }
}
```

3. Place Configuration File

Copy google-services.json to app/ directory in your Android project.

4. Initialize Firebase in Android App

MainActivity.java

```java
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        
        // Use Firebase services...
    }
}
```

Or in Kotlin:

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
    }
}
```

iOS App Setup

1. Add iOS App to Firebase

1. Firebase Console → Project Overview → iOS icon
2. Enter iOS bundle ID (e.g., com.yourapp.ios)
3. Enter app nickname
4. Download GoogleService-Info.plist

2. iOS Project Configuration

Podfile

```ruby
platform :ios, '13.0'

target 'YourApp' do
  use_frameworks!
  
  # Pods for YourApp
  pod 'Firebase/Analytics'
  pod 'Firebase/Auth'
  pod 'Firebase/Firestore'
  # Add other Firebase pods as needed
end
```

```bash
# Install pods
pod install
```

3. Add Configuration File

1. Open your project in Xcode
2. Drag GoogleService-Info.plist into your project
3. Ensure "Copy items if needed" is checked

4. Initialize Firebase in iOS App

AppDelegate.swift

```swift
import UIKit
import Firebase

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(_ application: UIApplication, 
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Configure Firebase
        FirebaseApp.configure()
        
        return true
    }
}
```

For Objective-C:
AppDelegate.m

```objective-c
#import "AppDelegate.h"
#import <Firebase/Firebase.h>

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application 
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    // Configure Firebase
    [FIRApp configure];
    
    return YES;
}

@end
```

Firebase Features Configuration

1. Authentication Setup

Firebase Console → Authentication → Get Started

· Enable Email/Password
· Enable Google Sign-In
· Configure other providers as needed

Example Usage (Web):

```javascript
// Initialize
import { initializeApp } from 'firebase/app';
import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// Sign in
signInWithEmailAndPassword(auth, email, password)
  .then((userCredential) => {
    const user = userCredential.user;
  })
  .catch((error) => {
    console.error(error);
  });
```

2. Firestore Database

Firebase Console → Firestore Database → Create Database

· Start in test mode initially
· Choose location closest to your users

Example Usage:

```javascript
import { getFirestore, collection, addDoc } from 'firebase/firestore';

const db = getFirestore(app);

// Add data
const docRef = await addDoc(collection(db, "users"), {
  name: "John Doe",
  email: "john@example.com",
  createdAt: new Date()
});
```

3. Storage Setup

Firebase Console → Storage → Get Started

```javascript
import { getStorage, ref, uploadBytes } from 'firebase/storage';

const storage = getStorage(app);
const storageRef = ref(storage, 'images/profile.jpg');

// Upload file
uploadBytes(storageRef, file).then((snapshot) => {
  console.log('Uploaded file!');
});
```

Deployment

Website Deployment

```bash
# Build and deploy
npm run build
firebase deploy --only hosting

# Deploy to specific site (if multiple sites)
firebase deploy --only hosting:my-site
```

Environment Setup

.firebaserc

```json
{
  "projects": {
    "default": "my-app-production",
    "staging": "my-app-staging",
    "dev": "my-app-development"
  }
}
```

```bash
# Deploy to different environments
firebase use dev
firebase deploy

firebase use production
firebase deploy
```

CI/CD Setup

GitHub Actions Example

.github/workflows/firebase-deploy.yml

```yaml
name: Deploy to Firebase

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build_and_deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
    
    - name: Install dependencies
      run: npm ci
    
    - name: Build
      run: npm run build
    
    - name: Deploy to Firebase
      uses: FirebaseExtended/action-hosting-deploy@v0
      with:
        repoToken: '${{ secrets.GITHUB_TOKEN }}'
        firebaseServiceAccount: '${{ secrets.FIREBASE_SERVICE_ACCOUNT }}'
        channelId: live
        projectId: my-app-production
      env:
        FIREBASE_CLI_EXPERIMENTS: webframeworks
```

Environment Variables Setup

1. Firebase Console → Project Settings → Service Accounts
2. Generate new private key
3. Add to GitHub Secrets as FIREBASE_SERVICE_ACCOUNT

Mobile App Deployment

Android Release

```bash
# Generate signed APK/Bundle
./gradlew bundleRelease # For App Bundle
./gradlew assembleRelease # For APK
```

Play Console:

1. Create app in Google Play Console
2. Upload signed bundle/APK
3. Set up store listing
4. Submit for review

iOS Release

1. Xcode → Product → Archive
2. Organizer → Distribute App
3. Upload to App Store Connect
4. App Store Connect → Submit for Review

Advanced Configuration

Custom Domain Setup

1. Firebase Console → Hosting → Add custom domain
2. Verify domain ownership
3. Update DNS records
4. Wait for SSL certificate provisioning

Multiple Sites Setup

```bash
# Add additional sites
firebase hosting:sites:create my-second-site

# Deploy to specific site
firebase deploy --only hosting:my-second-site
```

Security Rules

Firestore Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write access to authenticated users
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Public read access, authenticated write
    match /posts/{postId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

Storage Rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Monitoring & Analytics

Performance Monitoring

```javascript
import { getPerformance } from 'firebase/performance';

const perf = getPerformance(app);
```

Crashlytics Setup

Android (build.gradle):

```gradle
dependencies {
    implementation 'com.google.firebase:firebase-crashlytics'
}
```

iOS (Podfile):

```ruby
pod 'Firebase/Crashlytics'
```

Troubleshooting

Common Issues

1. Build Failures:
   · Check Firebase SDK versions
   · Verify configuration files are in correct locations
   · Clean and rebuild project
2. Authentication Issues:
   · Verify Auth providers are enabled in Firebase Console
   · Check authorized domains for web apps
   · Verify SHA certificates for Android
3. Deployment Issues:
   · Check Firebase CLI version: firebase --version
   · Verify project permissions
   · Check hosting quota limits

Useful Commands

```bash
# Debug deployment
firebase deploy --debug

# Check hosting status
firebase hosting:channel:list

# View logs
firebase functions:log

# Emulate locally
firebase emulators:start
```

Maintenance

Regular Tasks

· Update Firebase SDKs quarterly
· Review security rules
· Monitor usage and costs
· Backup Firestore data
· Renew SSL certificates (automatic with Firebase)

Cost Optimization

· Implement efficient database queries
· Use caching strategies
· Monitor storage usage
· Set up billing alerts

---

Support Resources

· Firebase Documentation
· Firebase Community
· Stack Overflow - Firebase tag
· Firebase Status Dashboard

This setup provides a robust foundation for hosting your website and mobile apps with Firebase. Adjust configurations based on your specific requirements and scale.
