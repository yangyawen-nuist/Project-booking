package lab

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

/** One process-level file lock covers reading, business validation and saving. */
class Repository(private val file: Path) {
    private val mapper = jacksonObjectMapper()

    fun <T> transaction(write: Boolean = true, action: (BookingService) -> T): T {
        try {
            val absolute = file.toAbsolutePath()
            Files.createDirectories(absolute.parent)
            val lockPath = absolute.resolveSibling("${absolute.fileName}.lck")
            FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { channel ->
                channel.lock().use {
                    return executeLocked(absolute, write, action)
                }
            }
        } catch (error: IOException) {
            throw BookingException(
                "Data access failed; check permissions, JSON format and disk space: ${error.message}", error,
            )
        }
    }

    private fun <T> executeLocked(path: Path, write: Boolean, action: (BookingService) -> T): T {
        val state = if (Files.exists(path)) mapper.readValue<State>(path.toFile()) else State()
        val service = BookingService(state)
        val result = action(service)
        if (write) save(path, service.snapshot())
        return result
    }

    private fun save(path: Path, state: State) {
        val temporary = Files.createTempFile(path.parent, "booking-", ".tmp")
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), state)
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
    }
}
