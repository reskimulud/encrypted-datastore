[![Test](https://github.com/reskimulud/encrypted-datastore/actions/workflows/test.yml/badge.svg)](https://github.com/reskimulud/encrypted-datastore/actions/workflows/test.yml)&nbsp;
[![Publish to Maven Central](https://github.com/reskimulud/encrypted-datastore/actions/workflows/publish-gradle.yml/badge.svg)](https://github.com/reskimulud/encrypted-datastore/actions/workflows/publish-gradle.yml)&nbsp;
[![Maven Central](https://img.shields.io/maven-central/v/io.github.reskimulud/encrypted-datastore.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.reskimulud%22%20AND%20a:%22encrypted-datastore%22)&nbsp;
![License](https://img.shields.io/github/license/reskimulud/encrypted-datastore?logo=github)&nbsp;
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)&nbsp;
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?logo=kotlin&logoColor=white)&nbsp;

# Encrypted DataStore

**Encrypted DataStore** is an `androidx.datastore.core.Serializer` that encrypts
a kotlinx.serialization object before DataStore writes it.

- AES-GCM with a random 12-byte IV and a 128-bit authentication tag.
- AES-128, AES-192, and AES-256 are selected by the key length.
- JSON is the default format. `kotlinx.serialization` ProtoBuf is supported too.
- File format: `[version][algorithm][IV][ciphertext + tag]`.
- Tampered or corrupt files raise `CorruptionException`, so DataStore's
  `ReplaceFileCorruptionHandler` can be used.
- Files written by 0.2.0 (AES-256-GCM, format version 1) can still be read.
- Values written by 0.0.3 (AES-256-CBC) can still be decrypted with a helper,
  but they need an application-side migration.

# Requirements

- JDK 17 to build the project.
- Java 8 bytecode for consumers.
- On Android, DataStore 1.3.0 sets `minSdk` to **24**.

# Installation

Kotlin DSL:

```kotlin
implementation("io.github.reskimulud:encrypted-datastore:0.3.0")
```

Groovy:

```groovy
implementation 'io.github.reskimulud:encrypted-datastore:0.3.0'
```

# Quick start

Define a serializable model:

```kotlin
@Serializable
data class Settings(
    val counter: Int = 0,
    val token: String = "",
)
```

Build the serializer with a key. The key length chooses the algorithm:

| Key length | Algorithm |
|---|---|
| 16 bytes | AES-128-GCM |
| 24 bytes | AES-192-GCM |
| 32 bytes | AES-256-GCM |

```kotlin
val aes = AES.Builder()
    .setKey("0123456789abcdef0123456789abcdef") // 32 bytes -> AES-256-GCM
    .build()

val serializer = EncryptedSerializer(
    defaultValue = Settings(),
    serializer = Settings.serializer(),
    aes = aes,
)
```

32-byte keys are recommended unless you have a specific reason to use a
shorter key. Changing the key length changes the algorithm and makes existing
data unreadable.

On Android, create the typed DataStore:

```kotlin
val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.enc",
    serializer = serializer,
)
```

On the JVM:

```kotlin
val store = DataStoreFactory.create(serializer = serializer) {
    File(directory, "settings.enc")
}
```

Read and write through DataStore as usual:

```kotlin
val settings = store.data.first()

store.updateData { current ->
    current.copy(counter = current.counter + 1)
}
```

# Protobuf

The serializer accepts any kotlinx.serialization `SerialFormat`. Add the
protobuf format dependency:

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.12.0")
```

Then pass `ProtoBuf` instead of the default JSON:

```kotlin
val serializer = EncryptedSerializer(
    defaultValue = Settings(),
    serializer = Settings.serializer(),
    aes = aes,
    format = ProtoBuf {
        encodeDefaults = true
    },
)
```

Binary formats are written as raw bytes, so `ByteArray` fields do not go
through a UTF-8 conversion. This is the **kotlinx.serialization ProtoBuf**
format; it does not generate classes from a `.proto` file. If you need Google
protobuf or Wire generated classes, use their own DataStore serializer instead.

The default format is JSON with `encodeDefaults = true` and
`ignoreUnknownKeys = true`. To customize JSON, pass a `Json` instance:

```kotlin
val serializer = EncryptedSerializer(
    defaultValue = Settings(),
    serializer = Settings.serializer(),
    aes = aes,
    json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    },
)
```

The `json = ...` parameter is kept for source compatibility with 0.2.0.
`format = Json { ... }` does the same thing.

# File format

New writes use format version 2:

```text
[0x02][algorithm id][12-byte IV][ciphertext + tag]
```

| Algorithm id | Meaning |
|---|---|
| `0x01` | AES-128-GCM |
| `0x02` | AES-192-GCM |
| `0x03` | AES-256-GCM |

The version and algorithm bytes are authenticated as additional data. If the
header is changed, decryption fails.

Files written by 0.2.0 use format version 1 and are always AES-256-GCM:

```text
[0x01][12-byte IV][ciphertext + tag]
```

Version 1 is read-only. New writes always use version 2.

# Migration from 0.2.0

- Existing 0.2.0 files remain readable with the same 32-byte key.
- Files written by 0.3.0 use format version 2. Version 0.2.0 cannot read them,
  so do not roll back to 0.2.0 after upgrading.
- Changing the key length changes the algorithm and makes existing data
  unreadable. If you need to switch algorithms, decrypt with the old
  configuration and re-encrypt with the new one in application code.

# Migration from 0.0.3

Version 0.0.3 stored each value as a hex-encoded AES-256-CBC string in a
Preferences DataStore. The new serializer stores one object in one file, so the
mapping from old preference keys to your new object must be written by the
application.

Keep the old 32-byte key **and IV**, decrypt each old value, map it to your
object, and write the object to the new store:

```kotlin
// The IV is only needed to read 0.0.3 data.
val legacyAes = AES.Builder()
    .setKey("0123456789abcdef0123456789abcdef")
    .setIv("abcdefghijklmnop")
    .build()

val name = LegacyEncryptedPreferences.decryptString(
    legacyAes,
    oldPreferences[stringPreferencesKey("user_name")],
)

store.updateData {
    Settings(token = name.orEmpty())
}
```

`LegacyEncryptedPreferences.decryptJson` is available when the old value was
itself a JSON string. Run the migration once, then clear the old DataStore.
Never write new data with the legacy CBC path.

# Security notes

- Do not hardcode the key in a production application. Load it from a secure
  source such as the Android Keystore; the demo app uses a build-config key only
  to keep the sample runnable.
- Changing the key, key length, or algorithm makes existing data unreadable.
- The `IV` in `AES.Builder` is only for reading 0.0.3 data. New writes always use
  a random IV.
- AES-256-GCM is the default and the only algorithm used by version 1 files.
  AES-128 and AES-192 are available when the key length requires them.

# Build and test

The project uses Gradle 9.6, AGP 9.4, Kotlin 2.4, and JDK 17. Build scripts are
written in Kotlin DSL, with dependency versions in
`gradle/libs.versions.toml`.

```bash
./gradlew :encrypteddatastore:test
./gradlew :app:assembleDebug
```

CI runs both commands on every push and pull request. The demo app requires an
Android SDK; the library tests only require JDK 17.

# License

License of this project is under [`Apache 2.0`](https://github.com/reskimulud/encrypted-datastore/blob/master/LICENSE) license.
