package lab

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Path

class CliTest {
    @TempDir
    lateinit var directory: Path

    private fun execute(vararg args: String): Int = commandLine().apply {
        err = PrintWriter(StringWriter())
        out = PrintWriter(StringWriter())
    }.execute("--data", directory.resolve("state.json").toString(), *args)

    @Test
    fun `parser rejects unknown commands options and missing arguments`() {
        assertEquals(2, execute("unknown"))
        assertEquals(2, execute("equipment-add", "--id", "x"))
        assertEquals(2, execute("bookings", "--unknown"))
        assertEquals(2, execute("reserve", "--equipment", "x"))
        assertEquals(0, execute("--help"))
    }

    @Test
    fun `commands persist across invocations with meaningful exit codes`() {
        assertEquals(0, execute("equipment-list"))
        assertEquals(0, execute("equipment-add", "--id", "x", "--name", "Electron microscope"))
        assertEquals(1, execute("equipment-add", "--id", "x", "--name", "Duplicate"))
        assertEquals(0, execute("equipment-list"))
        assertEquals(0, execute("reserve", "--equipment", "x", "--user", "Alice",
            "--start", "2026-12-01T10:00", "--end", "2026-12-01T11:00"))
        assertEquals(0, execute("bookings", "--equipment", "x"))
        assertEquals(1, execute("cancel", "--id", "missing"))
        val id = Repository(directory.resolve("state.json")).transaction(false) { it.reservations().single().id }
        assertEquals(0, execute("cancel", "--id", id))
        assertEquals(0, execute("bookings"))
    }
}
