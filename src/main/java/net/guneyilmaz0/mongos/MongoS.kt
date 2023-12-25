package net.guneyilmaz0.mongos

import com.google.gson.Gson
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.bson.Document
import java.util.regex.Pattern

@Suppress("unused")
class MongoS : Database {
    private val mongo: MongoClient

    constructor(host: String, port: Int, dbName: String) {
        mongo = MongoClient(host, port)
        init(mongo.getDatabase(dbName))
    }

    constructor(uri: MongoClientURI, dbName: String) {
        mongo = MongoClient(uri)
        init(mongo.getDatabase(dbName))
    }

    constructor(dbName: String) {
        mongo = MongoClient("localhost", 27017)
        init(mongo.getDatabase(dbName))
    }

    fun getCollection(collection: String): MongoCollection<Document> = database!!.getCollection(collection)

    fun getAnotherDatabase(dataBase: String): Database {
        val db = Database()
        db.init(mongo.getDatabase(dataBase))
        return db
    }
}

class CaseInsensitiveString(private val string: String) {
    fun compile(): Pattern = Pattern.compile(string, Pattern.CASE_INSENSITIVE)
}

abstract class MongoSObject {
    override fun toString(): String = Gson().toJson(this)
}
