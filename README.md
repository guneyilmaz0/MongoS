# MongoS
Simple Mongo API


### Patch Notes

- 4.2.1-BETA
  - No description
  

- 4.1.1-BETA
   - No description

### Usage

```JAVA
MongoS mongos = new MongoS("host", 27017, "DB");

mongos.set("moneys", "SwadeDev0", 5);
mongos.removeData("moneys", "SwadeDev0");
mongos.getInt("moneys", "SwadeDev0"); #Integer
mongos.exists("moneys", "SwadeDev0"); #Boolean
```
```JAVA
mongos.set("collection", YObject);
mongo.set("collection", key, Object);
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
