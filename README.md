# MongoS
Simple Mongo API



### Latest Features

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
<dependency>
    <groupId>net.swade.mongos</groupId>
    <artifactId>mongos</artifactId>
    <version>3.0.1</version>
</dependency>
```
