package net.guneyilmaz0.mongos

import com.google.gson.Gson
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.conversions.Bson

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class Database {
    var database: MongoDatabase? = null

    open fun init(database: MongoDatabase?) {
        this.database = database
    }

    fun set(collection: String, key: Any, value: Any) {
        var document: Document = when (key) {
            is CaseInsensitiveString -> Document().append("key", key.compile())
            else -> Document().append("key", key)
        }

        document = when (value) {
            is MongoSObject -> document.append("value", value.toString())
            else -> document.append("value", value)
        }

        set(collection, key, document)
    }

    fun set(collection: String, key: Any, document: Document) {
        setFinal(collection, key, document)
    }

    fun set(collection: String, key: CaseInsensitiveString, document: Document) {
        setFinal(collection, key, document)
    }

    fun setFinal(collection: String, key: Any, document: Document) {
        val removed = removeData(collection, key)
        if (removed != null) document.replace("key", removed["key"])
        database!!.getCollection(collection).insertOne(document)
    }

    fun removeData(collection: String, key: Any): Document? {
        return removeData(collection, "key", key)
    }

    fun removeData(collection: String, keyName: String, key: Any): Document? {
        val dbObject = BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        return database!!.getCollection(collection).findOneAndDelete(dbObject as Bson)
    }

    fun exists(collection: String, key: Any): Boolean {
        return exists(collection, "key", key)
    }

    fun exists(collection: String, keyName: String, key: Any): Boolean {
        return getDocument(collection, keyName, key) != null
    }

    fun exists(collection: String, dbObject: DBObject): Boolean {
        return database!!.getCollection(collection).find(dbObject as Bson).first() != null
    }

    fun getInt(collection: String, key: Any, defaultValue: Int): Int =
        getInt(collection, "key", key, "value", defaultValue)

    fun getInt(collection: String, keyName: String, key: Any, defaultValue: Int): Int =
        getInt(collection, keyName, key, "value", defaultValue)

    fun getInt(
        collection: String,
        keyName: String,
        key: Any,
        value: String,
        defaultValue: Int
    ): Int =
        getValue(collection, keyName, key, value, defaultValue) as? Int ?: defaultValue

    fun getString(collection: String, key: Any, defaultValue: String): String =
        getString(collection, "key", key, "value", defaultValue)

    fun getString(collection: String, keyName: String, key: Any, defaultValue: String): String =
        getString(collection, keyName, key, "value", defaultValue)

    fun getString(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: String
    ): String =
        getValue(collection, keyName, key, value, defaultValue) as? String ?: defaultValue

    fun getDouble(collection: String, key: Any, defaultValue: Double): Double =
        getDouble(collection, "key", key, "value", defaultValue)

    fun getDouble(collection: String, keyName: String, key: Any, defaultValue: Double): Double =
        getDouble(collection, keyName, key, "value", defaultValue)

    fun getDouble(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Double
    ): Double =
        getValue(collection, keyName, key, value, defaultValue) as? Double ?: defaultValue

    fun getFloat(collection: String, key: Any, defaultValue: Float): Float =
        getFloat(collection, "key", key, "value", defaultValue)

    fun getFloat(collection: String, keyName: String, key: Any, defaultValue: Float): Float =
        getFloat(collection, keyName, key, "value", defaultValue)

    fun getFloat(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Float
    ): Float =
        getValue(collection, keyName, key, value, defaultValue) as? Float ?: defaultValue

    fun getBoolean(collection: String, key: Any, defaultValue: Boolean): Boolean =
        getBoolean(collection, "key", key, "value", defaultValue)

    fun getBoolean(collection: String, keyName: String, key: Any, defaultValue: Boolean): Boolean =
        getBoolean(collection, keyName, key, "value", defaultValue)

    fun getBoolean(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Boolean
    ): Boolean =
        getValue(collection, keyName, key, value, defaultValue) as? Boolean ?: defaultValue

    fun getValue(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Any?
    ): Any? {
        val document = getDocument(collection, keyName, key) ?: return defaultValue
        return document[value]
    }

    inline fun <reified T> getObjects(collection: String, keyName: String, key: Any): Array<T> {
        val documents = getDocumentsAsList(collection, keyName, key)
        val gson = Gson()
        return Array(documents.size) { i -> gson.fromJson(documents[i].toJson(), T::class.java) }
    }

    inline fun <reified T> getObject(collection: String, key: Any): T? {
        return getObject(collection, "key", key)
    }

    inline fun <reified T> getObject(collection: String, keyName: String, key: Any): T? {
        val jsonString = getObjectJson(collection, keyName, key)
        return Gson().fromJson(jsonString, T::class.java)
    }

    fun getObjectJson(collection: String, key: Any): String? {
        return getObjectJson(collection, "key", key)
    }

    fun getObjectJson(collection: String, keyName: String, key: Any): String? {
        val doc = getDocument(collection, keyName, key)
        return doc?.toJson()
    }

    fun getDocument(collection: String, keyName: String, key: Any): Document? {
        return getDocuments(collection, keyName, key).first()
    }

    fun getDocuments(collection: String, keyName: String, key: Any): FindIterable<Document> {
        val dbObject: DBObject =
            BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        return database!!.getCollection(collection).find(dbObject as Bson)
    }

    fun getDocumentsAsList(collection: String, keyName: String, key: Any): ArrayList<Document> {
        val docs = ArrayList<Document>()
        for (document in getDocuments(collection, keyName, key)) {
            docs.add(document)
        }
        return docs
    }

    inline fun <reified T> getList(collection: String, key: Any): List<T>? {
        return getList(collection, "key", key)
    }

    inline fun <reified T> getList(collection: String, keyName: String, key: Any): List<T>? {
        return getList(collection, keyName, key, "value")
    }

    inline fun <reified T> getList(collection: String, keyName: String, key: Any, value: String): List<T>? {
        if (exists(collection, keyName, key)) {
            val doc = getDocument(collection, keyName, key)
            return doc?.getList(value, T::class.java)
        } else {
            return null
        }
    }
}