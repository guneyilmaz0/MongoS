package net.swade.mongos;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@SuppressWarnings("unused")
public class Database {
    public MongoDatabase database;

    protected void init(MongoDatabase database) {
        this.database = database;
    }

    public void set(String collection, Object key, Object object) {
        Document document;
        if (object instanceof MongoSObject){
            document = new Document().append("key", key instanceof CaseInsensitiveString ? key.toString() : key).append("value", object.toString());
        } else {
            document = new Document().append("key", key instanceof CaseInsensitiveString ? key.toString() : key).append("value", object);
        }
        set(collection, key, document);
    }

    public void set(String collection, Object key, Document document) {
        setFinal(collection, key, document);
    }

    public void set(String collection, CaseInsensitiveString key, Document document) {
        setFinal(collection, key, document);
    }

    private void setFinal(String collection, Object key, Document document) {
        Document removed = removeData(collection, key);
        if (removed != null) document.replace("key", removed.get("key"));
        this.database.getCollection(collection).insertOne(document);
    }

    public Document removeData(String collection, Object key) {
        return removeData(collection, "key", key);
    }

    public Document removeData(String collection, String keyName, Object key) {
        DBObject dbObject = new BasicDBObject().append(keyName, key instanceof CaseInsensitiveString ? ((CaseInsensitiveString) key).compile() : key);
        return this.database.getCollection(collection).findOneAndDelete((Bson) dbObject);
    }

    public boolean exists(String collection, Object key) {
        return exists(collection, "key", key);
    }

    public boolean exists(String collection, String keyName, Object key) {
        return getDocument(collection, keyName, key) != null;
    }

    public boolean exists(String collection, DBObject object) {
        return database.getCollection(collection).find((Bson) object).first() != null;
    }

    public Integer getInt(String collection, Object key, Integer defaultValue) {
        return getInt(collection, "key", key, "value", defaultValue);
    }

    public String getString(String collection, Object key, String defaultValue) {
        return getString(collection, "key", key, "value", defaultValue);
    }

    public Boolean getBoolean(String collection, Object key, Boolean defaultValue) {
        return getBoolean(collection, "key", key, "value", defaultValue);
    }

    public Double getDouble(String collection, Object key, Double defaultValue) {
        return getDouble(collection, "key", key, "value", defaultValue);
    }

    public Float getFloat(String collection, Object key, Float defaultValue) {
        return getFloat(collection, "key", key, "value", defaultValue);
    }

    public Integer getInt(String collection, String keyName, Object key, Integer defaultValue) {
        return getInt(collection, keyName, key, "value", defaultValue);
    }

    public String getString(String collection, String keyName, Object key, String defaultValue) {
        return getString(collection, keyName, key, "value", defaultValue);
    }

    public Boolean getBoolean(String collection, String keyName, Object key, Boolean defaultValue) {
        return getBoolean(collection, keyName, key, "value", defaultValue);
    }

    public Double getDouble(String collection, String keyName, Object key, Double defaultValue) {
        return getDouble(collection, keyName, key, "value", defaultValue);
    }

    public Float getFloat(String collection, String keyName, Object key, Float defaultValue) {
        return getFloat(collection, keyName, key, "value", defaultValue);
    }

    public Integer getInt(String collection, String keyName, Object key, String value, Integer defaultValue) {
        Document doc = getDocument(collection, keyName, key);
        if (doc != null && doc.get(value) instanceof Integer) {
            return (Integer) doc.get(value);
        }
        return defaultValue;
    }

    public String getString(String collection, String keyName, Object key, String value, String defaultValue) {
        Document doc = getDocument(collection, keyName, key);
        if (doc != null && doc.get(value) instanceof String) {
            return (String) doc.get(value);
        }
        return defaultValue;
    }

    public Double getDouble(String collection, String keyName, Object key, String value, Double defaultValue) {
        Document doc = getDocument(collection, keyName, key);
        if (doc != null && doc.get(value) instanceof Double) {
            return (Double) doc.get(value);
        }
        return defaultValue;
    }

