# MongoS
Simple Mongo API


### Patch Notes

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
    mongoS.set("collection", "guneyilmaz0", mongoSObject);
    mongoS.set("collection", "guneyilmaz0", String.Companion::class.java);
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
