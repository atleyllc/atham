# Untouched baseline build

Source commit: `6f3265e81abf64e812de7c653486c79e1903b2e8`  
Date: 25 September 2026  
Host JDK: OpenJDK 21.0.11  
Android SDK: `$HOME/Library/Android/sdk` (`ANDROID_HOME` was unset in the shell and was exported for this run)  
Working directory: `android-src/KV4PHT`  
Gradle: 9.2.1, started by `./gradlew`

No firmware was flashed. No RF was transmitted. No phone was attached.

## Command

```sh
export ANDROID_HOME="$HOME/Library/Android/sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
```

The combined command exited 1 after 2 minutes 54 seconds. Assemble and unit tests completed. Lint failed. The failure was not suppressed.

## Debug APK

`./gradlew` task `:app:assembleDebug` completed.

Output:

`android-src/KV4PHT/app/build/outputs/apk/debug/app-debug.apk` (13 MB)

This compile does not verify connect, tune, receive, transmit, unkey, or USB reconnect.

## Unit tests

`:app:testDebugUnitTest` completed. Result XML:

| Class | Tests | Failures | Errors |
| --- | --- | --- | --- |
| `com.vagell.kv4pht.radio.ProtocolKissTest` | 37 | 0 | 0 |
| `com.vagell.kv4pht.radio.ImaAdpcmTest` | 5 | 0 | 0 |

Instrumented tests under `usbSerialForAndroid/src/androidTest` were not run. They require a device.

## Lint

`:app:lintDebug` failed.

Lint found 1 error and 330 warnings.

Error, existing upstream code:

`RadioAudioService.java:352` — `MissingPermission` on `sendPositionBeacon()`.

HTML report:

`android-src/KV4PHT/app/build/reports/lint-results-debug.html`

The Java compile also printed warnings that source and target 8 are obsolete on JDK 21, plus deprecation and unchecked notes. Those warnings did not fail `:app:assembleDebug`.

## Tag

Annotated tag `baseline/kv4p-untouched` points at `6f3265e81abf64e812de7c653486c79e1903b2e8`, the untouched upstream commit. Documentation commits come after that tag.
