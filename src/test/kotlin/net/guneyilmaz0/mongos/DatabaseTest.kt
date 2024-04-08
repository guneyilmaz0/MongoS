package net.guneyilmaz0.mongos

import kotlin.test.Test
import kotlin.test.assertNotNull

class DatabaseTest {

    @Test
    fun test() {
        val mongoS = MongoS("localhost", 27017, "test")
        assertNotNull(mongoS.exists("test", "test"))
    }
}