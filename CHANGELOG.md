# Changelog

## 0.3.0

### Added

- AES-128-GCM and AES-192-GCM. The key length now selects the algorithm:
  16 bytes for AES-128, 24 bytes for AES-192, and 32 bytes for AES-256.
- `kotlinx.serialization` ProtoBuf support through the new `SerialFormat`
  parameter on `EncryptedSerializer`.
- Format version 2 for new writes:
  `[0x02][algorithm id][IV][ciphertext + tag]`, with the header authenticated
  as additional data.
- `AES.Builder.setKey(ByteArray)` for raw keys.

### Changed

- New files are written with format version 2. Version 1 files written by 0.2.0
  are still readable.
- Gradle build scripts were converted to Kotlin DSL, with dependency versions
  in `gradle/libs.versions.toml`.
- Publishing configuration is now part of the Kotlin DSL build scripts.
- Documentation now covers installation, key selection, JSON, ProtoBuf, file
  format, and migration.

### Upgrade notes

- Keep a 32-byte key to read existing 0.2.0 data. Version 1 data cannot be read
  with a 16-byte or 24-byte key.
- Version 0.2.0 cannot read files written by 0.3.0. Do not downgrade after the
  upgrade unless the data has been re-encrypted.
- Changing the key length changes the algorithm and makes existing data
  unreadable.
- `EncryptedSerializer(json = ...)` still works. `format = Json { ... }` is the
  new general form, and `format = ProtoBuf { ... }` selects Protocol Buffers.

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
