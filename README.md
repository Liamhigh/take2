Firebase Hosting & Mobile App Setup Guide

This comprehensive guide covers setting up Firebase Hosting for a website and Firebase for Android/iOS apps from start to finish. Firebase is a Backend-as-a-Service (BaaS) platform that provides developers with tools to build, improve, and grow their applications, including hosting, authentication, real-time databases, cloud storage, and more.

**What You'll Learn:**
- How to set up Firebase Hosting for web applications
- How to integrate Firebase into Android and iOS mobile apps
- How to configure Firebase features like Authentication, Firestore, and Storage
- How to deploy and manage your applications
- How to set up CI/CD pipelines for automated deployments
- Best practices for security and optimization

## Table of Contents

- [Quick Start](#quick-start)
- [Prerequisites](#prerequisites)
- [Initial Firebase Setup](#initial-firebase-setup)
- [Website Hosting](#website-hosting)
- [Android App Setup](#android-app-setup)
- [iOS App Setup](#ios-app-setup)
- [Firebase Features Configuration](#firebase-features-configuration)
- [Deployment](#deployment)
- [CI/CD Setup](#cicd-setup)
- [Advanced Configuration](#advanced-configuration)
- [Monitoring & Analytics](#monitoring--analytics)
- [Troubleshooting](#troubleshooting)
- [Useful Commands](#useful-commands)
- [Best Practices](#best-practices)
- [Support Resources](#support-resources)

---

## Quick Start

**For Web Apps (5 minutes):**
```bash
# 1. Install Firebase CLI
npm install -g firebase-tools

# 2. Login to Firebase
firebase login

# 3. Initialize your project
firebase init hosting

# 4. Deploy
firebase deploy --only hosting
```

**For Android Apps (10 minutes):**
1. Add Android app in Firebase Console
2. Download `google-services.json` → place in `app/` directory
3. Add Firebase dependencies to `build.gradle`
4. Sync and build your app

**For iOS Apps (10 minutes):**
1. Add iOS app in Firebase Console  
2. Download `GoogleService-Info.plist` → add to Xcode project
3. Install Firebase pods: `pod install`
4. Initialize Firebase in `AppDelegate`

---

## Prerequisites

Before starting, ensure you have the following tools and accounts set up.

Required Accounts & Tools

· **Firebase Account**: Create a free account at [console.firebase.google.com](https://console.firebase.google.com). The Spark (free) plan includes generous usage limits suitable for development and small projects.
· **Node.js (v16 or higher)**: Download from [nodejs.org](https://nodejs.org). Verify installation with `node --version`.
· **Firebase CLI**: Install globally with `npm install -g firebase-tools`. This command-line tool lets you deploy and manage Firebase projects.
· **Git**: Version control system available at [git-scm.com](https://git-scm.com). Essential for code management and CI/CD.
· **Android Studio** (for Android apps): Download from [developer.android.com/studio](https://developer.android.com/studio). Includes Android SDK and emulators.
· **Xcode** (for iOS apps, macOS only): Available from the Mac App Store. Required for iOS development and includes iOS simulators.

For Mobile Apps

· **Google Play Console account** (Android): One-time $25 registration fee. Required to publish apps on Google Play Store.
· **Apple Developer Account** (iOS): $99/year subscription. Required to publish apps on the App Store.

**System Requirements:**
- At least 8GB RAM (16GB recommended for mobile development)
- 10GB free disk space for development tools
- Stable internet connection for Firebase services

## Initial Firebase Setup

### 1. Create Firebase Project

**Method 1: Using Firebase CLI**

```bash
# Login to Firebase (opens browser for authentication)
firebase login

# Create new project interactively
firebase projects:create

# You'll be prompted for:
# - Project ID (must be unique across all Firebase)
# - Project display name
```

**Method 2: Via Firebase Console** (Recommended for first-time users)

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click **"Add project"**
3. Enter project name (e.g., `my-app-2024`)
   - Firebase will suggest a unique Project ID
   - You can customize the Project ID (cannot be changed later)
4. Click **Continue**
5. **Enable Google Analytics** (recommended)
   - Provides insights into user behavior
   - Enables A/B testing and predictions
6. Choose or create a **Google Analytics account**
7. Choose **Analytics location** (closest to your user base)
8. Accept terms and click **Create project**
9. Wait for project creation (usually takes 30-60 seconds)

**Important Notes:**
- Project ID is permanent and globally unique
- You can have multiple Firebase projects
- Free Spark plan includes generous quotas for development

2. Initialize Firebase in Your Project

**Important Notes:**
- Project ID is permanent and globally unique
- You can have multiple Firebase projects
- Free Spark plan includes generous quotas for development

### 2. Initialize Firebase in Your Project

```bash
# Navigate to your project directory
cd my-project

# Initialize Firebase
firebase init

# You'll be presented with interactive prompts:
# 1. Select features using arrow keys and spacebar:
#    ◯ Realtime Database: Rules and optional Security Rules
#    ◉ Firestore: Database and Security Rules
#    ◉ Functions: Cloud Functions
#    ◉ Hosting: Static file hosting
#    ◯ Storage: Cloud Storage
#    ◉ Emulators: Local development
#
# 2. Choose "Use an existing project" or "Create a new project"
# 3. Select your project from the list
# 4. Configure each selected feature:
#    - Firestore: Accept default rules file (firestore.rules)
#    - Hosting: Set public directory (typically 'public', 'build', or 'dist')
#    - Hosting: Configure as single-page app? (Yes for React/Angular/Vue)
#    - Functions: Choose language (JavaScript or TypeScript)
```

**Files Created:**
- `firebase.json` - Main configuration file
- `.firebaserc` - Project aliases and settings
- `firestore.rules` - Database security rules (if Firestore selected)
- `firestore.indexes.json` - Database indexes
- `functions/` - Cloud Functions directory (if Functions selected)

**Tip:** You can run `firebase init` again to add more features later.

## Website Hosting

Firebase Hosting provides fast and secure hosting for your web app, static and dynamic content, and microservices. It's backed by a global CDN (Content Delivery Network) and includes free SSL certificates.

### 1. Project Structure

```
my-project/
├── public/              # Website files (for static sites)
│   ├── index.html       # Main HTML file
│   ├── 404.html         # Custom 404 page (optional)
│   ├── css/             # Stylesheets
│   ├── js/              # JavaScript files
│   └── images/          # Image assets
├── build/               # Built files (for React, Vue, Angular)
├── functions/           # Cloud Functions (optional)
│   ├── index.js
│   └── package.json
├── firebase.json        # Firebase configuration
├── .firebaserc          # Project aliases
└── .gitignore           # Git ignore file
```

**For Framework-based Projects:**
- **React**: Use `build/` as public directory
- **Vue**: Use `dist/` as public directory
- **Angular**: Use `dist/[project-name]/` as public directory
- **Next.js**: Use Firebase hosting with Cloud Functions or export static site

### 2. Basic Firebase Configuration

**firebase.json**

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

### 3. Deploy Website

```bash
# For static sites - deploy directly
firebase deploy --only hosting

# For framework-based sites - build first
npm run build              # Creates optimized production build
firebase deploy --only hosting

# Preview before deploying
firebase hosting:channel:deploy preview  # Creates preview channel

# Deploy everything (hosting, functions, rules, etc.)
firebase deploy

# Deploy with a message
firebase deploy --only hosting -m "Deploying version 2.0"
```

**Deployment Output:**
```
✔ hosting[project-id]: Finalizing version...
✔ hosting[project-id]: Version finalized
✔ hosting[project-id]: Releasing new version...
✔ Deploy complete!

Project Console: https://console.firebase.google.com/project/project-id/overview
Hosting URL: https://project-id.web.app
```

**Rollback if Needed:**
```bash
# View deployment history
firebase hosting:channel:list

# Rollback to previous version
firebase hosting:rollback
```

## Android App Setup

Firebase integration with Android apps provides authentication, cloud database, analytics, and more with minimal configuration.

### 1. Add Android App to Firebase

**Step-by-step:**

1. Open [Firebase Console](https://console.firebase.google.com) → Select your project
2. Click **Project Overview** → Click the **Android icon** (or "Add app")
3. **Register your app:**
   - **Android package name**: Must match your app's package name exactly (e.g., `com.yourapp.android`)
     - Find this in `app/build.gradle` under `applicationId`
     - Or in `AndroidManifest.xml` under `package` attribute
   - **App nickname** (optional): Human-readable name like "My App - Production"
   - **Debug signing certificate SHA-1** (optional but recommended): Required for Google Sign-In
     - Get SHA-1: `./gradlew signingReport`
4. Click **Register app**
5. **Download google-services.json** - This file contains your Firebase configuration
6. Click **Next** and follow the setup instructions
7. Click **Continue to console**

**Finding Your Package Name:**
```gradle
// In app/build.gradle
android {
    defaultConfig {
        applicationId "com.yourapp.android"  // This is your package name
    }
}
```

### 2. Android Project Configuration

**Step 1: Add Google Services Plugin**

**project-level build.gradle** (or settings.gradle for newer projects):

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'
        classpath 'com.google.gms:google-services:4.4.0'  // Add this line
    }
}
```

**Step 2: Configure App-Level Build File**

**app/build.gradle**:

```gradle
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'  // Add this line at the top
}

android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.yourapp.android"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
        multiDexEnabled true  // If your app exceeds 64K methods
    }
}

dependencies {
    // Import Firebase BoM (Bill of Materials)
    // Using BoM ensures all Firebase libraries use compatible versions
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    
    // Firebase Analytics (automatically included with most Firebase products)
    implementation 'com.google.firebase:firebase-analytics'
    
    // Add Firebase products you need:
    implementation 'com.google.firebase:firebase-auth'         // Authentication
    implementation 'com.google.firebase:firebase-firestore'    // Cloud Firestore
    implementation 'com.google.firebase:firebase-storage'      // Cloud Storage
    implementation 'com.google.firebase:firebase-messaging'    // Cloud Messaging (FCM)
    implementation 'com.google.firebase:firebase-crashlytics'  // Crashlytics
    
    // When using BoM, don't specify versions for Firebase libraries
}
```

**Step 3: Place Configuration File**

1. Copy `google-services.json` to `app/` directory in your Android project
2. Verify the file location:
   ```
   MyApp/
   ├── app/
   │   ├── google-services.json  ← Should be here
   │   ├── build.gradle
   │   └── src/
   ```
3. **Important**: Add `google-services.json` to `.gitignore` if it contains sensitive information

**Step 4: Sync Project**

Click **"Sync Now"** in Android Studio or run:
```bash
./gradlew sync
```

### 4. Initialize Firebase in Android App

**MainActivity.java**

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

## iOS App Setup

Firebase provides comprehensive iOS SDK integration for features like authentication, database, analytics, and push notifications.

### 1. Add iOS App to Firebase

**Step-by-step:**

1. Open [Firebase Console](https://console.firebase.google.com) → Select your project
2. Click **Project Overview** → Click the **iOS icon** (or "Add app")
3. **Register your app:**
   - **iOS bundle ID**: Must match your app's bundle identifier exactly (e.g., `com.yourapp.ios`)
     - Find this in Xcode: Select your project → General tab → Bundle Identifier
   - **App nickname** (optional): Human-readable name like "My App - iOS Production"
   - **App Store ID** (optional): Your app's App Store ID (can add later)
4. Click **Register app**
5. **Download GoogleService-Info.plist** - Contains your Firebase configuration
6. Click **Next** and follow the setup instructions
7. Click **Continue to console**

**Finding Your Bundle ID:**
In Xcode:
1. Select your project in the navigator
2. Select your target
3. Go to **General** tab
4. Look for **Bundle Identifier** (e.g., `com.yourapp.ios`)

### 2. iOS Project Configuration

**Step 1: Install CocoaPods** (if not already installed)

```bash
# Check if CocoaPods is installed
pod --version

# If not installed, install it
sudo gem install cocoapods

# Initialize pods in your project (if Podfile doesn't exist)
cd /path/to/your/ios/project
pod init
```

**Step 2: Configure Podfile**

Edit your **Podfile**:

```ruby
# Platform requirement (iOS 13+ for latest Firebase SDKs)
platform :ios, '13.0'

target 'YourApp' do
  # Use frameworks is required for Firebase
  use_frameworks!
  
  # Firebase pods
  pod 'Firebase/Analytics'    # Google Analytics
  pod 'Firebase/Auth'         # Authentication
  pod 'Firebase/Firestore'    # Cloud Firestore
  pod 'Firebase/Storage'      # Cloud Storage
  pod 'Firebase/Messaging'    # Cloud Messaging (Push Notifications)
  pod 'Firebase/Crashlytics'  # Crashlytics
  
  # Add other Firebase pods as needed
  # pod 'Firebase/RemoteConfig'
  # pod 'Firebase/Performance'
  
  target 'YourAppTests' do
    inherit! :search_paths
    # Pods for testing
  end
end

# Post install script (optional, fixes some common issues)
post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '13.0'
    end
  end
end
```

**Step 3: Install Pods**

```bash
# Install dependencies
pod install

# This creates YourApp.xcworkspace file
# ⚠️ Always open .xcworkspace file from now on, NOT .xcodeproj
```

**Output:**
```
Analyzing dependencies
Downloading dependencies
Installing Firebase (10.x.x)
Installing FirebaseAnalytics (10.x.x)
...
Generating Pods project
Integrating client project

[!] Please close any current Xcode sessions and use `YourApp.xcworkspace` for this project from now on.
```

**Step 4: Add Configuration File**

1. Open your project: `open YourApp.xcworkspace` (not .xcodeproj)
2. In Xcode, right-click on your project name in the navigator
3. Choose **"Add Files to [YourApp]"**
4. Select `GoogleService-Info.plist`
5. **Important checkboxes:**
   - ✅ **Copy items if needed**
   - ✅ Select your app target
6. Click **Add**
7. Verify the file appears in the Project Navigator under your project

**Verify Setup:**
The file should be visible in:
- Project Navigator (left sidebar)
- Target → Build Phases → Copy Bundle Resources

**Important:** Add `GoogleService-Info.plist` to `.gitignore` if it contains sensitive data

### 4. Initialize Firebase in iOS App

**AppDelegate.swift**

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

**For Objective-C:**

**AppDelegate.m**

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

## Firebase Features Configuration

### 1. Authentication Setup

**Firebase Console → Authentication → Get Started**

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

### 2. Firestore Database

**Firebase Console → Firestore Database → Create Database**

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

### 3. Storage Setup

**Firebase Console → Storage → Get Started**

```javascript
import { getStorage, ref, uploadBytes } from 'firebase/storage';

const storage = getStorage(app);
const storageRef = ref(storage, 'images/profile.jpg');

// Upload file
uploadBytes(storageRef, file).then((snapshot) => {
  console.log('Uploaded file!');
});
```

## Deployment

### Website Deployment

```bash
# Build and deploy
npm run build
firebase deploy --only hosting

# Deploy to specific site (if multiple sites)
firebase deploy --only hosting:my-site
```

### Environment Setup

**.firebaserc**

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

## CI/CD Setup

### GitHub Actions Example

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

### Environment Variables Setup

1. Firebase Console → Project Settings → Service Accounts
2. Generate new private key
3. Add to GitHub Secrets as FIREBASE_SERVICE_ACCOUNT

### Mobile App Deployment

**Android Release**

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

**iOS Release**

1. Xcode → Product → Archive
2. Organizer → Distribute App
3. Upload to App Store Connect
4. App Store Connect → Submit for Review

## Advanced Configuration

### Custom Domain Setup

1. Firebase Console → Hosting → Add custom domain
2. Verify domain ownership
3. Update DNS records
4. Wait for SSL certificate provisioning

### Multiple Sites Setup

```bash
# Add additional sites
firebase hosting:sites:create my-second-site

# Deploy to specific site
firebase deploy --only hosting:my-second-site
```

### Security Rules

**Firestore Rules:**

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

**Storage Rules:**

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

## Monitoring & Analytics

### Performance Monitoring

```javascript
import { getPerformance } from 'firebase/performance';

const perf = getPerformance(app);
```

### Crashlytics Setup

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

## Troubleshooting

### Common Issues & Solutions

1. **Build Failures**

   **Android:**
   ```bash
   # Problem: Dependency resolution failed
   # Solution 1: Clear Gradle cache
   ./gradlew clean
   ./gradlew --stop
   rm -rf ~/.gradle/caches/
   
   # Solution 2: Update Google Services plugin
   # In project build.gradle: com.google.gms:google-services:4.4.0
   
   # Solution 3: Check google-services.json location
   # Must be in app/ directory, not project root
   ```
   
   **iOS:**
   ```bash
   # Problem: Pod installation fails
   # Solution 1: Update CocoaPods
   sudo gem install cocoapods
   pod repo update
   
   # Solution 2: Clean and reinstall
   pod deintegrate
   pod install
   
   # Problem: Module not found after pod install
   # Solution: Clean build folder in Xcode
   # Product → Clean Build Folder (Cmd+Shift+K)
   ```

2. **Authentication Issues**

   **Problem: Sign-in fails silently**
   
   Solutions:
   - **Web Apps:**
     ```bash
     # Check Firebase Console → Authentication → Settings → Authorized domains
     # Add your domain: yourdomain.com, localhost (for development)
     ```
   
   - **Android:**
     ```bash
     # Get SHA-1 certificate fingerprint
     ./gradlew signingReport
     
     # Add to Firebase Console → Project Settings → Your Android app
     # Under "SHA certificate fingerprints"
     ```
   
   - **iOS:**
     ```bash
     # Verify bundle ID matches exactly
     # Firebase Console → Project Settings → Your iOS app
     # Should match Xcode → General → Bundle Identifier
     ```
   
   - **All Platforms:**
     - Enable authentication method in Firebase Console → Authentication → Sign-in method
     - Check error messages in console logs
     - Verify API keys are correct in configuration files

3. **Deployment Issues**

   **Problem: Firebase deploy fails**
   ```bash
   # Check Firebase CLI version (should be 12.0.0+)
   firebase --version
   
   # Update if needed
   npm install -g firebase-tools@latest
   
   # Login again if authentication expired
   firebase logout
   firebase login
   
   # Verify you're using correct project
   firebase projects:list
   firebase use project-id
   ```
   
   **Problem: Hosting quota exceeded**
   - Check Firebase Console → Usage and billing
   - Free Spark plan: 10GB storage, 360MB/day transfer
   - Upgrade to Blaze (pay-as-you-go) for higher limits
   
   **Problem: Deploy succeeds but site not updating**
   ```bash
   # Clear browser cache
   # Or hard refresh: Ctrl+Shift+R (Windows) / Cmd+Shift+R (Mac)
   
   # Check if deploy actually completed
   firebase hosting:channel:list
   
   # View deployed version
   firebase hosting:channel:open live
   ```

4. **Firestore Issues**

   **Problem: Permission denied errors**
   ```javascript
   // Check security rules in Firebase Console → Firestore → Rules
   // Default rules for development (WARNING: Not for production!)
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.time < timestamp.date(2024, 12, 31);
       }
     }
   }
   ```
   
   **Problem: Queries timing out**
   - Create indexes: Firebase Console → Firestore → Indexes
   - Click the link in the error message to auto-create index
   - Or manually create in `firestore.indexes.json`

5. **Mobile App Specific Issues**

   **Android: App crashes on startup**
   ```bash
   # Check logcat for errors
   adb logcat | grep Firebase
   
   # Common causes:
   # - google-services.json not in app/ directory
   # - Package name mismatch
   # - Missing MultiDex (add to build.gradle)
   ```
   
   **iOS: Build errors after adding Firebase**
   ```bash
   # Clean derived data
   rm -rf ~/Library/Developer/Xcode/DerivedData
   
   # Ensure using .xcworkspace, not .xcodeproj
   open YourApp.xcworkspace
   
   # Update pods
   pod update
   ```

## Useful Commands

### Firebase CLI Commands

```bash
# Authentication
firebase login                    # Login to Firebase
firebase login --reauth          # Force re-authentication
firebase logout                  # Logout from Firebase

# Project Management
firebase projects:list           # List all your Firebase projects
firebase use project-id          # Switch to a specific project
firebase use --add              # Add project alias (dev, staging, prod)

# Deployment
firebase deploy                  # Deploy everything
firebase deploy --only hosting  # Deploy only hosting
firebase deploy --only functions # Deploy only Cloud Functions
firebase deploy --except functions # Deploy everything except functions
firebase deploy --debug         # Debug deployment issues
firebase deploy -m "message"    # Deploy with custom message

# Hosting
firebase hosting:channel:list           # List all hosting channels
firebase hosting:channel:deploy preview # Deploy to preview channel
firebase hosting:channel:open preview   # Open preview channel in browser
firebase hosting:channel:delete preview # Delete preview channel
firebase hosting:disable               # Disable hosting
firebase serve                         # Serve locally (port 5000)
firebase serve --port 8080            # Serve on custom port

# Functions
firebase functions:log                    # View function logs
firebase functions:log --only function-name # Logs for specific function
firebase functions:shell                  # Interactive shell for testing

# Emulators (Local Development)
firebase emulators:start                  # Start all emulators
firebase emulators:start --only hosting,firestore # Start specific emulators
firebase emulators:start --import=./data  # Start with imported data
firebase emulators:export ./data          # Export emulator data

# Database
firebase firestore:delete --all-collections # Delete all Firestore data
firebase database:get /path               # Get Realtime Database data
firebase database:set /path data.json     # Set Realtime Database data

# Extensions
firebase ext:install extension-name       # Install Firebase extension
firebase ext:list                        # List installed extensions

# Debugging
firebase --version                       # Check Firebase CLI version
firebase --help                         # Show help
firebase deploy --debug                 # Verbose deployment logs
```

### Git Commands for Firebase Projects

```bash
# Recommended .gitignore entries
echo "
# Firebase
.firebase/
*-debug.log
firebase-debug.log
.firebaserc

# API Keys (if storing locally)
google-services.json
GoogleService-Info.plist

# Environment files
.env
.env.local
" >> .gitignore
```

### Android Gradle Commands

```bash
# Build
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew bundleRelease          # Build release App Bundle

# Testing
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests

# Debugging
./gradlew signingReport          # Get SHA-1 for Firebase
./gradlew clean                  # Clean build artifacts

# Dependencies
./gradlew dependencies           # Show dependency tree
./gradlew --refresh-dependencies # Refresh dependencies
```

### iOS CocoaPods Commands

```bash
# Installation
pod install                      # Install dependencies
pod update                       # Update dependencies
pod update Firebase              # Update specific pod

# Debugging
pod deintegrate                  # Remove CocoaPods from project
pod repo update                  # Update local specs repo
pod cache clean --all           # Clear CocoaPods cache

# Information
pod list                        # List installed pods
pod outdated                    # Show outdated pods
pod search Firebase             # Search for Firebase pods
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

## Best Practices

### Security Best Practices

1. **Environment Variables**
   ```javascript
   // Never commit API keys to version control
   // Use environment variables instead
   
   // .env file (add to .gitignore)
   REACT_APP_FIREBASE_API_KEY=your-api-key
   REACT_APP_FIREBASE_PROJECT_ID=your-project-id
   
   // In your code
   const firebaseConfig = {
     apiKey: process.env.REACT_APP_FIREBASE_API_KEY,
     projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID,
     // ...
   };
   ```

2. **Security Rules**
   ```javascript
   // Never leave rules wide open in production
   // BAD - Allows anyone to read/write
   allow read, write: if true;
   
   // GOOD - Requires authentication
   allow read, write: if request.auth != null;
   
   // BETTER - Checks user ownership
   allow read, write: if request.auth.uid == userId;
   
   // BEST - Validates data structure
   allow write: if request.auth != null 
             && request.resource.data.keys().hasAll(['name', 'email'])
             && request.resource.data.name is string
             && request.resource.data.email is string;
   ```

3. **API Key Restrictions**
   - Google Cloud Console → Credentials
   - Restrict API keys to specific domains/apps
   - Android: Restrict to your app's package name and SHA-1
   - iOS: Restrict to your app's bundle ID
   - Web: Restrict to your domain

4. **Authentication Security**
   ```javascript
   // Enforce password requirements
   // Firebase Console → Authentication → Password policy
   
   // Enable multi-factor authentication
   // Implement email verification
   import { sendEmailVerification } from 'firebase/auth';
   
   await createUserWithEmailAndPassword(auth, email, password)
     .then((userCredential) => {
       sendEmailVerification(userCredential.user);
     });
   ```

### Performance Optimization

1. **Web Performance**
   ```javascript
   // Use lazy loading for Firebase modules
   const auth = () => import('firebase/auth');
   const firestore = () => import('firebase/firestore');
   
   // Implement code splitting
   // Minimize bundle size by only importing needed features
   import { initializeApp } from 'firebase/app';
   import { getAuth, signInWithEmailAndPassword } from 'firebase/auth';
   // Don't import entire 'firebase/auth' module
   ```

2. **Database Optimization**
   ```javascript
   // Use pagination for large collections
   const first = query(collection(db, "items"), 
                       orderBy("created"), 
                       limit(25));
   
   // Create indexes for complex queries
   // Firebase will prompt you with a link when needed
   
   // Use shallow queries
   const docRef = doc(db, "cities", "SF");
   const docSnap = await getDoc(docRef);
   // Better than getting entire collection
   ```

3. **Caching**
   ```javascript
   // Enable offline persistence
   import { enableIndexedDbPersistence } from 'firebase/firestore';
   
   enableIndexedDbPersistence(db)
     .catch((err) => {
       if (err.code == 'failed-precondition') {
         // Multiple tabs open
       } else if (err.code == 'unimplemented') {
         // Browser doesn't support
       }
     });
   ```

4. **Storage Optimization**
   ```javascript
   // Optimize images before upload
   // Use appropriate image formats (WebP, AVIF)
   // Implement lazy loading for images
   
   // Set max file size
   const maxSize = 5 * 1024 * 1024; // 5MB
   if (file.size > maxSize) {
     throw new Error('File too large');
   }
   
   // Use Firebase Storage rules to enforce limits
   allow write: if request.resource.size < 5 * 1024 * 1024;
   ```

### Development Workflow

1. **Use Firebase Emulators**
   ```bash
   # Develop locally without affecting production
   firebase emulators:start
   
   # Available emulators:
   # - Authentication (port 9099)
   # - Firestore (port 8080)
   # - Realtime Database (port 9000)
   # - Storage (port 9199)
   # - Functions (port 5001)
   # - Hosting (port 5000)
   ```

2. **Environment Management**
   ```bash
   # Use multiple Firebase projects
   # .firebaserc
   {
     "projects": {
       "dev": "myapp-dev",
       "staging": "myapp-staging", 
       "prod": "myapp-prod"
     }
   }
   
   # Switch environments
   firebase use dev
   firebase use staging
   firebase use prod
   ```

3. **Version Control**
   ```bash
   # Essential .gitignore entries
   .firebase/
   *-debug.log
   firebase-debug.log
   
   # Include in version control:
   firebase.json
   .firebaserc (if no sensitive data)
   firestore.rules
   firestore.indexes.json
   storage.rules
   ```

4. **Testing Strategy**
   ```javascript
   // Use Firebase Test Lab for mobile apps
   // Run automated tests across multiple devices
   
   // Unit test Firebase functions locally
   const test = require('firebase-functions-test')();
   const myFunctions = require('../index');
   
   describe('Cloud Functions', () => {
     test('should process data correctly', () => {
       // Test your function
     });
   });
   ```

### Cost Management

1. **Monitor Usage**
   - Firebase Console → Usage and billing
   - Set up budget alerts
   - Monitor Firestore read/write operations
   - Check storage and bandwidth usage

2. **Optimize Costs**
   ```javascript
   // Reduce Firestore reads
   // - Use real-time listeners efficiently
   // - Implement pagination
   // - Cache data locally
   // - Use Firestore queries instead of client-side filtering
   
   // Bad - reads all documents
   const querySnapshot = await getDocs(collection(db, "items"));
   const filtered = querySnapshot.docs.filter(doc => doc.data().status === 'active');
   
   // Good - only reads needed documents
   const q = query(collection(db, "items"), where("status", "==", "active"));
   const querySnapshot = await getDocs(q);
   ```

3. **Pricing Tiers**
   - **Spark (Free)**:
     - 1GB storage
     - 10GB hosting transfer/month
     - 50,000 Firestore reads/day
     - Good for development and small projects
   
   - **Blaze (Pay as you go)**:
     - Free tier included
     - Only pay for what you use beyond free tier
     - Required for Cloud Functions
     - Recommended for production apps

---

## Support Resources

### Official Documentation
- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Console](https://console.firebase.google.com)
- [Firebase CLI Reference](https://firebase.google.com/docs/cli)
- [Firebase SDKs](https://firebase.google.com/docs/libraries)

### Community Support
- [Firebase Community](https://firebase.google.com/community)
- [Stack Overflow - Firebase tag](https://stackoverflow.com/questions/tagged/firebase)
- [Firebase GitHub](https://github.com/firebase)
- [Firebase Discord](https://discord.gg/BN2cgc3)

### Status & Updates
- [Firebase Status Dashboard](https://status.firebase.google.com)
- [Firebase Release Notes](https://firebase.google.com/support/release-notes)
- [Firebase Blog](https://firebase.blog)

### Learning Resources
- [Firebase YouTube Channel](https://www.youtube.com/firebase)
- [Firebase Codelabs](https://firebase.google.com/codelabs)
- [Fireship.io Tutorials](https://fireship.io)

### Mobile Development
- [Android Firebase Setup](https://firebase.google.com/docs/android/setup)
- [iOS Firebase Setup](https://firebase.google.com/docs/ios/setup)
- [Flutter Firebase](https://firebase.google.com/docs/flutter/setup)
- [React Native Firebase](https://rnfirebase.io)

---

This comprehensive guide provides a robust foundation for hosting your website and mobile apps with Firebase. Remember to:
- Start with the free Spark plan for development
- Use emulators for local development
- Implement proper security rules before production
- Monitor usage and costs regularly
- Keep Firebase SDKs updated
- Follow security best practices
- Use environment-specific configurations

For questions or issues not covered here, consult the official Firebase documentation or reach out to the Firebase community through the support channels listed above.
