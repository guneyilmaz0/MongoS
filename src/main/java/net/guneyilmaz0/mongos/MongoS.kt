package net.guneyilmaz0.mongos

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.ChangeStreamIterable
import org.bson.Document

@Suppress("unused", "MemberVisibilityCanBePrivate")
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
        mongo = MongoClients.create()
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