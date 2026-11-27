package lab

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class RepositoryTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `data survives new repository instances and failed transactions do not write`() {
        val file = directory.resolve("state.json")
        Repository(file).transaction { it.addEquipment("e1", "Centrifuge") }
        val original = Files.readString(file)
        assertThrows(BookingException::class.java) {
            Repository(file).transaction {
                it.addEquipment("e2", "Oscilloscope")
                it.addEquipment("e1", "Duplicate")
            }
        }
        assertEquals(original, Files.readString(file))
        assertEquals("Centrifuge", Repository(file).transaction(false) { it.equipment().single().name })
    }

    @Test
    fun `corrupt data produces explicit error and stays untouched`() {
        val file = directory.resolve("state.json")
        Files.writeString(file, "broken")
        assertThrows(BookingException::class.java) { Repository(file).transaction { it.snapshot() } }
        assertEquals("broken", Files.readString(file))
    }

    @Test
    fun `read only command does not create a data file`() {
        val file = directory.resolve("state.json")
        assertTrue(Repository(file).transaction(false) { it.equipment().isEmpty() })
        assertFalse(Files.exists(file))
    }
}
