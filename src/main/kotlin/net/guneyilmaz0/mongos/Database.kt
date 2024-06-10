package net.guneyilmaz0.mongos

import com.google.gson.Gson
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.*
import org.bson.Document
import org.bson.conversions.Bson

/**
 * This class represents a MongoDB database.
 * It provides methods to initialize the database, set and get values, update and remove data, and check if data exists.
 *
 * @property database the MongoDB database instance.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class Database {
    var database: MongoDatabase? = null

    /**
     * Initializes the database with the specified MongoDB database instance.
     *
     * @param database the MongoDB database instance.
     */
    open fun init(database: MongoDatabase?) {
        this.database = database
    }

    /**
     * Sets a value in the specified collection with the provided key and value.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param value the value to set.
     */
    fun set(collection: String, key: Any, value: Any) = set(collection, key, value, false)

    /**
     * Sets a value in the specified collection with the provided key and value.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param value the value to set.
     * @param async whether the operation should be asynchronous.
     */
    fun set(collection: String, key: Any, value: Any, async: Boolean = false) {
        val keyDocument = Document().apply {
            append("key", if (key is CaseInsensitiveString) key.compile() else key)
        }

        val finalDocument = keyDocument.apply {
            append("value", if (value is MongoSObject) value.toString() else value)
        }

        set(collection, key, finalDocument, async)
    }


    /**
     * Sets a document in the specified collection with the provided key and document.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @param document the document to set.
     * @param async whether the operation should be asynchronous.
     */
    fun set(collection: String, key: Any, document: Document, async: Boolean = false) =
        setFinal(collection, key, document, async)

    /**
     * Sets a document in the specified collection with the provided key and document.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @param document the document to set.
     * @param async whether the operation should be asynchronous.
     */
    fun set(collection: String, key: CaseInsensitiveString, document: Document, async: Boolean = false) =
        setFinal(collection, key.compile(), document, async)

    /**
     * Final method to set a document in the specified collection with the provided key and document.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @param document the document to set.
     * @param async whether the operation should be asynchronous.
     */
    fun setFinal(collection: String, key: Any, document: Document, async: Boolean = false) {
        if (async) runBlocking { setFinalSuspend(collection, key, document) }
        else {
            val removed = removeData(collection, key)
            if (removed != null) document.replace("key", removed["key"])
            database!!.getCollection(collection).insertOne(document)
        }
    }

    /**
     * Suspended method to set a document in the specified collection with the provided key and document asynchronously.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @param document the document to set.
     */
    suspend fun setFinalSuspend(collection: String, key: Any, document: Document) {
        coroutineScope {
            val removed = removeData(collection, key)
            if (removed != null) document.replace("key", removed["key"])
            database!!.getCollection(collection).insertOne(document)
        }
    }

    /**
     * Sets multiple documents in the specified collection.
     *
     * @param collection the collection name.
     * @param documents the list of documents to set.
     */
    fun setMany(collection: String, documents: List<Document>) {
        database!!.getCollection(collection).insertMany(documents)
    }

    /**
     * Sets a value in the specified collection with the provided key and value if the key does not already exist.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param value the value to set.
     */
    fun setIfNotExists(collection: String, key: Any, value: Any) {
        if (!exists(collection, key)) set(collection, key, value)
    }

    /**
     * Renames a key in the specified collection.
     *
     * @param collection the collection name.
     * @param oldKey the old key.
     * @param newKey the new key.
     */
    fun renameKey(collection: String, oldKey: Any, newKey: Any) {
        return renameKey(collection, "key", oldKey, newKey)
    }

    /**
     * Renames a key in the specified collection.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param oldKey the old key.
     * @param newKey the new key.
     */
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

    /**
     * Updates a value in the specified collection with the provided key and new value.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param newValue the new value to set.
     */
    fun update(collection: String, key: Any, newValue: Any) {
        val filter = BasicDBObject().append("key", if (key is CaseInsensitiveString) key.compile() else key)
        val update = BasicDBObject().append("\$set", BasicDBObject().append("value", newValue))
        database!!.getCollection(collection).updateOne(filter as Bson, update as Bson)
    }

    /**
     * Removes a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @return the removed document.
     */
    fun removeData(collection: String, key: Any): Document? = removeData(collection, "key", key)

    /**
     * Removes a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the document.
     * @return the removed document.
     */
    fun removeData(collection: String, keyName: String, key: Any): Document? {
        val dbObject = BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        return database!!.getCollection(collection).findOneAndDelete(dbObject as Bson)
    }

    /**
     * Checks if a document exists in the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @return true if the document exists, false otherwise.
     */
    fun exists(collection: String, key: Any): Boolean = exists(collection, "key", key)

    /**
     * Checks if a document exists in the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the document.
     * @return true if the document exists, false otherwise.
     */
    fun exists(collection: String, keyName: String, key: Any): Boolean = getDocument(collection, keyName, key) != null

    /**
     * Checks if a document exists in the specified collection with the provided DBObject.
     *
     * @param collection the collection name.
     * @param dbObject the DBObject to check.
     * @return true if the document exists, false otherwise.
     */
    fun exists(collection: String, dbObject: DBObject): Boolean =
        database!!.getCollection(collection).find(dbObject as Bson).first() != null

    /**
     * Gets all keys in the specified collection.
     *
     * @param collection the collection name.
     * @return a list of keys.
     */
    fun getKeys(collection: String): List<String> {
        val keys = ArrayList<String>()
        for (document in database!!.getCollection(collection).find()) keys.add(document["key"].toString())
        return keys
    }

    /**
     * Gets all documents in the specified collection as a map.
     *
     * @param collection the collection name.
     * @return a map of keys and values.
     */
    fun getAll(collection: String): Map<String, Any> {
        val map = HashMap<String, Any>()
        for (document in database!!.getCollection(collection).find()) map[document["key"].toString()] =
            document["value"]!!
        return map
    }

    /**
     * Gets an integer value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the integer value.
     */
    fun getInt(collection: String, key: Any, defaultValue: Int = 0): Int =
        getInt(collection, "key", key, "value", defaultValue)

    /**
     * Gets an integer value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the integer value.
     */
    fun getInt(collection: String, keyName: String, key: Any, defaultValue: Int): Int =
        getInt(collection, keyName, key, "value", defaultValue)

    /**
     * Gets an integer value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @param defaultValue the default value if the key does not exist.
     * @return the integer value.
     */
    fun getInt(
        collection: String,
        keyName: String,
        key: Any,
        value: String,
        defaultValue: Int
    ): Int = getValue(collection, keyName, key, value, defaultValue) as? Int ?: defaultValue

    /**
     * Gets a string value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the string value.
     */
    fun getString(collection: String, key: Any, defaultValue: String = ""): String =
        getString(collection, "key", key, "value", defaultValue)

    /**
     * Gets a string value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the string value.
     */
    fun getString(collection: String, keyName: String, key: Any, defaultValue: String): String =
        getString(collection, keyName, key, "value", defaultValue)

    /**
     * Gets a string value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @param defaultValue the default value if the key does not exist.
     * @return the string value.
     */
    fun getString(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: String
    ): String = getValue(collection, keyName, key, value, defaultValue) as? String ?: defaultValue

    /**
     * Gets a double value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the double value.
     */
    fun getDouble(collection: String, key: Any, defaultValue: Double = 0.0): Double =
        getDouble(collection, "key", key, "value", defaultValue)

    /**
     * Gets a double value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the double value.
     */
    fun getDouble(collection: String, keyName: String, key: Any, defaultValue: Double): Double =
        getDouble(collection, keyName, key, "value", defaultValue)

    /**
     * Gets a double value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @param defaultValue the default value if the key does not exist.
     * @return the double value.
     */
    fun getDouble(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Double
    ): Double = getValue(collection, keyName, key, value, defaultValue) as? Double ?: defaultValue

    /**
     * Gets a float value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the float value.
     */
    fun getFloat(collection: String, key: Any, defaultValue: Float = 0f): Float =
        getFloat(collection, "key", key, "value", defaultValue)

    /**
     * Gets a float value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the float value.
     */
    fun getFloat(collection: String, keyName: String, key: Any, defaultValue: Float): Float =
        getFloat(collection, keyName, key, "value", defaultValue)

    /**
     * Gets a float value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @param defaultValue the default value if the key does not exist.
     * @return the float value.
     */
    fun getFloat(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Float
    ): Float = getValue(collection, keyName, key, value, defaultValue) as? Float ?: defaultValue

    /**
     * Gets a long value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the long value.
     */
    fun getLong(collection: String, key: Any, defaultValue: Long = 0): Long =
        getLong(collection, "key", key, "value", defaultValue)

    /**
     * Gets a long value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the long value.
     */
    fun getLong(collection: String, keyName: String, key: Any, defaultValue: Long): Long =
        getLong(collection, keyName, key, "value", defaultValue)

    /**
     * Gets a long value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @param defaultValue the default value if the key does not exist.
     * @return the long value.
     */
    fun getLong(
        collection: String,
        keyName: String,
        key: Any,
        value: String,
        defaultValue: Long
    ): Long = getValue(collection, keyName, key, value, defaultValue) as? Long ?: defaultValue

    /**
     * Gets a boolean value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the boolean value.
     */
    fun getBoolean(collection: String, key: Any, defaultValue: Boolean = false): Boolean =
        getBoolean(collection, "key", key, "value", defaultValue)

    /**
     * Gets a boolean value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the boolean value.
     */
    fun getBoolean(collection: String, keyName: String, key: Any, defaultValue: Boolean): Boolean =
        getBoolean(collection, keyName, key, "value", defaultValue)

    /**
     * Gets a boolean value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @param defaultValue the default value if the key does not exist.
     * @return the boolean value.
     */
    fun getBoolean(
        collection: String,
        keyName: String = "key",
        key: Any,
        value: String = "value",
        defaultValue: Boolean
    ): Boolean = getValue(collection, keyName, key, value, defaultValue) as? Boolean ?: defaultValue

    /**
     * Gets a value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @return the value.
     */
    fun getValue(collection: String, key: Any): Any? = getValue(collection, "key", key)

    /**
     * Gets a value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @return the value.
     */
    fun getValue(collection: String, keyName: String, key: Any): Any? = getValue(collection, keyName, key, "value")

    /**
     * Gets a value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @return the value.
     */
    fun getValue(collection: String, keyName: String, key: Any, value: String): Any? =
        getValue(collection, keyName, key, value, null)

    /**
     * Gets a value from the specified collection with the provided key and default value.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @param value the value field.
     * @param defaultValue the default value if the key does not exist.
     * @return the value.
     */
    fun getValue(collection: String, keyName: String, key: Any, value: String, defaultValue: Any?): Any? {
        val document = getDocument(collection, keyName, key)
        return document?.get(value) ?: defaultValue
    }

    /**
     * Gets a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @return the document.
     */
    fun getDocument(collection: String, key: Any): Document? = getDocument(collection, "key", key)

    /**
     * Gets a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the document.
     * @return the document.
     */
    fun getDocument(collection: String, keyName: String, key: Any): Document? {
        val dbObject = BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        val iterable = database!!.getCollection(collection).find(dbObject as Bson)
        return iterable.first()
    }

    /**
     * Gets an iterable of documents from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the documents.
     * @return the iterable of documents.
     */
    fun getIterable(collection: String, key: Any): FindIterable<Document> {
        val dbObject = BasicDBObject().append("key", if (key is CaseInsensitiveString) key.compile() else key)
        return database!!.getCollection(collection).find(dbObject as Bson)
    }

    /**
     * Converts a document to a JSON string.
     *
     * @param document the document to convert.
     * @return the JSON string.
     */
    fun convertJson(document: Document): String = Gson().toJson(document)

    /**
     * Converts a JSON string to a document.
     *
     * @param json the JSON string to convert.
     * @return the document.
     */
    fun convertJson(json: String): Document = Document.parse(json)
}
