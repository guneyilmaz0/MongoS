package net.guneyilmaz0.mongos

import com.google.gson.Gson
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.result.InsertManyResult
import kotlinx.coroutines.*
import org.bson.BsonDocument
import org.bson.BsonInt64
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

    companion object {
        const val KEY_FIELD = "key"
        const val VALUE_FIELD = "value"
        val gson: Gson = Gson()
    }

    lateinit var database: MongoDatabase

    /**
     * Initializes the database with the specified MongoDB database instance.
     *
     * @param database the MongoDB database instance.
     */
    open fun init(database: MongoDatabase) {
        try {
            this.database = database
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDBObject(keyName: String, key: Any): DBObject =
        BasicDBObject().append(keyName, key)

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
        val keyDocument = Document(KEY_FIELD, key)
        val finalDocument = keyDocument.append(VALUE_FIELD, if (value is MongoSObject) convertDocument(value) else value)
        setFinal(collection, key, finalDocument, async)
    }

    /**
     * Final method to set a document in the specified collection with the provided key and document.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @param document the document to set.
     * @param async whether the operation should be asynchronous.
     */
    fun setFinal(collection: String, key: Any, document: Document, async: Boolean = false) {
        if (async) CoroutineScope(Dispatchers.IO).launch { setFinalSuspend(collection, key, document) }
        else runBlocking(Dispatchers.IO) { setFinalSuspend(collection, key, document) }
    }

    /**
     * Suspended method to set a document in the specified collection with the provided key and document asynchronously.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @param document the document to set.
     */
    private suspend fun setFinalSuspend(collection: String, key: Any, document: Document) = coroutineScope {
        val removed = removeData(collection, key)
        removed?.let { document[KEY_FIELD] = it[KEY_FIELD] }
        database.getCollection(collection).insertOne(document)
    }

    /**
     * Sets multiple documents in the specified collection.
     *
     * @param collection the collection name.
     * @param documents the list of documents to set.
     * @return the result of the operation.
     */
    suspend fun setMany(collection: String, documents: List<Document>): InsertManyResult = coroutineScope {
        database.getCollection(collection).insertMany(documents)
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
        return renameKey(collection, KEY_FIELD, oldKey, newKey)
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
            document = document.append(KEY_FIELD, newKey)
            set(collection, newKey, document)
        }
    }

    /**
     * Removes a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @return the removed document.
     */
    fun removeData(collection: String, key: Any): Document? =
        database.getCollection(collection).findOneAndDelete(createDBObject(KEY_FIELD, key) as Bson)

    /**
     * Removes a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the document.
     * @return the removed document.
     */
    fun removeData(collection: String, keyName: String, key: Any): Document? =
        database.getCollection(collection).findOneAndDelete(createDBObject(keyName, key) as Bson)

    /**
     * Checks if a document exists in the specified collection with the provided key.
     * Supports CaseInsensitiveString for key matching.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @return true if the document exists, false otherwise.
     */
    fun exists(collection: String, key: Any): Boolean =
        exists(collection, KEY_FIELD, key)

    /**
     * Checks if a document exists in the specified collection with the provided key.
     * Supports CaseInsensitiveString for key matching.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the document.
     * @return true if the document exists, false otherwise.
     */
    fun exists(collection: String, keyName: String, key: Any): Boolean =
        database.getCollection(collection).find(createDBObject(keyName, key) as Bson).first() != null

    /**
     * Gets all keys in the specified collection.
     *
     * @param collection the collection name.
     * @return a list of keys.
     */
    fun getKeys(collection: String): List<String> =
        database.getCollection(collection).find().map { it[KEY_FIELD].toString() }.toList()

    /**
     * Gets all documents in the specified collection as a map.
     *
     * @param collection the collection name.
     * @return a map of keys and values.
     */
    fun getAll(collection: String): Map<String, Any> =
        database.getCollection(collection).find().associate { it[KEY_FIELD].toString() to it[VALUE_FIELD]!! }

    /**
     * Gets filtered documents in the specified collection as a map.
     *
     * @param collection the collection name.
     * @param filters a map where each entry represents key1 -> key2 pairs to filter.
     * @return a map of keys and values matching the filters.
     */
    fun getAll(collection: String, filters: Map<String, String>): Map<String, Any> =
        database.getCollection(collection)
            .find(Filters.and(filters.map { Filters.eq(it.key, it.value) }))
            .associate { it[KEY_FIELD].toString() to it[VALUE_FIELD]!! }

    /**
     * Gets an integer value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @param defaultValue the default value if the key does not exist.
     * @return the integer value.
     */
    fun getInt(collection: String, key: Any, defaultValue: Int = 0): Int =
        getInt(collection, KEY_FIELD, key, VALUE_FIELD, defaultValue)

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
        getInt(collection, keyName, key, VALUE_FIELD, defaultValue)

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
        getString(collection, KEY_FIELD, key, VALUE_FIELD, defaultValue)

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
        getString(collection, keyName, key, VALUE_FIELD, defaultValue)

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
        keyName: String = KEY_FIELD,
        key: Any,
        value: String = VALUE_FIELD,
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
        getDouble(collection, KEY_FIELD, key, VALUE_FIELD, defaultValue)

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
        getDouble(collection, keyName, key, VALUE_FIELD, defaultValue)

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
        keyName: String = KEY_FIELD,
        key: Any,
        value: String = VALUE_FIELD,
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
        getFloat(collection, KEY_FIELD, key, VALUE_FIELD, defaultValue)

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
        getFloat(collection, keyName, key, VALUE_FIELD, defaultValue)

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
        keyName: String = KEY_FIELD,
        key: Any,
        value: String = VALUE_FIELD,
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
        getLong(collection, KEY_FIELD, key, VALUE_FIELD, defaultValue)

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
        getLong(collection, keyName, key, VALUE_FIELD, defaultValue)

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
        getBoolean(collection, KEY_FIELD, key, VALUE_FIELD, defaultValue)

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
        getBoolean(collection, keyName, key, VALUE_FIELD, defaultValue)

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
        keyName: String = KEY_FIELD,
        key: Any,
        value: String = VALUE_FIELD,
        defaultValue: Boolean
    ): Boolean = getValue(collection, keyName, key, value, defaultValue) as? Boolean ?: defaultValue

    /**
     * Gets a value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the value.
     * @return the value.
     */
    fun getValue(collection: String, key: Any): Any? = getValue(collection, KEY_FIELD, key)

    /**
     * Gets a value from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the value.
     * @return the value.
     */
    fun getValue(collection: String, keyName: String, key: Any): Any? = getValue(collection, keyName, key, VALUE_FIELD)

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
     * Retrieves a single object from a collection based on a key.
     *
     * @param T The type of the object to retrieve.
     * @param collection The name of the collection.
     * @param key The value of the key to filter by.
     * @param classOff The class type of the object to retrieve.
     * @return The object of the specified type that matches the criteria.
     */
    fun <T> getObject(collection: String, key: Any, classOff: Class<T>): T =
        this.getObject(collection, KEY_FIELD, key, classOff)

    /**
     * Retrieves a single object from a collection based on a specified key and key name.
     *
     * @param T The type of the object to retrieve.
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by.
     * @param classOff The class type of the object to retrieve.
     * @return The object of the specified type that matches the criteria.
     * @throws NoSuchElementException if no document is found for the given criteria.
     */
    fun <T> getObject(collection: String, keyName: String, key: Any, classOff: Class<T>): T {
        val document = this.getDocument(collection, keyName, key)
            ?: throw NoSuchElementException("No document found in collection '$collection' for key '$keyName' with value '$key'.")
        return documentToObject(document, classOff)
    }

    /**
     * Converts a Document to an object of the specified type.
     *
     * @param T The type of the object to convert to.
     * @param document The Document to convert.
     * @param classOff The class type of the object.
     * @return The object of the specified type.
     */
    private fun <T> documentToObject(document: Document, classOff: Class<T>): T {
        val value = document["value"]
        val json = Gson().toJson(value)
        return Gson().fromJson(json, classOff)
    }

    /**
     * Retrieves a list of objects from a collection based on a key.
     *
     * @param T The type of objects to retrieve.
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by.
     * @param classOff The class type of the objects to retrieve.
     * @return A list of objects of the specified type, or null if no objects match the criteria.
     */
    fun <T> getList(collection: String, keyName: String, key: Any, classOff: Class<T>): List<T>? =
        this.getList(collection, keyName, key, VALUE_FIELD, classOff)

    /**
     * Retrieves a list of objects from a collection based on a key.
     *
     * @param T The type of objects to retrieve.
     * @param collection The name of the collection.
     * @param key The value of the key to filter by.
     * @param classOff The class type of the objects to retrieve.
     * @return A list of objects of the specified type, or null if no objects match the criteria.
     */
    fun <T> getList(collection: String, key: Any, classOff: Class<T>): List<T>? =
        this.getList(collection, KEY_FIELD, key, VALUE_FIELD, classOff)

    /**
     * Retrieves a list of objects from a collection based on a specified key, key name, and value.
     *
     * @param T The type of objects to retrieve.
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by.
     * @param value The name of the value to retrieve the list from.
     * @param classOff The class type of the objects to retrieve.
     * @return A list of objects of the specified type, or null if the key does not exist in the collection.
     */
    fun <T> getList(collection: String, keyName: String, key: Any, value: String, classOff: Class<T>): List<T>? {
        if (this.exists(collection, keyName, key)) {
            val doc = this.getDocument(collection, keyName, key)
            return doc!!.getList(value, classOff)
        } else return null
    }

    /**
     * Gets a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @return the document.
     */
    fun getDocument(collection: String, key: Any): Document? = getDocument(collection, KEY_FIELD, key)

    /**
     * Gets a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param keyName the name of the key field.
     * @param key the key for the document.
     * @return the document.
     */
    fun getDocument(collection: String, keyName: String, key: Any): Document? =
        database.getCollection(collection).find(createDBObject(keyName, key) as Bson).first()

    /**
     * Retrieves documents from a collection that match the specified key.
     *
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by. If the key is a `CaseInsensitiveString`, it will be compiled before querying.
     * @return A `FindIterable<Document>` containing the documents that match the criteria.
     */
    fun getDocuments(collection: String, keyName: String, key: Any): FindIterable<Document> =
        database.getCollection(collection).find(createDBObject(keyName, key) as Bson)

    /**
     * Retrieves documents from a collection as a list that match the specified key.
     *
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by.
     * @return An `List<Document>` containing the documents that match the criteria.
     */
    fun getDocumentsAsList(collection: String, keyName: String, key: Any): List<Document> =
        database.getCollection(collection).find(createDBObject(keyName, key) as Bson).toList()

    /**
     * Gets an iterable of documents from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the documents.
     * @return the iterable of documents.
     */
    fun getIterable(collection: String, key: Any): FindIterable<Document> =
        database.getCollection(collection).find(createDBObject(KEY_FIELD, key) as Bson)

    fun convertDocument(mongoSObject: MongoSObject): Document = convertJson(gson.toJson(mongoSObject))

    /**
     * Converts a document to a JSON string.
     *
     * @param document the document to convert.
     * @return the JSON string.
     */
    fun convertJson(document: Document): String = gson.toJson(document)

    /**
     * Converts a JSON string to a document.
     *
     * @param json the JSON string to convert.
     * @return the document.
     */
    fun convertJson(json: String): Document = Document.parse(json)

    fun isConnected(): Boolean =
        try {
            database.runCommand(BsonDocument("ping", BsonInt64(1)))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
}