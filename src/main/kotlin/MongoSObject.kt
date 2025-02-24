package net.guneyilmaz0.mongos

import com.google.gson.Gson

/**
 * This abstract class represents a MongoDB object.
 * It provides a method to convert the object into a JSON string.
 */
abstract class MongoSObject {
    companion object {
        val gson: Gson = Gson()
    }

    /**
     * Converts the MongoDB object into a JSON string.
     *
     * @return a JSON string representation of the MongoDB object.
     */
    override fun toString(): String = gson.toJson(this)
}