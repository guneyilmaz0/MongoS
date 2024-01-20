package net.guneyilmaz0.mongos

import com.google.gson.Gson
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.ChangeStreamIterable
import org.bson.Document
import java.util.regex.Pattern

@Suppress("unused")
class MongoS : Database {
    private val mongo: MongoClient

    constructor(host: String, port: Int, dbName: String) {
        mongo = MongoClients.create("mongodb://$host:$port")
        init(mongo.getDatabase(dbName))
    }

    constructor(uri: String, dbName: String) {
        mongo = MongoClients.create(uri)
        init(mongo.getDatabase(dbName))
    }

    constructor(dbName: String) {
        mongo = MongoClients.create("mongodb://localhost:27017")
        init(mongo.getDatabase(dbName))
    }

    fun getCollection(collection: String): MongoCollection<Document> = database!!.getCollection(collection)

    fun getAnotherDatabase(dataBase: String): Database {
        val db = Database()
        db.init(mongo.getDatabase(dataBase))
        return db
    }

    fun watchCollection(collection: String): ChangeStreamIterable<Document> = getCollection(collection).watch()

}

class CaseInsensitiveString(private val string: String) {
    fun compile(): Pattern = Pattern.compile(string, Pattern.CASE_INSENSITIVE)
}

abstract class MongoSObject {
    override fun toString(): String = Gson().toJson(this)
}