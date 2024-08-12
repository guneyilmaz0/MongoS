import net.guneyilmaz0.mongos.MongoS
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class MongoSTest {
    @Test
    fun testSetGet() {
        val mongoS = MongoS("test")
        mongoS.set("test", "test", "test")
        assertEquals(mongoS.getString("test", "test"), "test")
    }
}