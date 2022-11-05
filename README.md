[![Publish to Maven Central](https://github.com/reskimulud/encrypted-datastore/actions/workflows/publish-gradle.yml/badge.svg)](https://github.com/reskimulud/encrypted-datastore/actions/workflows/publish-gradle.yml)&nbsp;
[![Maven Central](https://img.shields.io/maven-central/v/io.github.reskimulud/encrypted-datastore.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.reskimulud%22%20AND%20a:%22encrypted-datastore%22)&nbsp;
![License](https://img.shields.io/github/license/reskimulud/encrypted-datastore?logo=github)&nbsp;
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)&nbsp;
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?logo=kotlin&logoColor=white)&nbsp;

# Encrypted DataStore

**Encrypted DataStore** is an extension function to encrypt and decrypt data **String** to DataStore using Advanced Encrypted Standard algorithm.

# Installation

```groovy
implementation 'io.github.reskimulud:encrypted-datastore:0.0.3'
```

# Usage

AES Builder

```kotlin
val aes: AES = AES.Builder()
    .setKey("your_cipher_key") // 256-bit
    .setIv("your_initialize_vector") // 128-bit
    .build()
```

To store using `secureEdit(aes: AES, defaultValue: String)` method, and use `secureMap(value: String, aes: AES)` to retrieve data from DataStore

```kotlin
// to store data
suspend fun setEmail(email: String) =
    dataStore.data.secureEdit(aes, DEDAULT_FALUE) {
        it[EMAIL_KEY] ?: DEFAULT_VALUE
    }

// to retrieve data
fun getEmail(): Flow<String> =
    dataStore.secureMap(value, aes) { preference, encryptedValue ->
        preference[EMAIL_KEY] = encryptedValue
    }
```

# License

License of this project is under [`Apache 2.0`](https://github.com/reskimulud/encrypted-datastore/blob/master/LICENSE) license
