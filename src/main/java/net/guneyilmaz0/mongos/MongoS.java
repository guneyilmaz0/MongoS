package net.guneyilmaz0.mongos;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import org.bson.Document;

@Getter
@SuppressWarnings("unused")
public class MongoS extends Database  {
    private final MongoClient mongo;

    public MongoS(String host, int port, String dbName) {
        this.mongo = new MongoClient(host, port);
        init(mongo.getDatabase(dbName));
    }

    public MongoS(MongoClientURI uri, String dbName) {
        this.mongo = new MongoClient(uri);
        init(mongo.getDatabase(dbName));
    }

    public MongoS(String dbName){
        this.mongo = new MongoClient("localhost", 27017);
        init(mongo.getDatabase(dbName));
    }

    public MongoCollection<Document> getCollection(String collection) {
        return this.database.getCollection(collection);
    }

    public Database getAnotherDatabase(String dataBase) {
        Database db = new Database();
        db.init(mongo.getDatabase(dataBase));
        return db;
    }
}
