package net.guneyilmaz0.mongos

import com.google.gson.Gson

abstract class MongoSObject {
    override fun toString(): String = Gson().toJson(this)
}