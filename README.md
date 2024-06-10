# MongoS
Simple Mongo API

<a href="https://app.codacy.com/gh/guneyilmaz0/MongoS/dashboard"><img src="https://app.codacy.com/project/badge/Grade/30e264923da2425a8b777a84b4028334"></a>

### Latest version: 4.2.3
 
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
