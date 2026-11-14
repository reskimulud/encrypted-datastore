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

Changes to the file format or to `AES` must keep both compatibility paths
working:

- format version 1 (0.2.0, AES-256-GCM) must remain readable with a 32-byte key
- format version 2 (0.3.0) must authenticate its version and algorithm header
- the legacy CBC reader must keep working for data written by 0.0.3

Add or update tests for:

- round-trip encryption and decryption for AES-128, AES-192, and AES-256
- random IV per write
- tampered ciphertext and tampered v2 header rejection
- algorithm mismatch rejection
- version 1 fixtures and legacy CBC fixtures
- legacy Preferences migration
- JSON and ProtoBuf round trips through `EncryptedSerializer`

## Pull requests

Keep changes scoped and explain any change to the stored format. Do not commit
real keys or signing material.
