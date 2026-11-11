# Contributing

## Build and test

Requirements:

- JDK 17
- Android SDK platform 36 and build-tools 36.0.0 for the demo app
- No Android device or emulator is required

```bash
./gradlew :encrypteddatastore:test
./gradlew :app:assembleDebug
```

The library unit tests are plain JVM tests. The demo app is only compiled; CI is
the reference for build results.

## Security changes

Changes to the file format or to `AES` must keep the legacy CBC reader working
for data written by 0.0.3. Add or update tests for:

- round-trip encryption and decryption
- random IV per write
- tampered ciphertext rejection
- legacy CBC fixtures
- legacy Preferences migration

## Pull requests

Keep changes scoped and explain any change to the stored format. Do not commit
real keys or signing material.
