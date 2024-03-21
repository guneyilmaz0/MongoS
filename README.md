# MongoS
Simple Mongo API

### Latest version: 4.1.4
### Patch Notes

- 4.1.4
   - "No information"

- 4.1.3
  - Added update function
  - Improved getObjects function
  - Added changing log level

- 4.1.2
  - Added getKeys() & getAll() functions

- 4.1.1
  - Fixed some bugs
  - Updated to MongoDB 4.11.1
  - Added watchCollection() function

- 4.0.3-alpha
  - Some improvements & getLong() function

- 4.0.2-alpha
  - Fixed some bugs

- 4.0.1-alpha
  - Converted all files to Kotlin
  - Optimized code
  - Some classes moved into a single class

 
### Usage

```kt
    val mongoS = MongoS("localhost", 27017, "test")
    mongoS.set("moneys","guneyilmaz0", 5000)
    mongoS.removeData("moneys", "SwadeDev0")
    mongoS.getInt("moneys", "SwadeDev0", 0)
    mongoS.exists("moneys", "SwadeDev0")
```
```kt
    val mongoS = MongoS("localhost", 27017, "test")
    mongoS.set("collection", "guneyilmaz0", mongoSObject)
    mongoS.set("collection", "guneyilmaz0", String.Companion::class.java)
```

### Maven
```XML
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```XML
<dependency>
    <groupId>com.github.guneyilmaz0</groupId>
    <artifactId>MongoS</artifactId>
    <version>VERSION</version>
</dependency>
```
