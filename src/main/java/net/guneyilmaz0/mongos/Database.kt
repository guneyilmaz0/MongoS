package net.guneyilmaz0.mongos

import com.google.gson.Gson
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.conversions.Bson
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class Database {
    var database: MongoDatabase? = null

    open fun init(database: MongoDatabase?) {
        setLogLevel(Level.INFO)
        this.database = database
    }

    fun setLogLevel(level: Level) {
        Logger.getLogger("org.mongodb.driver").level = level
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

    fun set(collection: String, key: Any, document: Document) = setFinal(collection, key, document)

    fun set(collection: String, key: CaseInsensitiveString, document: Document) = setFinal(collection, key.compile(), document)

    fun setFinal(collection: String, key: Any, document: Document) {
        val removed = removeData(collection, key)
        if (removed != null) document.replace("key", removed["key"])
        database!!.getCollection(collection).insertOne(document)
    }

    fun setIfNotExists(collection: String, key: Any, value: Any) {
        if (!exists(collection, key)) set(collection, key, value)
    }

    fun renameKey(collection: String, oldKey: Any, newKey: Any) {
        return renameKey(collection, "key", oldKey, newKey)
    }

    fun renameKey(collection: String, keyName: String, oldKey: Any, newKey: Any) {
        var document = getDocument(collection, keyName, oldKey)
        if (document != null) {
            removeData(collection, oldKey)
            document = when (newKey) {
                is CaseInsensitiveString -> document.append("key", newKey.compile())
                else -> document.append("key", newKey)
            }
            set(collection, newKey, document)
        }
    }

    fun update(collection: String, key: Any, newValue: Any) {
        val filter = BasicDBObject().append("key", if (key is CaseInsensitiveString) key.compile() else key)
        val update = BasicDBObject().append("\$set", BasicDBObject().append("value", newValue))
        database!!.getCollection(collection).updateOne(filter as Bson, update as Bson)
    }

    fun removeData(collection: String, key: Any): Document? = removeData(collection, "key", key)

    fun removeData(collection: String, keyName: String, key: Any): Document? {
        val dbObject = BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        return database!!.getCollection(collection).findOneAndDelete(dbObject as Bson)
    }

    fun exists(collection: String, key: Any): Boolean = exists(collection, "key", key)

    fun exists(collection: String, keyName: String, key: Any): Boolean = getDocument(collection, keyName, key) != null

    fun exists(collection: String, dbObject: DBObject): Boolean =
        database!!.getCollection(collection).find(dbObject as Bson).first() != null

    fun getKeys(collection: String): List<String> {
        val keys = ArrayList<String>()
        for (document in database!!.getCollection(collection).find()) keys.add(document["key"].toString())
        return keys
    }

    fun getAll(collection: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        for (document in database!!.getCollection(collection).find()) map[document["key"].toString()] =
            document["value"]!!
        return map
    }

    fun getInt(collection: String, key: Any, defaultValue: Int = 0): Int =
        getInt(collection, "key", key, "value", defaultValue)

    fun getInt(collection: String, keyName: String, key: Any, defaultValue: Int): Int =
        getInt(collection, keyName, key, "value", defaultValue)

    fun getInt(
        collection: String,
        keyName: String,
        key: Any,
        value: String,
        defaultValue: Int
    ): Int = getValue(collection, keyName, key, value, defaultValue) as? Int ?: defaultValue

    fun getString(collection: String, key: Any, defaultValue: String = ""): String =
        getString(collection, "key", key, "value", defaultValue)

    fun getString(collection: String, keyName: String, key: Any, defaultValue: String): String =
        getString(collection, keyName, key, "value", defaultValue)

    fun getString(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: String
    ): String = getValue(collection, keyName, key, value, defaultValue) as? String ?: defaultValue

    fun getDouble(collection: String, key: Any, defaultValue: Double = 0.0): Double =
        getDouble(collection, "key", key, "value", defaultValue)

    fun getDouble(collection: String, keyName: String, key: Any, defaultValue: Double): Double =
        getDouble(collection, keyName, key, "value", defaultValue)

    fun getDouble(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Double
    ): Double = getValue(collection, keyName, key, value, defaultValue) as? Double ?: defaultValue

    fun getFloat(collection: String, key: Any, defaultValue: Float = 0f): Float =
        getFloat(collection, "key", key, "value", defaultValue)

    fun getFloat(collection: String, keyName: String, key: Any, defaultValue: Float): Float =
        getFloat(collection, keyName, key, "value", defaultValue)

    fun getFloat(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Float
    ): Float = getValue(collection, keyName, key, value, defaultValue) as? Float ?: defaultValue

    fun getLong(collection: String, key: Any, defaultValue: Long = 0): Long =
        getLong(collection, "key", key, "value", defaultValue)

    fun getLong(collection: String, keyName: String, key: Any, defaultValue: Long): Long =
        getLong(collection, keyName, key, "value", defaultValue)

    fun getLong(
        collection: String,
        keyName: String,
        key: Any,
        value: String,
        defaultValue: Long
    ): Long = getValue(collection, keyName, key, value, defaultValue) as? Long ?: defaultValue

    fun getBoolean(collection: String, key: Any, defaultValue: Boolean = false): Boolean =
        getBoolean(collection, "key", key, "value", defaultValue)

    fun getBoolean(collection: String, keyName: String, key: Any, defaultValue: Boolean): Boolean =
        getBoolean(collection, keyName, key, "value", defaultValue)

    fun getBoolean(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Boolean
    ): Boolean = getValue(collection, keyName, key, value, defaultValue) as? Boolean ?: defaultValue

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

    fun <T> getObjects(collection: String, classOff: Class<T>, keyName: String, key: Any): List<T?> {
        val objects = this.getDocumentsAsList(collection, keyName, key)
        val objectsClass = mutableListOf<T?>()
        for (document in objects) objectsClass.add(Gson().fromJson(document.toJson(), classOff))
        return objectsClass
    }

    fun <T> getObject(collection: String, key: Any, classOff: Class<T>): T =
        this.getObject(collection, "key", key, classOff)

    fun <T> getObject(collection: String, keyName: String, key: Any, classOff: Class<T>): T =
        Gson().fromJson(this.getString(collection, keyName, key, ""), classOff)

    fun getObjectJson(collection: String, key: Any): String? = getObjectJson(collection, "key", key)

    fun getObjectJson(collection: String, keyName: String, key: Any): String? {
        return getDocument(collection, keyName, key)?.toJson()
    }

    fun getDocument(collection: String, keyName: String, key: Any): Document? =
        getDocuments(collection, keyName, key).first()

    fun getDocuments(collection: String, keyName: String, key: Any): FindIterable<Document> {
        val dbObject: DBObject =
            BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        return database!!.getCollection(collection).find(dbObject as Bson)
    }

    fun getDocumentsAsList(collection: String, keyName: String, key: Any): ArrayList<Document> {
        val docs = ArrayList<Document>()
        for (document in getDocuments(collection, keyName, key)) docs.add(document)
        return docs
    }

    fun <T> getList(collection: String, keyName: String, key: Any, classOff: Class<T>): List<T>? =
        this.getList(collection, keyName, key, "value", classOff)

    fun <T> getList(collection: String, key: Any, classOff: Class<T>): List<T>? =
        this.getList(collection, "key", key, "value", classOff)

    fun <T> getList(collection: String, keyName: String, key: Any, value: String, classOff: Class<T>): List<T>? {
        if (this.exists(collection, keyName, key)) {
            val doc = this.getDocument(collection, keyName, key)
            return doc!!.getList(value, classOff)
        } else return null
    }
}