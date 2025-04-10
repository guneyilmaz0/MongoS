package net.guneyilmaz0.mongos

import com.google.gson.Gson
import org.bson.Document

/**
 * This abstract class represents a MongoDB object.
 * It provides a method to convert the object into a Document.
 */
abstract class MongoSObject {
    companion object {
        private val gson: Gson = Gson()
    }

    /**
     * Converts the object into a Document.
     *
     * @return a Document representation of the object.
     */
    fun toDocument(): Document = Document.parse(gson.toJson(this))
}