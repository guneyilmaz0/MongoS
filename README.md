# MongoS

MongoS is a **Kotlin** project that provides a seamless interaction with **MongoDB**, offering a robust **backend** solution. It combines fast and optimized database operations with the flexibility of the Kotlin language.

![MongoS](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![MongoDB](https://img.shields.io/badge/Database-MongoDB-47A248?logo=mongodb&logoColor=white)

---

## ⛏ Features

- **MongoDB Connection Management:** Easily connect and perform operations with MongoDB.
- **JSON Data Format:** Supports using **JSON** format for input and output.
- **Asynchronous Support:** Leverages **Coroutines** for asynchronous data processing.
- **Unit Testing:** Features are tested using **JUnit5**.

---

## ⚡ Installation

1. Clone the **project repository**:
   ```bash
   git clone https://github.com/guneyilmaz0/MongoS.git
   cd MongoS
   ```
2. Run Gradle to **install dependencies**:
   ```bash
   ./gradlew build
   ```

---

## 📦 Adding to Your Project
To include `MongoS` in your existing Kotlin project,
add the following dependencies to your file:

- Gradle

```gradle
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.guneyilmaz0:MongoS:version")
}
```

- Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
   
<dependency>
    <groupId>com.github.guneyilmaz0</groupId>
    <artifactId>MongoS</artifactId>
    <version>version</version>
</dependency>
```

## 🔍 Usage Example

Below is an example of adding a simple document using the MongoS project:

```kotlin
import net.guneyilmaz0.mongos.MongoS
import net.guneyilmaz0.mongos.MongoSObject

fun main() {
   val mongoS = MongoS("mongodb://localhost:27017", "exampleDB")
   val person = Person(0,"John Doe", 30)
   mongoS.set("persons", person.id, person)

   val john = mongoS.getObject("persons", 0, Person::class.java)
   print(john)
}

data class Person(val id: Int, val name: String, val age: Int) : MongoSObject() {
   override fun toString(): String = super.toString()
}
```

---

## 💡 Contributing

Contributions are welcome! Please follow these steps:

1. Fork this repository.
2. Create a branch for your feature or fix:
   ```bash
   git checkout -b new-feature
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add a new feature"
   git push origin new-feature
   ```
4. Submit a pull request.

---

## 👤 Author

This project was developed by [**Güney Yılmaz**](https://github.com/guneyilmaz0).
Feel free to reach out for any questions:

- **GitHub:** [guneyilmaz0](https://github.com/guneyilmaz0)
- **Email:** `guneyyilmaz2707@gmail.com`

---
