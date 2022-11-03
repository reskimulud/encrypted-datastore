[![Maven Central](https://img.shields.io/maven-central/v/io.github.reskimulud/encrypted-datastore.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.reskimulud%22%20AND%20a:%22encrypted-datastore%22)

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