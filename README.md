[![Test](https://github.com/reskimulud/encrypted-datastore/actions/workflows/test.yml/badge.svg)](https://github.com/reskimulud/encrypted-datastore/actions/workflows/test.yml)&nbsp;
[![Publish to Maven Central](https://github.com/reskimulud/encrypted-datastore/actions/workflows/publish-gradle.yml/badge.svg)](https://github.com/reskimulud/encrypted-datastore/actions/workflows/publish-gradle.yml)&nbsp;
[![Maven Central](https://img.shields.io/maven-central/v/io.github.reskimulud/encrypted-datastore.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.reskimulud%22%20AND%20a:%22encrypted-datastore%22)&nbsp;
![License](https://img.shields.io/github/license/reskimulud/encrypted-datastore?logo=github)&nbsp;
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)&nbsp;
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?logo=kotlin&logoColor=white)&nbsp;

# Encrypted DataStore

**Encrypted DataStore** is an `androidx.datastore.core.Serializer` that encrypts
a kotlinx.serialization object with **AES-256-GCM** before DataStore writes it.

- Random 12-byte IV for every write, 128-bit authentication tag.
- File format: `[version][IV][ciphertext + tag]`.
- Tampered or corrupt files raise `CorruptionException`, so DataStore's
  `ReplaceFileCorruptionHandler` can be used.
- Values written by version 0.0.3 (AES-256-CBC) can still be decrypted with a
  helper, but they need an application-side migration.

# Installation

```groovy
implementation 'io.github.reskimulud:encrypted-datastore:0.2.0'
```

The library requires Java 8 bytecode. On Android, DataStore 1.3.0 sets
`minSdk` to **24**.

# Usage

Define a serializable model:

```kotlin
@Serializable
data class Settings(
    val counter: Int = 0,
    val token: String = "",
)
```

Build the serializer with a **32-byte (256-bit) key**:

```kotlin
val aes = AES.Builder()
    .setKey("0123456789abcdef0123456789abcdef")
    .build()

val serializer = EncryptedSerializer(
    defaultValue = Settings(),
    serializer = Settings.serializer(),
    aes = aes,
)
```

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

`EncryptedSerializer` also accepts a custom `Json` instance. By default it uses
`encodeDefaults = true` and `ignoreUnknownKeys = true`.

# Migration from 0.0.3

Version 0.0.3 stored each value as a hex-encoded AES-256-CBC string in a
Preferences DataStore. The new serializer stores one object in one file, so the
mapping from old preference keys to your new object must be written by the
application.

Keep the old key **and IV**, decrypt each old value, map it to your object, and
write the object to the new store:

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
- Changing the key makes existing data unreadable.
- The `IV` in `AES.Builder` is only for reading 0.0.3 data. New writes always use
  a random IV.

# Build and test

The project is built with Gradle 9.6, AGP 9.4, built-in Kotlin, and JDK 17.

```bash
./gradlew :encrypteddatastore:test
./gradlew :app:assembleDebug
```

CI runs both commands on every push and pull request.

# License

License of this project is under [`Apache 2.0`](https://github.com/reskimulud/encrypted-datastore/blob/master/LICENSE) license.
