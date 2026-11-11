# Changelog

## 0.2.0

### Added

- `EncryptedSerializer<T>`, an `androidx.datastore.core.Serializer` backed by
  kotlinx.serialization.
- `LegacyEncryptedPreferences` helpers to decrypt values written by 0.0.3.

### Changed

- Encryption now uses `javax.crypto` with AES-256-GCM, a random 12-byte IV, and
  a 128-bit authentication tag.
- The library is built with Gradle 9.6, AGP 9.4, built-in Kotlin, and JDK 17.
- AndroidX, Retrofit, Glide, and OkHttp dependencies were updated.
- The demo app uses one typed DataStore and shows the encrypted file contents.

### Removed

- `EncryptedDataStore.secureEdit` and `EncryptedDataStore.secureMap`.
- The hand-written AES implementation, S-box tables, and singleton.
- The plaintext duplicate DataStore in the demo app.

### Upgrade notes

- This release is a breaking API change; migrate to `EncryptedSerializer`.
- Data written by 0.0.3 is not readable by the new serializer directly. Read the
  old Preferences values with `LegacyEncryptedPreferences`, map them to your new
  object, and write that object to the typed DataStore.
- Android consumers need `minSdk` 24 because of DataStore 1.3.0.

## 0.0.3

- Last release with the Preferences-based `secureEdit`/`secureMap` API.
