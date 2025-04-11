# MongoDB Wrapper

A lightweight and easy-to-use Java wrapper for MongoDB operations.

## Features

- Simple connection setup
- Streamlined document retrieval operations
- Support for basic and complex data types
- Fluent API for common MongoDB operations

## Requirements

- Java 21 or higher
- MongoDB server
- Gradle 8.0+ (for building)

## Installation

### Gradle

Add the repository and dependency to your `build.gradle` file:

```gradle
repositories {
    mavenCentral()
    // Add repository if not published to Maven Central
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'net.guneyilmaz0:mongos:1.0.0'
}
```

### Maven

Add the dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>net.guneyilmaz0</groupId>
    <artifactId>mongos</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

 - Java
```java
    // Initialize and connect to MongoDB
    MongoS db = new MongoS("myDatabase");
    
    // Store data
    db.set("users", "user123", new User("John Doe", 25));
    
    // Retrieve data
    User user = db.get("users", "user123", User.class);
    
    // Check if document exists
    boolean exists = db.exists("users", "user123");
    
    // Retrieve a list of objects
    List<User> allUsers = db.getList("users", "userList", User.class);
```
- Kotlin
```kotlin
    // Initialize and connect to MongoDB
    val db = MongoS("myDatabase")

    // Store data
    db.set("users", "user123", User("John Doe", 25))

    // Retrieve data
    val user: User? = db.get<User>("users", "user123")

    // Check if document exists
    val exists = db.exists("users", "user123")

    // Retrieve a list of objects
    val allUsers: List<User>? = db.getList("users", "userList", User::class.java)
```

## Documentation

### Database Class Methods

| Method                             | Description                                          |
|------------------------------------|------------------------------------------------------|
| `isConnected()`                    | Checks if connected to the database                  |
| `get(collection, id)`              | Retrieves a document as a raw object                 |
| `getObject(collection, id, class)` | Retrieves and maps a document to the specified class |
| `getList(collection, class)`       | Retrieves all documents in a collection as objects   |
| `set(collection, id, object)`      | Stores an object in the specified collection         |

## Building from Source

```bash
git clone https://github.com/guneyilmaz0/mongos.git
cd mongos
./gradlew build
```

## Testing

Run the test suite with:

```bash
./gradlew test
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

- GitHub: [guneyilmaz0](https://github.com/guneyilmaz0)