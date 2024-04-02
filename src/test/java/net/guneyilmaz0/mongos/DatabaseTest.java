package net.guneyilmaz0.mongos;

import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTest {
    private final Database database = new MongoS("test");

    @Test
    public void testSet() {
        // Setup
        String collection = "testCollection";
        String key = "testKey";
        String value = "testValue";
        Document document = new Document().append("key", key).append("value", value);

        // Execute
        database.set(collection, key, document);

        // Verify
        Document result = database.getDocument(collection, "key", key);
        Assert.assertEquals(document, result);
    }

    @Test
    public void renameKeyTest() {
        // Setup
        String collection = "testCollection";
        String oldKey = "oldKey";
        String newKey = "newKey";
        String value = "testValue";

        // Execute
        database.set(collection, oldKey, value);
        database.renameKey(collection, oldKey, newKey);

        // Verify
        String result = database.getString(collection, newKey, "wrongKey");
        Assert.assertEquals(value, result);
    }

    @Test
    public void setManyTest() {
        // Setup
        String collection = "testCollection";
        List<Document> documents = new ArrayList<>();
        documents.add(new Document().append("key", "key1").append("value", "value1"));
        documents.add(new Document().append("key", "key2").append("value", "value2"));
        documents.add(new Document().append("key", "key3").append("value", "value3"));

        // Execute
        database.setMany(collection, documents);

        // Verify
        Document result1 = database.getDocument(collection, "key", "key1");
        Document result2 = database.getDocument(collection, "key", "key2");
        Document result3 = database.getDocument(collection, "key", "key3");
        Assert.assertEquals(documents.get(0), result1);
        Assert.assertEquals(documents.get(1), result2);
        Assert.assertEquals(documents.get(2), result3);
    }
}