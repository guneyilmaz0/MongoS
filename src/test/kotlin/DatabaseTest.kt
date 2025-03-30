package net.guneyilmaz0.mongos

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.FindIterable
import org.bson.Document
import org.bson.conversions.Bson
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class DatabaseTest {

    private lateinit var mockDatabase: MongoDatabase
    private lateinit var mockCollection: MongoCollection<Document>
    private lateinit var mockFindIterable: FindIterable<Document>
    private lateinit var databaseInstance: Database

    @BeforeEach
    @Suppress("UNCHECKED_CAST")
    fun setup() {
        mockDatabase = mock(MongoDatabase::class.java)
        mockCollection = mock(MongoCollection::class.java) as MongoCollection<Document>
        mockFindIterable = mock(FindIterable::class.java) as FindIterable<Document>
        databaseInstance = Database()
        databaseInstance.init(mockDatabase)

        `when`(mockDatabase.getCollection("testCollection")).thenReturn(mockCollection)
        `when`(mockCollection.find(any(Bson::class.java))).thenReturn(mockFindIterable)
    }

    @Test
    fun `test init assigns database correctly`() {
        databaseInstance.init(mockDatabase)
        assertEquals(mockDatabase, databaseInstance.database)
    }

    @Test
    fun `test isConnected returns true`() {
        databaseInstance.init(mockDatabase)
        assertEquals(true, databaseInstance.isConnected())
    }

    @Test
    fun `test getValue returns correct value when document exists`() {
        val document = Document("key", "testKey").append("value", "testValue")
        `when`(mockFindIterable.first()).thenReturn(document)

        val result = databaseInstance.getValue("testCollection", "key", "testKey", "value", "default")
        assertEquals("testValue", result)
    }

    @Test
    fun `test getValue returns default value when document does not exist`() {
        `when`(mockFindIterable.first()).thenReturn(null)

        val result = databaseInstance.getValue("testCollection", "key", "testKey", "value", "default")
        assertEquals("default", result)
    }

    @Test
    fun `test getList returns correct value`() {
        val document = Document("key", "testKey").append("value", listOf("testValue1", "testValue2"))
        `when`(mockFindIterable.first()).thenReturn(document)

        val result = databaseInstance.getList("testCollection", "key", "testKey", String::class.java)
        assertEquals(listOf("testValue1", "testValue2"), result)
    }

    class TestObject(val value: String) : MongoSObject()

// For data class, toString() method is already implemented. So we need to override it.
//    data class TestObject(val value: String) : MongoSObject() {
//        override fun toString(): String = super.toString()
//    }

    @Test
    fun `test getObject returns correct value`() {
        val document = Document("key", "testKey").append("value", TestObject("testValue").toString())
        `when`(mockFindIterable.first()).thenReturn(document)

        val result = databaseInstance.getObject("testCollection", "key", "testKey", TestObject::class.java)
        assertEquals("testValue", result.value)
    }

}
