# MongoS
Simple Mongo API


### Latest Features

- 3.2.0
```txt
getObject() & set() function improvements
```

- 3.1.0
```txt
Some bug fixes & improvements
Changed groupId
```

- 3.0.1
```
Relased new version
```

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
