package net.swade.mongos;

import com.google.gson.Gson;

public abstract class MongoSObject {
    public String toString(){
        return new Gson().toJson(this);
    }
}