    public Float getFloat(String collection, String keyName, Object key, String value, Float defaultValue) {
        Document doc = getDocument(collection, keyName, key);
        if (doc != null && doc.get(value) instanceof Float) {
            return (Float) doc.get(value);
        }
        return defaultValue;
    }

    public Boolean getBoolean(String collection, String keyName, Object key, String value, Boolean defaultValue) {
        Document doc = getDocument(collection, keyName, key);
        if (doc != null && doc.get(value) instanceof Boolean) {
            return (Boolean) doc.get(value);
        }
        return defaultValue;
    }

    public <T> T[] getObjects(String collection, Class<T> classOff, String keyName, Object key) {
        ArrayList<Document> objects = getDocumentsAsList(collection, keyName, key);
        @SuppressWarnings("unchecked")
        T[] objetsClass = (T[]) new Object[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            objetsClass[i] = new Gson().fromJson(objects.get(i).toJson(), classOff);
        }
        return objetsClass;
    }

    public <T> T getObject(String collection, Object key, Class<T> classOff) {
        return getObject(collection, "key", key, classOff);
    }

    public <T> T getObject(String collection, String keyName, Object key, Class<T> classOff) {
        return new Gson().fromJson(getObjectJson(collection, keyName, key), classOff);
    }

    public String getObjectJson(String collection, Object key) {
        return getObjectJson(collection, "key", key);
    }

    public String getObjectJson(String collection, String keyName, Object key) {
        Document doc = getDocument(collection, keyName, key);
        return doc == null ? "" : doc.toJson();
    }

    public Document getDocument(String collection, String keyName, Object key) {
        return getDocuments(collection, keyName, key).first();
    }

    public FindIterable<Document> getDocuments(String collection, String keyName, Object key) {
        DBObject dbObject = new BasicDBObject().append(keyName, key instanceof CaseInsensitiveString ? ((CaseInsensitiveString) key).compile() : key);
        return this.database.getCollection(collection).find((Bson) dbObject);
    }

    public ArrayList<Document> getDocumentsAsList(String collection, String keyName, Object key) {
        ArrayList<Document> docs = new ArrayList<>();
        for (Document document : getDocuments(collection, keyName, key)) {
            docs.add(document);
        }
        return docs;
    }

    @Deprecated(since = "2.3")
    public List<String> getStringList(String collection, String keyName, Object key, String value) {
        return getStringList(collection, keyName, key, value, null);
    }

    @Deprecated(since = "2.3")
    public List<String> getStringList(String collection, String keyName, Object key) {
        return getStringList(collection, keyName, key, "value", null);
    }

    @Deprecated(since = "2.3")
    public List<String> getStringList(String collection, Object key, List<String> defaultList) {
        return getStringList(collection, "key", key, "value", defaultList);
    }

    @Deprecated(since = "2.3")
    public List<String> getStringList(String collection, Object key) {
        return getStringList(collection, "key", key, "value", null);
    }

    @Deprecated(since = "2.3")
    public List<String> getStringList(String collection, String keyName, Object key, String value, List<String> defaultList) {
        if (exists(collection, keyName, key)) {
            Document doc = getDocument(collection, keyName, key);
            return new ArrayList<>(doc.getList(value, String.class));
        } else {
            return Objects.requireNonNullElseGet(defaultList, ArrayList::new);
        }
    }

    public <T> List<T> getList(String collection, String keyName, Object key, Class<T> classOff) {
        return getList(collection, keyName, key, "value", classOff);
    }

    public <T> List<T> getList(String collection, Object key, Class<T> classOff) {
        return getList(collection, "key", key, "value", classOff);
    }

    public <T> List<T> getList(String collection, String keyName, Object key, String value, Class<T> classOff) {
        if (exists(collection, keyName, key)) {
            Document doc = getDocument(collection, keyName, key);
            return doc.getList(value, classOff);
        } else {
            return null;
        }
    }
}
