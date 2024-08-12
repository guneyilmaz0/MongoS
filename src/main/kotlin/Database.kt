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

    companion object {
        const val KEY_FIELD = "key"
        const val VALUE_FIELD = "value"
    }

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
            append(KEY_FIELD, if (key is CaseInsensitiveString) key.compile() else key)
        }

        val finalDocument = keyDocument.apply {
            append(VALUE_FIELD, if (value is MongoSObject) value.toString() else value)
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
            if (removed != null) document.replace(KEY_FIELD, removed[KEY_FIELD])
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
            if (removed != null) document.replace(KEY_FIELD, removed[KEY_FIELD])
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
            document = when (newKey) {
                is CaseInsensitiveString -> document.append(KEY_FIELD, newKey.compile())
                else -> document.append(KEY_FIELD, newKey)
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
        val filter = BasicDBObject().append(KEY_FIELD, if (key is CaseInsensitiveString) key.compile() else key)
        val update = BasicDBObject().append("\$set", BasicDBObject().append(VALUE_FIELD, newValue))
        database!!.getCollection(collection).updateOne(filter as Bson, update as Bson)
    }

    /**
     * Removes a document from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the document.
     * @return the removed document.
     */
    fun removeData(collection: String, key: Any): Document? = removeData(collection, KEY_FIELD, key)

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
    fun exists(collection: String, key: Any): Boolean = exists(collection, KEY_FIELD, key)

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
        for (document in database!!.getCollection(collection).find()) keys.add(document[KEY_FIELD].toString())
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
        for (document in database!!.getCollection(collection).find()) map[document[KEY_FIELD].toString()] =
            document[VALUE_FIELD]!!
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
     * Retrieves a list of objects from a collection that match a specified key.
     *
     * @param T The type of objects to retrieve.
     * @param collection The name of the collection.
     * @param classOff The class type of the objects to retrieve.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by.
     * @return A list of objects of the specified type, or an empty list if no objects match the criteria.
     */
    fun <T> getObjects(collection: String, classOff: Class<T>, keyName: String, key: Any): List<T?> {
        val objects = this.getDocumentsAsList(collection, keyName, key)
        val objectsClass = mutableListOf<T?>()
        for (document in objects) objectsClass.add(Gson().fromJson(document.toJson(), classOff))
        return objectsClass
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
        this.getObject(collection, "key", key, classOff)

    /**
     * Retrieves a single object from a collection based on a specified key and key name.
     *
     * @param T The type of the object to retrieve.
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by.
     * @param classOff The class type of the object to retrieve.
     * @return The object of the specified type that matches the criteria.
     */
    fun <T> getObject(collection: String, keyName: String, key: Any, classOff: Class<T>): T =
        Gson().fromJson(this.getString(collection, keyName, key, ""), classOff)

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
        this.getList(collection, keyName, key, "value", classOff)

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
        this.getList(collection, "key", key, "value", classOff)

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
    fun getDocument(collection: String, keyName: String, key: Any): Document? {
        val dbObject = BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        val iterable = database!!.getCollection(collection).find(dbObject as Bson)
        return iterable.first()
    }

    /**
     * Retrieves documents from a collection that match the specified key.
     *
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by. If the key is a `CaseInsensitiveString`, it will be compiled before querying.
     * @return A `FindIterable<Document>` containing the documents that match the criteria.
     */
    fun getDocuments(collection: String, keyName: String, key: Any): FindIterable<Document> {
        val dbObject: DBObject =
            BasicDBObject().append(keyName, if (key is CaseInsensitiveString) key.compile() else key)
        return database!!.getCollection(collection).find(dbObject as Bson)
    }

    /**
     * Retrieves documents from a collection as a list that match the specified key.
     *
     * @param collection The name of the collection.
     * @param keyName The name of the key to filter by.
     * @param key The value of the key to filter by.
     * @return An `ArrayList<Document>` containing the documents that match the criteria.
     */
    fun getDocumentsAsList(collection: String, keyName: String, key: Any): ArrayList<Document> {
        val docs = ArrayList<Document>()
        for (document in getDocuments(collection, keyName, key)) docs.add(document)
        return docs
    }

    /**
     * Gets an iterable of documents from the specified collection with the provided key.
     *
     * @param collection the collection name.
     * @param key the key for the documents.
     * @return the iterable of documents.
     */
    fun getIterable(collection: String, key: Any): FindIterable<Document> {
        val dbObject = BasicDBObject().append(KEY_FIELD, if (key is CaseInsensitiveString) key.compile() else key)
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
