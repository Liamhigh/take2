# Build Troubleshooting Guide

## Issue: Cannot Build APK - Google Maven Repository Blocked

### Problem Description
The Android Gradle Plugin (AGP) cannot be downloaded because the Google Maven repository domains are blocked in this environment:
- `dl.google.com` - BLOCKED
- `maven.google.com` - BLOCKED (redirects to dl.google.com)

### Root Cause
The Android Gradle Plugin version 8.6.1 is only available from Google's Maven repository, which is inaccessible in restricted network environments.

### Symptoms
```
Plugin [id: 'com.android.application', version: '8.6.1', apply: false] was not found in any of the following sources:
- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
- Included Builds (No included builds contain this plugin)
- Plugin Repositories (could not resolve plugin artifact 'com.android.application:com.android.application.gradle.plugin:8.6.1')
```

### Solution Options

#### Option 1: Request Network Access (RECOMMENDED)
Request that `dl.google.com` be added to the network whitelist. This is the official Google Maven repository and is required for Android development.

**Domains needed:**
- `dl.google.com` - Google's Maven repository for Android dependencies
- `maven.google.com` - Alternative Google Maven URL (redirects to dl.google.com)

#### Option 2: Use Pre-built APK
Since the build succeeds in GitHub Actions (which has full internet access), you can use the pre-built APKs from successful workflow runs:

1. Go to [GitHub Actions](https://github.com/Liamhigh/take2/actions/workflows/build-apk.yml)
2. Find a successful build run
3. Download the APK artifacts:
   - `verum-omnis-debug-apk` - Debug version for testing
   - `verum-omnis-release-apk` - Release version for production

**Latest successful build:** Run #96 (2025-12-01)
- Commit: f4e76a2708801b27dde3b1bef2ac6db95833a3ea
- APKs available for 30 days

#### Option 3: Maven Mirror Configuration (if mirrors become accessible)
If alternative Maven mirrors become accessible, configure them in `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        // Tencent mirror (China)
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        // Aliyun mirror (China)  
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // Original repositories as fallback
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

**Note:** Currently, these mirrors are also blocked in this environment.

#### Option 4: Global Gradle Init Script
Create `~/.gradle/init.gradle.kts` to automatically redirect Google Maven to accessible mirrors:

```kotlin
val urlMappings = mapOf(
    "https://dl.google.com/dl/android/maven2" to "https://mirrors.tencent.com/nexus/repository/maven-public/",
    "https://maven.google.com" to "https://mirrors.tencent.com/nexus/repository/maven-public/"
)

fun RepositoryHandler.enableMirror() {
    all {
        if (this is MavenArtifactRepository) {
            val originalUrl = this.url.toString().removeSuffix("/")
            urlMappings[originalUrl]?.let { this.setUrl(it) }
        }
    }
}

gradle.allprojects {
    buildscript { repositories.enableMirror() }
    repositories.enableMirror()
}

gradle.beforeSettings {
    pluginManagement.repositories.enableMirror()
    dependencyResolutionManagement.repositories.enableMirror()
}
```

### Current Status
- ✅ **GitHub Actions builds:** Working (has internet access)
- ❌ **Local builds:** Failing (Google Maven blocked)
- ✅ **Workaround:** Pre-built APKs available from GitHub Actions

### Accessible Domains
The following domains ARE accessible in this environment:
- `github.com`
- `repo1.maven.org` (Maven Central)
- `plugins.gradle.org`
- `services.gradle.org`

### Blocked Domains
The following domains are BLOCKED:
- `dl.google.com` ⚠️ **REQUIRED for Android builds**
- `maven.google.com` (redirects to dl.google.com)
- `mirrors.tencent.com`
- `maven.aliyun.com`
- `repo.gradle.org`

### Testing Build
To test if the build works:
```bash
./gradlew assembleDebug
```

If you see plugin resolution errors, the network restrictions are still in place.

### Contact
If you need to build locally, request network access to `dl.google.com` or use the pre-built APKs from GitHub Actions.
